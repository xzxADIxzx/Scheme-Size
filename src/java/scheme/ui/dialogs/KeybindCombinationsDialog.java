package scheme.ui.dialogs;

import arc.KeyBinds.KeyBind;
import arc.graphics.Color;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.ui.dialogs.BaseDialog;
import scheme.moded.ModedBinding;

import static arc.Core.*;

public class KeybindCombinationsDialog extends BaseDialog {

    public String main;
    public String code;

    public KeybindCombinationsDialog() {
        super("@keycomb.name");
        addCloseButton();

        cont.marginLeft(24f);
    }

    /** do NOT call before moded binding has been initialized */
    public void load() {
        main = bundle.get("keycomb.main");
        code = keybinds.get(ModedBinding.alternative).key.toString();

        partition("@category.general.name");

        template("view_comb",  Binding.block_info);
        template("view_sets",  ModedBinding.rendercfg);
        template("reset_ai",   ModedBinding.toggle_ai);
        template("spawn_unit", ModedBinding.manage_unit);
        template("despawn",    Binding.respawn);
        template("teleport",   Binding.mouse_move);
        template("lock_move",  Binding.pan);
        // template("schematic",  Binding.schematic_select);

        partition("@category.bt.name");

        template("toggle_bt",  Binding.deselect);
        template("return",     Binding.schematic_menu);
        template("drop",       Binding.rotateplaced);
    }

    private void partition(String title) {
        cont.label(() -> title).color(Color.gray).padTop(10f).row();
        cont.image().color(Color.gray).fillX().height(3f).padBottom(6f).row();
    }

    private void template(String name, KeyBind bind) {
        String key = keybinds.get(bind).key.toString();
        String sec = bundle.get("keybind." + bind.name() + ".name");

        cont.add("@keycomb." + name, Color.white).left().padRight(20f);
        cont.add("", Pal.accent).left().minWidth(360f).padRight(20f).update(label -> {
            label.setText(label.hasMouse() ? code + " + " + key : main + " + " + sec);
        }).row();
    }
}
