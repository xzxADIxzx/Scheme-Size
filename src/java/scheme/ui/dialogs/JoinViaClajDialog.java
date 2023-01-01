package scheme.ui.dialogs;

import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.JoinDialog;
import scheme.ClajIntegration;

import static arc.Core.*;
import static mindustry.Vars.*;

public class JoinViaClajDialog extends BaseDialog {

    public String lastLink = "CLaJLink#ip:port";

    public boolean valid;
    public String output;

    public JoinViaClajDialog() {
        super("@join.name");

        cont.table(table -> {
            table.add("@join.link").padRight(5f).left();
            table.field(lastLink, this::setLink).size(550f, 54f).maxTextLength(100).valid(this::setLink);
        }).row();

        cont.label(() -> output).width(550f).left();

        buttons.defaults().size(140f, 60f).pad(4f);
        buttons.button("@cancel", this::hide);
        buttons.button("@ok", () -> {
            try {
                if (player.name.trim().isEmpty()) {
                    ui.showInfo("@noname");
                    return;
                }

                var link = ClajIntegration.parseLink(lastLink);
                ClajIntegration.joinRoom(link.ip(), link.port(), link.key(), () -> {
                    ui.join.hide();
                    hide();
                });

                ui.loadfrag.show("@connecting");
                ui.loadfrag.setButton(() -> {
                    ui.loadfrag.hide();
                    netClient.disconnectQuietly();
                });
            } catch (Throwable ignored) {
                ui.showErrorMessage(ignored.getMessage());
            }
        }).disabled(button -> lastLink.isEmpty() || net.active());

        ui.join.shown(this::fixJoinDialog);
    }

    public boolean setLink(String link) {
        if (lastLink.equals(link)) return valid;

        try {
            ClajIntegration.parseLink(link);

            output = "@join.valid";
            valid = true;
        } catch (Throwable ignored) {
            output = ignored.getMessage();
            valid = false;
        }

        lastLink = link;
        return valid;
    }

    private void fixJoinDialog() {
        var root = (Table) ui.join.getChildren().get(1);
        var add = (TextButton) root.getChildren().get(2);

        add.remove();
        root.getCells().remove(2);

        root.row();
        root.table(table -> {
            table.defaults().height(80f).growX();

            table.button("@server.add", Icon.add, () -> {}).padRight(8f).get().addListener(add.getListeners().peek()); // add click listener from orig button
            table.button("@join.name", Icon.play, this::show);
        }).width(targetWidth() + 38f).marginLeft(7f);
    }

    /** Copy-paste from {@link JoinDialog}. Who knows what it is for. */
    private float targetWidth() {
        return Math.min(graphics.getWidth() / Scl.scl() * .9f, 550f);
    }
}
