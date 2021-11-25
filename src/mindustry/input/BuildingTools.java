package mindustry.input;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.ui.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.power.PowerNode.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.production.Pump.*;
import mindustry.world.blocks.production.Drill.*;
import mindustry.world.blocks.production.Separator.*;
import mindustry.world.consumers.*;
import mindustry.input.Placement.*;
import mindustry.entities.units.*;
import mindustry.scheme.*;

import static mindustry.Vars.*;

public class BuildingTools{

	private InputHandler input;
	private Block select;
	private int bsize;

	public ProductionSeq product = new ProductionSeq();
	public Seq<Building> checked = new Seq<>();

	public Seq<BuildPlan> removed = new Seq<>();
	public Seq<BuildPlan> plan = new Seq<>();
	public Mode mode = Mode.none;
	public int size = 8;

	public Seq<BuildPlan> node = new Seq<>();

	public BuildingTools(InputHandler input){
		this.input = input;
		product.clear();

		Events.on(ConfigEvent.class, event -> {
			if(player.unit().plans.isEmpty()) node.clear();
			if(node.isEmpty()) return;

			PowerNodeBuild build = event.tile instanceof PowerNodeBuild pnb ? pnb : null;
			if(build == null) return;
			
			BuildPlan plan = node.find(bp -> bp.x == build.tileX() && bp.y == build.tileY());
			if(plan == null) return;

			Seq<Point2> config = new Seq<Point2>((Point2[])plan.config);
			new Seq<Point2>(build.config()).each(point -> {
				if(config.contains(point)) return;

				Tile tile = world.tile(build.tileX() + point.x, build.tileY() + point.y);
				build.onConfigureTileTapped(tile.build);
			});
		});
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

		Tile tile = world.tile(cx, cy);
		if(tile == null) return;

		select = tile.block();
		bsize = select.size;

		if(block().size == bsize && block() != select && tile.build != null) replace(tile);
	}

	private void replace(Tile tile){
		if(tile.block() != select) return;

		int bx = (int)tile.build.x / tilesize;
		int by = (int)tile.build.y / tilesize;
			
		if(plan.contains(build -> build.x == bx && build.y == by)) return;
			
		BuildPlan build = new BuildPlan(bx, by, tile.build.rotation, block(), block().nextConfig());
		plan.add(build);

		for(int x = bx - bsize + 1; x <= bx + bsize - 1; x += bsize) replace(world.tile(x, by + bsize));
		for(int y = by + bsize - 1; y >= by - bsize + 1; y -= bsize) replace(world.tile(bx + bsize, y));
		for(int x = bx + bsize - 1; x >= bx - bsize + 1; x -= bsize) replace(world.tile(x, by - bsize));
		for(int y = by - bsize + 1; y <= by + bsize - 1; y += bsize) replace(world.tile(bx - bsize, y));
	}

	public void power(int cx, int cy, Cons2<Intp, Intp> callback){
		if(block() == null) return;

		Boolf<Tile> check = (tile) -> {
			if(tile == null) return false;
			if(tile.block() instanceof PowerBlock && tile.build.team == player.team()){
				int bx = tile.x;
				int by = tile.y;
				callback.get(() -> cx > bx ? bx - 1 : bx + 1, () -> cy > by ? by - 1 : by + 1);
				return true;
			}
			return false;
		};

		for(int s = size; s <= 256; s++){
			for(int x = cx - s; x <= cx + s - 1; x += 1) if(check.get(world.tile(x, cy + s))) return;
			for(int y = cy + s; y >= cy - s + 1; y -= 1) if(check.get(world.tile(cx + s, y))) return;
			for(int x = cx + s; x >= cx - s + 1; x -= 1) if(check.get(world.tile(x, cy - s))) return;
			for(int y = cy - s; y <= cy + s - 1; y += 1) if(check.get(world.tile(cx - s, y))) return;
		}
	}

	public void calc(int sx, int sy, int ex, int ey){
		product.clear();
		checked.clear();

		for(int x = sx; x <= ex; x++){
			for(int y = sy; y <= ey; y++){
				Tile tile = world.tile(x, y);
				if(tile.build == null) continue;

				if(checked.contains(tile.build)) continue;
				checked.add(tile.build);

				if(tile.block() instanceof Drill block) product.add((DrillBuild)tile.build, block);
				if(tile.block() instanceof Pump block) product.add((PumpBuild)tile.build, block);
				if(tile.block() instanceof Separator block) product.add((SeparatorBuild)tile.build, block);

				calc(tile.block());
			}
		}

		product.show();
	}

	public void calc(Block block){
		block.consumes.each(cons -> {
			if(cons instanceof ConsumeItems c) product.add(c, block);
			if(cons instanceof ConsumeLiquid c) product.add(c);;
			if(cons instanceof ConsumePower c) product.add(c);
		});

		if(block instanceof GenericCrafter g) product.add(g);
		if(block instanceof SolidPump g) product.add(g);
		if(block instanceof PowerGenerator g) product.add(g);
	}

	public void save(Seq<BuildPlan> requests){
		requests.select(bp -> bp.block instanceof PowerNode).each(bp -> node.add(bp.copy()));
	}

	public void save(int sx, int sy, int ex, int ey, int size){
		removed.clear();

		NormalizeResult result = Placement.normalizeArea(sx, sy, ex, ey, 0, false, size);
		for(int x = result.x; x <= result.x2; x++){
			for(int y = result.y; y <= result.y2; y++){
				Building tile = world.build(x, y);
                if(tile == null) continue;

				BuildPlan build = new BuildPlan(tile.tileX(), tile.tileY(), tile.rotation, tile.block, tile.config());
				removed.add(build);
			}
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
		edit,
		calc;
	}

	public class ProductionSeq{
		public float[] items;
		public float[] liquids;
		public float power;

		public void add(ConsumeItems cons, Block block){
			float time = block instanceof GenericCrafter g ? g.craftTime :
						 block instanceof ImpactReactor i ? i.itemDuration :
						 block instanceof NuclearReactor r ? r.itemDuration : 60f;
			for(ItemStack stack : cons.items) items[stack.item.id] -= stack.amount / time * 60f;
		}

		public void add(ConsumeLiquid cons){
			liquids[cons.liquid.id] -= cons.amount * 60f;
		}

		public void add(ConsumePower cons){
			power -= cons.usage * 60f;
		}

		public void add(GenericCrafter gen){
			if(gen.outputsItems()) for(ItemStack stack : gen.outputItems) items[stack.item.id] += stack.amount / gen.craftTime * 60f;
			if(gen.outputsLiquid) liquids[gen.outputLiquid.liquid.id] += gen.outputLiquid.amount * 60f / (gen instanceof LiquidConverter ? 1 : gen.craftTime);
		}

		public void add(SolidPump pump){
			liquids[pump.result.id] += pump.pumpAmount * 60f;
		}

		public void add(PowerGenerator gen){
			power += gen.powerProduction * 60f;
		}

		public void add(DrillBuild build, Drill block){
			items[build.dominantItem.id] += 60f / (block.drillTime + block.hardnessDrillMultiplier * build.dominantItem.hardness) * 
											build.dominantItems * build.efficiency() * build.warmup * 
											(build.cons.optionalValid() ? block.liquidBoostIntensity : 1f);
			// oh no ,_,
		}

		public void add(PumpBuild build, Pump block){
			if(build.liquidDrop == null) return;
			liquids[build.liquidDrop.id] += build.amount * block.pumpAmount * 60f;
		}

		public void add(SeparatorBuild build, Separator block){
			for(ItemStack stack : block.results) items[stack.item.id] += stack.amount / block.craftTime * 60f;
		}

		public void clear(){
			items = new float[content.items().size];
			liquids = new float[content.liquids().size];
			power = 0f;
		}

		public void show(){
			String output = forType(new String(), content.items(), items) + "\n";
			output = forType(output, content.liquids(), liquids) + "\n";
			output += (power >= 0 ? "[accent]" : "[red]") + " " + (power >= 0 ? "+" : "") + power;

			ui.showInfoToast(output, 8);
		}

		private String forType(String input, Seq<? extends UnlockableContent> content, float[] amount){
			int id = 0;
			for(UnlockableContent item : content){
				if(amount[item.id] == 0) continue;
				input += Fonts.getUnicodeStr(item.name) + Mathf.round(amount[item.id], .01f) + "[gray]" + Core.bundle.get("unit.persecond") + "[] ";

				if(id % 4 == 3) input += "\n";
				id++;
			}
			return input;
		}
	}
}