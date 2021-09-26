package mindustry.input;

import arc.*;
import arc.KeyBinds.*;
import arc.input.InputDevice.*;
import arc.input.*;

public enum ModBinding implements KeyBind{
    toggle_core_items(KeyCode.f7, "mod"),
    switch_team_btw(KeyCode.semicolon),
    switch_team(KeyCode.apostrophe),
    look_at(KeyCode.altLeft),
    teleport(KeyCode.altLeft);

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