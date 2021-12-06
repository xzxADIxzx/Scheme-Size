package mindustry.scheme;

import arc.*;
import arc.KeyBinds.*;
import mindustry.ui.dialogs.*;
import mindustry.ui.fragments.*;
import mindustry.mod.*;
import mindustry.type.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.input.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class SchemeSize extends Mod{

    public static ModSchematics schematic;
    public static ModInputHandler input;
    public static AdditionalRenderer render;

    public static SecretConfigDialog secret;
    public static KeybindCombinationsDialog keycomb;
    public static RenderSettingsDialog renderingset;
    public static ModSettingsMenuDialog setting;
    public static ModTraceDialog trace;
    public static ModHudFragment hudfrag;
    public static ModPlayerListFragment listfrag;

    public static AISelectDialog ai;
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
            render = new AdditionalRenderer();

            secret = new SecretConfigDialog();
            keycomb = new KeybindCombinationsDialog();
            renderingset = new RenderSettingsDialog();
            setting = new ModSettingsMenuDialog();
            trace = new ModTraceDialog();
            hudfrag = new ModHudFragment();
            listfrag = new ModPlayerListFragment();

            ai = new AISelectDialog("@aiselect");
            team = new TeamSelectDialog("@teamselect");
            tile = new TileSelectDialog("@tileselect");
            unit = new ContentSelectDialog<UnitType>("@unitselect", content.units(), 1, 20, 1, value -> {
                return Core.bundle.format("unit.zero.units", value);
            });
            effect = new ContentSelectDialog<StatusEffect>("@effectselect", content.statusEffects(), 0, 60 * 60 * 5, 60, value -> {
                return value == 0 ? "@cleareffect" : Core.bundle.format("unit.zero.seconds", value / 60);
            });
            item = new ContentSelectDialog<Item>("@itemselect", content.items(), -10000, 10000, 200, value -> {
                return value == 0 ? "@clearitem" : Core.bundle.format("unit.zero.items", UI.formatAmount((long)value));
            });

            ui.settings = setting;
            ui.traces = trace;
            ui.listfrag = listfrag;
            hudfrag.build(ui.hudGroup);
            listfrag.build(ui.hudGroup);

            // hide secret
            setting.mod.getCells().get(mobile ? 8 : 10).visible(Core.settings.getBool("secret"));

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
