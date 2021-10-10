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
import mindustry.type.*;
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

    public static ModContentSelectDialog<UnitType> unit;
    public static ModContentSelectDialog<StatusEffect> effect;
    public static ModContentSelectDialog<Item> item;

    public SchemeSize(){
        Events.on(ClientLoadEvent.class, e -> {
            schematic = new ModSchematics();
            setting = new ModSettingsMenuDialog();
            hudfrag = new ModHudFragment();

            unit = new ModContentSelectDialog("@unitselect", content.units(), 1, 10, 1, value -> {
                return Core.bundle.format("unit.zero.units", value);
            });
            effect = new ModContentSelectDialog("@unitselect", content.statusEffects(), 0, 180, 1, value -> {
                return Core.bundle.format("unit.zero.seconds", value);
            });
            item = new ModContentSelectDialog("@unitselect", content.items(), -10000, 10000, 1000, value -> {
                return Core.bundle.format("unit.zero.items", UI.formatAmount((long)value));
            });

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