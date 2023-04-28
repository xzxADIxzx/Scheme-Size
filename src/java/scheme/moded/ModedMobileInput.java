package scheme.moded;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.input.*;
import mindustry.input.Placement.NormalizeResult;
import mindustry.world.blocks.power.PowerNode;
import scheme.ai.GammaAI;
import scheme.tools.BuildingTools.Mode;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;
import static scheme.SchemeVars.*;

/** Last update - Feb 10, 2023 */
public class ModedMobileInput extends MobileInput implements ModedInputHandler {

    public boolean using, movementLocked, lastTouched, shootingLocked;
    public int buildX, buildY, lastX, lastY, lastSize = 8;

    public Player observed;

    private boolean isRelease() {
        return lastTouched && !input.isTouched(0);
    }

    private boolean isTap() {
        return !lastTouched && input.isTouched(0);
    }

    @Override
    protected void removeSelection(int x1, int y1, int x2, int y2, boolean flush) {
        build.save(x1, y1, x2, y2, maxSchematicSize);
        super.removeSelection(x1, y1, x2, y2, flush, maxSchematicSize);
    }

    @Override
    public void buildPlacementUI(Table table) {
        super.buildPlacementUI(table);

        var button = table.getChildren().get(table.getChildren().size - 1);
        button.clicked(() -> {
            if (m_schematics.isCursed(selectPlans)) admins.flush(selectPlans);
        });

        int size = button.getListeners().size;
        button.getListeners().swap(size - 1, size - 2);
    }

    @Override
    public void drawTop() {
        if (mode == schematicSelect) {
            drawSelection(lineStartX, lineStartY, lastLineX, lastLineY, maxSchematicSize);
            drawSize(lineStartX, lineStartY, lastLineX, lastLineY, maxSchematicSize);
        } else if (mode == breaking && lineMode)
            drawSize(lineStartX, lineStartY, tileX(), tileY(), maxSchematicSize);

        if (using) {
            if (build.mode == Mode.edit)
                drawEditSelection(buildX, buildY, lastX, lastY, maxSchematicSize);

            if (build.mode == Mode.connect && isPlacing())
                drawEditSelection(lastX - build.size + 1, lastY - build.size + 1, lastX + build.size - 1, lastY + build.size - 1, 256);
        }

        if (build.mode == Mode.brush)
            drawEditSelection(lastX, lastY, build.size);

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
        if (ai.ai instanceof GammaAI gamma) gamma.draw();
    }

    @Override
    public void update() {
        super.update();

        if (locked()) return;

        if (observed != null) {
            camera.position.set(observed.unit()); // idk why, but unit moves smoother
            if (input.isTouched(0) && !scene.hasMouse()) observed = null;
        }

        buildInput();
        if (movementLocked) drawLocked(player.unit().x, player.unit().y);
    }

    @Override
    protected void updateMovement(Unit unit) {
        if (ai.ai != null && !input.isTouched()) {
            if (!movementLocked) camera.position.set(unit.x, unit.y);
            ai.update();
        } else if (!movementLocked) super.updateMovement(unit);

        if (shootingLocked) {
            unit.aimLook(player.mouseX, player.mouseY);
            unit.controlWeapons(true, false);
            player.shooting = unit.isShooting = false;
        }
    }

    public void buildInput() {
        if (!hudfrag.building.fliped) build.setMode(Mode.none);
        if (build.mode == Mode.none) return;

        int cursorX = tileX();
        int cursorY = tileY();

        boolean has = hasMoved(cursorX, cursorY);
        if (has) build.plan.clear();

        if (using) {
            if (build.mode == Mode.drop) build.drop(cursorX, cursorY);
            if (has) {
                if (build.mode == Mode.replace) build.replace(cursorX, cursorY);
                if (build.mode == Mode.remove) build.remove(cursorX, cursorY);
                if (build.mode == Mode.connect) {
                    if (block instanceof PowerNode == false) block = Blocks.powerNode;
                    build.connect(cursorX, cursorY, (x, y) -> {
                        updateLine(x, y);
                        build.plan.addAll(linePlans).remove(0);
                    });
                }

                if (build.mode == Mode.fill) build.fill(buildX, buildY, cursorX, cursorY, maxSchematicSize);
                if (build.mode == Mode.circle) build.circle(cursorX, cursorY);
                if (build.mode == Mode.square) build.square(cursorX, cursorY, (x1, y1, x2, y2) -> {
                    updateLine(x1, y1, x2, y2);
                    build.plan.addAll(linePlans);
                });

                if (build.mode == Mode.brush) admins.brush(cursorX, cursorY, build.size);

                lastX = cursorX;
                lastY = cursorY;
                lastSize = build.size;
                linePlans.clear();
            }

            if (isRelease()) {
                flushBuildingTools();

                if (build.mode == Mode.pick) tile.select(cursorX, cursorY);
                if (build.mode == Mode.edit) {
                    NormalizeResult result = Placement.normalizeArea(buildX, buildY, cursorX, cursorY, 0, false, maxSchematicSize);
                    admins.fill(result.x, result.y, result.x2, result.y2);
                }
            }
        }

        if (isTap() && !scene.hasMouse()) {
            buildX = cursorX;
            buildY = cursorY;
            using = true;
        }

        if (isRelease()) using = false;

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

    public void lockShooting() {
        shootingLocked = !shootingLocked;
    }

    public void observe(Player target) {
        observed = target;
    }

    public void flush(Seq<BuildPlan> plans) {
        flushPlans(plans);
    }

    public InputHandler asHandler() {
        return this;
    }
}
