package mindustry.game;

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
import mindustry.entities.units.*;

import static mindustry.Vars.*;

// Last Update - Sep 11, 2021
public class ModSchematics extends Schematics{

    @Override
    public Schematic create(int x, int y, int x2, int y2){
        NormalizeResult result = Placement.normalizeArea(x, y, x2, y2, 0, false, 511);
        x = result.x;
        y = result.y;
        x2 = result.x2;
        y2 = result.y2;

        int ox = x, oy = y, ox2 = x2, oy2 = y2;

        Seq<Stile> tiles = new Seq<>();

        int minx = x2, miny = y2, maxx = x, maxy = y;
        boolean found = false;
        for(int cx = x; cx <= x2; cx++){
            for(int cy = y; cy <= y2; cy++){
                Building linked = world.build(cx, cy);
                Block realBlock = linked == null ? null : linked instanceof ConstructBuild cons ? cons.current : linked.block;

                if(linked != null && realBlock != null && (realBlock.isVisible() || realBlock instanceof CoreBlock)){
                    int top = realBlock.size/2;
                    int bot = realBlock.size % 2 == 1 ? -realBlock.size/2 : -(realBlock.size - 1)/2;
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
        }else{
            return new Schematic(new Seq<>(), new StringMap(), 1, 1);
        }

        int width = x2 - x + 1, height = y2 - y + 1;
        int offsetX = -x, offsetY = -y;
        IntSet counted = new IntSet();
        for(int cx = ox; cx <= ox2; cx++){
            for(int cy = oy; cy <= oy2; cy++){
                Building tile = world.build(cx, cy);
                Block realBlock = tile == null ? null : tile instanceof ConstructBuild cons ? cons.current : tile.block;

                if(tile != null && !counted.contains(tile.pos()) && realBlock != null
                    && (realBlock.isVisible() || realBlock instanceof CoreBlock)){
                    Object config = tile instanceof ConstructBuild cons ? cons.lastConfig : tile.config();

                    tiles.add(new Stile(realBlock, tile.tileX() + offsetX, tile.tileY() + offsetY, config, (byte)tile.rotation));
                    counted.add(tile.pos());
                }
            }
        }

        return new Schematic(tiles, new StringMap(), width, height);
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
}