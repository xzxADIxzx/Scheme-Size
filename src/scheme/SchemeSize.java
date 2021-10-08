package mindustry.scheme;

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
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.input.*;

import static mindustry.Vars.*;

public class SchemeSize extends Mod{

    public static ModInputHandler input;
    public static ModSchematics schematic = new ModSchematics();
    public static ModUnitSelectDialog unit = new ModUnitSelectDialog();
    public static ModSettingsMenuDialog setting = new ModSettingsMenuDialog();
    public static ModHudFragment hudfrag = new ModHudFragment();

    public SchemeSize(){
        Events.on(ClientLoadEvent.class, e -> {
            schematics = schematic;
            schematic.loadSync();

            control.setInput(input = mobile ? new ModMobileInput() : new ModDesktopInput());

            ui.settings = setting;
            hudfrag.build(ui.hudGroup);

            // hide secret
            setting.mod.getCells().get(mobile ? 8 : 9).visible(false);

            // mobiles haven`t keybinds
            if(mobile) return;

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