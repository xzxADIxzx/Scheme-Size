package mindustry.ai.types;

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
            moveTo(following, (following instanceof Sized s ? s.hitSize() / 2f * 1.1f : 0f) + unit.hitSize / 2f + 15f, 50f);
            unit.lookAt(following.prefRotation());
            unit.aim(following.aimX(), following.aimY());
            unit.controlWeapons(true, following.isShooting);

            unit.plans.clear();
            unit.plans.addFirst(following.buildPlan());
        }
    }
}