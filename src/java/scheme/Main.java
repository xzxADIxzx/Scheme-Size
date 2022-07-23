package scheme;

import arc.util.Log;
import mindustry.mod.Mod;
import mindustry.mod.Scripts;
import scheme.moded.ModedBinding;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class Main extends Mod {

    @Override
    public void init() {
        Backdoor.load();
        ModedBinding.load();
        SchemeVars.load();
        SchemeUpdater.load();

        // schematics = m_schematics;
        ui.traces = traces;
        ui.listfrag = listfrag;

        units.load();
        builds.load();
        keycomb.load();

        hudfrag.build(ui.hudGroup);
        listfrag.build(ui.hudGroup);

        control.setInput(m_input.asHandler());
        renderer.addEnvRenderer(0, render::draw);

        if (settings.getBool("welcome")) ui.showOkText("@welcome.name", "@welcome.text", () -> {});
        if (settings.getBool("check4update")) SchemeUpdater.check();

        try { // run main.js without the wrapper to access the constant values in the game console
            Scripts scripts = mods.getScripts();
            scripts.context.evaluateReader(scripts.scope, SchemeUpdater.script().reader(), "main.js", 0);
        } catch (Throwable e) { error(e); }
    }

    public static void log(String info) {
        app.post(() -> Log.infoTag("Scheme", info));
    }

    public static void error(Throwable info) {
        app.post(() -> Log.err("Scheme", info));
    }
}
