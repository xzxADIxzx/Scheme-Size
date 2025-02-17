package schema;

import arc.util.*;
import mindustry.mod.*;
import schema.ui.*;
import schema.ui.dialogs.*;
import schema.ui.fragments.*;
import scheme.*; // TODO update updater (lol)

import static arc.Core.*;
import static mindustry.Vars.*;

/** Main class of the mod that loads, initializes and stores different components of it. */
public class Main extends Mod {

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

        ui.loadfrag = loadfrag.getAgent();

        log("=> [green]Running postinit hooks...");

        SchemeUpdater.load();

        try { // run the main script without wrapper to make constants available in the dev console
            Scripts scripts = mods.getScripts();
            scripts.context.evaluateReader(scripts.scope, SchemeUpdater.script().reader(), "main.js", 0);

            log("Added constants to the dev console");
        } catch (Throwable e) { err(e); }
    }

    /** Loads content such as dialogs, fragments and so on. */
    public void load() {
        // these styles are used for building dialogs and fragments and thus are loaded here
        Style.load();

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
}
