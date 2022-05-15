package mindustry.scheme;

import mindustry.mod.*;
import mindustry.input.ModBinding;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.scheme.SchemeVars.*;

public class SchemeSize extends Mod {

    @Override
    public void init() {
        SchemeVars.load();

        enableConsole = true; // temp

        schematics = m_schematics;
        schematics.loadSync();
        control.setInput(m_input);

        ui.settings = m_settings;
        ui.traces = m_traces;
        ui.listfrag = listfrag;

        hudfrag.build(ui.hudGroup);
        listfrag.build(ui.hudGroup);

        SchemeUpdater.init(); // restore colors
        if (settings.getBool("checkupdate")) SchemeUpdater.check();

        if (mobile) return; // mobiles haven`t keybinds
        ModBinding.load();
        keycomb.init(); // init main keys
    }
}
