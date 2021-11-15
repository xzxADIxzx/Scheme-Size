package mindustry.ai.types;

import arc.math.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class MimicAI extends AIController{

    @Override
    public void updateMovement(){
        if(target != null){
            moveTo(target, (target instanceof Sized s ? s.hitSize() / 2f * 1.1f : 0f) + unit.hitSize / 2f + 15f, 50f);
            unit.lookAt(target.prefRotation());
            unit.aim(target.aimX(), target.aimY());
            unit.controlWeapons(true, target.isShooting);

            unit.plans.clear();
            unit.plans.addFirst(following.buildPlan());
        }
    }
}