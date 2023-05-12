package scheme.ui;

import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.Group;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.util.Interval;
import arc.util.Time;
import mindustry.core.UI;
import mindustry.game.Team;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.world.modules.ItemModule;
import scheme.ui.dialogs.ListDialog;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class CoreInfoFragment {

    public CoreItemsDisplay items = new CoreItemsDisplay();
    public PowerBars power = new PowerBars();

    /** Whether the player chooses a power node, team or views resource statistics. */
    public boolean choosesTeam, choosesNode, viewStats;
    /** Used when changing the schematic layer. */
    public Interval timer = new Interval(2);
    /** Icon of the selected team. */
    public Drawable chosenTeamRegion;

    public void build(Group parent) {
        Events.run(WorldLoadEvent.class, power::refreshNode);
        Events.run(BlockBuildEndEvent.class, power::refreshNode);
        Events.run(BlockDestroyEvent.class, power::refreshNode);
        Events.run(ConfigEvent.class, power::refreshNode);

        Events.run(ResetEvent.class, items::resetUsed);
        Events.run(WorldLoadEvent.class, () -> {
            items.rebuild(player.team());
            chosenTeamRegion = texture(player.team());
        });

        var root = (Table) ((Table) ui.hudGroup.find("coreinfo")).getChildren().get(1);

        root.defaults().fillX().top();
        root.visible(() -> !mobile && ui.hudfrag.shown);

        root.clear();
        root.collapser(cont -> { // Core Items Display
            cont.background(Styles.black6).margin(8f);

            cont.add(items).growX();
            cont.button(Icon.edit, Styles.clearNoneTogglei, () -> choosesTeam = !choosesTeam).checked(t -> choosesTeam).size(44f).top().padLeft(8f)
                    .update(i -> i.getStyle().imageUp = chosenTeamRegion);
        }, () -> settings.getBool("coreitems")).row();

        root.collapser(cont -> { // Team Selection
            cont.background(Styles.black6).margin(8f).left();

            int[] amount = new int[1];
            Runnable rebuild = () -> {
                cont.clear();

                for (Team team : Team.all) {
                    if (!team.active()) continue;

                    var texture = texture(team);
                    cont.button(texture, Styles.clearNoneTogglei, 36f, () -> {
                        items.rebuild(team);
                        chosenTeamRegion = texture;
                    }).checked(i -> items.team == team).size(44f);
                }
            };

            cont.update(() -> {
                var active = state.teams.getActive();
                if (amount[0] == active.size) return;

                amount[0] = active.size;
                rebuild.run();
            });
        }, true, () -> settings.getBool("coreitems") && choosesTeam).row();

        root.collapser(cont -> { // Power Bars
            cont.background(Styles.black6).margin(8f);

            cont.table(bars -> {
                bars.defaults().height(18f).growX();
                bars.add(power.balance()).row();
                bars.add(power.stored()).padTop(8f);
            }).growX();
            cont.button(Icon.edit, Styles.clearNoneTogglei, () -> choosesNode = !choosesNode).checked(t -> choosesNode).size(44f).padLeft(8f);
        }, () -> settings.getBool("coreitems")).row();

        float[] coreAttackTime = new float[1];
        Events.run(Trigger.teamCoreDamage, () -> coreAttackTime[0] = 240f);

        root.collapser(cont -> { // Core Under Attack
            cont.background(Styles.black6).margin(8f);
            cont.add("@coreattack").update(label -> label.color.set(Color.orange).lerp(Color.scarlet, Mathf.absin(Time.time, 2f, 1f)));
        }, true, () -> {
            if (state.isPaused()) return false;
            if (state.isMenu() || player.team().data().noCores()) {
                coreAttackTime[0] = 0f;
                return false;
            }

            return (coreAttackTime[0] -= Time.delta) > 0;
        }).row();

        root.collapser(cont -> { // Schematic Layer
            cont.background(Styles.black6).margin(8f);

            timer.reset(0, 240f);
            cont.label(() -> bundle.format("layer", bundle.get("layer." + m_schematics.layer)));
        }, true, () -> !timer.check(0, 240f));
    }

    public void trySetNode(int x, int y) {
        if (choosesNode && power.setNode(world.build(x, y))) choosesNode = false;
    }

    public void nextLayer() {
        if (!timer.get(0, 240f)) m_schematics.nextLayer();
    }

    public static Drawable texture(Team team) {
        if (team.id < 6)
            return new TextureRegionDrawable(ListDialog.texture(team));
        else {
            var white = (TextureRegionDrawable) Tex.whiteui;
            return white.tint(team.color);
        }
    }

    /** Same as vanilla, but supports display of any team & resource statistics. */
    public class CoreItemsDisplay extends Table {

        public final ObjectSet<Item> used = new ObjectSet<>();

        public ItemModule display, core, last = new ItemModule();
        public Team team;

        public void resetUsed() {
            used.clear();
            clear();
        }

        public void rebuild(Team team) {
            this.team = team;

            clear();
            content.items().each(item -> {
                if (!used.contains(item)) return;

                image(item.uiIcon).size(iconSmall).padRight(3f);
                label(() -> display == null ? "0" : format(display.get(item))).padRight(3f).minWidth(52f).left();

                if (children.size % 8 == 0) row();
            });

            hovered(() -> {
                viewStats = true;
                display = new ItemModule();
            });

            exited(() -> {
                viewStats = false;
                last.clear();
            });

            update(() -> {
                core = team.data().hasCore() ? team.core().items : null;
                if (!viewStats) display = core;

                if (core == null) return;

                if (content.items().contains(item -> core.get(item) > 0 && used.add(item))) rebuild(team);
                if (viewStats && timer.get(1, 30f)) updateStats(); // update resource stats only once every half second
            });

        }

        // region stats

        public void updateStats() {
            if (last.any()) core.each((item, amount) -> display.set(item, amount - last.get(item)));
            last.set(core);
        }

        public String format(int amount) {
            if (viewStats) // paint according to the qty?
                return (amount > 0 ? "+" : "") + amount + "[gray]" + bundle.get("unit.persecond");
            else
                return UI.formatAmount(amount);
        }

        // endregion
    }
}
