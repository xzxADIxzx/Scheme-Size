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
            var group = ui.hudGroup.getChildren().get(5);
            group.clear();
            hudfrag.build(group);

            // add secret
            settings.mod.getCells().get(9).visible(false); // hide secret

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