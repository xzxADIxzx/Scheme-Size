package mindustry.game;

import arc.*;
import arc.util.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.mod.*;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.input.*;

public class SchemeSize extends Mod{

    public SchemeSize(){
        Events.on(ClientLoadEvent.class, e -> {
            // wait 10 secs, because... idk
            Time.runTask(10f, () -> {
                // Change schematics
                Vars.schematics = new Schematics512();
                Vars.schematics.loadSync();

                // Change input
                if(!Vars.mobile){
                    Vars.control.setInput(new DesktopInput512());
                }

                // Add settings
                var game = Vars.ui.settings.game;
                game.checkPref("secret", false);
                game.sliderPref("maxzoommul", 4, 4, 8, 1, i -> i / 4f + "x");
                game.sliderPref("minzoommul", 4, 4, 8, 1, i -> i / 4f + "x");
                game.sliderPref("copysize", 512, 32, 512, 32, i -> Core.bundle.format("setting.blocks", i));
                game.sliderPref("breaksize", 512, 32, 512, 32, i -> Core.bundle.format("setting.blocks", i));
                game.checkPref("copyshow", true);
                game.checkPref("breakshow", true);
                game.getCells().get(11).visible(false); // Hide secret

                // Add zoom scale
                Stack elementMax = (Stack)game.getCells().get(12).get();
                Stack elementMin = (Stack)game.getCells().get(13).get();
                Slider sliderMax = (Slider)elementMax.getChildren().get(0);
                Slider sliderMin = (Slider)elementMin.getChildren().get(0);
                sliderMax.changed(() -> { Vars.renderer.maxZoom = sliderMax.getValue() / 4f * 6f; });
                sliderMin.changed(() -> { Vars.renderer.minZoom = 1 / (sliderMin.getValue() / 4f) * 1.5f; });
                Vars.renderer.maxZoom = sliderMax.getValue() / 4f * 6f; // Apply zoom
                Vars.renderer.minZoom = 1f / (sliderMin.getValue() / 4f) * 1.5f;

                // Add keybind
                var keybinds = new Array();
                Binding.values().forEach(item -> keybinds.splice(0, 0, item));
                keybinds.reverse();
                keybinds.splice(3, 0, new Bind());
                Core.keybinds.setDefaults(keybinds);

                // Add logs
                // Log.info(Vars.schematics);
                // Log.info(Vars.control.input);
            });
        });
    }

    public class Bind implements KeyBind {
        
        @Override
        public KeybindValue defaultValue(DeviceType type){
            return (KeybindValue)KeyCode.backslash;
        }
    }
}