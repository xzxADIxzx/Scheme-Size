package schema;

import arc.files.*;
import arc.util.*;
import arc.util.serialization.*;
import mindustry.mod.Mods.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/** Class that handles interactions with the repository of the mod. This includes checking for updates and fetching CLaJ URLs. */
public class Updater {

    /** Repository of the mod that... has the old name, but what can I do? */
    public static final String repo = "xzxADIxzx/Scheme-Size";
    /** Entry of the mod loader that represents this mod. */
    public static LoadedMod mod;

    /** Loads the entry of the mod and its meta data. */
    public static void load() {
        mod = mods.getMod(Main.class);

        // restore colors from the meta data file of the mod
        var meta = Jval.read(meta().reader());
        mod.meta.author      = meta.getString("author");
        mod.meta.description = meta.getString("description");
    }

    /** Fetches the latest version of the mod and CLaJ URLs from GitHub. */
    public static void fetch() {
        Http.get(ghApi + "/repos/" + repo + "/releases/latest", res -> {

            var latest = Jval.read(res.getResult()).getString("tag_name").substring(1);
            if (latest.equals(mod.meta.version))
                log("The mod is up to date");
            else {
                log("The mod is outdated");
                ui.showCustomConfirm("@update.name", bundle.format("update.info", mod.meta.version, latest), "@mods.browser.reinstall", "@ok", Updater::update, () -> {});
            }

        }, Main::err);
        Http.get("https://raw.githubusercontent.com/" + repo + "/main/servers-claj.hjson", res -> {

            clajURLs = Jval.read(res.getResult()).asArray().map(Jval::asString);
            log("Fetched " + clajURLs.size + " CLaJ URLs");

        }, Main::err);
    }

    /** Downloads the latest version of the mod from GitHub. */
    public static void update() { ui.mods.githubImportMod(repo, true); }

    /** Returns the file that contains the meta data of the mod. */
    public static Fi meta() { return mod.root.child("mod.hjson"); }

    /** Returns the file that contains the main script of the mod. */
    public static Fi script() { return mod.root.child("scripts").child("main.js"); }
}
