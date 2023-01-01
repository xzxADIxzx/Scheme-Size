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

    public JoinViaClajDialog() {
        super("@join.name");

        cont.add("@join.link").padRight(5f).left();
        cont.field(lastLink, link -> lastLink = link).size(550f, 54f).maxTextLength(100).get();

        buttons.defaults().size(140f, 60f).pad(4f);
        buttons.button("@cancel", this::hide);
        buttons.button("@ok", () -> {
            try {
                ClajIntegration.joinRoom(lastLink);
            } catch (Throwable ignored) {
                ui.showErrorMessage(ignored.getMessage());
            }
            this.hide();
        }).disabled(button -> lastLink.isEmpty() || net.active());

        ui.join.shown(this::fixJoinDialog);
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
