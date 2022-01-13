package mindustry.game;

import arc.func.*;
import arc.util.*;
import arc.struct.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.game.Schematic.*;
import mindustry.input.*;
import mindustry.input.Placement.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.scheme.*;

import static arc.Core.*;
import static mindustry.Vars.*;

// Last Update - Sep 11, 2021
public class ModSchematics extends Schematics{

    public Mode mode = Mode.standard;
    public Interval timer = new Interval();

    public void next(){
        if(!timer.get(4f * Time.toSeconds)) mode = Mode.next(mode);
        ui.showInfoPopup("@mode." + mode.toString(), 4f, 4, 0, 0, 0, 0);
    }

    @Override
    public Schematic create(int x, int y, int x2, int y2){
        NormalizeResult result = Placement.normalizeArea(x, y, x2, y2, 0, false, 511);
        return mode.get(result.x, result.y, result.x2, result.y2);
    }

    public boolean isStandard(){
        return mode == Mode.standard;
    }

    // previews
    public Texture getTexture(Schematic schematic){
        Tmp.m1.set(Draw.proj());
        Tmp.m2.set(Draw.trans());

        Draw.blend();
        Draw.reset();

        FrameBuffer buffer = new FrameBuffer((schematic.width + 2) * 32, (schematic.height + 2) * 32);
        buffer.begin(Color.clear);

        Draw.proj().setOrtho(0, buffer.getHeight(), buffer.getWidth(), -buffer.getHeight());
        Draw.trans().scale(4, 4).translate(12, 12);

        Seq<BuildPlan> requests = schematic.tiles.map(t -> new BuildPlan(t.x, t.y, t.rotation, t.block, t.config));

        requests.each(req -> {
            req.animScale = 1f;
            req.worldContext = false;
            req.block.drawRequestRegion(req, requests);
        });

        requests.each(req -> req.block.drawRequestConfigTop(req, requests));

        Draw.flush();
        Draw.trans().idt();

        buffer.end();

        Draw.proj(Tmp.m1);
        Draw.trans(Tmp.m2);

        return buffer.getTexture();
    }

    @Override
    public Texture getPreview(Schematic schematic){
        if(schematic.width > 42 || schematic.height > 42) return getTexture(schematic);
        else return super.getPreview(schematic);
    }

    public enum Mode {
        standard{
            public Schematic get(int x, int y, int x2, int y2){
                int ox = x, oy = y, ox2 = x2, oy2 = y2;

                Seq<Stile> tiles = new Seq<>();

                int minx = x2, miny = y2, maxx = x, maxy = y;
                boolean found = false;
                for(int cx = x; cx <= x2; cx++){
                    for(int cy = y; cy <= y2; cy++){
                        Building linked = world.build(cx, cy);
                        Block realBlock = linked == null ? null : linked instanceof ConstructBuild cons ? cons.current : linked.block;

                        if(linked != null && realBlock != null && (realBlock.isVisible() || realBlock instanceof CoreBlock)){
                            int top = realBlock.size / 2;
                            int bot = realBlock.size % 2 == 1 ? -realBlock.size / 2 : -(realBlock.size - 1) / 2;
                            minx = Math.min(linked.tileX() + bot, minx);
                            miny = Math.min(linked.tileY() + bot, miny);
                            maxx = Math.max(linked.tileX() + top, maxx);
                            maxy = Math.max(linked.tileY() + top, maxy);
                            found = true;
                        }
                    }
                }

                if(found){
                    x = minx;
                    y = miny;
                    x2 = maxx;
                    y2 = maxy;
                }else return new Schematic(tiles, new StringMap(), 1, 1);

                IntSet counted = new IntSet();
                for(int cx = ox; cx <= ox2; cx++){
                    for(int cy = oy; cy <= oy2; cy++){
                        Building tile = world.build(cx, cy);
                        Block realBlock = tile == null ? null : tile instanceof ConstructBuild cons ? cons.current : tile.block;

                        if(tile != null && !counted.contains(tile.pos()) && realBlock != null && (realBlock.isVisible() || realBlock instanceof CoreBlock)){
                            Object config = tile instanceof ConstructBuild cons ? cons.lastConfig : tile.config();

                            tiles.add(new Stile(realBlock, tile.tileX() + -x, tile.tileY() + -y, config, (byte)tile.rotation));
                            counted.add(tile.pos());
                        }
                    }
                }

                return new Schematic(tiles, new StringMap(), x2 - x + 1, y2 - y + 1);
            }
        },
        floor{
            public Schematic get(int x, int y, int x2, int y2){
                return create(x, y, x2, y2, tile -> tile.floor());
            }
        },
        block{
            public Schematic get(int x, int y, int x2, int y2){
                return create(x, y, x2, y2, tile -> tile.build == null && tile.block() != Blocks.air ? tile.block() : null);
            }
        },
        overlay{
            public Schematic get(int x, int y, int x2, int y2){
                return create(x, y, x2, y2, tile -> tile.overlay() == Blocks.air ? null : tile.overlay());
            }
        };

        public static Mode next(Mode last){
            return values()[(new Seq<Mode>(values()).indexOf(last) + 1) % 4];
        }

        public static Schematic create(int x1, int y1, int x2, int y2, Func<Tile, Block> cons){
            Seq<Stile> tiles = new Seq<>();
    
            for(int x = x1; x < x2; x++){
                for(int y = y1; y < y2; y++){
                    Tile tile = world.tile(x, y);
                    Block block;
    
                    if(tile != null && (block = cons.get(tile)) != null) tiles.add(new Stile(block, x - x1, y - y1, null, (byte)0));
                }
            }

            int minx = tiles.min(st -> st.x).x;
            int miny = tiles.min(st -> st.y).y;

            tiles.each(st -> {
                st.x -= minx;
                st.y -= miny;
            });

            app.post(() -> SchemeSize.input.showSchematicSaveMod());
            return new Schematic(tiles, new StringMap(), tiles.isEmpty() ? 1 : tiles.max(st -> st.x).x, tiles.isEmpty() ? 1 : tiles.max(st -> st.y).y);
        }

        public abstract Schematic get(int x, int y, int x2, int y2);
    }
}