package schema.ui.dialogs;

import arc.input.*;
import arc.math.geom.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import schema.*;
import schema.input.*;
import schema.ui.*;

/// Dialog that displays the list of keybinds.
public class KeybindDialog extends BaseDialog
{
    /// Buttons used for reassigning keybinds.
    private Button[] mask = new Button[Keybind.all.length], keys = new Button[Keybind.all.length];

    public KeybindDialog()
    {
        super("@keybind.name");

        addCloseButton();
        addButton("@keybind.reset-all", Icon.refresh, 384f, this::reset);

        hidden(this::resolve);

        cont.pane(Style.scr, pane ->
        {
            pane.margin(0f, 0f, -4f, 4f);
            pane.defaults().pad(4f);

            for (var bind : Keybind.all)
            {
                if (bind.category != null) pane.table(t ->
                {
                    t.add("@category." + bind.category, Pal.lightishGray).row();
                    t.image().growX().height(4f).padTop(4f).color(Pal.lightishGray);
                }
                ).fillX().colspan(5).row();

                pane.add("@keybind." + bind).left();

                pane.button(b -> set(mask, bind, b).label(bind::formatMask).color(Pal.accent), Style.cbe, () -> rebindMask(bind)).size(256f, 48f).visible(bind::single);
                pane.button(b -> set(keys, bind, b).label(bind::formatKeys).color(Pal.accent), Style.cbe, () -> rebindKeys(bind)).size(256f, 48f);

                pane.button(Icon.rotate, Style.ibd, bind::reset).size(48f).tooltip("@keybind.reset");
                pane.button(Icon.cancel, Style.ibd, bind::clear).size(48f).tooltip("@keybind.clear").row();
            }
        });
    }

    // region rebinding

    private Button set(Button[] arr, Keybind bind, Button btn) { return arr[bind.ordinal()] = btn; }

    private Vec2 get(Button[] arr, Keybind bind) { return arr[bind.ordinal()].localToStageCoordinates(new Vec2()); }

    /// Shows the rebind dialog used to reassign the mask of the keybind.
    public void rebindMask(Keybind bind)
    {
        new BaseDialog("")
        {{
            bottom().left().clearChildren();
            closeOnBack();

            for (int i = 0; i < Keymask.all.length; i++)
            {
                int j = i;
                button(b ->
                {
                    b.add(Keymask.names[j]).color(Pal.accent).size(256f, 48f).labelAlign(Align.center);
                    b.translation = get(mask, bind).sub(256f * j, (48f + 8f) * (j - bind.mask()));
                },
                Style.tbt, () ->
                {
                    bind.rebind(j);
                    bind.save();
                    hide();
                }
                ).size(256f, 48f).pad(0f).checked(i == bind.mask());
            }
        }}.show();
    }

    /// Shows the rebind dialog used to reassign the keys of the keybind.
    public void rebindKeys(Keybind bind)
    {
        new BaseDialog("")
        {{
            bottom().left().clearChildren();
            closeOnBack();

            add("@keybind.press").color(Pal.accent).size(256f, 48f).pad(0f).labelAlign(Align.center).get().translation = get(keys, bind);
            addListener(new InputListener()
            {
                /// Keycode to be assigned as the minimum value of an axis keybind.
                private KeyCode min = KeyCode.unset;

                /// Main logic of rebinding.
                private void rebind(KeyCode key)
                {
                    if (bind.single())
                    {
                        bind.rebind(key);
                        bind.save();
                        hide();
                    }
                    else if (min == KeyCode.unset)
                    {
                        min = key;
                        Sounds.uiBack.play(16f);
                    }
                    else
                    {
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

    /// Loads the values of the keybinds.
    public void load()
    {
        for (var bind : Keybind.all) bind.load();
        Tools.log("[green] < Loaded [accent]" + Keybind.all.length + "[] keybinds");
    }

    /// Resets the values of the keybinds.
    public void reset()
    {
        for (var bind : Keybind.all) bind.reset();
        Tools.log("[green] < Reset all keybinds");
    }

    /// Resolves conflicts of the keybinds.
    public void resolve()
    {
        int count = 0;
        for (var bind : Keybind.all) count += bind.resolveConflicts();
        Tools.log("[green] < Resolved [accent]" + count + "[] conflicts");

        Seq // extremely invasive, yet efficient
            .with(Binding.menu, Binding.pause, Binding.fullscreen, Binding.screenshot)
            .each(b -> b.value = new KeyBind.Axis(KeyCode.unset));
    }

    // endregion
}
