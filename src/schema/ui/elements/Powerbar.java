package schema.ui.elements;

import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.power.*;
import schema.*;

import static arc.Core.*;

/// Elements that displays a power bar.
public class Powerbar extends Bar
{
    public Powerbar(PowerGraph graph, boolean balance)
    {
        final var out = new StringBuilder(bundle.get(balance ? "hud.power" : "hud.store"));
        final var len = out.length();
        super
        (
            () ->
            {
                out.setLength(len);

                if (balance)
                    out.append(graph.getPowerBalance() >= 0f ? "+" : "").append(Tools.format(graph.getPowerBalance() * 60f)).append("[light]/s");
                else
                    out.append(Tools.format(graph.getLastPowerStored())).append("[light]/[white]").append(Tools.format(graph.getLastCapacity()));

                return out;
            },
            () -> Pal.powerBar,
            () -> balance ? graph.getSatisfaction() : graph.getLastPowerStored() / graph.getLastCapacity()
        );
    }
}
