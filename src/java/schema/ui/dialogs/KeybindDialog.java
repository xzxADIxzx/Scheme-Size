package schema.ui.dialogs;

import arc.graphics.*;
import arc.input.*;
import arc.math.geom.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import schema.input.*;
import schema.ui.*;

import static arc.Core.*;
import static schema.Main.*;

/** Dialog that displays the list of keybinds. */
public class KeybindDialog extends BaseDialog {

    /** List of vanilla keybinds that conflict with the modded ones. */
    private static final String[] override = { "keybind-default-keyboard-fullscreen-key", "keybind-default-keyboard-screenshot-key" };

    /** Stores buttons used for reassigning keybinds' masks and keys. */
    private Button[] mask = new Button[Keybind.all.length], keys = new Button[Keybind.all.length];

    public KeybindDialog() {
        super("@keybind.name");
        addCloseButton();
        hidden(this::resolve);

        cont.pane(Style.scr, pane -> {
        pane.marginRight(4f);
        pane.defaults().pad(4f);

        for (var bind : Keybind.all) {

            if (bind.category != null) pane.table(t -> {
                t.add("@category." + bind.category, Color.gray).row();
                t.image().growX().height(4f).padTop(4f).color(Color.gray);
            }).fillX().colspan(5).row();

            pane.add("@keybind." + bind).left();

            pane.button(b -> set(mask, bind, b).label(bind::formatMask).color(Pal.accent), Style.cbe, () -> rebindMask(bind)).size(256f, 48f).visible(bind::single);
            pane.button(b -> set(keys, bind, b).label(bind::formatKeys).color(Pal.accent), Style.cbe, () -> rebindKeys(bind)).size(256f, 48f);

            pane.button(Icon.rotate, Style.ibd, bind::reset).size(48f).tooltip("@keybind.reset");
            pane.button(Icon.cancel, Style.ibd, bind::clear).size(48f).tooltip("@keybind.clear").row();
        }
        pane.button("@keybind.reset-all", Style.tbd, this::reset).fillX().height(48f).padBottom(0f).colspan(5);
        });
    }

    // region rebinding

    private Button set(Button[] arr, Keybind bind, Button btn) { return arr[bind.ordinal()] = btn; }

    private Vec2 get(Button[] arr, Keybind bind) { return arr[bind.ordinal()].localToStageCoordinates(new Vec2()); }

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

    /** Shows the rebind dialog used to reassign the keys of the keybind. */
    public void rebindKeys(Keybind bind) {
        new BaseDialog("") {{
            bottom().left().clearChildren();
            closeOnBack();

            add("@keybind.press").color(Pal.accent).size(256f, 48f).pad(0f).labelAlign(Align.center).get().translation = get(keys, bind);
            addListener(new InputListener() {

                /** Keycode to be assigned as the minimum value of an axis keybind. */
                private KeyCode min = KeyCode.unset;

                /** Main logic of rebinding. */
                private void rebind(KeyCode key) {
                    if (bind.single()) {
                        bind.rebind(key);
                        bind.save();
                        hide();
                    } else if (min == KeyCode.unset) {
                        min = key;
                        Sounds.back.play(16f);
                    } else {
                        bind.rebind(min, key);
                        bind.save();
                        hide();
                    }
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode key) { rebind(key); return false; }

                @Override
                public boolean keyDown(InputEvent event, KeyCode key) { rebind(key); return false; }
            });
        }}.show();
    }

    // endregion
    // region actions

    /** Loads the values of the mod keybinds. */
    public void load() {
        for (var bind : Keybind.all) bind.load();
        log("Loaded " + Keybind.all.length + " keybinds");
    }

    /** Resets the values of the mod keybinds. */
    public void reset() {
        for (var bind : Keybind.all) bind.reset();
        log("Reset keybinds");
    }

    /** Resolves conflicts of the mod keybinds. */
    public void resolve() {
        int amount = 0;
        for (var bind : Keybind.all) amount += bind.resolveConflicts();
        log("Resolved " + amount + " conflicts");

        for (var key : override) settings.put(key, KeyCode.unknown.ordinal());
        Reflect.invoke(keybinds, "load");
        for (var key : override) settings.remove(key);
    }

    // endregion
}
