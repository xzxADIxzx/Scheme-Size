package scheme.ui.dialogs;

import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import mindustry.gen.Call;
import mindustry.gen.ClientSnapshotCallPacket;
import mindustry.ui.dialogs.BaseDialog;
import scheme.tools.admins.*;
import scheme.ui.TextSlider;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class AdminsConfigDialog extends BaseDialog {

    public boolean enabled = settings.getBool("adminsenabled", false);
    public boolean always = settings.getBool("adminsalways", false);
    public boolean strict = settings.getBool("adminsstrict", false);
    public int way = settings.getInt("adminsway", 0);

    public AdminsConfigDialog() {
        super("@admins.name");
        addCloseButton();

        hidden(() -> {
            settings.put("adminsenabled", enabled);
            settings.put("adminsalways", always);
            settings.put("adminsstrict", strict);
            settings.put("adminsway", way);
            admins = getTools();
        });

        new TextSlider(0, 1, 1, enabled ? 1 : 0, value -> {
            return bundle.format("admins.lever", bundle.get((enabled = value == 1) ? "admins.enabled" : "admins.disabled"));
        }).build(cont).width(320f).row();

        cont.labelWrap("@admins.way").padTop(16f).width(320f).row();
        cont.table(table -> {
            addCheck(table, "@admins.way.internal", 0);
            addCheck(table, "@admins.way.slashjs", 1);
            addCheck(table, "@admins.way.darkdustry", 2);
        }).left().row();

        cont.labelWrap("@admins.always").padTop(16f).width(320f).row();
        new TextSlider(0, 1, 1, always ? 1 : 0, value -> {
            return (always = value == 1) ? "@yes" : "@no";
        }).update(slider -> slider.setDisabled(!enabled)).build(cont).width(320f).row();

        cont.labelWrap("@admins.strict").padTop(16f).width(320f).row();
        new TextSlider(0, 1, 1, strict ? 1 : 0, value -> {
            return (strict = value == 1) ? "@yes" : "@no";
        }).update(slider -> slider.setDisabled(net.client())).build(cont).width(320f).row();

        net.handleServer(ClientSnapshotCallPacket.class, (con, snapshot) -> {
            if (strict && con.player != null && !con.player.dead() && !con.kicked) {
                var unit = con.player.unit();

                if (!snapshot.dead && unit.id == snapshot.unitID && !Mathf.within(snapshot.x, snapshot.y, unit.x, unit.y, 112f)) {
                    Call.setPosition(con, unit.x, unit.y); // teleport and correct position when necessary
                    return;
                }
            }

            snapshot.handleServer(con); // built-in
        });
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
}
