package schema.ui.dialogs;

import arc.graphics.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import scheme.*;
import schema.input.*;
import schema.ui.*;

/** Dialog that displays the list of keybinds. */
public class KeybindDialog extends BaseDialog {

    public KeybindDialog() {
        super("@keybind.name");
        addCloseButton();

        for (var bind : Keybind.all) {

            if (bind.category != null) cont.table(t -> {
                t.add("@category." + bind.category, Color.gray).row();
                t.image().growX().height(4f).padTop(4f).color(Color.gray);
            }).fillX().colspan(5).row();

            cont.add("@keybind." + bind).left();

            cont.button(b -> b.label(bind::formatMask).color(Pal.accent), Style.cbe, () -> {}).size(256f, 48f);
            cont.button(b -> b.label(bind::formatKeys).color(Pal.accent), Style.cbe, () -> {}).size(256f, 48f);

            cont.button(Icon.rotate, Style.ibd, bind::reset).size(48f).tooltip("@keybind.reset");
            cont.button(Icon.cancel, Style.ibd, bind::clear).size(48f).tooltip("@keybind.clear").row();
        }
        cont.button("@keybind.reset-all", Style.tbd, this::reset).fillX().height(48f).colspan(5);
    }

    /** Loads the values of the mod keybinds. */
    public void load() {
        for (var bind : Keybind.all) bind.load();
        Main.log("Loaded " + Keybind.all.length + " keybinds");
    }

    /** Resets the values of the mod keybinds. */
    public void reset() {
        for (var bind : Keybind.all) bind.reset();
        Main.log("Reset keybinds");
    }

    /** Resolves conflicts of the mod keybinds. */
    public void resolve() {
        int amount = 0;
        for (var bind : Keybind.all) amount += bind.resolveConflicts();
        Main.log("Resolved " + amount + " conflicts");
    }
}
