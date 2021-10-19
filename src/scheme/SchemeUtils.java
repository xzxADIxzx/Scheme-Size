package mindustry.scheme;

import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.game.*;
import mindustry.content.*;

import static arc.Core.*;
import static mindustry.Vars.*;

// all the helper functions here ...
// also this class makes it easy to add admin`s commands
// oh no /js looks so bad
public class SchemeUtils{

    public static void template(Runnable admins, Runnable js, Runnable server){
        if(settings.getBool("adminssecret")){
            if(settings.getBool("usejs")) js.run();
            else admins.run();
        }else {
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
            SchemeSize.unit.select(false, false, (ppl, unit, amount) -> {
                if(!hasCore(ppl)) return;
                Call.sendChatMessage("/units change " + unit.name);
            });
        };
        Runnable js = () -> {
            SchemeSize.unit.select(false, true, (ppl, unit, amount) -> {
                if(!hasCore(ppl)) return;
                Call.sendChatMessage(js(getPlayer(ppl)));
                Call.sendChatMessage(js("var oldUnit = player.unit()"));
                Call.sendChatMessage(js("var newUnit = " + getUnit(unit) + ".spawn(player.team(), player.x, player.y)"));
                Call.sendChatMessage(js("Call.unitControl(player, newUnit)"));
                Call.sendChatMessage(js("oldUnit.kill()"));
            });
        };
        Runnable server = () -> {
            SchemeSize.unit.select(false, true, (ppl, unit, amount) -> { // I think there is an easier way, but I do not know it
                if(!hasCore(ppl)) return;
                var oldUnit = ppl.unit();
                var newUnit = unit.spawn(ppl.team(), ppl.x, ppl.y);
                Call.unitControl(ppl, newUnit);
                oldUnit.kill(); // remove does work in multiplayer, so I use kill
            });
        };
        template(admins, js, server);
        updatefrag();
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
                Call.sendChatMessage("/give " + item.name + " " + fix(item, (int)amount.get()));
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
                Call.sendChatMessage("/team " + team.name + " " + ppl.name);
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
        player.unit().set(pos);
    }

    public static void selfDest(){
        Runnable admins = () -> {
            Call.sendChatMessage("/despw");
        };
        Runnable js = () -> {
            Call.sendChatMessage(js(getPlayer(player)));
            Call.sendChatMessage(js("player.unit().kill()"));
        };
        Runnable server = () -> {
            player.unit().kill();
        };
        template(admins, js, server);
        updatefrag();
    }

    public static void spawnUnit(){
        Runnable admins = () -> {
            SchemeSize.unit.select(true, false, (ppl, unit, amount) -> {
                if(!hasCore(ppl)) return;
                Call.sendChatMessage("/spawn " + unit.name + " " + (int)amount.get() + " " + player.team().name);
            });
        };
        Runnable js = () -> {
            SchemeSize.unit.select(true, true, (ppl, unit, amount) -> {
                if(!hasCore(ppl)) return;
                Call.sendChatMessage(js(getPlayer(ppl)));
                Call.sendChatMessage(js("var unit = " + getUnit(unit)));
                Call.sendChatMessage(js("for(var i = 0; i < " + amount.get() + "; i++) unit.spawn(player.team(), player.x, player.y)"));
            });
        };
        Runnable server = () -> {
            SchemeSize.unit.select(true, true, (ppl, unit, amount) -> {
                if(!hasCore(ppl)) return;
                for (int i = 0; i < amount.get(); i++)
                    unit.spawn(player.team(), player.x, player.y);
            });
        };
        template(admins, js, server);
    }

    public static void showInfo(){
        SchemeSize.keycomb.show();
    }


    // helpfull methods
    private static void updatefrag(){
        SchemeSize.hudfrag.updateShield(player.unit());
    }

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
        return "var player = Groups.player.find(p => p.name == \"" + ppl.name + "\")";
    }

    private static String getUnit(UnitType unit){
        return "Vars.content.units().find(u => u.name == \"" + unit.name + "\")";
    }

    private static String getEffect(StatusEffect effect){
        return "Vars.content.statusEffects().find(i => i.name == \"" + effect.name + "\")";
    }

    private static String getItem(Item item){
        return "Vars.content.items().find(i => i.name == \"" + item.name + "\")";
    }

    private static String getTeam(Team team){
        return "Team." + team.name;
    }
}