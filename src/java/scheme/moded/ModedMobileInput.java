package scheme.moded;

import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.entities.Predict;
import mindustry.entities.Units;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.BlockUnitUnit;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Healthc;
import mindustry.gen.Mechc;
import mindustry.gen.Payloadc;
import mindustry.gen.Unit;
import mindustry.input.InputHandler;
import mindustry.input.MobileInput;
import mindustry.input.Placement;
import mindustry.input.Placement.NormalizeResult;
import mindustry.type.UnitType;
import mindustry.world.blocks.ControlBlock;
import mindustry.world.blocks.power.PowerNode;
import scheme.tools.BuildingTools.Mode;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;
import static scheme.SchemeVars.*;


public class ModedMobileInput extends MobileInput implements ModedInputHandler {

    public boolean using, movementLocked, lastTouched;
    public int buildX = -1, buildY = -1;
    public int lastX, lastY, lastSize = 8;

    private boolean isRelease(){
        return lastTouched && !input.isTouched(0);
    }

    private boolean isTap(){
        return !lastTouched && input.isTouched(0);
    }

    @Override
    public void drawTop() {
        if (mode == schematicSelect) {
            drawSelection(lineStartX, lineStartY, lastLineX, lastLineY, maxSchematicSize);
        }

        if (using) {
            if (build.mode == Mode.edit)
                drawEditSelection(isDarkdustry() ? player.tileX() : buildX, isDarkdustry() ? player.tileY() : buildY, lastX, lastY, maxSchematicSize);

            if (build.mode == Mode.connect && isPlacing())
                drawEditSelection(lastX - build.size + 1, lastY - build.size + 1, lastX + build.size - 1, lastY + build.size - 1, 256);
        }

        drawCommanded();
    }

    @Override
    public void drawBottom() {
        if (!build.isPlacing()) super.drawBottom();
        else build.plan.each(plan -> {
            plan.animScale = 1f;
            if (build.mode != Mode.remove) drawPlan(plan);
            else drawBreaking(plan);
        });
    }

    @Override
    public void update() {
        super.update();

        if (locked()) return;

        buildInput();
        if (!player.dead() && !state.isPaused()) updateMovement(player.unit());
    }

    @Override
    protected void updateMovement(Unit unit){
        Rect rect = Tmp.r3;

        UnitType type = unit.type;
        if(type == null) return;

        boolean omni = unit.type.omniMovement;
        boolean allowHealing = type.canHeal;
        boolean validHealTarget = allowHealing && target instanceof Building b && b.isValid() && target.team() == unit.team && b.damaged() && target.within(unit, type.range);
        boolean boosted = (unit instanceof Mechc && unit.isFlying());

        if((Units.invalidateTarget(target, unit, type.range) && !validHealTarget) || state.isEditor()){
            target = null;
        }

        targetPos.set(camera.position);
        float attractDst = 15f;

        float speed = unit.speed();
        float range = unit.hasWeapons() ? unit.range() : 0f;
        float bulletSpeed = unit.hasWeapons() ? type.weapons.first().bullet.speed : 0f;
        float mouseAngle = unit.angleTo(unit.aimX(), unit.aimY());
        boolean aimCursor = omni && player.shooting && type.hasWeapons() && !boosted && type.faceTarget;

        if(aimCursor){
            unit.lookAt(mouseAngle);
        }else{
            unit.lookAt(unit.prefRotation());
        }

        //validate payload, if it's a destroyed unit/building, remove it
        if(payloadTarget instanceof Healthc h && !h.isValid()){
            payloadTarget = null;
        }

        if(payloadTarget != null && unit instanceof Payloadc pay){
            targetPos.set(payloadTarget);
            attractDst = 0f;

            if(unit.within(payloadTarget, 3f * Time.delta)){
                if(payloadTarget instanceof Vec2 && pay.hasPayload()){
                    tryDropPayload();
                }else if(payloadTarget instanceof Building build && build.team == unit.team){
                    Call.requestBuildPayload(player, build);
                }else if(payloadTarget instanceof Unit other && pay.canPickup(other)){
                    Call.requestUnitPayload(player, other);
                }

                payloadTarget = null;
            }
        }else{
            payloadTarget = null;
        }

        movement.set(targetPos).sub(player).limit(speed);
        movement.setAngle(Mathf.slerp(movement.angle(), unit.vel.angle(), 0.05f));

        if(player.within(targetPos, attractDst)){
            movement.setZero();
            unit.vel.approachDelta(Vec2.ZERO, unit.speed() * type.accel / 2f);
        }

        unit.hitbox(rect);
        rect.grow(4f);

        player.boosting = collisions.overlapsTile(rect, unit.solidity()) || !unit.within(targetPos, 85f);

        if (!movementLocked) unit.movePref(movement);

        if(!player.unit().activelyBuilding() && player.unit().mineTile == null){

            if(manualShooting){
                player.shooting = !boosted;
                unit.aim(player.mouseX = input.mouseWorldX(), player.mouseY = input.mouseWorldY());
            }else if(target == null){
                player.shooting = false;
                if(settings.getBool("autotarget") && !(player.unit() instanceof BlockUnitUnit u && u.tile() instanceof ControlBlock c && !c.shouldAutoTarget())){
                    if(player.unit().type.canAttack){
                        target = Units.closestTarget(unit.team, unit.x, unit.y, range, u -> u.checkTarget(type.targetAir, type.targetGround), u -> type.targetGround);
                    }

                    if(allowHealing && target == null){
                        target = Geometry.findClosest(unit.x, unit.y, indexer.getDamaged(Team.sharded));
                        if(target != null && !unit.within(target, range)){
                            target = null;
                        }
                    }
                }

                unit.aim(input.mouseWorldX(), input.mouseWorldY());
            }else{
                Vec2 intercept = Predict.intercept(unit, target, bulletSpeed);

                player.mouseX = intercept.x;
                player.mouseY = intercept.y;
                player.shooting = !boosted;

                unit.aim(player.mouseX, player.mouseY);
            }
        }

        unit.controlWeapons(player.shooting && !boosted);
    }

    public void buildInput() {
        if (!hudfrag.building.fliped) build.setMode(Mode.none);
        if (build.mode == Mode.none) return;

        int cursorX = tileX();
        int cursorY = tileY();

        boolean has = hasMoved(cursorX, cursorY);
        if (has) build.plan.clear();

        if (using) {
            if (has) {
                if (build.mode == Mode.replace) build.replace(cursorX, cursorY);
                if (build.mode == Mode.remove) build.remove(cursorX, cursorY);
                if (build.mode == Mode.connect) {
                    if (block instanceof PowerNode == false) block = Blocks.powerNode;
                    build.connect(cursorX, cursorY, (x, y) -> {
                        updateLine(x, y);
                        build.plan.addAll(linePlans);
                        linePlans.clear();
                        build.plan.remove(0);
                    });
                }

                if (build.mode == Mode.fill) build.fill(buildX, buildY, cursorX, cursorY, maxSchematicSize);
                if (build.mode == Mode.circle) build.circle(cursorX, cursorY);
                if (build.mode == Mode.square) build.square(cursorX, cursorY, (x1, y1, x2, y2) -> {
                    updateLine(x1, y1, x2, y2);
                    build.plan.addAll(linePlans);
                    linePlans.clear();
                });

                lastX = cursorX;
                lastY = cursorY;
                lastSize = build.size;
            }

            if (isRelease()) {
                flushBuildingTools();

                if (build.mode == Mode.edit) {
                    NormalizeResult result = Placement.normalizeArea(isDarkdustry() ? player.tileX() : buildX, isDarkdustry() ? player.tileY() : buildY, cursorX, cursorY, 0, false, maxSchematicSize);
                    admins.edit(result.x, result.y, result.x2, result.y2);
                }
            }
        }

        if (isTap() && !scene.hasMouse()) {
            buildX = cursorX;
            buildY = cursorY;
            using = true;
        }

        if (isRelease()) {
            buildX = lastX = -1;
            buildY = lastY = -1;
            using = false;
        }

        lastTouched = input.isTouched();
    }

    public boolean hasMoved(int x, int y) {
        return lastX != x || lastY != y || lastSize != build.size;
    }

    // there is nothing because, you know, it's mobile
    public void changePanSpeed(float value) {}

    public void lockMovement() {
        movementLocked = !movementLocked;
    }

    public void flush(Seq<BuildPlan> plans) {
        flushPlans(plans);
    }

    public InputHandler asHandler() {
        return this;
    }
}
