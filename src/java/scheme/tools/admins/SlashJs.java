package scheme.tools.admins;

import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import scheme.tools.MessageQueue;
import scheme.tools.RainbowTeam;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class SlashJs implements AdminsTools {

    public void manageUnit() {
        if (unusable()) return;
        unit.select(false, true, false, (target, team, unit, amount) -> {
            if (!canCreate(team, unit)) return;
            getPlayer(target);
            send("player.unit().spawnedByCore = true");
            send("player.unit(@.spawn(player.team(), player))", getUnit(unit));
            units.refresh();
        });
    }

    public void spawnUnits() {
        if (unusable()) return;
        unit.select(true, true, true, (target, team, unit, amount) -> {
            if (amount == 0f) {
                send("Groups.unit.each(u => u.team == Team.@ && u.type == @, u => u.spawnedByCore = true)", team, getUnit(unit));
                return;
            }

            if (!canCreate(team, unit)) return;
            getPlayer(target);
            send("var unit = @", getUnit(unit));
            send("for (var i = 0; i < @; i++) unit.spawn(Team.@, player)", amount, team);
            units.refresh();
        });
    }

    public void manageEffect() {
        if (unusable()) return;
        effect.select(true, true, false, (target, team, effect, amount) -> {
            getPlayer(target);
            if (amount == 0f) send("player.unit().unapply(" + getEffect(effect) + ")");
            else send("player.unit().apply(" + getEffect(effect) + ", " + amount + ")");
        });
    }

    public void manageItem() {
        if (unusable()) return;
        item.select(true, false, true, (target, team, item, amount) -> {
            if (!hasCore(team)) return;
            send("Team.@.core().items.add(@, @)", team, getItem(item), fixAmount(item, amount));
        });
    }

    public void manageTeam() {
        if (unusable()) return;
        team.select((target, team) -> {
            if (team != null) {
                RainbowTeam.remove(target);
                send("Groups.player.getByID(@).team(Team.@)", target.id, team);
            } else
                RainbowTeam.add(target, t -> send("Groups.player.getByID(@).team(Team.get(@))", target.id, t.id));
        });
    }

    public void placeCore() {
        if (unusable()) return;
        getPlayer(player);
        send("var tile = player.tileOn()");
        send("if (tile != null) tile.setNet(tile.build instanceof CoreBlock.CoreBuild ? Blocks.air : Blocks.coreShard, player.team(), 0)");
    }

    public void despawn(Player target) {
        if (unusable()) return;
        getPlayer(target);
        send("player.unit().spawnedByCore = true");
        send("player.clearUnit()");
    }

    public void teleport(Position pos) {
        if (unusable()) return;
        String conpos = "(player.con, " + pos.toString().replace("(", ""); // Vec2 and Point2 returns (x, y)
        getPlayer(player);
        send("var spawned = player.unit().spawnedByCore; var unit = player.unit(); unit.spawnedByCore = false; player.clearUnit()");
        send("unit.set@; Call.setPosition@; Call.setCameraPosition@", pos, conpos, conpos);
        send("player.unit(unit); unit.spawnedByCore = spawned");
    }

    public void fill(int sx, int sy, int ex, int ey) {
        if (unusable()) return;
        tile.select((floor, block, overlay) -> {
            edit(floor, block, overlay);
            send("for (var x = @; x <= @; x++) for (var y = @; y <= @; y++) todo(Vars.world.tile(x, y))", sx, ex, sy, ey);
        });
    }

    public void brush(int x, int y, int radius) {
        if (unusable()) return;
        tile.select((floor, block, overlay) -> {
            edit(floor, block, overlay);
            send("Geometry.circle(@, @, @, (cx, cy) => todo(Vars.world.tile(cx, cy)))", x, y, radius);
        });
    }

    public void flush(Seq<BuildPlan> plans) {
        if (unusable()) return;
        ui.showInfoFade("@admins.notsupported");
    }

    private static void send(String command, Object... args) {
        MessageQueue.send("/js " + Strings.format(command, args));
    }

    private static void getPlayer(Player target) {
        send("var player = Groups.player.getByID(@)", target.id);
    }

    private static String getUnit(UnitType unit) {
        return "Vars.content.unit(" + unit.id + ")";
    }

    private static String getEffect(StatusEffect effect) {
        return "Vars.content.statusEffects().get(" + effect.id + ")";
    }

    private static String getItem(Item item) {
        return "Vars.content.item(" + item.id + ")";
    }

    private static String getBlock(Block block) {
        return block == null ? "null" : "Vars.content.block(" + block.id + ")";
    }

    private static void edit(Block floor, Block block, Block overlay) {
        boolean fo = floor != null || overlay != null;

        send("f = @; b = @; o = @", getBlock(floor), getBlock(block), getBlock(overlay));
        send("todo = tile => { if(tile!=null){" + (fo ? "sflr(tile);" : "") + (block != null ? "if(tile.block()!=b)tile.setNet(b)" : "") + "} }");
        if (fo) send("sflr = tile => { if(tile.floor()!=f||tile.overlay()!=o)tile.setFloorNet(f==null?tile.floor():f,o==null?tile.overlay():o) }");
    }
}
