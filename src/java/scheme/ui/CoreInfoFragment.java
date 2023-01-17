package scheme.ui;

import arc.Events;
import arc.scene.Group;
import arc.scene.ui.layout.Table;
import arc.util.Interval;
import arc.util.Reflect;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.ui.CoreItemsDisplay;
import mindustry.ui.Styles;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class CoreInfoFragment {

    public CoreItemsDisplay items = Reflect.get(ui.hudfrag, "coreItems");
    public PowerBars power = new PowerBars();

    /** Whether the player chooses a power node. */
    public boolean checked;
    /** Used when changing the schematic layer. */
    public Interval timer = new Interval();

    public void build(Group parent) {
        Events.run(WorldLoadEvent.class, power::refreshNode);
        Events.run(BlockBuildEndEvent.class, power::refreshNode);
        Events.run(BlockDestroyEvent.class, power::refreshNode);
        Events.run(ConfigEvent.class, power::refreshNode);

        var root = (Table) ((Table) ui.hudGroup.find("coreinfo")).getChildren().get(1);
        root.defaults().fillX();

        root.collapser(cont -> { // Power Bars
            cont.background(Styles.black6).margin(8f, 8f, 8f, 0f);

            cont.table(bars -> {
                bars.defaults().height(18f).growX();
                bars.add(power.balance()).row();
                bars.add(power.stored()).padTop(8f);
            }).growX();
            cont.button(Icon.edit, Styles.clearNoneTogglei, () -> checked = !checked).checked(t -> checked).size(44f).padLeft(8f);
        }, () -> settings.getBool("coreitems") && !mobile && ui.hudfrag.shown).row();

        root.collapser(cont -> { // Schematic Layer
            cont.background(Styles.black6).margin(8f, 8f, 8f, 0f);

            timer.reset(0, 240f);
            cont.label(() -> bundle.format("layer", bundle.get("layer." + m_schematics.layer)));
        }, true, () -> !timer.check(0, 240f) && !mobile && ui.hudfrag.shown);
    }

    public void nextLayer() {
        if (!timer.get(240f)) m_schematics.nextLayer();
    }
}

