package scheme.tools.admins;

import arc.math.geom.Position;
import arc.struct.Seq;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;
import scheme.tools.MessageQueue;
import scheme.tools.RainbowTeam;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class Darkdustry implements AdminsTools {

    public void manageUnit() {
        if (unusable()) return;
        unit.select(false, true, false, (target, team, unit, amount) -> {
            send("unit", unit.id, "#" + target.id);
            units.refresh();
        });
    }

    public void spawnUnits() {
        if (unusable()) return;
        unit.select(true, false, true, (target, team, unit, amount) -> {
            if (amount == 0f) {
                send("despawn");
                return;
            }

            send("spawn", unit.id, amount.intValue(), team.id);
            units.refresh();
        });
    }

    public void manageEffect() {
        if (unusable()) return;
        effect.select(true, true, false, (target, team, effect, amount) -> send("effect", effect.id, amount.intValue() / 60, "#" + target.id));
    }

    public void manageItem() {
        if (unusable()) return;
        item.select(true, false, true, (target, team, item, amount) -> send("give", item.id, amount.intValue(), team.id));
    }

    public void manageTeam() {
        if (unusable()) return;
        team.select((target, team) -> {
            if (team != null) {
                RainbowTeam.remove(target);
                send("team", team.id, "#" + target.id);
            } else
                RainbowTeam.add(target, t -> send("team", t.id, "#" + target.id));
        });
    }

    public void placeCore() {
        if (unusable()) return;
        if (player.buildOn() instanceof CoreBuild)
            sendPacket("fill", "null 0 null", player.tileX(), player.tileY(), 1, 1);
        else send("core");
    }

    public void despawn(Player target) {
        if (unusable()) return;
        send("despawn", "#" + target.id);
    }

    public void teleport(Position pos) {
        if (unusable()) return;
        send("tp", (int) pos.getX() / tilesize, (int) pos.getY() / tilesize);
    }

    public void fill(int sx, int sy, int ex, int ey) {
        if (unusable()) return;
        tile.select((floor, block, overlay) -> sendPacket("fill", id(floor), id(block), id(overlay), sx, sy, ex - sx + 1, ey - sy + 1));
    }

    public void brush(int x, int y, int radius) {
        if (unusable()) return;
        tile.select((floor, block, overlay) -> sendPacket("brush", id(floor), id(block), id(overlay), x, y, radius));
    }

    public void flush(Seq<BuildPlan> plans) {
        if (unusable()) return;
        ui.showInfoFade("@admins.notsupported");
    }


    private static void send(String command, Object... args) {
        StringBuilder message = new StringBuilder(netServer.clientCommands.getPrefix()).append(command);
        for (var arg : args) message.append(" ").append(arg);
        MessageQueue.send(message.toString());
    }

    private static void sendPacket(String command, Object... args) {
        StringBuilder message = new StringBuilder();
        for (var arg : args) message.append(arg).append(" ");
        Call.serverPacketReliable(command, message.toString());
    }

    private static String id(Block block) {
        return block == null ? "null" : String.valueOf(block.id);
    }
}
