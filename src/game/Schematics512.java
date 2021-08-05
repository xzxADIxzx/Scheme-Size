package mindustry.game;

import arc.*;
import arc.assets.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.io.Streams.*;
import arc.util.pooling.*;
import arc.util.serialization.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.game.Schematic.*;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.input.Placement.*;
import mindustry.io.*;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.meta.*;

import java.io.*;
import java.util.zip.*;

import static mindustry.Vars.*;

public class Schematics512 extends Schematics{

    public static final int schemeSize = 512;

    private static final int padding = 2;
    private Seq<Schematic> all = new Seq<>();
    private FrameBuffer shadowBuffer;
    private Texture errorTexture;

    public Schematics512(){
        Events.on(ClientLoadEvent.class, event -> {
            errorTexture = new Texture("sprites/error.png");
        });
    }

    @Override
    public void loadSync(){
        // why?
        load();
    }

    @Override
    public void load(){
        all.clear();

        // loadLoadouts();

        for(Fi file : schematicDirectory.list()){
            loadFile(file);
        }

        platform.getWorkshopContent(Schematic.class).each(this::loadFile);

        mods.listFiles("schematics", (mod, file) -> {
            Schematic s = loadFile(file);
            if(s != null){
                s.mod = mod;
            }
        });

        all.sort();

        if(shadowBuffer == null){
            Core.app.post(() -> shadowBuffer = new FrameBuffer(schemeSize + padding + 8, schemeSize + padding + 8));
        }
    }

    private @Nullable Schematic loadFile(Fi file){
        if(!file.extension().equals(schematicExtension)) return null;

        try{
            Schematic s = read(file);
            all.add(s);
            // checkLoadout(s, true);

            if(!s.file.parent().equals(schematicDirectory)){
                s.tags.put("steamid", s.file.parent().name());
            }

            return s;
        }catch(Throwable e){
            Log.err("Failed to read schematic from file '@'", file);
            Log.err(e);
        }
        return null;
    }

    @Override
    public Schematic create(int x, int y, int x2, int y2){
        NormalizeResult result = Placement.normalizeArea(x, y, x2, y2, 0, false, schemeSize);
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
                // idk why... but it won`t compile
                // Block realBlock = linked == null ? null : linked instanceof ConstructBuild cons ? cons.current : linked.block;
                Block realBlock = null;
                if(linked != null){
                    if(linked.getClass() == ConstructBuild.class){
                        realBlock = linked.block;
                        // realBlock = linked.current;
                    }else{
                        realBlock = linked.block;
                    }
                }

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
                // idk why... but it won`t compile
                // Block realBlock = tile == null ? null : tile instanceof ConstructBuild cons ? cons.current : tile.block;
                Block realBlock = null;
                if(tile != null){
                    if (tile.getClass() == ConstructBuild.class){
                        realBlock = tile.block;
                        // realBlock = tile.current;
                    }else{
                        realBlock = tile.block;
                    }
                }

                if(tile != null && !counted.contains(tile.pos()) && realBlock != null
                    && (realBlock.isVisible() || realBlock instanceof CoreBlock)){
                    // idk why... but it won`t compile
                    // Object config = tile instanceof ConstructBuild cons ? cons.lastConfig : tile.config();
                    Object config = null;
                    if (tile.getClass() == ConstructBuild.class){
                        config = tile.config();
                        // config = tile.lastConfig;
                    }else{
                        config = tile.config();
                    }

                    tiles.add(new Stile(realBlock, tile.tileX() + offsetX, tile.tileY() + offsetY, config, (byte)tile.rotation));
                    counted.add(tile.pos());
                }
            }
        }

        return new Schematic(tiles, new StringMap(), width, height);
    }

    @Override
    public Seq<Schematic> all(){
        // need to return this.all
        return all;
    }
}