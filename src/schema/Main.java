package schema;

import arc.struct.*;
import arc.util.*;
import mindustry.mod.*;
import schema.input.*;
import schema.tools.*;
import schema.ui.*;
import schema.ui.dialogs.*;
import schema.ui.fragments.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/// Main class of the mod that loads, initializes and stores different components of it.
public class Main extends Mod
{
    // region components

    /// Advanced renderer combining both vanilla and schema overlays.
    public static Overlay overlay;
    /// Utility helping with buildings.
    public static Builds builds;
    /// Utility helping with units.
    public static Units units;

    /// Advanced input system lying in the foundation of the project.
    public static InputSystem insys;

    /// List of fetched servers that host CLaJ.
    public static Seq<String> clajURLs;
    /// List of events acquired via reflection.
    public static ObjectMap<?, Seq<?>> events;

    // endregion
    // region dialogs

    public static KeybindDialog keybind;

    // endregion
    // region fragments

    // public static InventoryFragment inv;
    public static ConfigFragment config;
    public static HudFragment hudfrag;
    public static MapFragment mapfrag;
    public static CommandFragment cmndfrag;
    public static LoadingFragment loadfrag;

    // endregion

    /// Loads content such as tools, dialogs, fragments and so on.
    public void load()
    {
        Style.load();

        overlay = new Overlay();
        builds = new Builds();
        units = new Units();

        insys = mobile ? null : new DesktopInput();

        keybind = new KeybindDialog();

        config = new ConfigFragment();
        hudfrag = new HudFragment();
        mapfrag = new MapFragment();
        cmndfrag = new CommandFragment();
        loadfrag = new LoadingFragment();
    }

    /// Hooks content such as input, dialogs, fragments and so on.
    public void hook()
    {
        ui.hudGroup.clear();

        // TODO inventory
        config.build(ui.hudGroup);
        hudfrag.build(ui.hudGroup);
        mapfrag.build(ui.hudGroup);
        cmndfrag.build(ui.hudGroup);
        loadfrag.build(scene.root);

        control.setInput(insys.agent());

        // TODO hudfrag
        ui.minimapfrag = mapfrag.agent();
        ui.loadfrag = loadfrag.agent();

        Reflect.set(renderer, "overlays", overlay.agent());
        // TODO inventory
        Reflect.set(mindustry.input.InputHandler.class, control.input, "config", config.agent());
    }

    @Override
    public void init()
    {
        Tools.log("[green]=> Loading content...");
        load();

        Tools.log("[green]=> Initializing content...");
        hook();

        keybind.load();
        keybind.resolve();

        Tools.log("[green]=> Running postinit hooks...");

        Updater.load();
        Updater.fetch();

        try // run the script outside of wrapper to make constants available in the dev console
        {
            Scripts scripts = mods.getScripts();
            scripts.context.evaluateReader(scripts.scope, Updater.script().reader(), "main.js", 0);

            Tools.log("[green] < Loaded constants into the dev console");
        }
        catch (Throwable e) { Tools.err(e); }

        Tools.log("[green]=> Unhooking events...");

        Tools.clear(mindustry.graphics.OverlayRenderer.class);
        Tools.clear(mindustry.input.InputHandler.class);
        Tools.clear(mindustry.ui.fragments.BlockInventoryFragment.class);
        Tools.clear(mindustry.ui.fragments.HudFragment.class);
        Tools.clear(mindustry.ui.fragments.PlacementFragment.class);
    }
}
