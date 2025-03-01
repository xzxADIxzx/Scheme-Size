package scheme;

import arc.graphics.g2d.Draw;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.game.Schematics;
import mindustry.gen.Building;
import mindustry.mod.Mod;
import mindustry.type.Item;
import mindustry.ui.CoreItemsDisplay;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Router;
import mindustry.world.blocks.logic.LogicDisplay;
import scheme.moded.ModedBinding;
import scheme.moded.ModedGlyphLayout;
import scheme.moded.ModedSchematics;
import scheme.tools.MessageQueue;
import scheme.tools.RainbowTeam;
import scheme.ui.MapResizeFix;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class Main extends Mod {

    public Main() {
        // well, after the 136th build, it became much easier
        maxSchematicSize = 512;

        // mod reimported through mods dialog
        if (schematics.getClass().getSimpleName().startsWith("Moded")) return;

        assets.load(schematics = m_schematics = new ModedSchematics());
        assets.unload(Schematics.class.getSimpleName()); // prevent dual loading
    }

    @Override
    public void init() {
        Backdoor.load();
        ServerIntegration.load();
        ClajIntegration.load();
        ModedBinding.load();
        ModedGlyphLayout.load();
        SchemeVars.load();
        MapResizeFix.load();
        MessageQueue.load();
        RainbowTeam.load();

        ui.schematics = schemas; // do it before build hudfrag
        ui.listfrag = listfrag;

        units.load();

        m_settings.apply(); // sometimes settings are not self-applying

        hudfrag.build(ui.hudGroup);
        listfrag.build(ui.hudGroup);
        shortfrag.build(ui.hudGroup);
        consolefrag.build();
        corefrag.build(ui.hudGroup);

        control.setInput(m_input.asHandler());
        renderer.addEnvRenderer(0, render::draw);

        if (m_schematics.requiresDialog) ui.showOkText("@rename.name", "@rename.text", () -> {});
        if (settings.getBool("welcome")) ui.showOkText("@welcome.name", "@welcome.text", () -> {});
        if (settings.getBool("check4update"));

        if (/*SchemeUpdater.installed("miner-tools")*/true) { // very sad but they are incompatible
            ui.showOkText("@incompatible.name", "@incompatible.text", () -> {});
            ui.hudGroup.fill(cont -> { // crutch to prevent crash
                cont.visible = false;
                cont.add(new CoreItemsDisplay());
            });
        }

        Blocks.distributor.buildType = () -> ((Router) Blocks.distributor).new RouterBuild() {
            @Override
            public boolean canControl() { return true; }

            @Override
            public Building getTileTarget(Item item, Tile from, boolean set) {
                Building target = super.getTileTarget(item, from, set);

                if (unit != null && isControlled() && unit.isShooting()) {
                    float angle = angleTo(unit.aimX(), unit.aimY());
                    Tmp.v1.set(block.size * tilesize, 0f).rotate(angle).add(this);

                    Building other = world.buildWorld(Tmp.v1.x, Tmp.v1.y);
                    if (other != null && other.acceptItem(this, item)) target = other;
                }

                return target;
            }
        };

        content.blocks().each(block -> block instanceof LogicDisplay, block -> block.buildType = () -> ((LogicDisplay) block).new LogicDisplayBuild() {
            @Override
            public void draw() {
                super.draw();
                if (render.borderless) Draw.draw(Draw.z(), () -> {
                    Draw.rect(Draw.wrap(buffer.getTexture()), x, y, block.region.width * Draw.scl, -block.region.height * Draw.scl);
                });
            }
        });
    }

    public static void log(String info) {
        app.post(() -> Log.infoTag("Scheme", info));
    }

    public static void error(Throwable info) {
        app.post(() -> Log.err("Scheme", info));
    }

    public static void copy(String text) {
        if (text == null) return;

        app.setClipboardText(text);
        ui.showInfoFade("@copied");
    }
}
