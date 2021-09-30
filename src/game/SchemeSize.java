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

import static mindustry.Vars.*;

public class SchemeSize extends Mod{

    public static ModHudFragment hudfrag = new ModHudFragment();

    public SchemeSize(){
        Events.on(ClientLoadEvent.class, e -> {
            // change schematics
            schematics = new ModSchematics();
            schematics.loadSync();

            // change input
            control.setInput(mobile ? new ModMobileInput() : new ModDesktopInput());

            // change dialog
            var settings = new ModSettingsMenuDialog();
            ui.settings = settings;

            // add fragment
            hudfrag.build(ui.hudGroup);

            // add secret
            var mod = settings.mod;
            mod.getCells().get(7).visible(false); // hide secret

            // add zoom scale
            Stack elementMax = (Stack)mod.getCells().get(0).get();
            Stack elementMin = (Stack)mod.getCells().get(1).get();
            Slider sliderMax = (Slider)elementMax.getChildren().get(0);
            Slider sliderMin = (Slider)elementMin.getChildren().get(0);
            sliderMax.changed(() -> { renderer.maxZoom = sliderMax.getValue() / 4f * 6f; });
            sliderMin.changed(() -> { renderer.minZoom = 1 / (sliderMin.getValue() / 4f) * 1.5f; });
            renderer.maxZoom = sliderMax.getValue() / 4f * 6f; // apply zoom
            renderer.minZoom = 1f / (sliderMin.getValue() / 4f) * 1.5f;

            // mobiles haven`t keybinds
            if(mobile) return;

            // add keybinds
            KeyBind[] origi = (KeyBind[])Binding.values();
            KeyBind[] moded = (KeyBind[])ModBinding.values();
            KeyBind[] binds = new KeyBind[origi.length + moded.length];
            System.arraycopy(origi, 0, binds, 0, origi.length);
            System.arraycopy(moded, 0, binds, origi.length, moded.length);
            Core.keybinds.setDefaults(binds);
            Core.settings.load(); // update controls
            ui.controls = new KeybindDialog(); // update dialog
        });
    }
}