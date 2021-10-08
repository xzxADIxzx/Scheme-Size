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
            SchemeSize.unit.select((u) -> Call.sendChatMessage("/unit change " + u.name));
        };
        Runnable server = () -> {
            SchemeSize.unit.select((unit) -> { // I think there is an easier way, but I do not know it
                var oldUnit = player.unit();
                var newUnit = unit.spawn(player.team(), player.x, player.y);
                Call.unitControl(player, newUnit);
                oldUnit.remove();
            });
        };
        template(admins, server);
        SchemeSize.hudfrag.updateShield(player.unit());
    }

	public static void switchTeam(){
        Runnable admins = () -> {
            Call.sendChatMessage("/team " + player.team().name);
        };
        Runnable server = () -> {
            var team = new Seq(Team.baseTeams).indexOf(player.team());
            player.team(Team.baseTeams[++team < 6 ? team : 0]);
        };
        template(admins, server);
    }

    public static void switchTeamBtw(){
        Runnable admins = () -> {
            Call.sendChatMessage("/team " + player.team().name);
        };
        Runnable server = () -> {
            player.team(player.team() != Team.sharded ? Team.sharded : Team.crux);
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
    	SchemeSize.hudfrag.updateShield(player.unit());
    }
}