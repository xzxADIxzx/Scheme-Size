package mindustry.scheme;

import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.content.*;

import static arc.Core.*;
import static mindustry.Vars.*;

// all the helper functions here ...
// also this class makes it easy to add admin`s commands
public class SchemeUtils{

    public static void template(Runnable admins, Runnable js, Runnable server){
        if(!settings.getBool("enabledsecret")) ui.showInfoFade("@feature.secretonly");
        else if(settings.getBool("adminssecret")){
            if(settings.getBool("usejs")) js.run();
            else admins.run();
        } else {
            if(net.client()) ui.showInfoFade("@feature.serveronly");
            else server.run();
        }
    }

	public static void history(){
		Call.sendChatMessage("/history");
	}

	public static void toggleCoreItems(){
		settings.put("coreitems", !settings.getBool("coreitems"));
	}

    public static void changeUnit(){
        Runnable admins = () -> {
            SchemeSize.unit.select(false, true, (ppl, unit, amount) -> {
                if(!hasCore(ppl)) return;
                Call.sendChatMessage("/unit " + unit.id + " " + ppl.name);
                SchemeSize.render.update();
            });
        };
        Runnable js = () -> {
            SchemeSize.unit.select(false, true, (ppl, unit, amount) -> {
                if(!hasCore(ppl)) return;
                Call.sendChatMessage(js(getPlayer(ppl)));
                Call.sendChatMessage(js("player.unit().spawnedByCore = true"));
                Call.sendChatMessage(js("player.unit(" + getUnit(unit) + ".spawn(player.team(), player.x, player.y))"));
                SchemeSize.render.update();
            });
        };
        Runnable server = () -> {
            SchemeSize.unit.select(false, true, (ppl, unit, amount) -> {
                if(!hasCore(ppl)) return;
                ppl.unit().spawnedByCore(true);
                ppl.unit(unit.spawn(ppl.team(), ppl.x, ppl.y));
                SchemeSize.render.update();
            });
        };
        template(admins, js, server);
    }

    public static void changeEffect(){
        Runnable admins = () -> {
            ui.showInfoFade("@feature.jsonly");
        };
        Runnable js = () -> {
            SchemeSize.effect.select(true, true, (ppl, effect, amount) -> {
                Call.sendChatMessage(js(getPlayer(ppl)));
                if(amount.get() == 0) Call.sendChatMessage(js("player.unit().unapply(" + getEffect(effect) + ")"));
                else Call.sendChatMessage(js("player.unit().apply(" + getEffect(effect) + ", " + amount.get() + ")"));
            });
        };
        Runnable server = () -> {
            SchemeSize.effect.select(true, true, (ppl, effect, amount) -> {
                if(amount.get() == 0) ppl.unit().unapply(effect);
                else ppl.unit().apply(effect, amount.get());
            });
        };
        template(admins, js, server);
    }

    public static void changeItem(){
        Runnable admins = () -> {
            SchemeSize.item.select(true, false, (ppl, item, amount) -> {
                if(!hasCore(ppl)) return;
                Call.sendChatMessage("/give " + item.id + " " + fix(item, (int)amount.get()));
            });
        };
        Runnable js = () -> {
            SchemeSize.item.select(true, false, (ppl, item, amount) -> {
                if(!hasCore(ppl)) return;
                Call.sendChatMessage(js(getPlayer(ppl)));
                Call.sendChatMessage(js("player.core().items.add(" + getItem(item) + ", " + fix(item, (int)amount.get())) + ")");
            });
        };
        Runnable server = () -> {
            SchemeSize.item.select(true, false, (ppl, item, amount) -> {
                if(!hasCore(ppl)) return;
                ppl.core().items.add(item, fix(item, (int)amount.get()));
            });
        };
        template(admins, js, server);
    }

	public static void changeTeam(){
        Runnable admins = () -> {
            SchemeSize.team.select((ppl, team) -> {
                Call.sendChatMessage("/team " + team.id + " " + ppl.name);
            });
        };
        Runnable js = () -> {
            SchemeSize.team.select((ppl, team) -> {
                Call.sendChatMessage(js(getPlayer(ppl)));
                Call.sendChatMessage(js("player.team(" + getTeam(team) + ")"));
            });
        };
        Runnable server = () -> {
            SchemeSize.team.select((ppl, team) -> {
                ppl.team(team);
            });
        };
        template(admins, js, server);
    }

	public static void placeCore(){
        Runnable admins = () -> {
            Call.sendChatMessage("/core small");
        };
        Runnable js = () -> {
            Call.sendChatMessage(js(getPlayer(player)));
            Call.sendChatMessage(js("var tile = Vars.world.tiles.get(player.tileX(), player.tileY())"));
            Call.sendChatMessage(js("if(tile != null){ tile.setNet(tile.block() != Blocks.coreShard ? Blocks.coreShard : Blocks.air, player.team(), 0) }"));
        };
        Runnable server = () -> {
            var tile = world.tiles.get(player.tileX(), player.tileY());
            if(tile != null) tile.setNet(tile.block() != Blocks.coreShard ? Blocks.coreShard : Blocks.air, player.team(), 0);
        };
        template(admins, js, server);
    }

    public static void lookAt(){
    	player.unit().lookAt(input.mouseWorld());
    }

    public static void teleport(Vec2 pos){
        player.set(pos);
        player.unit().set(pos);
    }

    public static void kill(Player ppl){
        Runnable admins = () -> {
            Call.sendChatMessage("/despw " + ppl.id);
        };
        Runnable js = () -> {
            Call.sendChatMessage(js(getPlayer(ppl)));
            Call.sendChatMessage(js("player.unit().spawnedByCore = true"));
            Call.sendChatMessage(js("player.clearUnit()"));
        };
        Runnable server = () -> {
            ppl.unit().spawnedByCore(true);
            ppl.clearUnit();
        };
        template(admins, js, server);
    }

    public static void spawnUnit(){
        Runnable admins = () -> {
            SchemeSize.unit.select(true, false, (ppl, unit, amount) -> {
                if(!hasCore(ppl)) return;
                Call.sendChatMessage("/spawn " + unit.id + " " + (int)amount.get() + " " + player.team().id);
                SchemeSize.render.update();
            });
        };
        Runnable js = () -> {
            SchemeSize.unit.select(true, true, (ppl, unit, amount) -> {
                if(!hasCore(ppl)) return;
                Call.sendChatMessage(js(getPlayer(ppl)));
                Call.sendChatMessage(js("var unit = " + getUnit(unit)));
                Call.sendChatMessage(js("for(var i = 0; i < " + amount.get() + "; i++) unit.spawn(player.team(), player.x, player.y)"));
                SchemeSize.render.update();
            });
        };
        Runnable server = () -> {
            SchemeSize.unit.select(true, true, (ppl, unit, amount) -> {
                if(!hasCore(ppl)) return;
                for (int i = 0; i < amount.get(); i++) unit.spawn(ppl.team(), ppl.x, ppl.y);
                SchemeSize.render.update();
            });
        };
        template(admins, js, server);
    }

    public static void showComb(){
        SchemeSize.keycomb.show();
    }

    public static void showSecret(){
        SchemeSize.secret.show();
    }

    public static void edit(int sx, int sy, int ex, int ey){
        Runnable admins = () -> {
            SchemeSize.tile.select(false, (floor, block, overlay) -> {
                Call.sendChatMessage("/fill " + (ex - sx + 1) + " " + (ey - sy + 1) + " " + (floor == null ? (block == null ? (overlay == null ? "" : overlay.id) : block.id) : floor.id));
            });
        };
        Runnable js = () -> {
            SchemeSize.tile.select(false, (floor, block, overlay) -> {
                Call.sendChatMessage(js("var floor = " + getBlock(floor)));
                Call.sendChatMessage(js("var block = " + getBlock(block)));
                Call.sendChatMessage(js("var overlay = " + getBlock(overlay)));
                Call.sendChatMessage(js("var setb = (tile) => { if(block != null) tile.setNet(block) }"));
                Call.sendChatMessage(js("var setf = (tile) => { tile.setFloorNet(floor==null?tile.floor():floor.asFloor(),overlay==null?tile.overlay():overlay.asFloor());setb(tile) }"));
                Call.sendChatMessage(js("var nulc = (tile) => { if(tile != null) setf(tile) }"));
                Call.sendChatMessage(js("var todo = (x, y) => { nulc(tile = Vars.world.tiles.get(x, y)) }"));
                Call.sendChatMessage(js("for(var x = " + sx +"; x <= " + ex + "; x++){ for(var y = " + sy + "; y <= " + ey + "; y++){ todo(x, y) } }"));
            });
        };
        Runnable server = () -> {
            SchemeSize.tile.select(false, (floor, block, overlay) -> {
                for(int x = sx; x <= ex; x++){
                    for(int y = sy; y <= ey; y++){
                        Tile tile = world.tiles.get(x, y);
                        if(tile == null) continue;

                        tile.setFloorNet(floor == null ? tile.floor() : floor, overlay == null ? tile.overlay() : overlay);
                        if(block != null) tile.setNet(block);
                    }
                }
            });
        };
        template(admins, js, server);
    }


    // helpfull methods
    private static int fix(Item item, int amount){
        var items = player.team().core().items;
        return amount == 0 ? -items.get(item) : (items.get(item) + amount < 0 ? -items.get(item) : amount);
    }

    private static boolean hasCore(Player ppl){
        boolean has = ppl.core() != null;
        if(!has) ui.showInfoFade("@nocore");
        return has;
    }


    // js helpfull methods
    private static String js(String code){
        return "/js " + code;
    }

    private static String getPlayer(Player ppl){
        return "var player = Groups.player.getByID(" + ppl.id + ")";
    }

    private static String getUnit(UnitType unit){
        return "Vars.content.unit(" + unit.id + ")";
    }

    private static String getEffect(StatusEffect effect){
        return "Vars.content.statusEffects().get(" + effect.id + ")";
    }

    private static String getItem(Item item){
        return "Vars.content.item(" + item.id + ")";
    }

    private static String getBlock(Block block){
        return block == null ? "null" : "Vars.content.block(" + block.id + ")";
    }

    private static String getTeam(Team team){
        return "Team." + team.name;
    }
}
