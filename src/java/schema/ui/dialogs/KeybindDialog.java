package schema.ui.dialogs;

import arc.graphics.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import scheme.*;
import schema.input.*;
import schema.ui.*;

/** Dialog that displays the list of keybinds. */
public class KeybindDialog extends BaseDialog {

    /** Stores buttons used for reassigning keybinds' masks and keys. */
    private Button[] mask = new Button[Keybind.all.length], keys = new Button[Keybind.all.length];

    public KeybindDialog() {
        super("@keybind.name");
        addCloseButton();
        hidden(this::resolve);

        for (var bind : Keybind.all) {

            if (bind.category != null) cont.table(t -> {
                t.add("@category." + bind.category, Color.gray).row();
                t.image().growX().height(4f).padTop(4f).color(Color.gray);
            }).fillX().colspan(5).row();

            cont.add("@keybind." + bind).left();

            cont.button(b -> set(mask, bind, b).label(bind::formatMask).color(Pal.accent), Style.cbe, () -> rebindMask(bind)).size(256f, 48f).visible(bind::single);
            cont.button(b -> set(keys, bind, b).label(bind::formatKeys).color(Pal.accent), Style.cbe, () -> {}).size(256f, 48f);

            cont.button(Icon.rotate, Style.ibd, bind::reset).size(48f).tooltip("@keybind.reset");
            cont.button(Icon.cancel, Style.ibd, bind::clear).size(48f).tooltip("@keybind.clear").row();
        }
        cont.button("@keybind.reset-all", Style.tbd, this::reset).fillX().height(48f).colspan(5);
    }

    // region rebinding

    private Button set(Button[] arr, Keybind bind, Button btn) { arr[bind.ordinal()] = btn; return btn; }

    private Vec2 get(Button[] arr, Keybind bind) { var btn = arr[bind.ordinal()]; return new Vec2(cont.x + btn.x, cont.y + btn.y); }

    /** Shows the rebind dialog used to reassign the mask of the keybind. */
    public void rebindMask(Keybind bind) {
        new BaseDialog("") {{
            bottom().left().clearChildren();
            closeOnBack();

            for (int i = 0; i < Keymask.all.length; i++) {
                int j = i;

                button(b -> {
                    b.add(Keymask.names[j]).color(Pal.accent).size(256f, 48f).labelAlign(Align.center);
                    b.translation = get(mask, bind).sub(256f * j, (48f + 8f) * (j - bind.mask()));
                }, Style.tbt, () -> {
                    bind.rebind(j);
                    bind.save();
                    hide();
                }).size(256f, 48f).pad(0f).checked(i == bind.mask());
            }
        }}.show();
    }

    // endregion
    // region actions

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

    // endregion
}
