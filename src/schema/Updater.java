package schema;

import arc.files.*;
import arc.util.*;
import arc.util.serialization.*;
import mindustry.mod.Mods.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Class that handles interactions with the project's repository. This includes checking for updates and fetching CLaJ URLs.
public class Updater
{
    /// Repository of the project.
    public static final String repo = "xzxADIxzx/Scheme-Size";
    /// Entry of the modification.
    public static LoadedMod mod;

    /// Loads the entry and its meta data.
    public static void load()
    {
        mod = mods.getMod(Main.class);

        // restore colors
        var meta = Jval.read(meta().reader());
        mod.meta.author      = meta.getString("author");
        mod.meta.description = meta.getString("description");
    }

    /// Fetches the latest version of the project and CLaJ URLs from GitHub.
    public static void fetch()
    {
        Http.get(ghApi + "/repos/" + repo + "/releases/latest", r ->
        {
            var latest = Jval.read(r.getResult()).getString("tag_name").substring(1);
            if (latest.equals(mod.meta.version))
            {
                Tools.log("The project is up to date");
            }
            else
            {
                Tools.log("The project is outdated");
                ui.showCustomConfirm("@update.name", bundle.format("update.info", mod.meta.version, latest), "@update", "@ok", Updater::update, () -> {});
            }
        }, Tools::err);
        Http.get("https://raw.githubusercontent.com/" + repo + "/main/servers-claj.hjson", r ->
        {
            clajURLs = Jval.read(r.getResult()).asArray().map(Jval::asString);
            Tools.log("Fetched " + clajURLs.size + " CLaJ URLs");
        }, Tools::err);
    }

    /// Downloads the latest version of the mod from GitHub.
    public static void update() { ui.mods.githubImportMod(repo, true); }

    /// Returns the file containing the meta data of the project.
    public static Fi meta() { return mod.root.child("mod.hjson"); }

    /// Returns the file containing the main script of the project.
    public static Fi script() { return mod.root.child("scripts").child("main.js"); }
}
