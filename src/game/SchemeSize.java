package mindustry.game;

import arc.*;
import arc.util.*;
import arc.input.*;
import arc.KeyBinds.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.ui.dialogs.*;
import mindustry.ui.fragments.*;
import mindustry.mod.*;
import mindustry.core.*;
import mindustry.game.EventType.*;
import mindustry.input.*;
import mindustry.Vars;

public class SchemeSize extends Mod{

    public SchemeSize(){
        Events.on(ClientLoadEvent.class, e -> {
            // Change schematics
            Vars.schematics = new ModSchematics();
            Vars.schematics.loadSync();

            // Change input
            if(!Vars.mobile){
                Vars.control.setInput(new ModDesktopInput());
            }

            // Change dialog
            var settings = new ModSettingsMenuDialog();
            Vars.ui.settings = settings;

            // Change fragment
            Vars.ui.hudfrag = new ModHudFragment();
            Vars.ui.hudfrag.build(Vars.ui.hudgroup);

            // Add secret
            var mod = settings.mod;
            mod.getCells().get(7).visible(false); // Hide secret

            // Add zoom scale
            Stack elementMax = (Stack)mod.getCells().get(0).get();
            Stack elementMin = (Stack)mod.getCells().get(1).get();
            Slider sliderMax = (Slider)elementMax.getChildren().get(0);
            Slider sliderMin = (Slider)elementMin.getChildren().get(0);
            sliderMax.changed(() -> { Vars.renderer.maxZoom = sliderMax.getValue() / 4f * 6f; });
            sliderMin.changed(() -> { Vars.renderer.minZoom = 1 / (sliderMin.getValue() / 4f) * 1.5f; });
            Vars.renderer.maxZoom = sliderMax.getValue() / 4f * 6f; // Apply zoom
            Vars.renderer.minZoom = 1f / (sliderMin.getValue() / 4f) * 1.5f;

            // Add keybinds
            KeyBind[] origi = (KeyBind[])Binding.values();
            KeyBind[] moded = (KeyBind[])ModBinding.values();
            KeyBind[] binds = new KeyBind[origi.length + moded.length];
            System.arraycopy(origi, 0, binds, 0, origi.length);
            System.arraycopy(moded, 0, binds, origi.length, moded.length);
            Core.keybinds.setDefaults(binds);
            Core.settings.load(); // Update controls
            Vars.ui.controls = new KeybindDialog(); // Update dialog

            // Add logs
            // Log.info(Vars.schematics);
            // Log.info(Vars.control.input);
            // Log.info(Vars.ui.settings);
        });
    }
}