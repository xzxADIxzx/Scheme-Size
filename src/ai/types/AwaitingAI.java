package mindustry.ai.types;

import mindustry.entities.units.*;
import mindustry.gen.*;

import static mindustry.Vars.*;

public class AwaitingAI extends AIController{

    @Override
    public void updateMovement(){
        circle(target, 100);
        // for (int deg = 0; deg <= 360; deg++) {
		// 	int x = cx + Mathf.round(Mathf.cosDeg(deg) * size, block().size);
		// 	int y = cy + Mathf.round(Mathf.sinDeg(deg) * size, block().size);

		// 	BuildPlan build = new BuildPlan(x, y, 0, block(), block().nextConfig());
		// 	plan.add(build);
		// }
    }
}