package mindustry.scheme;

import arc.func.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.serialization.*;
import arc.util.Http.*;
import arc.files.*;
import mindustry.mod.Mods.*;

import static arc.Core.*;
import static mindustry.Vars.*;

import java.net.*;

public class SchemeUpdater{

    private static LoadedMod mod;
    private static float progress;
    private static String repo;

    public static String mv;
    public static String rv;
    public static boolean e;

    public static void check(){
        mod = mods.getMod(SchemeSize.class);
        repo = ghApi + "/repos/" + mod.getRepo() + "/releases/latest";

        Http.get(repo, res -> {
            var json = Jval.read(res.getResultAsString());
            String version = json.getString("tag_name").substring(1);

            e = version == mod.meta.version;
            if(e) return;
            ui.showCustomConfirm("@updater.name",
                bundle.format("updater.info", mod.meta.version, version),
                "@updater.load", "@ok", SchemeUpdater::update, () -> {});

            mv = mod.meta.version;
            rv = version;
        }, e -> {});
    }

    public static void update(){
        try{
            // dancing with tambourines, just to remove the old mod
            if(mod.loader instanceof URLClassLoader cl) cl.close();
            mod.loader = null;
        }catch (Exception e){
            return;
        }

        ui.loadfrag.show("@downloading");
        ui.loadfrag.setProgress(() -> progress);

        Http.get(repo, res -> {
            var json = Jval.read(res.getResultAsString());
            var assets = json.get("assets").asArray();

            var dexed = assets.find(j -> j.getString("name").startsWith("dexed") && j.getString("name").endsWith(".jar"));
            var asset = dexed == null ? assets.find(j -> j.getString("name").endsWith(".jar")) : dexed;

            if(asset != null){
                String url = asset.getString("browser_download_url");
                Http.get(url, r -> handle(mod.getRepo(), r, p -> progress = p), e -> {});
            }
        }, e -> {});
    }

    public static void handle(String repo, HttpResponse res, Floatc cons){
        try{
            Fi file = tmpDirectory.child(repo.replace("/", "") + ".zip");
            long len = res.getContentLength();

            Streams.copyProgress(res.getResultAsStream(), file.write(false), len, 4096, cons);

            mod = mods.importMod(file);
            mod.setRepo(repo);

            file.delete();
            app.post(ui.loadfrag::hide);

            ui.showInfoOnHidden("@mods.reloadexit", app::exit);
        }catch (Throwable e) {}
    }
}
