package mindustry.input;

import arc.KeyBinds.*;
import arc.input.InputDevice.*;
import arc.input.*;
import mindustry.ui.dialogs.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public enum ModBinding implements KeyBind{
    secret(KeyCode.f12, "mod"),
    history(KeyCode.f9),
    toggle_core_items(KeyCode.f7),
    change_unit(KeyCode.f2),
    change_effect(KeyCode.f3),
    change_item(KeyCode.f4),
    change_team(KeyCode.semicolon),
    change_ai(KeyCode.i),
    place_core(KeyCode.apostrophe),
    renderset(KeyCode.y),
    look_at(KeyCode.altLeft),
    alternative(KeyCode.altLeft);

    private final KeybindValue defaultValue;
    private final String category;

    private ModBinding(KeybindValue defaultValue, String category){
        this.defaultValue = defaultValue;
        this.category = category;
    }

    private ModBinding(KeybindValue defaultValue){
        this(defaultValue, null);
    }

    @Override
    public KeybindValue defaultValue(DeviceType type){
        return defaultValue;
    }

    @Override
    public String category(){
        return category;
    }

    public static void load() {
        KeyBind[] orign = (KeyBind[]) Binding.values();
        KeyBind[] moded = (KeyBind[]) ModBinding.values();
        KeyBind[] binds = new KeyBind[orign.length + moded.length];

        System.arraycopy(orign, 0, binds, 0, orign.length);
        System.arraycopy(moded, 0, binds, orign.length, moded.length);

        keybinds.setDefaults(binds);
        settings.load(); // update controls
        ui.controls = new KeybindDialog(); // update dialog
    }
}