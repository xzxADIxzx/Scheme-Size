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
import mindustry.world.blocks.power.*;
import mindustry.world.modules.*;
import schema.*;
import schema.input.*;
import schema.ui.*;
import schema.ui.elements.*;

import static mindustry.Vars.*;

/// Subfragment that displays the core items and power grids.
public class CoreInfo extends Table
{
    /// Team whose core items and power grids are shown.
    private Team team;
    /// Whether the player is choosing a team or a grid.
    private boolean choosesTeam, choosesGrid;

    /// List of items that were seen.
    private final ObjectSet<Item> seen = new ObjectSet<>();
    /// Item module obtained from a core.
    private ItemModule core;

    /// Amounted mean of core items' flow.
    private WindowedMean[] flow;
    /// Last amount of items of each type.
    private int[] last;

    /// List of power graphs that were found.
    private final ObjectSet<PowerGraph> graphs = new ObjectSet<>();
    /// Power graph obtained from a building.
    private PowerGraph graph;

    public CoreInfo() { super(Style.find("panel-u-shape")); }

    /// Builds the fragment.
    public void build()
    {
        Events.run(ResetEvent.class, seen::clear);
        Events.run(ResetEvent.class, graphs::clear);
        Events.run(WorldLoadEvent.class, () ->
        {
            team = player.team();
            graph = new PowerGraph(true);

            rebuild();
        });

        flow = new WindowedMean[content.items().size];
        last = new int         [content.items().size];

        for (int i = 0; i < flow.length; i++) flow[i] = new WindowedMean(20);

        Timer.schedule(() ->
        {
            if (state.isMenu() || state.isPaused()) return;

            boolean[] rebuild = { false };

            core = team.data().hasCore() ? team.core().items : null;
            if (core != null)
            {
                content.items().each(seen::contains, i ->
                {
                    flow[i.id].add(core.get(i) - last[i.id]);
                    last[i.id] = core.get(i);
                });
                core.each((i, a) -> { if (seen.add(i)) rebuild[0] = true; });
            }

            if (Groups.powerGraph.contains(g -> valid(g.graph()) && graphs.add(g.graph()))) rebuild[0] = true;
            graphs.each(g ->
            {
                if (valid(g)) return;

                graphs.remove(g);
                if (graph == g) graph = new PowerGraph(true);

                rebuild[0] = true;
            });

            if (rebuild[0]) rebuild();
        }, 0f, .2f);
    }

    /// Rebuilds the fragment.
    public void rebuild()
    {
        margin(4f, 12f, 12f, 12f);
        defaults().pad(4f);

        clearChildren();
        table(cont ->
        {
            cont.table(t -> content.items().each(seen::contains, i ->
            {
                t.margin(0f, 0f, 4f, 0f).top().left();

                t.image(i.uiIcon).size(24f).tooltip(i.localizedName);
                t.label(() -> core == null
                    ? "[light]0"
                    : Keybind.display_prod.down()
                        ? Tools.flow(flow[i.id].mean())
                        : Tools.format(core.get(i))
                ).minWidth(80f).padLeft(4f).left();

                if (t.getChildren().size % 8 == 0) t.row();
            }
            )).growY().width(4f * (24f + 4f + 80f)).row();

            cont.collapser(t ->
            {
                t.margin(4f, 4f, 4f, 0f).left();

                for (var team : Team.all) if (team.active() || team.id <= 5)
                {
                    t.button(Style.icon(team), Style.ibt, 32f, () ->
                    {
                        this.team = team;
                        rebuild();
                    }
                    ).checked(_ -> this.team == team).size(40f).padLeft(-4f).get().getImage().setColor(team.active() ? Color.white : Pal.gray);
                }
            }, true, () -> choosesTeam).growX().row();

            cont.add(new Powerbar(graph, true )).growX().height(20f).pad(4f, 0f, 4f, 0f).row();
            cont.add(new Powerbar(graph, false)).growX().height(20f).pad(4f, 0f, 0f, 0f).row();

            cont.collapser(t ->
            {
                t.margin(12f, 0f, 0f, 0f).top();

                if (graphs.isEmpty()) t.add("@hud.grids").padTop(-4f);
                else graphs.each(graph ->
                {
                    t.button(b ->
                    {
                        b.margin(8f);
                        b.add(new Powerbar(graph, true )).grow().row();
                        b.add(new Powerbar(graph, false)).grow().row();
                    },
                    Style.cbt, () ->
                    {
                        this.graph = graph;
                        rebuild();
                    }
                    ).checked(b -> this.graph == graph).growX().height(48f).padTop(-4f).row();
                });
            }, true, () -> choosesGrid).growX().row();
        }
        ).growY();
        table(btns ->
        {
            btns.button(Style.icon(team), Style.ibt, 40f, () -> choosesTeam = !choosesTeam).checked(_ -> choosesTeam).size(48f).padBottom(8f).row();
            btns.button(Icon.menu,        Style.ibc,      () -> choosesGrid = !choosesGrid).checked(_ -> choosesGrid).size(48f);
        }).top();
    }

    /// Whether the power graph is valid.
    private boolean valid(PowerGraph graph)
    {
        return graph.all.size > 1 && graph.all.peek().team == team && Groups.powerGraph.contains(g -> g.graph() == graph);
    }
}
