package mindustry.scheme;

import arc.math.geom.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.content.*;

import static arc.Core.*;
import static mindustry.Vars.*;

// all the helper functions here ...
// also this class makes it easy to add admin`s commands
public class SchemeUtils{

    public static void template(Runnable admins, Runnable server){
        if(settings.getBool("adminssecret")){
            admins.run();
        }else {
            if(net.client()){
                ui.showInfoToast("@feature.serveronly", 5);
            }else{
                server.run();
            }
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
            SchemeSize.unit.select(false, (unit, amount) -> {
                Call.sendChatMessage("/units change " + unit.name);
                updatefrag();
            });
        };
        Runnable server = () -> {
            SchemeSize.unit.select(false, (unit, amount) -> { // I think there is an easier way, but I do not know it
                var oldUnit = player.unit();
                var newUnit = unit.spawn(player.team(), player.x, player.y);
                Call.unitControl(player, newUnit);
                oldUnit.kill(); // oof... remove does work in multiplayer, so I use kill
                updatefrag();
            });
        };
        template(admins, server);
    }

    public static void changeEffect(){
        SchemeSize.effect.select(true, (effect, amount) -> {
            if(amount.get() == 0) player.unit().unapply(effect);
            else player.unit().apply(effect, amount.get());
        });
    }

    public static void changeItem(){
        Runnable admins = () -> {
            SchemeSize.item.select(true, (item, amount) -> {
                Call.sendChatMessage("/give " + item.name + " " + String.valueOf(amount.get()));
            });
        };
        Runnable server = () -> {
            SchemeSize.item.select(true, (item, amount) -> {
                var items = player.team().core().items;
                int fix = items.get(item) - (int)amount.get() > 0 ? (int)amount.get() : items.get(item);
                items.add(item, fix);
            });
        };
        template(admins, server);
    }

	public static void switchTeam(){
        var index = new Seq(Team.baseTeams).indexOf(player.team());
        var team = Team.baseTeams[++index < 6 ? index : 0];
        Runnable admins = () -> {
            Call.sendChatMessage("/team " + team.name);
        };
        Runnable server = () -> {
            player.team(team);
        };
        template(admins, server);
    }

    public static void switchTeamBtw(){
        var team = player.team() != Team.sharded ? Team.sharded : Team.crux;
        Runnable admins = () -> {
            Call.sendChatMessage("/team " + team.name);
        };
        Runnable server = () -> {
            player.team(team);
        };
        template(admins, server);
    }

	public static void placeCore(){
        Runnable admins = () -> {
            Call.sendChatMessage("/core small");
        };
        Runnable server = () -> {
            var tile = world.tiles.get(player.tileX(), player.tileY());
            if(tile != null) tile.setNet(tile.block() != Blocks.coreShard ? Blocks.coreShard : Blocks.air, player.team(), 0);
        };
        template(admins, server);
    }

    public static void lookAt(){
    	player.unit().lookAt(input.mouseWorld());
    }

    public static void teleport(Vec2 pos){
    	player.unit().set(pos);
    }

    public static void selfDest(){
    	player.unit().kill();
    	updatefrag();
    }

    public static void spawnUnit(){
        Runnable admins = () -> {
            SchemeSize.unit.select(true, (unit, amount) -> {
                Call.sendChatMessage("/spawn " + unit.name + " " + String.valueOf(amount.get()) + " " + player.team().name);
            });
        };
        Runnable server = () -> {
            SchemeSize.unit.select(true, (unit, amount) -> {
                for (int i = 0; i < amount.get(); i++)
                    unit.spawn(player.team(), player.x, player.y);
            });
        };
        template(admins, server);
    }

    private static void updatefrag(){
        SchemeSize.hudfrag.updateShield(player.unit());
    }
}
