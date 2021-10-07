package mindustry.scheme;

import mindustry.gen.*;
import mindustry.content.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class SchemeUtils{

	public static void placeCore(){
        var tile = world.tiles.get(player.tileX(), player.tileY());
        if(tile != null){
            tile.setNet(tile.block() != Blocks.coreShard ? Blocks.coreShard : Blocks.air, player.team(), 0);
            if(settings.getBool("adminssecret")) Call.sendChatMessage("/core small");
        }
    }
	
}