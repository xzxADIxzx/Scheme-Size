package mindustry.game;

import arc.*;
import arc.util.*;
import arc.scene.ui.*;
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
                game.sliderPref("maxzoommul", 4, 1, 8, 1, i -> i / 100 + "x");
                game.sliderPref("minzoommul", 4, 1, 8, 1, i -> i / 100 + "x");
                game.sliderPref("copysize", 512, 32, 512, 32, i -> Core.bundle.format("setting.blocks", i));
                game.sliderPref("breaksize", 512, 32, 512, 32, i -> Core.bundle.format("setting.blocks", i));
                game.checkPref("copyshow", true);
                game.checkPref("breakshow", true);
                game.getCells().get(11).visible(false); // Hide justspace

                // Add Zoom Scale
                Slider sliderMax = game.getCells().get(12).get().getChildren().get(0);
                Slider sliderMin = game.getCells().get(13).get().getChildren().get(0);
                sliderMax.changed(() -> { Vars.renderer.maxZoom = sliderMax.getValue() / 4f * 6f; });
                sliderMin.changed(() -> { Vars.renderer.minZoom = sliderMin.getValue() / 4f * 1.5f; });

                // Add Logs
                // Log.info(Vars.schematics);
                // Log.info(Vars.control.input);
            });
        });
    }
}