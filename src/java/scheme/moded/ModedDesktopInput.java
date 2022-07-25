package scheme.moded;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.TextField;
import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Unit;
import mindustry.input.Binding;
import mindustry.input.DesktopInput;
import mindustry.input.InputHandler;
import mindustry.input.Placement;
import mindustry.input.Placement.NormalizeResult;
import mindustry.world.blocks.power.PowerNode;
import scheme.tools.BuildingTools.Mode;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;
import static scheme.SchemeVars.*;

/** Last update - Jun 14, 2022 */
public class ModedDesktopInput extends DesktopInput implements ModedInputHandler {

    public boolean using, movementLocked;
    public int buildX = -1, buildY = -1;
    public int lastX, lastY, lastSize = 8;

    @Override
    protected void removeSelection(int x1, int y1, int x2, int y2, int maxLength) {
        build.save(x1, y1, x2, y2, maxSchematicSize);
        super.removeSelection(x1, y1, x2, y2, maxSchematicSize);
    }

    @Override
    public void drawTop() {
        Lines.stroke(1f);
        int cursorX = tileX();
        int cursorY = tileY();

        if (mode == breaking) {
            drawBreakSelection(selectX, selectY, cursorX, cursorY, maxSchematicSize);
            drawSize(selectX, selectY, cursorX, cursorY, maxSchematicSize);
        } else if (input.keyDown(Binding.schematic_select) && !scene.hasKeyboard()) {
            drawSelection(schemX, schemY, cursorX, cursorY, maxSchematicSize);
            drawSize(schemX, schemY, cursorX, cursorY, maxSchematicSize);
        }

        if (using) {
            if (build.mode == Mode.edit)
                drawEditSelection(isDarkdustry() ? player.tileX() : buildX, isDarkdustry() ? player.tileY() : buildY, cursorX, cursorY, maxSchematicSize);

            if (build.mode == Mode.connect && isPlacing())
                drawEditSelection(cursorX - build.size, cursorY - build.size, cursorX + build.size, cursorY + build.size, maxSchematicSize);
        }

        drawCommanded();

        Draw.reset();
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

        if (scene.getKeyboardFocus() instanceof TextField || locked()) return;

        modedInput();
        buildInput();
    }

    @Override
    protected void updateMovement(Unit unit) {
        if (input.keyDown(ModedBinding.alternative) && input.keyTap(Binding.respawn)) admins.despawn();

        if (ai.ai != null 
                && input.axis(Binding.move_x) == 0 && input.axis(Binding.move_y) == 0
                && !input.keyDown(Binding.mouse_move) && !input.keyDown(Binding.select)) {
            ai.update();
            player.shooting = unit.isShooting;
        } else if (!movementLocked) super.updateMovement(unit);
    }

    /** Punishment awaits me for this... */
    public void modedInput() {

        boolean alt = input.keyDown(ModedBinding.alternative);

        if (input.keyTap(ModedBinding.adminscfg)) adminscfg.show();
        if (input.keyTap(ModedBinding.rendercfg)) rendercfg.show(alt);

        if (input.keyTap(ModedBinding.toggle_history)) render.toggleHistory();
        if (input.keyTap(ModedBinding.toggle_core_items)) render.toggleCoreItems();
        if (input.keyTap(ModedBinding.toggle_ai))
            if (alt) ai.deselect();
            else ai.select();

        if (input.keyTap(ModedBinding.manage_effect)) admins.manageEffect();
        if (input.keyTap(ModedBinding.manage_item)) admins.manageItem();
        if (input.keyTap(ModedBinding.manage_team)) admins.manageTeam();
        if (input.keyTap(ModedBinding.manage_unit))
            if (alt) admins.spawnUnits();
            else admins.manageUnit();

        if (input.keyTap(ModedBinding.place_core)) admins.placeCore();

        if (alt) {
            admins.look();

            if (input.keyTap(Binding.block_info)) keycomb.show();

            // alternative + respawn moved to updateMovement because it needs to be called before internal respawn
            if (input.keyTap(Binding.mouse_move)) admins.teleport(input.mouseWorld());
            if (input.keyTap(Binding.pan)) lockMovement();

            if (input.keyTap(Binding.deselect)) hudfrag.building.flip();
            if (input.keyTap(Binding.schematic_menu)) flushLastRemoved();
            if (input.keyDown(Binding.rotateplaced)) build.drop(tileX(), tileY());
        }

        if (input.keyTap(Binding.select) && !scene.hasMouse()) {
            if (hudfrag.power.setNode(tileAt().build)) hudfrag.checked = false;
        } // power node selection
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
            if (has) { // TODO: try replace buildX/Y to selectX/Y
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

                lastX = cursorX;
                lastY = cursorY;
                lastSize = build.size;
                linePlans.clear();
            }

            if (input.keyRelease(Binding.select)) {
                flushBuildingTools();

                if (build.mode == Mode.pick) tile.select(cursorX, cursorY);
                if (build.mode == Mode.edit) {
                    NormalizeResult result = Placement.normalizeArea(isDarkdustry() ? player.tileX() : buildX, isDarkdustry() ? player.tileY() : buildY, cursorX, cursorY, 0, false, maxSchematicSize);
                    admins.edit(result.x, result.y, result.x2, result.y2);
                }
            } else build.resize(input.axis(Binding.zoom));
        }

        if (input.keyTap(Binding.select) && !scene.hasMouse()) {
            buildX = cursorX;
            buildY = cursorY;
            using = true;
            renderer.minZoom = renderer.maxZoom = renderer.getScale(); // a crutch to lock camera zoom
        }

        if (input.keyRelease(Binding.select) || input.keyTap(Binding.deselect) || input.keyTap(Binding.break_block)) {
            buildX = lastX = -1;
            buildY = lastY = -1;
            using = false;
            m_settings.apply();
        }
    }

    public boolean hasMoved(int x, int y) {
        return lastX != x || lastY != y || lastSize != build.size;
    }

    public void changePanSpeed(float value) {
        panSpeed = 4.5f * value / 4f;
        panBoostSpeed = 15f * Mathf.sqrt(value / 4f);
    }

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
