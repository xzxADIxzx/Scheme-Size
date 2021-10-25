package mindustry.input;

import arc.func.*;
import arc.math.*;
import arc.struct.*;
import mindustry.world.*;
import mindustry.input.*;
import mindustry.input.Placement.*;
import mindustry.entities.units.*;

public class BuildingTools{
	private InputHandler input;

	public Seq<BuildPlan> plan = new Seq<>();
	public Mode mode = Mode.none;
	public int size = 8;

	public BuildingTools(InputHandler input){
		this.input = input;
	}
	
	public boolean isPlacing(){
		return !plan.isEmpty() && mode != Mode.none && input.isPlacing();
	}

	public void resize(int amount){
		size += amount;
	}

	public void resize(float amount){
		size += amount;
	}

	public void setMode(Mode set){
		mode = mode == set ? Mode.none : set;
	}

	public void fill(int sx, int sy, int ex, int ey, int size){
		if(block() == null) return;

		NormalizeResult result = Placement.normalizeArea(sx, sy, ex, ey, 0, false, size);
		for(int x = result.x; x <= result.x2; x += block().size){
			for(int y = result.y; y <= result.y2; y += block().size){
				BuildPlan build = new BuildPlan(x, y, 0, block(), block().nextConfig());
				plan.add(build);
			}
		}
	}

	public void square(int cx, int cy){
		if(block() == null) return;

		Intc4 line = (x, y, r, n) -> {
			BuildPlan build = new BuildPlan(x, y, r, block(), block().nextConfig());
			plan.add(build);
		};

		for(int x = cx - size; x <= cx + size; x += block().size) line.get(x, cy + size, 0, 0);
		for(int y = cy + size; y >= cy - size; y -= block().size) line.get(cx + size, y, 1, 0);
		for(int x = cx + size; x >= cx - size; x -= block().size) line.get(x, cy - size, 2, 0);
		for(int y = cy - size; y <= cy + size; y += block().size) line.get(cx - size, y, 3, 0);
	}

	public void circle(int cx, int cy){
		if(block() == null) return;

		for (int deg = 0; deg <= 360; deg++) {
			if(deg % 90 == 0) continue;

			int x = cx + Mathf.round(Mathf.cosDeg(deg) * size, block().size);
			int y = cy + Mathf.round(Mathf.sinDeg(deg) * size, block().size);

			BuildPlan build = new BuildPlan(x, y, 0, block(), block().nextConfig());
			plan.add(build);
		}
	}

	private Block block(){
		return input.block;
	}
	
	public enum Mode{
		none,
		fill,
		square,
		circle,
		replace,
		wall,
		edit;
	}
}