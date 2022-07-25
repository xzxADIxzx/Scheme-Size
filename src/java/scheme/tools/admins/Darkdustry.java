package scheme.tools.admins;

import arc.math.geom.Position;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class Darkdustry implements AdminsTools {

    public void manageUnit() {
        if (unusable()) return;
        unit.select(false, true, false, (target, team, unit, amount) -> {
            send("unit", unit.id, target.id);
            units.refresh();
        });
    }

    public void spawnUnits() {
        if (unusable()) return;
        unit.select(true, true, true, (target, team, unit, amount) -> {
            send("spawn", unit.id, amount.intValue(), team.id);
            units.refresh();
        });
    }

    public void manageEffect() {
        if (unusable()) return;
        ui.showInfoFade("@admins.notsupported");
    }

    public void manageItem() {
        if (unusable()) return;
        item.select(true, false, true, (target, team, item, amount) -> send("give", item.id, amount.intValue(), team.id));
    }

    public void manageTeam() {
        if (unusable()) return;
        team.select((target, team) -> send("team", team.id, target.id));
    }

    public void placeCore() {
        if (unusable()) return;
        send("core");
    }

    public void despawn(Player target) {
        if (unusable()) return;
        send("despawn", target.id);
    }

    public void teleport(Position pos) {
        player.unit().set(pos); // it's always available
    }

    public void edit(int sx, int sy, int ex, int ey) {
        if (unusable()) return;
        tile.select((floor, block, overlay) -> {
            if (floor != null) send("fill", sx, sy, ex, ey, floor.id);
            if (block != null) send("fill", sx, sy, ex, ey, block.id);
            if (overlay != null) send("fill", sx, sy, ex, ey, overlay.id);
        });
    }

    public boolean unusable() {
        if (!settings.getBool("adminsenabled")) {
            ui.showInfoFade(disabled);
            return true;
        } else if (!player.admin) ui.showInfoFade("@admins.notanadmin");
        return !player.admin; // darkness was be here
    }

    private static void send(String command, Object... args) {
        StringBuilder message = new StringBuilder(netServer.clientCommands.getPrefix()).append(command);
        for (var arg : args) message.append(" ").append(arg);
        Call.sendChatMessage(message.toString());
    }
}
