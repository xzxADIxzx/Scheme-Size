package scheme.tools;

import arc.Events;
import arc.func.Cons;
import arc.func.Cons2;
import arc.func.Cons4;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.content.Items;
import mindustry.entities.Units;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.*;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.input.InputHandler;
import mindustry.input.Placement;
import mindustry.input.Placement.NormalizeResult;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.modules.ItemModule;
import scheme.Main;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class BuildingTools {

    public InputHandler input;
    public Seq<Mode> useful;

    public Mode mode = Mode.none;
    public int size = 8;

    public Seq<BuildPlan> plan = new Seq<>();
    public Seq<BuildPlan> removed = new Seq<>();

    public Cons<Building> iterator;
    public Block iterated;
    public int ibsize;

    public BuildingTools() {
        this.input = m_input.asHandler();
        this.useful = Seq.with(Mode.drop, Mode.replace, Mode.remove, Mode.connect);

        Events.on(WorldLoadEvent.class, event -> {
            if (settings.getBool("hardscheme")) state.rules.schematicsAllowed = true;
        });
    }

    public void drop(int x, int y) {
        if (!MessageQueue.drop()) return;

        Tile tile = world.tile(x, y);
        if (tile == null || tile.build == null) return;

        ItemModule items = tile.build.items;
        if (items == null || items.empty()) return;
        else if (items.has(Items.sand)) drop(tile, Items.sand);
        else if (items.has(Items.coal)) drop(tile, Items.coal);
        else drop(tile, items.first());
    }

    private void drop(Tile tile, Item item) {
        Call.requestItem(player, tile.build, item, units.maxAccepted);
        if (player.unit().stack.amount > 0) Call.dropItem(units.maxAccepted);
    }

    public void replace(int x, int y) {
        if (block() == null) return;

        Tile tile = world.tile(x, y);
        if (tile == null || tile.build == null) return;

        iterator = build -> plan(build.tileX(), build.tileY(), build.rotation);
        iterated = tile.block();
        ibsize = iterated.size;

        try { // StackOverflowException was here
            if (block().size == iterated.size && block() != iterated) iterate(tile);
        } catch (Throwable e) { Main.error(e); }
    }

    public void remove(int x, int y) {
        Tile tile = world.tile(x, y);
        if (tile == null || tile.build == null) return;

        iterator = build -> plan(build.tileX(), build.tileY());
        iterated = tile.block();
        ibsize = iterated.size;

        try { iterate(tile); } catch (Throwable e) { Main.error(e); }
    }

    private void iterate(Tile tile){
        if (tile.block() != iterated) return;
        
        int bx = tile.build.tileX(), by = tile.build.tileY();
        if (plan.contains(plan -> plan.x == bx && plan.y == by)) return;
        iterator.get(tile.build);
        
        for (int x = bx - ibsize + 1; x <= bx + ibsize - 1; x += ibsize) iterate(world.tile(x, by + ibsize));
        for (int y = by + ibsize - 1; y >= by - ibsize + 1; y -= ibsize) iterate(world.tile(bx + ibsize, y));
        for (int x = bx + ibsize - 1; x >= bx - ibsize + 1; x -= ibsize) iterate(world.tile(x, by - ibsize));
        for (int y = by - ibsize + 1; y <= by + ibsize - 1; y += ibsize) iterate(world.tile(bx - ibsize, y));
    } // bruhness is everywhere bruhness is everywhere bruhness is everywhere bruhness is everywhere bruhness

    public void connect(int x, int y, Cons2<Integer, Integer> callback) {
        if (block() == null) return;

        Building power = Units.closestBuilding(player.team(), x * tilesize, y * tilesize, 999999f, build -> {
            return build.power != null && ((build.tileX() < x - size || build.tileX() > x + size) || (build.tileY() < y - size || build.tileY() > y + size));
        }); // search for a power build that is not in the zone

        if (power == null) return;
        int px = power.tileX(), py = power.tileY();
        callback.get(x > px ? px - 1 : px + 1, y > py ? py - 1 : py + 1); // magic
    }

    public void fill(int x1, int y1, int x2, int y2, int maxLength) {
        if (block() == null) return;

        NormalizeResult result = Placement.normalizeArea(x1, y1, x2, y2, 0, false, maxLength);
        for (int x = result.x; x <= result.x2; x += block().size)
            for (int y = result.y; y <= result.y2; y += block().size)
                plan(x, y, 0);
    }

    public void square(int x, int y, Cons4<Integer, Integer, Integer, Integer> callback) {
        if (block() == null) return;

        callback.get(x - size, y + size, x + size - 1, y + size);
        callback.get(x + size, y + size, x + size, y - size + 1);
        callback.get(x + size, y - size, x - size + 1, y - size);
        callback.get(x - size, y - size, x - size, y + size - 1);
    }

    public void circle(int x, int y){
        if (block() == null) return;

        ibsize = block().size;
        for (int dx = -size; dx <= size; dx += ibsize)
            for (int dy = -size; dy <= size; dy += ibsize)
                if (Mathf.within(dx, dy, size) && !Mathf.within(dx, dy, size - ibsize)) plan(x + dx, y + dy, 0);
    }

    public void save(int x1, int y1, int x2, int y2, int maxLength) {
        removed.clear();

        NormalizeResult result = Placement.normalizeArea(x1, y1, x2, y2, 0, false, maxLength);
        for (int x = result.x; x <= result.x2; x++)
            for (int y = result.y; y <= result.y2; y++) {
                Building build = world.build(x, y);
                if (build != null) plan(build);
        }
    }

    public boolean isPlacing() {
        return (!plan.isEmpty() || mode == Mode.connect) && mode != Mode.none && (input.isPlacing() || mode == Mode.remove);
    }

    public void resized() {
        size = Mathf.clamp(size, 1, 512);
        hudfrag.size.setText(String.valueOf(size));
    }

    public void resize(int amount) {
        size += amount;
        resized();
    }

    public void resize(float amount) {
        if (amount == 0) return;
        amount = (size / 16f) * amount;
        size += amount > 0 ? Mathf.clamp(amount, 1, 8) : Mathf.clamp(amount, -8, -1);
        resized();
    }

    public void resize(String amount) {
        if (amount.isEmpty()) return;
        size = Strings.parseInt(amount);
        resized();
    }

    public void setMode(Mode set) {
        if (set == Mode.none && useful.contains(mode)) return;
        mode = mode == set ? Mode.none : set;
    }

    private void plan(int x, int y) {
        plan.add(new BuildPlan(x, y));
    }

    private void plan(int x, int y, int rotation) {
        plan.add(new BuildPlan(x, y, rotation, block(), block().nextConfig()));
    }

    private void plan(Building build) {
        removed.add(new BuildPlan(build.tileX(), build.tileY(), build.rotation, build.block, build.config()));
    }

    private Block block() {
        return input.block;
    }

    public enum Mode {
        none, drop, replace, remove, connect, fill, square, circle, pick, edit, brush;
    }
}
