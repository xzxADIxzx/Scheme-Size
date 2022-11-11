package scheme.moded;

import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.Font.FontData;
import arc.graphics.g2d.Font.Glyph;
import arc.struct.Seq;
import arc.util.Reflect;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;

import static arc.Core.*;

/** Last update - Feb 28, 2022 */
public class ModedGlyphLayout extends GlyphLayout {

    public final Seq<Color> colorStack = Reflect.get(GlyphLayout.class, this, "colorStack");

    public static void load() {
        Pools.set(GlyphLayout.class, new Pool<GlyphLayout>(4, 5000) {
            @Override
            protected GlyphLayout newObject() {
                return new ModedGlyphLayout();
            }
        });
    }

    @Override
    public void setText(Font font, CharSequence str, int start, int end, Color color, float targetWidth, int halign, boolean wrap, String truncate) {
        FontData fontData = font.getData();
        if (fontData.markupEnabled || input.keyDown(ModedBinding.alternative)) {
            super.setText(font, str, start, end, color, targetWidth, halign, wrap, truncate);
            return; // use default renderer if alt key is pressed or markup is enabled
        }

        if (truncate != null)
            wrap = true; // causes truncate code to run, doesn't actually cause wrapping
        else if (targetWidth <= fontData.spaceXadvance * 3)
            wrap = false; // avoid one line per character, which is very inefficient

        Pool<GlyphRun> glyphRunPool = Pools.get(GlyphRun.class, GlyphRun::new);
        glyphRunPool.freeAll(runs);
        runs.clear();

        float x = 0, y = 0, width = 0;
        int lines = 0, blankLines = 0;
        Glyph lastGlyph = null;

        Color nextColor = color;
        colorStack.add(color);
        Pool<Color> colorPool = Pools.get(Color.class, Color::new);

        int runStart = start;
        int skip = 0;

        outer:
        while (true) {
            // each run is delimited by newline or left square bracket.
            int runEnd = -1;

            if (start == end) {
                if (runStart == end) break; // end of string with no run to process, we're done
                runEnd = end; // end of string, process last run
            } else switch (str.charAt(start++)) {
                case '\n': // end of line
                    runEnd = start - 1;
                    break;
                case '[': // possible color tag
                    int length = Reflect.invoke(GlyphLayout.class, this, "parseColorMarkup", new Object[] {
                            str, start, end, colorPool
                    }, CharSequence.class, int.class, int.class, Pool.class);

                    if (length >= 0) {
                        runEnd = start - 1;

                        start--;
                        skip = length + 2;

                        nextColor = colorStack.peek();
                    } else if (length == -2) {
                        start++; // skip first of "[[" escape sequence
                        continue outer;
                    }

                    break;
            }

            if (runEnd != -1) {
                runEnded:
                if (runEnd != runStart) { // eg, when a color tag is at text start or a line is "\n"
                    GlyphRun run = glyphRunPool.obtain(); // store the run that has ended
                    run.color.set(color);
                    fontData.getGlyphs(run, str, runStart, runEnd, lastGlyph);

                    if (run.glyphs.size == 0) {
                        glyphRunPool.free(run);
                        break runEnded;
                    }

                    if (lastGlyph != null) { // move back the width of the last glyph from the previous run
                        x -= lastGlyph.fixedWidth ? lastGlyph.xadvance * fontData.scaleX
                        : (lastGlyph.width + lastGlyph.xoffset) * fontData.scaleX - fontData.padRight;
                    }

                    lastGlyph = run.glyphs.peek();
                    run.x = x;
                    run.y = y;

                    if (runEnd == end) Reflect.invoke(GlyphLayout.class, this, "adjustLastGlyph", new Object[] {
                            fontData, run
                    }, FontData.class, GlyphRun.class);
                    runs.add(run);

                    float[] xAdvances = run.xAdvances.items;
                    int n = run.xAdvances.size;
                    if (!wrap) { // no wrap or truncate
                        float runWidth = 0;
                        for (int i = 0; i < n; i++)
                            runWidth += xAdvances[i];

                        x += runWidth;
                        run.width = runWidth;
                        break runEnded;
                    }

                    // wrap or truncate
                    x += xAdvances[0];
                    run.width = xAdvances[0];
                    if (n < 1) break runEnded;
                    x += xAdvances[1];
                    run.width += xAdvances[1];
                    for (int i = 2; i < n; i++) {
                        Glyph glyph = run.glyphs.get(i - 1);
                        float glyphWidth = (glyph.width + glyph.xoffset) * fontData.scaleX - fontData.padRight;
                        if (x + glyphWidth <= targetWidth) { // glyph fits
                            x += xAdvances[i];
                            run.width += xAdvances[i];
                            continue;
                        }

                        if (truncate != null) {
                            Reflect.invoke(GlyphLayout.class, this, "truncate", new Object[] {
                                    fontData, run, targetWidth, truncate, i, glyphRunPool
                            }, FontData.class, GlyphRun.class, float.class, String.class, int.class, Pool.class);
                            x = run.x + run.width;
                            break outer;
                        }

                        int wrapIndex = fontData.getWrapIndex(run.glyphs, i);
                        if ((run.x == 0 && wrapIndex == 0) // require at least one glyph per line
                                || wrapIndex >= run.glyphs.size) { // wrap at least the glyph that didn't fit
                            wrapIndex = i - 1;
                        }

                        GlyphRun next;
                        if (wrapIndex == 0) { // move entire run to next line
                            next = run;
                            run.width = 0;

                            // remove leading whitespace
                            for (int glyphCount = run.glyphs.size; wrapIndex < glyphCount; wrapIndex++)
                                if (!fontData.isWhitespace((char) run.glyphs.get(wrapIndex).id)) break;
                            if (wrapIndex > 0) {
                                run.glyphs.removeRange(0, wrapIndex - 1);
                                run.xAdvances.removeRange(1, wrapIndex);
                            }
                            run.xAdvances.set(0, -run.glyphs.first().xoffset * fontData.scaleX - fontData.padLeft);

                            if (runs.size > 1) { // previous run is now at the end of a line
                                // remove trailing whitespace and adjust last glyph.=
                                GlyphRun previous = runs.get(runs.size - 2);
                                int lastIndex = previous.glyphs.size - 1;
                                for (; lastIndex > 0; lastIndex--) {
                                    Glyph g = previous.glyphs.get(lastIndex);
                                    if (!fontData.isWhitespace((char) g.id)) break;
                                    previous.width -= previous.xAdvances.get(lastIndex + 1);
                                }
                                previous.glyphs.truncate(lastIndex + 1);
                                previous.xAdvances.truncate(lastIndex + 2);
                                Reflect.invoke(GlyphLayout.class, this, "adjustLastGlyph", new Object[] {
                                        fontData, previous
                                }, FontData.class, GlyphRun.class);
                                width = Math.max(width, previous.x + previous.width);
                            }
                        } else {
                            next = Reflect.invoke(GlyphLayout.class, this, "wrap", new Object[] {
                                    fontData, run, glyphRunPool, wrapIndex, i
                            }, FontData.class, GlyphRun.class, Pool.class, int.class, int.class);
                            width = Math.max(width, run.x + run.width);
                            if (next == null) { // all wrapped glyphs were whitespace
                                x = 0;
                                y += fontData.down;
                                lines++;
                                lastGlyph = null;
                                break;
                            }
                            runs.add(next);
                        }

                        // start the loop over with the new run on the next line
                        n = next.xAdvances.size;
                        xAdvances = next.xAdvances.items;
                        x = xAdvances[0];
                        if (n > 1) x += xAdvances[1];
                        next.width += x;
                        y += fontData.down;
                        lines++;
                        next.x = 0;
                        next.y = y;
                        i = 1;
                        run = next;
                        lastGlyph = null;
                    }
                }

                runStart = start;
                color = nextColor;
            }

            if (skip > 0) {
                start += skip;
                skip = 0;
            }
        }

        width = Math.max(width, x);

        for (int i = 1, n = colorStack.size; i < n; i++)
            colorPool.free(colorStack.get(i));
        colorStack.clear();

        this.width = width;
        this.height = fontData.capHeight - lines * fontData.down - blankLines * fontData.down * fontData.blankLineScale;
    }
}
