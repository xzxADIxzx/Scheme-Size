package mindustry.ai.types;

import arc.util.*;
import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.entities.units.*;

import static mindustry.Vars.*;

public class CircleAI extends AIController{

    public static @Nullable Player following;
    public static Vec2 target = new Vec2();

    public void init(Player ppl){
        following = null;
        if(ppl == player) target.set(ppl.x, ppl.y);
        else following = ppl;
    }

    @Override
    public void updateMovement(){
        circle(target, 80f);
    }

    @Override
    public void updateTargeting(){
        if(following != null) target.set(following.x, following.y);
    }
}