package scheme.moded;

import arc.KeyBinds.KeyBind;
import arc.KeyBinds.KeybindValue;
import arc.input.KeyCode;
import arc.input.InputDevice.DeviceType;
import mindustry.ui.dialogs.KeybindDialog;
import scheme.Main;

import static arc.Core.*;
import static mindustry.Vars.*;

/** Last update - Mar 11, 2022 */
public enum ModedBinding implements KeyBind {
    adminscfg(KeyCode.f12, "mod"),
    rendercfg(KeyCode.y),
    schematic_shortcut(KeyCode.g),
    toggle_core_items(KeyCode.f7),
    toggle_ai(KeyCode.i),
    manage_unit(KeyCode.f2),
    manage_effect(KeyCode.f3),
    manage_item(KeyCode.f4),
    manage_team(KeyCode.semicolon),
    place_core(KeyCode.apostrophe),
    alternative(KeyCode.altLeft);

    private final KeybindValue defaultValue;
    private final String category;

    private ModedBinding(KeybindValue defaultValue, String category) {
        this.defaultValue = defaultValue;
        this.category = category;
    }

    private ModedBinding(KeybindValue defaultValue) {
        this(defaultValue, null);
    }

    @Override
    public KeybindValue defaultValue(DeviceType type) {
        return defaultValue;
    }

    @Override
    public String category() {
        return category;
    }

    public static void load() {
        KeyBind[] orign = (KeyBind[]) keybinds.getKeybinds();
        KeyBind[] moded = (KeyBind[]) values();
        KeyBind[] binds = new KeyBind[orign.length + moded.length];

        System.arraycopy(orign, 0, binds, 0, orign.length);
        System.arraycopy(moded, 0, binds, orign.length, moded.length);

        keybinds.setDefaults(binds);
        settings.load(); // update controls
        ui.controls = new KeybindDialog();

        Main.log("Mod keybinds loaded.");
    }
}
