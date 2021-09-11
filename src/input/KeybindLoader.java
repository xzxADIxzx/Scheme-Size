package mindustry.input;

import arc.*;
import arc.KeyBinds.*;
import arc.math.*;
import arc.input.*;
import arc.input.InputDevice.*;
import arc.struct.*;

import static arc.Core.*;

public class KeybindLoader{

    public static void load(){
        for(Section sec : keybinds.getSections()){
            for(DeviceType type : DeviceType.values()){
                for(KeyBind def : keybinds.getKeybinds()){
                    String rname = "keybind-" + sec.name + "-" + type.name() + "-" + def.name();

                    Axis loaded = loadAxis(rname);

                    if(loaded != null){
                        sec.binds.get(type, OrderedMap::new).put(def, loaded);
                    }
                }
            }

            sec.device = input.getDevices().get(Mathf.clamp(settings.getInt(sec.name + "-last-device-type", 0), 0, input.getDevices().size - 1));
        }
    }

    public static Axis loadAxis(String name){
        if(settings.getBool(name + "-single", true)){
            KeyCode key = KeyCode.byOrdinal(settings.getInt(name + "-key", KeyCode.unset.ordinal()));
            return key == KeyCode.unset ? null : new Axis(key);
        }else{
            KeyCode min = KeyCode.byOrdinal(settings.getInt(name + "-min", KeyCode.unset.ordinal()));
            KeyCode max = KeyCode.byOrdinal(settings.getInt(name + "-max", KeyCode.unset.ordinal()));
            return min == KeyCode.unset || max == KeyCode.unset ? null : new Axis(min, max);
        }
    }
}