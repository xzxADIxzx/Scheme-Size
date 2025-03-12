package schema.ui.hud;

import arc.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.modules.*;
import schema.input.*;
import schema.ui.*;

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

    // TODO power grids

    public CoreInfo() { super(Style.find("panel-top")); }

    /** Builds the subfragment. */
    public void build() {
        Events.run(ResetEvent.class, used::clear);
        Events.run(WorldLoadEvent.class, () -> {
            team = player.team();
            rebuild();
        });

        update(() -> {
            core = team.data().hasCore() ? team.core().items : null;
            if (core != null && content.items().contains(i -> core.has(i) && used.add(i))) rebuild();
        });
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
                        ? formatFlow(core.getFlowRate(i))
                        : format(core.get(i), false)
                ).minWidth(80f).padLeft(4f).left();

                if (t.getChildren().size % width == 0) t.row();

            })).growY().width(4f * (24f + 4f + 80f)).padBottom(8f).row();

        }).growY();
        table(btns -> {
            btns.button(Style.icon(team), Style.ibt, 40f, () -> chooseTeam = !chooseTeam).size(48f).padBottom(8f).row();
            btns.button(Icon.menu,        Style.ibc,      () -> chooseGrid = !chooseGrid).size(48f);
        }).top();
    }

    /** Formats the given number as a flow. */
    public String formatFlow(float num) { return (num > 0 ? "[green]+" : num < 0 ? "[scarlet]" : "") + format(num, true) + "[light]/s"; }
}
