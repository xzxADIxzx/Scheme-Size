package scheme.ui.dialogs;

import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import scheme.moded.ModedBinding;
import scheme.tools.admins.*;

import static arc.Core.*;
import static scheme.SchemeVars.*;

public class AdminsConfigDialog extends BaseDialog {

    public boolean enabled = settings.getBool("adminsenabled", false);
    public int way = settings.getInt("adminsway", 0);

    public AdminsConfigDialog() {
        super("@admins.name");
        addCloseButton();

        hidden(() -> {
            settings.put("adminsenabled", enabled);
            settings.put("adminsway", way);
            admins = getTools();
        });

        new Table(table -> {
            table.touchable = Touchable.disabled;

            Label text = table.labelWrap("").style(Styles.outlineLabel).padLeft(32f).growX().left().get();
            Slider lever = new Slider(0, 1, 1, false);

            lever.moved(value -> text.setText(bundle.format("admins.lever", bundle.get((enabled = value == 1) ? "admins.enabled" : "admins.disabled"))));
            lever.setValue(enabled ? 1 : 0);
            lever.change();

            cont.stack(lever, table).width(320f).row();
        });

        cont.labelWrap("@admins.way").padTop(16f).width(320f).row();
        cont.table(table -> {
            addCheck(table, "@admins.way.internal", 0);
            addCheck(table, "@admins.way.slashjs", 1);
            addCheck(table, "@admins.way.darkdustry", 2);
        }).left().row();
    }

    private void addCheck(Table table, String text, int way) {
        table.check(text + ".name", value -> this.way = way).checked(t -> this.way == way).disabled(t -> !enabled).tooltip(text + ".desc").left().row();
    }

    /** Made static so that it can be accessed before the dialog is created. */
    public static AdminsTools getTools() {
        return new AdminsTools[] {
                new Internal(), new SlashJs(), new Darkdustry()
        }[settings.getInt("adminsway", 0)];
    }

    /** Key to press to open the dialog. */
    public static String keybind() {
        return "([accent]\uE82C/" + keybinds.get(ModedBinding.adminscfg).key.toString() + "[])";
    }
}
