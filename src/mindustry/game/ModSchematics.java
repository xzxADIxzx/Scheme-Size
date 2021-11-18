package mindustry.game;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.game.Schematic.*;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.input.Placement.*;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.world.blocks.storage.*;

import static arc.Core.*;
import static mindustry.Vars.*;

// Last Update - Sep 11, 2021
public class ModSchematics extends Schematics{

    // private static final Schematic tmpSchem = new Schematic(new Seq<>(), new StringMap(), 0, 0);
    // private static final Schematic tmpSchem2 = new Schematic(new Seq<>(), new StringMap(), 0, 0);

    // private static final byte[] header = {'m', 's', 'c', 'h'};
    // private static final byte version = 1;

    private static final int padding = 2;
    private static final int maxPreviewsMobile = 32;
    private static final int resolution = 32;

    // private OptimizedByteArrayOutputStream out = new OptimizedByteArrayOutputStream(1024);
    private Seq<Schematic> all = new Seq<>();
    private OrderedMap<Schematic, FrameBuffer> previews = new OrderedMap<>();
    private ObjectSet<Schematic> errored = new ObjectSet<>();
    private ObjectMap<CoreBlock, Seq<Schematic>> loadouts = new ObjectMap<>();
    private FrameBuffer shadowBuffer;
    private Texture errorTexture;
    private long lastClearTime;

    public ModSchematics(){
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
            // 512 because it's safer
            Core.app.post(() -> shadowBuffer = new FrameBuffer(512 + padding + 8, 512 + padding + 8));
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
        NormalizeResult result = Placement.normalizeArea(x, y, x2, y2, 0, false, settings.getInt("copysize") - 1);
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

    @Override
    public void add(Schematic schematic){
        all.add(schematic);
        try{
            Fi file = schematicDirectory.child(Time.millis() + "." + schematicExtension);
            write(schematic, file);
            schematic.file = file;
        }catch(Exception e){
            ui.showException(e);
            Log.err(e);
        }

        // checkLoadout(schematic, true);
        all.sort();
    }

    @Override
    public void remove(Schematic schematic){
        all.remove(schematic);
        loadouts.each((block, seq) -> seq.remove(schematic));
        if(schematic.file != null){
            schematic.file.delete();
        }

        if(previews.containsKey(schematic)){
            previews.get(schematic).dispose();
            previews.remove(schematic);
        }
        all.sort();
    }

    @Override
    public void overwrite(Schematic target, Schematic newSchematic){
        if(previews.containsKey(target)){
            previews.get(target).dispose();
            previews.remove(target);
        }

        target.tiles.clear();
        target.tiles.addAll(newSchematic.tiles);
        target.width = newSchematic.width;
        target.height = newSchematic.height;
        newSchematic.labels = target.labels;
        newSchematic.tags.putAll(target.tags);
        newSchematic.file = target.file;

        loadouts.each((block, list) -> list.remove(target));
        // checkLoadout(target, true);

        try{
            write(newSchematic, target.file);
        }catch(Exception e){
            Log.err("Failed to overwrite schematic '@' (@)", newSchematic.name(), target.file);
            Log.err(e);
            ui.showException(e);
        }
    }

    @Override
    public Seq<Schematic> all(){
        // need to return this.all
        return all;
    }

    @Override
    public Texture getPreview(Schematic schematic){
        if(errored.contains(schematic)) return errorTexture;

        try{
            return getBuffer(schematic).getTexture();
        }catch(Throwable t){
            Log.err("Failed to get preview for schematic '@' (@)", schematic.name(), schematic.file);
            Log.err(t);
            errored.add(schematic);
            return errorTexture;
        }
    }

    @Override
    public FrameBuffer getBuffer(Schematic schematic){
        if(mobile && Time.timeSinceMillis(lastClearTime) > 1000 * 2 && previews.size > maxPreviewsMobile){
            Seq<Schematic> keys = previews.orderedKeys().copy();
            for(int i = 0; i < previews.size - maxPreviewsMobile; i++){
                previews.get(keys.get(i)).dispose();
                previews.remove(keys.get(i));
            }
            lastClearTime = Time.millis();
        }

        if(!previews.containsKey(schematic)){
            Draw.blend();
            Draw.reset();
            Tmp.m1.set(Draw.proj());
            Tmp.m2.set(Draw.trans());
            FrameBuffer buffer = new FrameBuffer((schematic.width + padding) * resolution, (schematic.height + padding) * resolution);

            shadowBuffer.begin(Color.clear);

            Draw.trans().idt();
            Draw.proj().setOrtho(0, 0, shadowBuffer.getWidth(), shadowBuffer.getHeight());

            Draw.color();
            schematic.tiles.each(t -> {
                int size = t.block.size;
                int offsetx = -(size - 1) / 2;
                int offsety = -(size - 1) / 2;
                for(int dx = 0; dx < size; dx++){
                    for(int dy = 0; dy < size; dy++){
                        int wx = t.x + dx + offsetx;
                        int wy = t.y + dy + offsety;
                        Fill.square(padding/2f + wx + 0.5f, padding/2f + wy + 0.5f, 0.5f);
                    }
                }
            });

            shadowBuffer.end();

            buffer.begin(Color.clear);

            Draw.proj().setOrtho(0, buffer.getHeight(), buffer.getWidth(), -buffer.getHeight());

            Tmp.tr1.set(shadowBuffer.getTexture(), 0, 0, schematic.width + padding, schematic.height + padding);
            Draw.color(0f, 0f, 0f, 1f);
            Draw.rect(Tmp.tr1, buffer.getWidth()/2f, buffer.getHeight()/2f, buffer.getWidth(), -buffer.getHeight());
            Draw.color();

            Seq<BuildPlan> requests = schematic.tiles.map(t -> new BuildPlan(t.x, t.y, t.rotation, t.block, t.config));

            Draw.flush();
            Draw.trans().scale(resolution / tilesize, resolution / tilesize).translate(tilesize*1.5f, tilesize*1.5f);

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

            previews.put(schematic, buffer);
        }

        return previews.get(schematic);
    }
}