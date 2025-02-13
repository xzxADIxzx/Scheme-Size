package schema.ui;

import arc.scene.ui.Button.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.TextButton.*;

import static arc.Core.*;

/** List of all styles of the mod, the purpose of which is to modernize the look of the game. */
public class Style {

    // region styles

    /** Common button, both default, empty and toggle variant. */
    public static ButtonStyle cbd, cbe, cbt;
    /** Image button, both default, empty and toggle variant. */
    public static ImageButtonStyle ibd, ibe, ibt;
    /** Text button, both default, empty and toggle variant. */
    public static TextButtonStyle tbd, tbe, tbt;

    // endregion

    /** Updates the sprites' splits and loads the styles. */
    public static void load() {
        for (var sprite : new String[] {
            "schema-button",
            "schema-button-over",
            "schema-button-down",
            "schema-button-disabled" })
            atlas.find(sprite).splits = new int[] { 16, 16, 16, 16 };

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
        }};
        tbd = new TextButtonStyle(tbe) {{ up = atlas.drawable("schema-button"); }};
        tbt = new TextButtonStyle(tbe) {{ checked = atlas.drawable("schema-button-over"); }};

        cbe = tbe;
        cbd = tbd;
        cbt = tbt;
    }
}
