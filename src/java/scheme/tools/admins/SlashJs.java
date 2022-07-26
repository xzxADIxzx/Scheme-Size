package scheme.tools.admins;

import arc.math.geom.Position;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class SlashJs implements AdminsTools {

    public void manageUnit() {
        if (unusable()) return;
        unit.select(false, true, false, (target, team, unit, amount) -> {
            if (!canCreate(team, unit)) return;
            getPlayer(target);
            js("player.unit().spawnedByCore = true");
            js("player.unit(" + getUnit(unit) + ".spawn(player.team(), player))");
            units.refresh();
        });
    }

    public void spawnUnits() {
        if (unusable()) return;
        unit.select(true, true, true, (target, team, unit, amount) -> {
            if (!canCreate(team, unit)) return;
            getPlayer(target);
            js("var unit = " + getUnit(unit));
            js("for (var i = 0; i < " + amount + "; i++) unit.spawn(Team." + team + ", player)");
            units.refresh();
        });
    }

    public void manageEffect() {
        if (unusable()) return;
        effect.select(true, true, false, (target, team, effect, amount) -> {
            getPlayer(target);
            if (amount == 0f) js("player.unit().unapply(" + getEffect(effect) + ")");
            else js("player.unit().apply(" + getEffect(effect) + ", " + amount + ")");
        });
    }

    public void manageItem() {
        if (unusable()) return;
        item.select(true, false, true, (target, team, item, amount) -> {
            if (!hasCore(team)) return;
            js("Team." + team + ".core().items.add(" + getItem(item) + ", " + fixAmount(item, amount) + ")");
        });
    }

    public void manageTeam() {
        if (unusable()) return;
        team.select((target, team) -> {
            getPlayer(target);
            js("player.team(Team." + team + ")");
        });
    }

    public void placeCore() {
        if (unusable()) return;
        getPlayer(player);
        js("var tile = player.tileOn()");
        js("if (tile != null) tile.setNet(tile.build instanceof CoreBlock.CoreBuild ? Blocks.air : Blocks.coreShard, player.team(), 0)");
    }

    public void despawn(Player target) {
        if (unusable()) return;
        getPlayer(target);
        js("player.unit().spawnedByCore = true");
        js("player.clearUnit()");
    }

    public void teleport(Position pos) {
        if (unusable()) return;
        getPlayer(player); // Vec2 and Point2 returns (x, y)
        js("player.unit().set" + pos);
    }

    public void edit(int sx, int sy, int ex, int ey) {
        if (unusable()) return;
        tile.select((floor, block, overlay) -> {
            js("var floor = " + getBlock(floor) + "; var block = " + getBlock(block) + "; var over = " + getBlock(overlay));
            js("var todo = tile => { tile.setFloorNet(floor==null?tile.floor():floor,overlay==null?tile.overlay():overlay);if(block!=null)tile.setNet(block) }");
            js("for (var x = " + sx + "; x <= " + ex + "; x++) for (var y = " + sy + "; y <= " + ey + "; y++) todo(Vars.world.tiles.getc(x, y))");
        });
    }

    public boolean unusable() {
        if (!settings.getBool("adminsenabled")) {
            ui.showInfoFade(disabled);
            return true;
        } else if (!player.admin) ui.showInfoFade("@admins.notanadmin");
        return !player.admin;
    }

    private static void js(String command) {
        Call.sendChatMessage("/js " + command);
    }

    private static void getPlayer(Player target) {
        js("var player = Groups.player.getByID(" + target.id + ")");
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
}
