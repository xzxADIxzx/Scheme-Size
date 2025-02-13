package scheme;

import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.struct.Seq;
import mindustry.content.StatusEffects;
import mindustry.core.UI;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import scheme.moded.*;
import scheme.tools.*;
import scheme.tools.admins.AdminsTools;
import scheme.ui.*;
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
    public static TagSelectDialog tag;

    public static ContentSelectDialog<UnitType> unit;
    public static ContentSelectDialog<StatusEffect> effect;
    public static ContentSelectDialog<Item> item;

    public static SettingsMenuDialog m_settings;
    public static KeybindCombinationsDialog keycomb;
    public static SchemasDialog schemas;
    public static ImageParserDialog parser;
    public static WaveApproachingDialog approaching;
    public static JoinViaClajDialog joinViaClaj;
    public static ManageRoomsDialog manageRooms;

    public static HudFragment hudfrag;
    public static PlayerListFragment listfrag;
    public static ShortcutFragment shortfrag;
    public static ConsoleFragment consolefrag;
    public static CoreInfoFragment corefrag;

    public static Seq<String> clajURLs = Seq.with(
            "n3.xpdustry.com:7025",
            "37.187.73.180:7025",
            "claj.phoenix-network.dev:4000",
            "167.235.159.121:4000",
            "new.xem8k5.top:1050"
    );

    /** List of ip servers that block the mod. */
    public static Seq<String> antiModIPs = Seq.with(
            "play.thedimas.pp.ua",
            "91.209.226.11");

    public static void load() {
        var pixmap = atlas.getPixmap("schema-status-invincible").pixmap.outline(Pal.gray, 3);
        var texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.linear);

        atlas.addRegion("status-invincible-ui", texture, 0, 0, 34, 34);
        StatusEffects.invincible.loadIcon(); // slip a mod texture under the guise of vanilla

        // m_schematics is created in Main to prevent dual loading
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
        tag = new TagSelectDialog();

        unit = new ContentSelectDialog<>("@select.unit", content.units(), 0, 25, 1, value -> {
            return value == 0 ? "@select.unit.clear" : bundle.format("select.units", value);
        });
        effect = new ContentSelectDialog<>("@select.effect", content.statusEffects(), 0, 5 * 3600, 600, value -> {
            return value == 0 ? "@select.effect.clear" : bundle.format("select.seconds", value / 60f);
        });
        item = new ContentSelectDialog<>("@select.item", content.items(), -10000, 10000, 500, value -> {
            return value == 0 ? "@select.item.clear" : bundle.format("select.items", UI.formatAmount(value.longValue()));
        });

        m_settings = new SettingsMenuDialog();
        keycomb = new KeybindCombinationsDialog();
        schemas = new SchemasDialog();
        parser = new ImageParserDialog();
        approaching = new WaveApproachingDialog();
        joinViaClaj = new JoinViaClajDialog();
        manageRooms = new ManageRoomsDialog();

        hudfrag = new HudFragment();
        listfrag = new PlayerListFragment();
        shortfrag = new ShortcutFragment();
        consolefrag = new ConsoleFragment();
        corefrag = new CoreInfoFragment();
    }
}
