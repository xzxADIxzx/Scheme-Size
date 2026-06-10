package schema.ui.fragments;

import arc.input.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.world.*;
import schema.*;
import schema.input.*;
import schema.ui.*;

import static mindustry.Vars.*;
import static schema.Main.*;

/// Fragment that displays a search bar.
public class SearchFragment extends Table
{
    /// Search bar.
    private TextField field;
    /// Search results.
    private Table results;

    /// Ranked list of blocks.
    private Seq<Block> ranked = new Seq<>();
    /// Selected ranked block.
    private int selected;

    public SearchFragment() { touchable = Touchable.enabled; }

    /// Builds the fragment and overrides the original.
    public void build(Group parent)
    {
        parent.addChild(this);

        setTransform(true);
        setFillParent(true);
        hideImmediately();

        keyDown(key ->
        {
            if (key == KeyCode.escape || key == KeyCode.back) hide();
            if (key == KeyCode.enter) handle();

            if (key == KeyCode.backspace && Keymask.any()) field.setText("");

            if (Keybind.chat_prev.tap()) selected = Math.max(0, selected - 1);
            if (Keybind.chat_next.tap()) selected = Math.min(5, selected + 1);
        });

        field = field("", Style.tfs, this::handle).growX().height(40f).update(Element::requestKeyboard).visible(() -> visible).get();
        field.setAlignment(Align.center);
        field.setMaxLength(96);
        results = row().table(Style.find("panel-x-shape")).get();
    }

    // region control

    /// Shows the fragment with a simple animation.
    public void show()
    {
        visible = true;

        actions(Actions.originCenter(), Actions.scaleTo(1f, 0f), Actions.scaleTo(1f, 1f, .1f));
    }

    /// Hides the fragment with a simple animation.
    public void hide()
    {
        actions(Actions.originCenter(), Actions.scaleTo(1f, 0f, .1f), Actions.run(this::hideImmediately));
    }

    /// Immediately hides the fragment.
    public void hideImmediately()
    {
        visible = false;
    }

    /// Handles text input.
    private void handle(String value)
    {
        results.margin(4f).clear();
        results.defaults().pad(4f);

        if (value.isBlank())
        {
            results.add("@search.placeholder").style(Style.outline);
            return;
        }

        if(value.matches("[0-9 () \\.\\+\\-\\*\\/]*"))
        {
            var num = evaluate(value, 0);
            results.add(Float.isNaN(num) ? "@search.syntax-flaw" : "[light]= " + Strings.autoFixed(num, 4)).style(Style.outline);
        }
        else
        {
            rank(value);
            selected = 0;

            for (int i = 0; i < Math.min(6, ranked.size); i++)
            {
                int j = i;
                results.image(ranked.get(i).uiIcon       ).update(e -> e.color.a = j == selected ? 1f : .6f).size(32f);
                results.add  (ranked.get(i).localizedName).update(e -> e.color.a = j == selected ? 1f : .6f).style(Style.outline).row();
            }
        }
    }

    /// Handles text input.
    private void handle()
    {
        Time.run(.1f * 60f, () ->
        {
            field.setText("");
            handle("");
        });
        hide();

        if (field.getText().isBlank()) return;
        if (field.getText().matches("[0-9 () \\.\\+\\-\\*\\/]*"))
        {
            Tools.copy(Strings.autoFixed(evaluate(field.getText(), 0), 4));
        }
        else insys.block = ranked.get(selected);
    }

    // endregion
    // region display

    @Override
    public void invalidate()
    {
        super.invalidate();
        if (results != null) translation.set(0f, -results.getPrefHeight() / 2f);
    }

    // endregion
    // region engine

    int i;

    /// Expression evaluation level.
    private interface Level
    {
        float evaluate(String expression);
    }
    /// Expression evaluation levels.
    private final Level[] levels =
    {
        expression -> evaluate(expression.replaceAll("\\s", "") + " ", (i = 0) + 1),
        expression ->
        {
            var value = evaluate(expression, 2);

            while (i <= expression.length())
            {
                switch (expression.charAt(i++))
                {
                    case '+': value += evaluate(expression, 2); break;
                    case '-': value -= evaluate(expression, 2); break;
                    default:
                        i--; // failed to process
                        return value;
                }
            }
            return value;
        },
        expression ->
        {
            var value = evaluate(expression, 3);

            while (i <= expression.length())
            {
                switch (expression.charAt(i++))
                {
                    case '*': value *= evaluate(expression, 3); break;
                    case '/': value /= evaluate(expression, 3); break;
                    default:
                        i--; // failed to process
                        return value;
                }
            }
            return value;
        },
        expression ->
        {
            var c = expression.charAt(i);
            if (c == '+' || c == '-' || c == '.' || Character.isDigit(c))
            {
                int s = i;
                while ((c = expression.charAt(++i)) == '.' || Character.isDigit(c)) ;

                return Strings.parseFloat(expression.substring(s, i), Float.NaN);
            }
            if (c == '(')
            {
                int s = i + 1,
                    n = 1;
                while (n > 0) switch (c = expression.charAt(++i))
                {
                    case '(': n++; break;
                    case ')': n--; break;
                    case ' ': return Float.NaN;
                }
                i = s;
                var ret = evaluate(expression, 1);
                i++;
                return ret;
            }
            return Float.NaN;
        },
    };

    /// Matrix required by the Levenshtein distance algorithm.
    private static final byte[][] m = new byte[128][128];

    /// Returns the case-independent biased Levenshtein distance with position-independent bias and minimal allocations.
    public static float distance(String inA, String inB)
    {
        if (inA == null || inA.isBlank() || inA.length() >= 127 ||
            inB == null || inB.isBlank() || inB.length() >= 127) return Float.POSITIVE_INFINITY;

        var a = inA.toLowerCase(java.util.Locale.ROOT);
        var b = inB.toLowerCase(java.util.Locale.ROOT);

        var lnA = a.length();
        var lnB = b.length();

        for (byte x = 0; x <= lnA; x++)
        for (byte y = 0; y <= lnB; y++)
        {
            if (x == 0) m[x][y] = y;
            else
            if (y == 0) m[x][y] = x;
            else
                m[x][y] = (byte)Math.min
                (
                    Math.min
                    (
                        m[x - 1][y] + 1,
                        m[x][y - 1] + 1
                    ),
                    m[x - 1][y - 1] + (a.charAt(x - 1) == b.charAt(y - 1) ? 0 : 1)
                );
        }

        for (int i = 0; i <= lnA - lnB; i++)
        {
            if ((i == 0 || a.charAt(i - 1) == ' ') && a.regionMatches(i, b, 0, lnB)) return m[lnA][lnB] / 4f;
        }
        for (int i = 0; i <= lnB - lnA; i++)
        {
            if ((i == 0 || b.charAt(i - 1) == ' ') && b.regionMatches(i, a, 0, lnA)) return m[lnA][lnB] / 4f;
        }
        return m[lnA][lnB] / (a.contains(b) || b.contains(a) ? 2f : 1f) * (lnA < 5 ? 1.2f : 1f) * (lnB < 5 ? 1.2f : 1f);
    }

    /// Parses the expression and then evaluates it.
    public float evaluate(String expression, int level) { return levels[level].evaluate(expression); }

    /// Selects unlocked blocks and then ranks them.
    public Seq<Block> rank(String name) { return ranked.selectFrom(content.blocks(), polyblock::unlocked).sort(b -> distance(b.localizedName, name)); }

    // endregion
}
