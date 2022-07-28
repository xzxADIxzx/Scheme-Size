package scheme;

import mindustry.core.UI;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import scheme.moded.*;
import scheme.tools.*;
import scheme.tools.admins.AdminsTools;
import scheme.ui.HudFragment;
import scheme.ui.PlayerListFragment;
import scheme.ui.dialogs.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class SchemeVars {

    public static ModedSchematics m_schematics;
    public static ModedInputHandler m_input;

    public static AdminsTools admins;
    public static RendererTools render;
    public static BuildingTools build;
    public static UnitsCache units;
    public static BuildsCache builds;

    public static AdminsConfigDialog adminscfg;
    public static RendererConfigDialog rendercfg;

    public static AISelectDialog ai;
    public static TeamSelectDialog team;
    public static TileSelectDialog tile;

    public static ContentSelectDialog<UnitType> unit;
    public static ContentSelectDialog<StatusEffect> effect;
    public static ContentSelectDialog<Item> item;

    public static SettingsMenuDialog m_settings;
    public static KeybindCombinationsDialog keycomb;
    public static TraceDialog traces;
    public static SchematicsDialog schemas;

    public static HudFragment hudfrag;
    public static PlayerListFragment listfrag;

    public static void load() {
        // m_schematics = new ModedSchematics();
        m_input = mobile ? new ModedMobileInput() : new ModedDesktopInput();

        admins = AdminsConfigDialog.getTools();
        render = new RendererTools();
        build = new BuildingTools();
        units = new UnitsCache();
        builds = new BuildsCache();

        adminscfg = new AdminsConfigDialog();
        rendercfg = new RendererConfigDialog();

        ai = new AISelectDialog();
        team = new TeamSelectDialog();
        tile = new TileSelectDialog();

        unit = new ContentSelectDialog<>("@select.unit", content.units(), 1, 25, 1, value -> {
            return bundle.format("select.units", value);
        });
        effect = new ContentSelectDialog<>("@select.effect", content.statusEffects(), 0, 5 * 3600, 600, value -> {
            return value == 0 ? "@select.effect.clear" : bundle.format("select.seconds", value / 60f);
        });
        item = new ContentSelectDialog<>("@select.item", content.items(), -10000, 10000, 500, value -> {
            return value == 0 ? "@select.item.clear" : bundle.format("select.items", UI.formatAmount(value.longValue()));
        });

        m_settings = new SettingsMenuDialog();
        keycomb = new KeybindCombinationsDialog();
        traces = new TraceDialog();
        schemas = new SchematicsDialog();

        hudfrag = new HudFragment();
        listfrag = new PlayerListFragment();
    }
}
