package schema.ui;

import mindustry.graphics.*;
import mindustry.ui.*;
import arc.graphics.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.ScrollPane.*;

import static arc.Core.*;
import static schema.Main.*;

/** List of all styles of the mod, the purpose of which is to modernize the look of the game. */
public class Style {

    // region styles

    /** Common button, both default, empty and toggle variant. */
    public static ButtonStyle cbd, cbe, cbt;
    /** Image button, both default, empty and toggle variant. */
    public static ImageButtonStyle ibd, ibe, ibt;
    /** Text button, both default, empty and toggle variant. */
    public static TextButtonStyle tbd, tbe, tbt;
    /** Scroll pane style, simple knob without anything else. */
    public static ScrollPaneStyle scr;

    // endregion

    /** Updates the sprites' splits and loads the styles. */
    public static void load() {
        for (var sprite : new String[] {
            "schema-button",
            "schema-button-over",
            "schema-button-down",
            "schema-button-disabled",
            "schema-panel-bottom" })
            atlas.find(sprite).splits = new int[] { 16, 16, 16, 16 };

        atlas.find("schema-scroll-knob").splits = new int[] { 0, 0, 24, 16 };

        log("Loaded 5 sprites for UI");

        ibe = new ImageButtonStyle() {{
            over = atlas.drawable("schema-button-over");
            down = atlas.drawable("schema-button-down");
            disabled = atlas.drawable("schema-button-disabled");
        }};
        ibd = new ImageButtonStyle(ibe) {{ up = atlas.drawable("schema-button"); }};
        ibt = new ImageButtonStyle(ibe) {{ checked = atlas.drawable("schema-button-over"); }};

        tbe = new TextButtonStyle() {{
            over = atlas.drawable("schema-button-over");
            down = atlas.drawable("schema-button-down");
            disabled = atlas.drawable("schema-button-disabled");

            font = Fonts.def;
        }};
        tbd = new TextButtonStyle(tbe) {{ up = atlas.drawable("schema-button"); }};
        tbt = new TextButtonStyle(tbe) {{ checked = atlas.drawable("schema-button-over"); }};

        cbe = tbe;
        cbd = tbd;
        cbt = tbt;

        scr = new ScrollPaneStyle() {{ vScrollKnob = atlas.drawable("schema-scroll-knob"); }};

        log("Created 10 styles for UI");

        // this is the color that is used for disabled elements
        Colors.put("disabled", Pal.gray);
    }
}
