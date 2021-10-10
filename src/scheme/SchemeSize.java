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

    public static ModSchematics schematic;
    public static ModInputHandler input;
    public static ModSettingsMenuDialog setting;
    public static ModHudFragment hudfrag;

    public static ModContentSelectDialog unit;
    public static ModContentSelectDialog effect;
    public static ModContentSelectDialog item;

    public SchemeSize(){
        Events.on(ClientLoadEvent.class, e -> {
            schematic = new ModSchematics();
            setting = new ModSettingsMenuDialog();
            hudfrag = new ModHudFragment();

            // unit = new ModContentSelectDialog("@unitselect", content.units(), 0, 10, 1, value -> {
            //     return Core.bundle.format("setting.blocks", value.get());
            // });
            // effect = new ModContentSelectDialog("@unitselect", content.units(), 0, 180, 1, value -> {
            //     return Core.bundle.format("setting.blocks", value.get());
            // });
            // item = new ModContentSelectDialog("@unitselect", content.units(), -10000, 10000, 1000, value -> {
            //     return Core.bundle.format("setting.blocks", UI.formatAmount(value.get()));
            // });

            unit = new ModContentSelectDialog("@unitselect", content.units(), 0, 10, 1);
            effect = new ModContentSelectDialog("@unitselect", content.units(), 0, 180, 1);
            item = new ModContentSelectDialog("@unitselect", content.units(), -10000, 10000, 1000);

            schematics = schematic;
            schematics.loadSync();

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