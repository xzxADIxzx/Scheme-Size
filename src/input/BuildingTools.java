package mindustry.input;

import arc.func.*;
import arc.math.*;
import arc.struct.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.input.*;
import mindustry.input.Placement.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.scheme.*;

import static mindustry.Vars.*;

public class BuildingTools{

	private InputHandler input;
	private Block select;
	private int bsize;

	public Seq<BuildPlan> plan = new Seq<>();
	public Mode mode = Mode.none;
	public int size = 8;

	public BuildingTools(InputHandler input){
		this.input = input;
	}
	
	public boolean isPlacing(){
		return (!plan.isEmpty() || mode == Mode.power) && mode != Mode.none && input.isPlacing();
	}

	public void resize(){
		size = Mathf.clamp(size, 1, 512);
		SchemeSize.hudfrag.resize(size);
	}

	public void resize(int amount){
		size += amount;
		resize();
	}

	public void resize(float amount){
		if(amount == 0) return;
		amount = (size / 16f) * amount;
		size += amount > 0 ? Mathf.clamp(amount, 1, 8) : Mathf.clamp(amount, -8, -1);
		resize();
	}

	public void resize(String amount){
		if(amount.isEmpty()) return;
		size = Integer.valueOf(amount);
		resize();
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

		for(int x = cx - size; x <= cx + size - 1; x += block().size) line.get(x, cy + size, 0, 0);
		for(int y = cy + size; y >= cy - size + 1; y -= block().size) line.get(cx + size, y, 3, 0);
		for(int x = cx + size; x >= cx - size + 1; x -= block().size) line.get(x, cy - size, 2, 0);
		for(int y = cy - size; y <= cy + size - 1; y += block().size) line.get(cx - size, y, 1, 0);
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

	public void replace(int cx, int cy){
		if(block() == null) return;

		Tile tile = world.tiles.get(cx, cy);
		select = tile.block();
		bsize = select.size;

		if(block().size == bsize && block() != select && tile.build != null) replace(tile);
	}

	private void replace(Tile tile){
		if(tile.block() != select) return;

		int bx = (int)tile.build.x / tilesize;
		int by = (int)tile.build.y / tilesize;
			
		if(plan.contains(build -> build.x == bx && build.y == by)) return;
			
		var build = new BuildPlan(bx, by, tile.build.rotation, block(), block().nextConfig());
		plan.add(build);

		for(int x = bx - bsize + 1; x <= bx + bsize - 1; x += bsize) replace(world.tiles.get(x, by + bsize));
		for(int y = by + bsize - 1; y >= by - bsize + 1; y -= bsize) replace(world.tiles.get(bx + bsize, y));
		for(int x = bx + bsize - 1; x >= bx - bsize + 1; x -= bsize) replace(world.tiles.get(x, by - bsize));
		for(int y = by - bsize + 1; y <= by + bsize - 1; y += bsize) replace(world.tiles.get(bx - bsize, y));
	}

	public void power(int cx, int cy){
		if(block() == null) return;

		power(cx, cy, tile -> {
			var build = new BuildPlan(tile.x, tile.y, 0, block());
			plan.add(build);
		});
	}

	private void power(int cx, int cy, Cons<Tile> callback){
		if(block() instanceof PowerNode == false && Blocks.powerNode.unlocked()) input.block = Blocks.powerNode;

		Boolf<Tile> check = (tile) -> {
			if(tile == null) return false;
			if(tile.block() instanceof PowerBlock){
				callback.get(tile);
				return true;
			}
			return false;
		};

		for(int s = size; s <= 128; s++){
			for(int x = cx - s; x <= cx + s - 1; x += 1) if(check.get(world.tiles.get(x, cy + s))) return;
			for(int y = cy + s; y >= cy - s + 1; y -= 1) if(check.get(world.tiles.get(cx + s, y))) return;
			for(int x = cx + s; x >= cx - s + 1; x -= 1) if(check.get(world.tiles.get(x, cy - s))) return;
			for(int y = cy - s; y <= cy + s - 1; y += 1) if(check.get(world.tiles.get(cx - s, y))) return;
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
		power,
		edit;
	}
}