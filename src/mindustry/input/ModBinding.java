package mindustry.input;

import arc.KeyBinds.*;
import arc.input.InputDevice.*;
import arc.input.*;

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
    renderingset(KeyCode.y),
    look_at(KeyCode.altLeft),
    alternative(KeyCode.altLeft);

    private final KeybindValue defaultValue;
    private final String category;

    ModBinding(KeybindValue defaultValue, String category){
        this.defaultValue = defaultValue;
        this.category = category;
    }

    ModBinding(KeybindValue defaultValue){
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
}