package scheme.ui.dialogs;

import arc.KeyBinds.KeyBind;
import arc.graphics.Color;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.ui.dialogs.BaseDialog;
import scheme.moded.ModedBinding;

import static arc.Core.*;
import static mindustry.Vars.*;

public class KeybindCombinationsDialog extends BaseDialog {

    /** Key to press to open the admins config dialog. */
    public static final String adminsConfig = "[accent]\uE82C/" + keybind(ModedBinding.adminscfg) + "[]";
    /** Key to press to lock movement. */
    public static final String lockMovement = "[scarlet]\uE88D/" + keybind(ModedBinding.alternative) + " + " + keybind(Binding.pan) + "[]";
    /** Key to press to disable ai. */
    public static final String resetAI = "[accent]\uE80E/" + keybind(ModedBinding.alternative) + " + " + keybind(ModedBinding.toggle_ai);

    public String main;
    public String code;

    public KeybindCombinationsDialog() {
        super("@keycomb.name");
        addCloseButton();

        cont.marginLeft(24f);
    }

    /** do NOT call before moded binding has been initialized */
    public void load() {
        if (mobile) return; // mobiles do not have key bindings

        main = bundle.get("keycomb.main");
        code = keybind(ModedBinding.alternative);

        partition("@category.general.name");

        template("view_comb",  Binding.block_info);
        template("view_sets",  ModedBinding.rendercfg);
        template("reset_ai",   ModedBinding.toggle_ai);
        template("spawn_unit", ModedBinding.manage_unit);
        template("despawn",    Binding.respawn);
        template("teleport",   Binding.mouse_move);
        template("lock_move",  Binding.pan);
        template("schematic",  Binding.schematic_select);

        partition("@category.bt.name");

        template("toggle_bt",  Binding.deselect);
        template("return",     Binding.schematic_menu);
        template("drop",       Binding.rotateplaced);
    }

    private static String keybind(KeyBind bind) {
        return keybinds.get(bind).key.toString();
    }

    private void partition(String title) {
        cont.label(() -> title).color(Color.gray).padTop(10f).row();
        cont.image().color(Color.gray).fillX().height(3f).padBottom(6f).row();
    }

    private void template(String name, KeyBind bind) {
        String key = keybind(bind);
        String sec = bundle.get("keybind." + bind.name() + ".name");

        cont.add("@keycomb." + name, Color.white).left().padRight(20f);
        cont.add("", Pal.accent).left().minWidth(400f).padRight(20f).update(label -> {
            label.setText(label.hasMouse() ? code + " + " + key : main + " + " + sec);
        }).row();
    }
}
