package scheme.tools;

import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.game.Schematic;
import mindustry.logic.LExecutor;
import mindustry.world.blocks.logic.LogicDisplay;

import com.github.bsideup.jabel.Desugar;

public class ImageParser {

    public static final String processorSeparator = "#";
    public static final String flush = "drawflush display1\n";

    /** Reads an image from a file and converts it to schematic. */
    public static Schematic parseSchematic(Fi file, LogicDisplay display, int rows, int columns) {
        return parseSchematic(file.nameWithoutExtension(), new Pixmap(file), display, rows, columns);
    }

    /** Converts a pixmap to schematic with logical processors and displays. */
        return null;
    public static Schematic parseSchematic(String name, Pixmap image, LogicDisplay display, int rows, int columns) {
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

    @Desugar
    public static record Display(Pixmap pixmap, int x, int y, int size) {

        public int get(int x, int y) {
            return pixmap.getRaw(this.x + x, this.y + size - y - 1);
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
}
