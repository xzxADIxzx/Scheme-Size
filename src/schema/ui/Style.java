package schema.ui;

import arc.graphics.*;
import arc.scene.style.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.ScrollPane.*;
import arc.scene.ui.TextButton.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import schema.*;

import static arc.Core.*;

/// List of all styles designed to modernize the look of the game.
public class Style
{
    // region styles

    /// Common button: default, empty and toggle variants.
    public static ButtonStyle cbd, cbe, cbt;
    /// Image button: default, empty and toggle variants.
    public static ImageButtonStyle ibd, ibe, ibt;
    /// Text button: default, empty and toggle variants.
    public static TextButtonStyle tbd, tbe, tbt;
    /// Scroll style, simple knob without anything else.
    public static ScrollPaneStyle scr;
    /// Special style for the command fragment.
    public static ImageButtonStyle ibc;
    /// Label style that uses the outline font.
    public static LabelStyle outline;

    // endregion

    /// Loads the sprites & styles.
    public static void load()
    {
        // region sprites

        String[] names =
        {
            "schema-button-up",
            "schema-button-over",
            "schema-button-down",
            "schema-button-disabled",
            "schema-scroll-knob",
        };
        for (var name : names) atlas.find(name).splits = name.endsWith("knob") ? new int[] { 0, 0, 24, 16 } : new int[] { 16, 16, 16, 16 };

        Tools.log("[green] < Loaded [accent]" + names.length + "[] sprites");

        // endregion
        // region styles

        ibe = new ImageButtonStyle()
        {{
            over     = find("button-over"    );
            down     = find("button-down"    );
            disabled = find("button-disabled");
        }};
        ibd = new ImageButtonStyle(ibe) {{ up      = find("button-up"  ); }};
        ibt = new ImageButtonStyle(ibe) {{ checked = find("button-over"); }};

        tbe = new TextButtonStyle()
        {{
            over     = find("button-over"    );
            down     = find("button-down"    );
            disabled = find("button-disabled");

            font = Fonts.def;
        }};
        tbd = new TextButtonStyle(tbe) {{ up      = find("button-up"  ); }};
        tbt = new TextButtonStyle(tbe) {{ checked = find("button-over"); }};

        cbe = tbe;
        cbd = tbd;
        cbt = tbt;

        scr = new ScrollPaneStyle() {{ vScrollKnob = find("scroll-knob"); }};

        ibc = new ImageButtonStyle(ibt)
        {{
            imageUpColor      = Pal.accentBack;
            imageOverColor    = Pal.accent;
            imageDownColor    = Pal.accentBack;
            imageCheckedColor = Pal.accent;
        }};

        outline = Styles.outlineLabel;

        Tools.log("[green] < Created [accent]12[] styles");

        // endregion

        Colors.put("light", Pal.lightishGray);
        Colors.put("heavy", Pal.gray);
    }

    /// Finds a drawable by name.
    public static Drawable find(String name) { return atlas.drawable("schema-" + name); }

    /// Finds a drawable by team.
    public static Drawable icon(Team team)
    {
        if (team.id < 6) return atlas.drawable(new String[] {
            "team-derelict",
            "team-sharded",
            "team-crux",
            "team-malis",
            "status-electrified-ui",
            "status-wet-ui"
        }[team.id]);

        return ( (TextureRegionDrawable) Tex.whiteui ).tint(team.color);
    }
}
