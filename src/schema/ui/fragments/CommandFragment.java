package schema.ui.fragments;

import arc.input.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;
import schema.input.*;
import schema.ui.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Fragment that is displayed when the [command mode][InputSystem#commanding()] is on.
public class CommandFragment extends Table
{
    /// Amount of controlled units.
    private int amount;
    /// Amount of controlled units of each type.
    private int[] amounts = new int[content.units().size];

    /// Available and active commands.
    private Bits availableCommands = new Bits(), activeCommands = new Bits(), lastCommands = new Bits();
    /// Available and active stances.
    private Bits availableStances  = new Bits(), activeStances  = new Bits(), lastStances  = new Bits();

    /// Builds the fragment and overrides the original.
    public void build(Group parent)
    {
        parent.addChild(this);
        parent.removeChild(Reflect.get(ui.hudfrag.blockfrag, "toggler"));

        var out = new Seq<UnitStance>();

        setFillParent(true);
        rebuild();
        update(() ->
        {
            int lastAmount = amount;
            amount = 0;
            java.util.Arrays.fill(amounts, 0);

            availableCommands.clear();
            activeCommands.clear();
            availableStances.clear();
            activeStances.clear();

            insys.releaseUnits(u ->
            {
                amount++;
                amounts[u.type.id]++;

                activeCommands.set(u.command().command.id);
                activeStances.or(u.command().stances);

                u.type.commands.each(c -> availableCommands.set(c.id));
                u.type.getUnitStances(u, out);
                out.each(c -> availableStances.set(c.id));
                out.clear();

                return false;
            });

            if (lastAmount != amount || !lastCommands.equals(availableCommands) || !lastStances.equals(availableStances))
            {
                lastCommands.set(availableCommands);
                lastStances.set(availableStances);
                rebuild();
            }
        }
        ).visible(insys::commanding);
    }

    /// Rebuilds the fragment.
    public void rebuild()
    {
        clear();
        bottom().table(Style.find("panel-n-shape"), cont ->
        {
            cont.margin(12f, 12f, 4f, 12f);
            cont.defaults().pad(4f);

            if (amount == 0) cont.add("@cmnd.empty").height(48f);
            else
            {
                for (int i = 0; i < amounts.length; i++) if (amounts[i] > 0)
                {
                    var type = content.unit(i);
                    var help = amounts[i];

                    cont.table(t ->
                    {
                        t.add(StatValues.stack(type, help)).size(32f).get().getListeners().removeAll(ClickListener.class::isInstance);
                        t.addListener(new HandCursorListener());

                        t.clicked(KeyCode.mouseLeft,  () -> insys.releaseUnits(u -> u.type != type));
                        t.clicked(KeyCode.mouseRight, () -> insys.releaseUnits(u -> u.type == type));

                        t.hovered(() -> t.background(Style.find("button-over")));
                        t.exited (() -> t.background(null));
                    }
                    ).size(48f).touchable(Touchable.enabled);
                }

                cont.image().growY().width(4f).color(Pal.accent);

                cont.add(bundle.format("cmnd.clear", Keybind.deselect.formatBind()));

                cont.image().growY().width(4f).color(Pal.accent);

                content.unitCommands().each(c -> availableCommands.get(c.id), c ->
                {
                    cont.button(c.getIcon(), Style.ibc, () -> insys.commandUnits(c)).size(48f).checked(_ -> activeCommands.get(c.id)).tooltip(c.localized());
                });

                cont.image().growY().width(4f).color(Pal.accent);

                content.unitStances().each(s -> availableStances.get(s.id), s ->
                {
                    cont.button(s.getIcon(), Style.ibc, () -> insys.commandUnits(s, !activeStances.get(s.id))).size(48f).checked(_ -> activeStances.get(s.id)).tooltip(s.localized());
                });
            }
        }).touchable(Touchable.enabled);
    }
}
