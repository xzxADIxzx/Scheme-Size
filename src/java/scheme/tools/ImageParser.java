package scheme.tools;

import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Pixmaps;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Strings;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.logic.LExecutor;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicDisplay;
import mindustry.world.blocks.logic.LogicBlock.LogicLink;

import static mindustry.Vars.*;

import com.github.bsideup.jabel.Desugar;

public class ImageParser {

    public static final String processorSeparator = "#";
    public static final String flush = "drawflush display1\n";

    // region parse

    /** Reads an image from a file and converts it to schematic. */
    public static Schematic parseSchematic(Fi file, Config cfg) {
        return parseSchematic(file.nameWithoutExtension(), new Pixmap(file), cfg);
    }

    /** Converts a pixmap to schematic with logical processors and displays. */
    public static Schematic parseSchematic(String name, Pixmap image, Config cfg) {
        final int size = cfg.display.size, pixels = cfg.display.displaySize;
        final int width = cfg.columns * size, height = cfg.rows * size;

        image = Pixmaps.scale(image, (float) pixels * cfg.columns / image.width, (float) pixels * cfg.rows / image.height);

        // region creating tiles

        int layer = 0; // the current layer on which the processors are located
        Seq<Point2> available = new Seq<>(); // sequence of all positions available to the processor

        Seq<Stile> tiles = new Seq<>();
        for (int row = 0; row < cfg.rows; row++) {
            for (int column = 0; column < cfg.columns; column++) {

                int x = column * size - cfg.offset(), y = row * size - cfg.offset();
                tiles.add(new Stile(cfg.display, x, y, null, (byte) 0));

                Display block = new Display(image, column * pixels, image.height - row * pixels, pixels);
                for (String code : parseCode(block).split(processorSeparator)) {

                    var pos = next(available, x, y, cfg.processor.range);
                    if (pos == null) refill(available, layer++, width, height);

                    pos = next(available, x, y, cfg.processor.range);
                    if (pos == null) return null; // processor range is too small

                    byte[] compressed = LogicBlock.compress(code, Seq.with(new LogicLink(x - pos.x, y - pos.y, "display1", true)));
                    tiles.add(new Stile(cfg.processor, pos.x, pos.y, compressed, (byte) 0));
                    available.remove(pos); // this position is now filled
                }
            }
        }

        // endregion

        int minx = tiles.min(st -> st.x + st.block.sizeOffset).x;
        int miny = tiles.min(st -> st.y + st.block.sizeOffset).y;

        tiles.each(st -> {
            st.x -= minx;
            st.y -= miny;
        });

        return new Schematic(tiles, StringMap.of("name", name), width + layer * 2, height + layer * 2);
    }

    /** Converts a display into a sequence of instructions for a logical processor. */
    public static String parseCode(Display display) {
        StringBuilder code = new StringBuilder();

        Color last = null;
        int instructions = 0;
        for (Line rect : parseLines(display).sort(rect -> rect.color.abgr())) {
            if (!rect.color.equals(last)) {
                last = rect.color;

                code.append(rect.colorCode());
                instructions++;
            }

            code.append(rect.rectCode());
            instructions++;

            if (instructions + 2 >= LExecutor.maxInstructions) {
                code.append(flush).append(processorSeparator);
                instructions = 0;
                last = null;

                continue;
            }

            if (instructions % LExecutor.maxGraphicsBuffer <= 1) {
                code.append(flush).append(rect.colorCode());
                instructions += 2;
            }
        }

        return code.append(flush).toString();
    }

    /** Converts a display into a sequence of colored lines. */
    public static Seq<Line> parseLines(Display display) {
        var result = new Seq<Line>();

        for (int y = 0; y < display.size; y++) {
            for (int x = 0; x < display.size; x++) {
                int raw = display.get(x, y);

                int length = 1;
                for (int i = x + 1; i < display.size; i++)
                    if (display.get(i, y) == raw) length++;
                    else break;

                result.add(new Line(new Color(raw), x, y, length));
                x += length - 1; // skip same pixels
            }
        }

        return result;
    }

    // endregion
    // region available positions

    private static void refill(Seq<Point2> available, int layer, int width, int height) {
        int amount = 2 * (width + height) + 8 * layer + 4;
        Point2 pos = new Point2(-layer - 1, -layer), dir = new Point2(0, 1);

        for (int i = 0; i < amount; i++) {
            available.add(pos.cpy());
            pos.add(dir);

            if (pos.equals(-layer - 1, height + layer)) dir.set(1, 0);
            if (pos.equals(width + layer, height + layer)) dir.set(0, -1);
            if (pos.equals(width + layer, -layer - 1)) dir.set(-1, 0);
        }
    }

    private static Point2 next(Seq<Point2> available, int x, int y, float range) {
        var inRange = available.select(point -> point.dst(x, y) < range / tilesize);
        return inRange.isEmpty() ? null : inRange.first();
    }

    // endregion

    @Desugar
    public record Display(Pixmap pixmap, int x, int y, int size) {

        public int get(int x, int y) {
            return pixmap.getRaw(this.x + x, this.y - y - 1);
        }
    }

    @Desugar
    public record Line(Color color, int x, int y, int length) { // height is always 1 pixel

        public String colorCode() {
            return Strings.format("draw color @ @ @ @\n", (int) (color.r * 255), (int) (color.g * 255), (int) (color.b * 255), (int) (color.a * 255));
        }

        public String rectCode() {
            return Strings.format("draw rect @ @ @ 1\n", x, y, length);
        }
    }

    public static class Config {

        public LogicBlock processor = (LogicBlock) Blocks.microProcessor;
        public LogicDisplay display = (LogicDisplay) Blocks.logicDisplay;

        public int rows, columns;
        public boolean filter;

        public int offset() {
            return display.sizeOffset;
        }
    }
}
