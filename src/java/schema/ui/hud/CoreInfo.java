package schema.ui.hud;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.power.*;
import mindustry.world.modules.*;
import schema.input.*;
import schema.ui.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/** Subfragment that displays the core items, their flow and power grids. */
public class CoreInfo extends Table {

    /** Team whose core items and power grids are displayed. */
    private Team team = Team.derelict;
    /** Whether the player is choosing a team or grid. */
    private boolean chooseTeam, chooseGrid;

    /** Set of items that were used in this sector. */
    private final ObjectSet<Item> used = new ObjectSet<>();
    /** Item module obtained from the core of the selected team. */
    private ItemModule core;

    /** Components used to calculate the mean of core items. */
    private WindowedMean[] flow;
    /** Last amount of items of each type. */
    private int[] last;

    /** Set of power graphs that were found on the map. */
    private final ObjectSet<PowerGraph> graphs = new ObjectSet<>();
    /** Power graph obtained from buildings' power modules. */
    private PowerGraph graph;

    public CoreInfo() { super(Style.find("panel-top")); }

    /** Builds the subfragment. */
    public void build() {
        Events.run(ResetEvent.class, used::clear);
        Events.run(ResetEvent.class, graphs::clear);
        Events.run(WorldLoadEvent.class, () -> {
            team = player.team();
            graph = new PowerGraph(true);

            rebuild();
        });

        flow = new WindowedMean[content.items().size];
        last = new int[content.items().size];

        for (int i = 0; i < flow.length; i++)
            flow[i] = new WindowedMean(8);

        Timer.schedule(() -> {
            if (state.isMenu()) return;
            var toRebuild = new boolean[] { false };

            core = team.data().hasCore() ? team.core().items : null;
            if (core != null) core.each((i, a) -> { if (used.add(i)) toRebuild[0] = true; });

            if (core != null && state.isPlaying()) content.items().each(used::contains, i -> {
                flow[i.id].add(core.get(i) - last[i.id]);
                last[i.id] = core.get(i);
            });

            graphs.each(g -> {
                if (valid(g) && Groups.powerGraph.contains(e -> e.graph() == g)) return;

                graphs.remove(g);
                toRebuild[0] = true;

                if (graph == g) graph = new PowerGraph(true);
            });

            if (Groups.powerGraph.contains(e -> valid(e.graph()) && graphs.add(e.graph()))) toRebuild[0] = true;
            if (toRebuild[0]) rebuild();
        }, 0f, .5f);
    }

    /** Rebuilds the subfragment to update the list of core items and power grids. */
    public void rebuild() {
        margin(4f, 12f, 12f, 12f);
        defaults().pad(4f);

        clearChildren();
        table(cont -> {

            cont.table(t -> content.items().each(used::contains, i -> {

                t.top().left();

                t.image(i.uiIcon).size(24f);
                t.label(() -> core == null
                    ? "[disabled]-"
                    : Keybind.display_prod.down()
                        ? formatFlow(flow[i.id].mean())
                        : format(core.get(i), false)
                ).minWidth(80f).padLeft(4f).left();

                if (t.getChildren().size % 8 == 0) t.row();

            })).growY().width(4f * (24f + 4f + 80f)).pad(0f, 0f, 4f, 0f).row();

            cont.collapser(t -> {

                t.margin(4f, 4f, 4f, 0f).left();

                for (var team : Team.all) {
                    if (!team.active() && team.id >= 6) continue;

                    t.button(Style.icon(team), Style.ibt, 32f, () -> {
                        this.team = team;
                        rebuild();
                    }).checked(i -> this.team == team).size(40f).padLeft(-4f).get().getImage().setColor(team.active() ? Color.white : Pal.lightishGray);
                }

            }, true, () -> chooseTeam).growX().row();

            cont.add(balance()).growX().height(20f).pad(4f, 0f, 4f, 0f).row();
            cont.add(stored()).growX().height(20f).pad(4f, 0f, 0f, 0f).row();

        }).growY();
        table(btns -> {
            btns.button(Style.icon(team), Style.ibt, 40f, () -> chooseTeam = !chooseTeam).checked(i -> chooseTeam).size(48f).padBottom(8f).row();
            btns.button(Icon.menu,        Style.ibc,      () -> chooseGrid = !chooseGrid).checked(i -> chooseGrid).size(48f);
        }).top();
    }

    /** Formats the given number as a flow. */
    public String formatFlow(float num) { return (num > 0 ? "[green]+" : num < 0 ? "[scarlet]" : "") + format(num, true) + "[light]/s"; }

    /** Whether the given power graph is valid. */
    public boolean valid(PowerGraph graph) { return graph.all.size > 1 && graph.all.peek().team == team; }

    /** Creates a power bar that displays the power balance. */
    public Bar balance() {
        return new Bar(
            () -> bundle.format("bar.powerbalance", (graph.getPowerBalance() >= 0f ? "+" : "") + format(graph.getPowerBalance() * 60f, false)),
            () -> Pal.powerBar,
            () -> graph.getSatisfaction());
    }

    /** Creates a power bar that displays the power stored. */
    public Bar stored() {
        return new Bar(
            () -> bundle.format("bar.powerstored", format(graph.getLastPowerStored(), false), format(graph.getLastCapacity(), false)),
            () -> Pal.powerBar,
            () -> graph.getLastPowerStored() / graph.getLastCapacity());
    }
}
