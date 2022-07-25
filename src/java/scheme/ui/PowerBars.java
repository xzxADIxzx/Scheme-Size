package scheme.ui;

import mindustry.core.UI;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.power.PowerGraph;
import mindustry.world.blocks.power.PowerNode.PowerNodeBuild;

import static arc.Core.*;
import static mindustry.Vars.*;

import arc.math.geom.Point2;

public class PowerBars {

    public Building node;
    public Point2[] connections;
    public PowerGraph graph = new PowerGraph();

    public boolean setNode(Building node) {
        if (node != null && node.power != null) {
            this.node = node; // caching the power node connection for case of its absence
            this.connections = node instanceof PowerNodeBuild build ? build.config() : new Point2[0];
            this.graph = node.power.graph;
            return true;
        } else return false;
    }

    public void refreshNode() {
        if (node == null) return; // nothing to refresh
        if (setNode(world.build(node.pos()))) return;
        for (Point2 point : connections) // looking for a new power node among those to which the old one was connected
            if (setNode(world.build(point.add(node.tileX(), node.tileY()).pack()))) return;

        node = null; // correct power node not found
        graph = new PowerGraph();
    }

    public Bar balance() {
        return new Bar(
                () -> bundle.format("bar.powerbalance", (graph.getPowerBalance() >= 0 ? "+" : "") + UI.formatAmount((long) graph.getPowerBalance() * 60L)),
                () -> Pal.powerBar,
                () -> graph.getSatisfaction());
    }

    public Bar stored() {
        return new Bar(
                () -> bundle.format("bar.powerstored", UI.formatAmount((long) graph.getLastPowerStored()), UI.formatAmount((long) graph.getLastCapacity())),
                () -> Pal.powerBar,
                () -> graph.getLastPowerStored() / graph.getLastCapacity());
    }
}
