package mindustry.scheme;

import arc.util.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.ui.dialogs.*;
import mindustry.ui.fragments.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class SchemeVars {

    public static ModSchematics schematics;
    public static ModInputHandler input;
    public static AdditionalRenderer renderer;

    public static ModSettingsMenuDialog settings;
    public static KeybindCombinationsDialog keycomb;
    public static ModTraceDialog traces;

    public static SecretConfigDialog secretcfg;
    public static RenderSettingsDialog rendercfg;

    public static AISelectDialog ai;
    public static TeamSelectDialog team;
    public static TileSelectDialog tile;

    public static ContentSelectDialog<UnitType> unit;
    public static ContentSelectDialog<StatusEffect> effect;
    public static ContentSelectDialog<Item> item;

    public static ModHudFragment hudfrag;
    public static ModPlayerListFragment listfrag;

    public static void load() {
        schematics = new ModSchematics();
        input = mobile ? new ModMobileInput() : new ModDesktopInput();
        renderer = new AdditionalRenderer();

        settings = new ModSettingsMenuDialog();
        keycomb = new KeybindCombinationsDialog();
        traces = new ModTraceDialog();

        secretcfg = new SecretConfigDialog();
        rendercfg = new RenderSettingsDialog();

        ai = new AISelectDialog();
        team = new TeamSelectDialog();
        tile = new TileSelectDialog();

        unit = new ContentSelectDialog<UnitType>("@unitselect", content.units(), 1, 20, 1, value -> {
            return bundle.format("unit.zero.units", value);
        });
        effect = new ContentSelectDialog<StatusEffect>("@effectselect", content.statusEffects(), 0, 5 * Time.toMinutes, 60, value -> {
            return value == 0 ? "@cleareffect" : bundle.format("unit.zero.seconds", value / 60);
        });
        item = new ContentSelectDialog<Item>("@itemselect", content.items(), -10000, 10000, 200, value -> {
            return value == 0 ? "@clearitem" : bundle.format("unit.zero.items", UI.formatAmount((long) value));
        });

        hudfrag = new ModHudFragment();
        listfrag = new ModPlayerListFragment();
    }
}
