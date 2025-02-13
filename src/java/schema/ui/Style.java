package schema.ui;

import arc.scene.ui.Button.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.TextButton.*;

import static arc.Core.*;

/** List of all styles of the mod, the purpose of which is to modernize the look of the game. */
public class Style {

    // region styles

    /** Common button, both default and empty variant. */
    public static ButtonStyle cbd, cbe;
    /** Image button, both default and empty variant. */
    public static ImageButtonStyle ibd, ibe;
    /** Text button, both default and empty variant. */
    public static TextButtonStyle tbd, tbe;

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
            disabled = checked = atlas.drawable("schema-button-disabled");
        }};
        ibd = new ImageButtonStyle(ibe) {{ up = atlas.drawable("schema-button"); }};

        tbe = new TextButtonStyle() {{
            over = atlas.drawable("schema-button-over");
            down = atlas.drawable("schema-button-down");
            disabled = checked = atlas.drawable("schema-button-disabled");
        }};
        tbd = new TextButtonStyle(tbe) {{ up = atlas.drawable("schema-button"); }};

        cbe = tbe;
        cbd = tbd;
    }
}
