package schema;

import arc.struct.*;
import arc.util.*;
import mindustry.mod.*;
import schema.input.*;
import schema.ui.*;
import schema.ui.dialogs.*;
import schema.ui.fragments.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/** Main class of the mod that loads, initializes and stores different components of it. */
public class Main extends Mod {

    // region components

    /** Advanced input system lying in the foundation of the mod. */
    public static InputSystem insys;

    /** List of servers' URLs that host Copy-Link-and-Join. */
    public static Seq<String> clajURLs = Seq.with("Couldn't fetch CLaJ URLs :(");

    // endregion
    // region dialogs

    public static KeybindDialog keybind;

    // endregion
    // region fragments

    public static LoadingFragment loadfrag;

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

        loadfrag.build(scene.root);

        control.setInput(insys.getAgent());
        ui.loadfrag = loadfrag.getAgent();

        log("=> [green]Running postinit hooks...");

        Updater.load();
        Updater.fetch();

        try { // run the main script without wrapper to make constants available in the dev console
            Scripts scripts = mods.getScripts();
            scripts.context.evaluateReader(scripts.scope, Updater.script().reader(), "main.js", 0);

            log("Added constants to the dev console");
        } catch (Throwable e) { err(e); }

        log("=> [greed]Unhooking events...");
        clear(mindustry.ui.fragments.PlacementFragment.class);
    }

    /** Loads content such as dialogs, fragments and so on. */
    public void load() {
        // these styles are used for building dialogs and fragments and thus are loaded here
        Style.load();

        insys = mobile ? null : new DesktopInput();
        keybind = new KeybindDialog();
        loadfrag = new LoadingFragment();
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
        Reflect.<ObjectMap<?, Seq<?>>>get(Events.class, "events").each((t, s) -> s.removeAll(l -> l.toString().startsWith(target.getName())));
    }
}
