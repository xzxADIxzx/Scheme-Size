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

    public static KeybindCombinationsDialog keycomb;
    public static ModSettingsMenuDialog setting;
    public static ModHudFragment hudfrag;
    public static ModPlayerListFragment listfrag;

    public static TeamSelectDialog team;
    public static TileSelectDialog tile;
    public static ContentSelectDialog<UnitType> unit;
    public static ContentSelectDialog<StatusEffect> effect;
    public static ContentSelectDialog<Item> item;

    public SchemeSize(){
        Events.on(ClientLoadEvent.class, event -> {
            schematic = new ModSchematics();
            schematics = schematic;
            schematics.loadSync();

            control.setInput(input = mobile ? new ModMobileInput() : new ModDesktopInput());

            keycomb = new KeybindCombinationsDialog();
            setting = new ModSettingsMenuDialog();
            hudfrag = new ModHudFragment();
            listfrag = new ModPlayerListFragment();

            team = new TeamSelectDialog("@teamselect");
            tile = new TileSelectDialog("@tileselect");
            unit = new ContentSelectDialog("@unitselect", content.units(), 1, 20, 1, value -> {
                return Core.bundle.format("unit.zero.units", value);
            });
            effect = new ContentSelectDialog("@effectselect", content.statusEffects(), 0, 60 * 60 * 5, 60, value -> {
                return value == 0 ? "@cleareffect" : Core.bundle.format("unit.zero.seconds", value / 60);
            });
            item = new ContentSelectDialog("@itemselect", content.items(), -10000, 10000, 200, value -> {
                return value == 0 ? "@clearitem" : Core.bundle.format("unit.zero.items", UI.formatAmount((long)value));
            });

            ui.settings = setting;
            ui.listfrag = listfrag;
            hudfrag.build(ui.hudGroup);
            listfrag.build(ui.hudGroup);

            // hide secret
            setting.mod.getCells().get(mobile ? 7 : 8).visible(false);

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