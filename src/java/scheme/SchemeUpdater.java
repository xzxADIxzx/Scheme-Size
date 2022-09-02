package scheme;

import arc.util.Http;
import arc.util.Http.HttpResponse;
import arc.util.io.Streams;
import arc.util.serialization.Jval;
import arc.files.Fi;
import arc.files.ZipFi;
import mindustry.mod.Mods.LoadedMod;

import static arc.Core.*;
import static mindustry.Vars.*;

import java.net.*;

public class SchemeUpdater {

    public static final String repo = "xzxADIxzx/Scheme-Size";

    public static LoadedMod mod;
    public static String url;

    public static float progress;
    public static String download;

    public static void load() {
        mod = mods.getMod(Main.class);
        url = ghApi + "/repos/" + repo + "/releases/latest";

        String updates = "[orange]"; // coloring description of the mod
        for (String[] names : new String[][] {
                { "Release!", "Settings", "Java 8", "Controls", "Updater", "Mobile Support", "Admins Tools" },
                { "Building Tools", "AI Power", "Renderer Tools", "Deep Cleaning", "Schematic Shortcuts" } // TODO: "Cursed Schemes"
        }) {
            updates += "\n"; // add update names
            for (String name : names) updates += "\n   - " + name;
        }

        Jval meta = Jval.read(new ZipFi(mod.file).child("mod.json").readString());
        mod.meta.author = meta.getString("author"); // restore colors in mod's meta
        mod.meta.description = meta.getString("description") + updates;
    }

    public static void check() {
        Main.log("Checking for updates.");
        Http.get(url, res -> {
            Jval json = Jval.read(res.getResultAsString());
            String latest = json.getString("tag_name").substring(1);
            download = json.get("assets").asArray().get(0).getString("browser_download_url");

            if (!latest.equals(mod.meta.version)) ui.showCustomConfirm(
                    "@updater.name", bundle.format("updater.info", mod.meta.version, latest),
                    "@updater.load", "@ok", SchemeUpdater::update, () -> {});
        }, Main::error);
    }

    public static void update() {
        try { // dancing with tambourines, just to remove the old mod
            if (mod.loader instanceof URLClassLoader cl) cl.close();
            mod.loader = null;
        } catch (Throwable e) { Main.error(e); } // this has never happened before, but everything can be

        ui.loadfrag.show("@downloading");
        ui.loadfrag.setProgress(() -> progress);

        Http.get(download, SchemeUpdater::handle, Main::error);
    }

    public static void handle(HttpResponse res) {
        try {
            Fi file = tmpDirectory.child(repo.replace("/", "") + ".zip");
            Streams.copyProgress(res.getResultAsStream(), file.write(false), res.getContentLength(), 4096, p -> progress = p);

            mods.importMod(file).setRepo(repo);
            file.delete();

            app.post(ui.loadfrag::hide);
            ui.showInfoOnHidden("@mods.reloadexit", app::exit);
        } catch (Throwable e) { Main.error(e); }
    }

    public static Fi script() {
        return mod.root.child("scripts").child("main.js");
    }
}
