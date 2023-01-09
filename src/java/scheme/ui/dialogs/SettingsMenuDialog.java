package scheme.ui.dialogs;

import mindustry.gen.Icon;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class SettingsMenuDialog {

    public SettingsMenuDialog() {
        ui.settings.hidden(this::apply);
        ui.settings.addCategory("@category.mod.name", Icon.book, table -> {
            if (!mobile) table.sliderPref("panspeedmul", 4, 4, 20, this::processor);

            table.sliderPref("maxzoommul", 4, 4, 20, this::processor);
            table.sliderPref("minzoommul", 4, 4, 20, this::processor);

            if (!mobile) table.checkPref("mobilebuttons", true);

            table.checkPref("hardscheme", false);
            table.checkPref("approachenabled", true);
            table.checkPref("welcome", true);
            table.checkPref("check4update", true);

            table.areaTextPref("subtitle", "I am using Scheme Size btw");
        });
    }

    public void apply() {
        m_input.changePanSpeed(settings.getInt("panspeedmul"));

        // 6f and 1.5f are default values in Renderer
        renderer.maxZoom = settings.getInt("maxzoommul") / 4f * 6f;
        renderer.minZoom = 1f / (settings.getInt("minzoommul") / 4f) * 1.5f;
    }

    private String processor(int value) {
        return value / 4f + "x";
    }
}
