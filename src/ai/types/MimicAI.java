package mindustry.ai.types;

import arc.util.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class MimicAI extends AIController{

    public @Nullable Unit following;

    @Override
    public void updateMovement(){
        if(following != null){
            moveTo(following, following.hitSize() / 2f * 1.1f + unit.hitSize / 2f + 15f, 50f);

            unit.aim(following.aimX(), following.aimY());
            unit.controlWeapons(true, following.isShooting);

            unit.plans.clear();
            unit.plans.addFirst(following.buildPlan());
        }
    }
}