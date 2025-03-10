package schema.ui;

import mindustry.graphics.*;
import mindustry.ui.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.Tooltip.*;
import arc.scene.ui.ScrollPane.*;

import static arc.Core.*;
import static schema.Main.*;

/** List of all styles of the mod, the purpose of which is to modernize the look of the game. */
public class Style {

    // region styles

    /** Common button: default, empty and toggle variant. */
    public static ButtonStyle cbd, cbe, cbt;
    /** Image button: default, empty and toggle variant. */
    public static ImageButtonStyle ibd, ibe, ibt;
    /** Text button: default, empty and toggle variant. */
    public static TextButtonStyle tbd, tbe, tbt;
    /** Scroll pane style, simple knob without anything else. */
    public static ScrollPaneStyle scr;
    /** Special style for command fragment. */
    public static ImageButtonStyle ibc;
    /** Label style that uses the outline font. */
    public static LabelStyle outline = Styles.outlineLabel;

    // endregion

    /** Updates the sprites' splits and loads the styles. */
    public static void load() {
        for (var name : new String[] {
            "schema-button",
            "schema-button-over",
            "schema-button-down",
            "schema-button-disabled",
            "schema-scroll-knob",
            "schema-panel",
            "schema-panel-top",
            "schema-panel-bottom",
            "schema-panel-clear" })
            atlas.find(name).splits = name.endsWith("knob") ? new int[] { 0, 0, 24, 16 } : new int[] { 16, 16, 16, 16 };

        log("Loaded 9 sprites for UI");

        ibe = new ImageButtonStyle() {{
            over = drawable("button-over");
            down = drawable("button-down");
            disabled = drawable("button-disabled");
        }};
        ibd = new ImageButtonStyle(ibe) {{ up = drawable("button"); }};
        ibt = new ImageButtonStyle(ibe) {{ checked = drawable("button-over"); }};

        tbe = new TextButtonStyle() {{
            over = drawable("button-over");
            down = drawable("button-down");
            disabled = drawable("button-disabled");

            font = Fonts.def;
        }};
        tbd = new TextButtonStyle(tbe) {{ up = drawable("button"); }};
        tbt = new TextButtonStyle(tbe) {{ checked = drawable("button-over"); }};

        cbe = tbe;
        cbd = tbd;
        cbt = tbt;

        scr = new ScrollPaneStyle() {{ vScrollKnob = drawable("scroll-knob"); }};

        ibc = new ImageButtonStyle(ibt) {{
            imageUpColor = Pal.accentBack;
            imageOverColor = Pal.accent;
            imageDownColor = Pal.accentBack;
            imageCheckedColor = Pal.accent;
        }};

        log("Created 12 styles for UI");

        // these are the colors that are used for disabled and light elements
        Colors.put("disabled", Pal.gray);
        Colors.put("light", Pal.lightishGray);

        // replace the background of tooltips to match the new style
        var background = drawable("panel-clear");
        Tooltips.getInstance().textProvider = cont -> new Tooltip(t -> t.background(background).margin(4f).add(cont).style(outline));
    }
}
