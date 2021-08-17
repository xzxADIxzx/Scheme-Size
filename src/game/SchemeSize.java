package mindustry.game;

import arc.*;
import arc.util.*;
import mindustry.mod.*;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.input.*;

public class SchemeSize extends Mod{

    public SchemeSize(){
        Events.on(ClientLoadEvent.class, e -> {
            //wait 10 secs, because... idk
            Time.runTask(10f, () -> {
                // Change Schematics
                Vars.schematics = new Schematics512();
                Vars.schematics.loadSync();

                // Change Input
                if(!Vars.mobile){
                    Vars.control.setInput(new DesktopInput512());
                }

                // Add Settings
                var game = Vars.ui.settings.game;
                game.sliderPref("justspace", 1, 1, 1, 1, i -> i + "");
                game.sliderPref("maxzoommul", 100, 25, 200, 25, i -> i / 100 + "x");
                game.sliderPref("minzoommul", 100, 25, 200, 25, i -> i / 100 + "x");
                game.sliderPref("copysize", 512, 32, 512, 32, i -> Core.bundle.format("setting.blocks", i));
                game.sliderPref("breaksize", 512, 32, 512, 32, i -> Core.bundle.format("setting.blocks", i));
                game.checkPref("copyshow", true);
                game.checkPref("breakshow", true);
                game.getCells().get(11).visible(false); // Hide justspace

                // Add Zoom Scale
                Slider slider = game.getCells().get(12).slider;
                slider.changed(() -> { Vars.renderer.maxZoom = 6 * slider.GetValue() / 100; });
                slider = game.getCells().get(13).slider;
                slider.changed(() -> { Vars.renderer.maxZoom = 6 * slider.GetValue() / 100; });

                // Add Logs
                // Log.info(Vars.schematics);
                // Log.info(Vars.control.input);
            });
        });
    }
}