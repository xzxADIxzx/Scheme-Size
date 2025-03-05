package schema;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.mod.*;
import schema.input.*;
import schema.tools.*;
import schema.ui.*;
import schema.ui.dialogs.*;
import schema.ui.fragments.*;
import schema.ui.polygon.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/** Main class of the mod that loads, initializes and stores different components of it. */
public class Main extends Mod {

    // region components

    /** Combines the vanilla and schema overlay. */
    public static Overlay overlay;
    /** Utility that helps with buildings. */
    public static Builds builds;
    /** Utility that helps with units. */
    public static Units units;

    /** Advanced input system lying in the foundation of the mod. */
    public static InputSystem insys;

    /** List of servers' URLs that host Copy-Link-and-Join. */
    public static Seq<String> clajURLs = Seq.with("Couldn't fetch CLaJ URLs :(");
    /** List of {@link Events events} acquired via reflection. */
    public static ObjectMap<?, Seq<?>> events;

    // endregion
    // region dialogs

    public static KeybindDialog keybind;

    // endregion
    // region fragments

    // public static InventoryFragment inv;
    public static ConfigFragment config;

    public static MapFragment mapfrag;
    public static CommandFragment cmndfrag;
    public static LoadingFragment loadfrag;

    public static Polygon polyblock;
    public static Polygon polyschema;

    // endregion

    public Main() {
        // this field is no longer final since 136th build
        maxSchematicSize = 128;
    }

    @Override
    public void init() {
        log("=> [green]Loading content...");
        load();

        log("=> [green]Initializing content...");

        keybind.load();
        keybind.resolve();

        config.build(ui.hudGroup);

        mapfrag.build(ui.hudGroup);
        cmndfrag.build(ui.hudGroup);
        loadfrag.build(scene.root);

        polyblock.build(ui.hudGroup);
        polyschema.build(ui.hudGroup);

        control.setInput(insys.getAgent());
        ui.minimapfrag=mapfrag.getAgent();
        ui.loadfrag = loadfrag.getAgent();

        Reflect.set(renderer, "overlays", overlay.getAgent());
        // TODO override inventory too
        Reflect.set(mindustry.input.InputHandler.class, control.input, "config", config.getAgent());

        log("=> [green]Running postinit hooks...");

        Updater.load();
        Updater.fetch();

        try { // run the main script without wrapper to make constants available in the dev console
            Scripts scripts = mods.getScripts();
            scripts.context.evaluateReader(scripts.scope, Updater.script().reader(), "main.js", 0);

            log("Added constants to the dev console");
        } catch (Throwable e) { err(e); }

        log("=> [green]Unhooking events...");
        clear(mindustry.graphics.OverlayRenderer.class);
        clear(mindustry.input.InputHandler.class);
        clear(mindustry.ui.fragments.PlacementFragment.class);
    }

    /** Loads content such as dialogs, fragments and so on. */
    public void load() {
        // these styles are used for building dialogs and fragments and thus are loaded here
        Style.load();

        overlay = new Overlay();
        builds = new Builds();
        units = new Units();

        insys = mobile ? null : new DesktopInput();

        keybind = new KeybindDialog();

        config = new ConfigFragment();

        mapfrag = new MapFragment();
        cmndfrag = new CommandFragment();
        loadfrag = new LoadingFragment();

        polyblock = new Polygon();
        polyschema = new Polygon();
    }

    /** Prints the given info in the dev console. */
    public static void log(String info) { app.post(() -> Log.infoTag("Schema", info)); }

    /** Prints the given error in the dev console. */
    public static void err(Throwable e) { app.post(() -> Log.errTag("Schema", Strings.getStackTrace(e))); }

    /** Copies the given text to the clipboard. */
    public static void copy(String text) {
        if (text == null) return;

        app.setClipboardText(text);
        ui.showInfoFade("@copied");
    }

    /** Use this <b>extremely carefully</b> as it clears event listeners created by the given class. */
    public static void clear(Class<?> target) {
        if (events == null) events = Reflect.get(Events.class, "events");

        int count = 0;
        for (var pair : events) count += pair.value.size - pair.value.removeAll(l -> l.toString().startsWith(target.getName())).size;
        log("Found [red]" + count + "[] events in " + target.getSimpleName());
    }
}
