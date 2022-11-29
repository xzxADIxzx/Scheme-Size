package scheme.moded;

import arc.Events;
import arc.files.Fi;
import arc.func.Func;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.Reflect;
import arc.util.Structs;
import arc.util.io.Reads;
import arc.util.serialization.Base64Coder;
import mindustry.content.Blocks;
import mindustry.ctype.ContentType;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.game.EventType.*;
import mindustry.game.Schematic.Stile;
import mindustry.input.Placement;
import mindustry.input.Placement.NormalizeResult;
import mindustry.io.*;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Prop;
import mindustry.world.blocks.legacy.LegacyBlock;

import static mindustry.Vars.*;

import java.io.*;
import java.util.zip.InflaterInputStream;

/** Last update - Aug 26, 2022 */
public class ModedSchematics extends Schematics {

    /** Too large schematic file extension. */
    public static final String largeSchematicExtension = "mtls";

    /** Copu paste from {@link Schematics}. */
    public static final byte[] header = { 'm', 's', 'c', 'h' };

    /** Current layer to get blocks from. */
    public Layer layer = Layer.building;

    /** Do need to show the dialog. */
    public boolean requiresDialog;

    // region too large schematics fix

    @Override
    public void loadSync() {
        super.loadSync();
        for (Fi file : schematicDirectory.list()) fix(file);

        Events.run(WorldLoadEvent.class, () -> layer = Layer.building);
    }

    private void fix(Fi file) { // dont check size for mtls files
        if (!file.extension().equals(largeSchematicExtension) && !isTooLarge(file)) return;

        try {
            if (file.extension().equals(schematicExtension))
                file = rename(file, file.nameWithoutExtension() + "." + largeSchematicExtension);
            all().add(read(file));
        } catch (Throwable error) {
            Log.err("Failed to read schematic from file '@'", file);
            Log.err(error);
        }
    }

    private static boolean isTooLarge(Fi file) {
        try (DataInputStream stream = new DataInputStream(file.read())) {
            for (byte b : header)
                if (stream.read() != b) return false; // missing header

            stream.skip(1L); // schematic version or idk what is it

            DataInputStream dis = new DataInputStream(new InflaterInputStream(stream));
            return dis.readShort() > 128 || dis.readShort() > 128; // next two shorts is a width and height
        } catch (Throwable ignored) {
            return false;
        }
    }

    private Fi rename(Fi file, String to) {
        requiresDialog = true; // show dialog on startup

        Fi dest = file.parent().child(to);
        file.file().renameTo(dest.file());
        return dest;
    }

    public static Schematic readBase64(String schematic) {
        try {
            return read(new ByteArrayInputStream(Base64Coder.decode(schematic.trim())));
        } catch (IOException error) {
            throw new RuntimeException(error);
        }
    }

    public static Schematic read(Fi file) throws IOException {
        Schematic schematic = read(new DataInputStream(file.read(1024)));
        schematic.file = file;

        if (!schematic.tags.containsKey("name")) schematic.tags.put("name", file.nameWithoutExtension());

        return schematic;
    }

    public static Schematic read(InputStream input) throws IOException {
        input.skip(4L); // header bytes already checked
        int ver = input.read();

        try (DataInputStream stream = new DataInputStream(new InflaterInputStream(input))) {
            short width = stream.readShort(), height = stream.readShort();

            StringMap map = new StringMap();
            int tags = stream.readUnsignedByte();
            for (int i = 0; i < tags; i++)
                map.put(stream.readUTF(), stream.readUTF());

            String[] labels = null;
            try { // try to read the categories, but skip if it fails
                labels = JsonIO.read(String[].class, map.get("labels", "[]"));
            } catch (Exception ignored) {}

            IntMap<Block> blocks = new IntMap<>();
            byte length = stream.readByte();
            for (int i = 0; i < length; i++) {
                String name = stream.readUTF();
                Block block = content.getByName(ContentType.block, SaveFileReader.fallback.get(name, name));
                blocks.put(i, block == null || block instanceof LegacyBlock ? Blocks.air : block);
            }

            int total = stream.readInt();
            Seq<Stile> tiles = new Seq<>(total);
            for (int i = 0; i < total; i++) {
                Block block = blocks.get(stream.readByte());
                int position = stream.readInt();
                Object config = ver == 0 ?
                        Reflect.invoke(Schematics.class, "mapConfig", new Object[] { block, stream.readInt(), position }, Block.class, int.class, int.class) :
                        TypeIO.readObject(Reads.get(stream));
                if (block != Blocks.air)
                    tiles.add(new Stile(block, Point2.x(position), Point2.y(position), config, stream.readByte()));
            }

            Schematic out = new Schematic(tiles, map, width, height);
            if (labels != null) out.labels.addAll(labels);
            return out;
        }
    }

    // endregion
    // region cursed schematics

    public Layer nextLayer() {
        return layer = layer.next();
    }

    public boolean isCursed(Seq<BuildPlan> plans) {
        if (plans.isEmpty()) return false;
        return plans.first().block.isFloor() || plans.first().block instanceof Prop;
    }

    @Override
    public Seq<BuildPlan> toPlans(Schematic schem, int x, int y) {
        int dx = x - schem.width / 2, dy = y - schem.height / 2;
        return schem.tiles.map(t -> new BuildPlan(t.x + dx, t.y + dy, t.rotation, t.block, t.config).original(t.x, t.y, schem.width, schem.height))
                .removeAll(plan -> !plan.block.unlockedNow())
                .sort(Structs.comparingInt(plan -> -plan.block.schematicPriority));
    }

    @Override
    public Schematic create(int x, int y, int x2, int y2) {
        if (layer == Layer.building)
            return super.create(x, y, x2, y2);
        else
            return layer.create(x, y, x2, y2);
    }

    // endregion

    public enum Layer {
        building(null),
        floor(Tile::floor),
        block(tile -> tile.build == null && tile.block() != Blocks.air ? tile.block() : null),
        overlay(tile -> tile.overlay() != Blocks.air ? tile.overlay() : null),
        terrain();

        private final Func<NormalizeResult, Schematic> create;

        private Layer(Func<Tile, Block> provider) {
            this.create = result -> create(result.x, result.y, result.x2, result.y2, provider);
        }

        private Layer() {
            this.create = result -> createTerrain(result.x, result.y, result.x2, result.y2);
        }

        public Layer next() {
            return values()[(new Seq<Layer>(values()).indexOf(this) + 1) % 5];
        }

        public Schematic create(int x, int y, int x2, int y2) {
            NormalizeResult result = Placement.normalizeArea(x, y, x2, y2, 0, false, maxSchematicSize);
            return create.get(result);
        }

        private Schematic create(int x1, int y1, int x2, int y2, Func<Tile, Block> provider) {
            Seq<Stile> tiles = new Seq<>();

            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    Tile tile = world.tile(x, y);
                    if (tile == null) continue;

                    Block block = provider.get(tile);
                    if (block != null) tiles.add(new Stile(block, x - x1, y - y1, null, (byte) 0));
                }
            }

            if (tiles.isEmpty()) return new Schematic(tiles, new StringMap(), 1, 1);

            int minx = tiles.min(st -> st.x).x;
            int miny = tiles.min(st -> st.y).y;

            tiles.each(st -> {
                st.x -= minx;
                st.y -= miny;
            });

            return new Schematic(tiles, new StringMap(), tiles.max(st -> st.x).x + 1, tiles.max(st -> st.y).y + 1);
        }

        private Schematic createTerrain(int x1, int y1, int x2, int y2) {
            x1 = Mathf.clamp(x1, 0, world.width()); y1 = Mathf.clamp(y1, 0, world.height());
            x2 = Mathf.clamp(x2, 0, world.width()); y2 = Mathf.clamp(y2, 0, world.height());

            Seq<Stile> tiles = new Seq<>();
            int width = x2 - x1 + 1, height = y2 - y1 + 1;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Tile tile = world.tile(x + x1, y + y1);
                    if (tile == null) continue;

                    tiles.add(new Stile(tile.floor(), x, y, null, (byte) 0));
                    if (tile.block() != Blocks.air && tile.build == null) tiles.add(new Stile(tile.block(), x, y, null, (byte) 0));
                    if (tile.overlay() != Blocks.air) tiles.add(new Stile(tile.overlay(), x, y, null, (byte) 0));
                }
            }

            return new Schematic(tiles, new StringMap(), width, height);
        }
    }
}
