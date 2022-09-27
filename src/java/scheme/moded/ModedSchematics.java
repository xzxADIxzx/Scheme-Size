package scheme.moded;

import arc.files.Fi;
import arc.math.geom.Point2;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.Reflect;
import arc.util.io.Reads;
import mindustry.content.Blocks;
import mindustry.ctype.ContentType;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.game.Schematic.Stile;
import mindustry.io.JsonIO;
import mindustry.io.SaveFileReader;
import mindustry.io.TypeIO;
import mindustry.world.Block;
import mindustry.world.blocks.legacy.LegacyBlock;

import static mindustry.Vars.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

/** Last update - Aug 26, 2022 */
public class ModedSchematics extends Schematics {

    /** Too large schematic file extension. */
    public static final String largeSchematicExtension = "mtls";

    /** Copu paste from {@link Schematics}. */
    public static final byte[] header = { 'm', 's', 'c', 'h' };

    /** Do need to show the dialog. */
    public static boolean requiresDialog;

    @Override
    public void loadSync() {
        super.loadSync();
        for (Fi file : schematicDirectory.list()) fix(file);
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

    private static Fi rename(Fi file, String to) {
        requiresDialog = true; // show dialog on startup

        Fi dest = file.parent().child(to);
        file.file().renameTo(dest.file());
        return dest;
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

/**
    public Mode mode = Mode.standard;
    public Interval timer = new Interval();

    public void next() {
        if (!timer.get(4f * Time.toSeconds)) mode = Mode.next(mode);
        ui.showInfoPopup("@mode." + mode.toString(), 4f, 4, 0, 0, 0, 0);
    }

    @Override
    public Schematic create(int x, int y, int x2, int y2) {
        NormalizeResult result = Placement.normalizeArea(x, y, x2, y2, 0, false, 511);
        return mode.get(result.x, result.y, result.x2, result.y2);
    }

    public boolean isStandard() {
        return mode == Mode.standard;
    }

    public enum Mode {
        standard {
            public Schematic get(int x, int y, int x2, int y2) {
                int ox = x, oy = y, ox2 = x2, oy2 = y2;

                Seq<Stile> tiles = new Seq<>();

                int minx = x2, miny = y2, maxx = x, maxy = y;
                boolean found = false;
                for (int cx = x; cx <= x2; cx++) {
                    for (int cy = y; cy <= y2; cy++) {
                        Building linked = world.build(cx, cy);
                        Block realBlock = linked == null ? null : linked instanceof ConstructBuild cons ? cons.current : linked.block;

                        if (linked != null && realBlock != null && (realBlock.isVisible() || realBlock instanceof CoreBlock)) {
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

                if (found) {
                    x = minx;
                    y = miny;
                    x2 = maxx;
                    y2 = maxy;
                } else
                    return new Schematic(tiles, new StringMap(), 1, 1);

                IntSet counted = new IntSet();
                for (int cx = ox; cx <= ox2; cx++) {
                    for (int cy = oy; cy <= oy2; cy++) {
                        Building tile = world.build(cx, cy);
                        Block realBlock = tile == null ? null : tile instanceof ConstructBuild cons ? cons.current : tile.block;

                        if (tile != null && !counted.contains(tile.pos()) && realBlock != null && (realBlock.isVisible() || realBlock instanceof CoreBlock)) {
                            Object config = tile instanceof ConstructBuild cons ? cons.lastConfig : tile.config();

                            tiles.add(new Stile(realBlock, tile.tileX() + -x, tile.tileY() + -y, config, (byte) tile.rotation));
                            counted.add(tile.pos());
                        }
                    }
                }

                return new Schematic(tiles, new StringMap(), x2 - x + 1, y2 - y + 1);
            }
        },
        floor {
            public Schematic get(int x, int y, int x2, int y2) {
                return create(x, y, x2, y2, tile -> tile.floor());
            }
        },
        block {
            public Schematic get(int x, int y, int x2, int y2) {
                return create(x, y, x2, y2, tile -> tile.build == null && tile.block() != Blocks.air ? tile.block() : null);
            }
        },
        overlay {
            public Schematic get(int x, int y, int x2, int y2) {
                return create(x, y, x2, y2, tile -> tile.overlay() == Blocks.air ? null : tile.overlay());
            }
        };

        public static Mode next(Mode last) {
            return values()[(new Seq<Mode>(values()).indexOf(last) + 1) % 4];
        }

        public static Schematic create(int x1, int y1, int x2, int y2, Func<Tile, Block> cons) {
            Seq<Stile> tiles = new Seq<>();

            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    Tile tile = world.tile(x, y);
                    Block block;

                    if (tile != null && (block = cons.get(tile)) != null) tiles.add(new Stile(block, x - x1, y - y1, null, (byte) 0));
                }
            }

            if (tiles.isEmpty()) return new Schematic(tiles, new StringMap(), 1, 1);

            int minx = tiles.min(st -> st.x).x;
            int miny = tiles.min(st -> st.y).y;

            tiles.each(st -> {
                st.x -= minx;
                st.y -= miny;
            });

            // app.post(() -> m_input.showSchematicSaveMod());
            return new Schematic(tiles, new StringMap(), tiles.max(st -> st.x).x + 1, tiles.max(st -> st.y).y + 1);
        }

        public abstract Schematic get(int x, int y, int x2, int y2);
    }
*/
}
