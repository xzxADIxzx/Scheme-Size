package scheme.moded;

import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.entities.units.BuildPlan;
import mindustry.input.InputHandler;
import mindustry.input.MobileInput;
import mindustry.input.Placement;
import mindustry.input.Placement.NormalizeResult;
import mindustry.world.blocks.power.PowerNode;
import scheme.tools.BuildingTools.Mode;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;
import static scheme.SchemeVars.*;


public class ModedMobileInput extends MobileInput implements ModedInputHandler {

    public boolean using, lastTouched;
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
        if (!locked()) buildInput();
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

    public void flush(Seq<BuildPlan> plans) {
        flushPlans(plans);
    }

    public InputHandler asHandler() {
        return this;
    }
}
