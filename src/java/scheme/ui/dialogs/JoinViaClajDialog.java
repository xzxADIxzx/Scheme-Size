package scheme.ui.dialogs;

import arc.scene.ui.layout.*;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;
import scheme.ClajIntegration;

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

        fixJoinDialog();
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
        var stack = (Stack) ui.join.getChildren().get(1);
        var root = (Table) stack.getChildren().get(1);

        // poor mobile players =<
        boolean infoButton = !steam && !mobile;

        root.button("@join.name", Icon.play, this::show);

        if (infoButton)
            root.getCells().insert(4, root.getCells().remove(6));
        else
            root.getCells().insert(3, root.getCells().remove(4));
    }
}
