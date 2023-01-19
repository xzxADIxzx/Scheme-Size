package scheme.ui;

import arc.Events;
import arc.scene.Group;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.util.Interval;
import mindustry.core.UI;
import mindustry.game.Team;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.ui.Styles;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class CoreInfoFragment {

    public CoreItemsDisplay items = new CoreItemsDisplay();
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

        Events.run(ResetEvent.class, items::resetUsed);

        var root = (Table) ((Table) ui.hudGroup.find("coreinfo")).getChildren().get(1);

        root.defaults().fillX();
        root.visible(() -> !mobile && ui.hudfrag.shown);

        root.clear();
        root.collapser(cont -> { // Core Items Display
            cont.background(Styles.black6).margin(8f);

            cont.add(items);
            cont.button(Icon.modePvp, Styles.clearNoneTogglei, () -> items.rebuild(player.team())).size(44f).top().right().padLeft(8f);
        }, () -> settings.getBool("coreitems")).fillX().row();

        root.collapser(cont -> { // Power Bars
            cont.background(Styles.black6).margin(8f);

            cont.table(bars -> {
                bars.defaults().height(18f).growX();
                bars.add(power.balance()).row();
                bars.add(power.stored()).padTop(8f);
            }).growX();
            cont.button(Icon.edit, Styles.clearNoneTogglei, () -> checked = !checked).checked(t -> checked).size(44f).padLeft(8f);
        }, () -> settings.getBool("coreitems")).row();

        root.collapser(cont -> { // Schematic Layer
            cont.background(Styles.black6).margin(8f);

            timer.reset(0, 240f);
            cont.label(() -> bundle.format("layer", bundle.get("layer." + m_schematics.layer)));
        }, true, () -> !timer.check(0, 240f));
    }

    public void trySetNode(int x, int y) {
        if (checked && power.setNode(world.build(x, y))) checked = false;
    }

    public void nextLayer() {
        if (!timer.get(240f)) m_schematics.nextLayer();
    }

    /** Same as vanilla, but supports display of any team. */
    public class CoreItemsDisplay extends Table {

        public final ObjectSet<Item> used = new ObjectSet<>();

        public void resetUsed() {
            used.clear();
            clear();
        }

        public void rebuild(Team team) {
            var core = team.core();

            clear();
            update(() -> {
                if (core != null && content.items().contains(item -> core.items.get(item) > 0 && used.add(item))) rebuild(team);
            });

            used.each(item -> {
                image(item.uiIcon).size(iconSmall).padRight(3).tooltip(t -> t.background(Styles.black6).margin(4f).add(item.localizedName).style(Styles.outlineLabel));
                label(() -> core == null ? "0" : UI.formatAmount(core.items.get(item))).padRight(3f).minWidth(52f).left();

                if (children.size % 8 == 0) row();
            });
        }
    }
}

