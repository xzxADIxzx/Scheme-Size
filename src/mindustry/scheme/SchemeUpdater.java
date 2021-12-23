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

    private static LoadedMod mod = mods.getMod(SchemeSize.class);
    private static float progress;
    private static String repo = ghApi + "/repos/" + mod.getRepo() + "/releases/latest";

    public static void check(){
        Http.get(repo, res -> {
            var json = Jval.read(res.getResultAsString());
            String version = json.getString("tag_name").substring(1);
            if(version != mod.meta.version)
                ui.showCustomConfirm("@updater.name",
                    bundle.format("@updater.info", mod.meta.version, version),
                    "@ok", "@updater.reinstall", null, SchemeUpdater::update);
        }, e -> {});
    }

    public static void update(){
        try{
            // dancing with tambourines, just to remove the old mod
            if(mod.loader instanceof URLClassLoader cl) cl.close();
            else return;
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
                Http.get(url, r -> handle(mod.meta.repo, r, p -> progress = p), e -> {});
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
        }catch (Throwable e) {}
    }
}
