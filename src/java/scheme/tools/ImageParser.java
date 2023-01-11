package scheme.tools;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.game.Schematic;
import mindustry.logic.LExecutor;

import com.github.bsideup.jabel.Desugar;

public class ImageParser {

    public static final char processorSeparator = '#';

    /** Converts a pixmap into a sequence of instructions for a logical processor. */
    public static String parseCode(Pixmap image) {
        StringBuilder code = new StringBuilder();

        Color last = null;
        int instructions = 0;
        for (Line rect : parseLines(image).sort(rect -> rect.color.abgr())) {
            if (!rect.color.equals(last)) {
                last = rect.color;

                code.append(rect.colorCode());
                instructions++;
            }

            code.append(rect.rectCode());
            instructions++;

            if (instructions + 2 >= LExecutor.maxInstructions) {
                code.append(processorSeparator);
                instructions = 0;
            }

            if (instructions % LExecutor.maxGraphicsBuffer <= 1) {
                code.append("drawflush display1\n");
                instructions++;
            }
        }

        return code.append("drawflush display1").toString();
    }

    /** Converts a pixmap into a sequence of colored lines. */
    public static Seq<Line> parseLines(Pixmap image) {
        var result = new Seq<Line>();

        for (int y = 0; y < image.width; y++) {
            for (int x = 0; x < image.height; x++) {
                int raw = image.getRaw(x, y);

                int length = 1;
                for (int i = x + 1; i < image.width; i++)
                    if (image.getRaw(i, y) == raw) length++;
                    else break;

                result.add(new Line(new Color(raw), x, image.height - y - 1, length));
                x += length - 1; // skip same pixels
            }
        }

        return result;
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
