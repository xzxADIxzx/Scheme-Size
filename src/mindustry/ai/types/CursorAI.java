package mindustry.ai.types;

import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;

import static mindustry.Vars.*;

public class CursorAI extends AIController{

    public @Nullable Unit following;

    @Override
    public void updateMovement(){
        if(following != null){
            unit.movePref( Tmp.v1.set(following.aimX(), following.aimY()).sub(player).scl(1f / 25f * unit.speed()).limit(unit.speed()) );
            player.boosting = following.isFlying();
        }
    }
}