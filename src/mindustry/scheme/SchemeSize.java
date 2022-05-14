package mindustry.scheme;

import arc.Core;
import mindustry.mod.*;
import mindustry.Vars;
import mindustry.input.ModBinding;

import static mindustry.scheme.SchemeVars.*;

public class SchemeSize extends Mod {

    @Override
    public void init() {
        SchemeVars.load();

        Vars.enableConsole = true; // temp

        Vars.schematics = schematics;
        Vars.schematics.loadSync();
        Vars.control.setInput(input);

        Vars.ui.settings = settings;
        Vars.ui.traces = traces;
        Vars.ui.listfrag = listfrag;

        hudfrag.build(Vars.ui.hudGroup);
        listfrag.build(Vars.ui.hudGroup);

        SchemeUpdater.init(); // restore colors
        if (Core.settings.getBool("checkupdate")) SchemeUpdater.check();

        if (Vars.mobile) return; // mobiles haven`t keybinds
        ModBinding.load();
        keycomb.init(); // init main keys
    }
}
