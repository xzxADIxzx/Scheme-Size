package scheme.ui.dialogs;

import arc.graphics.Color;
import arc.net.Client;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import scheme.ClajIntegration;
import scheme.Main;

import static mindustry.Vars.*;

import java.io.IOException;

public class ManageRoomsDialog extends BaseDialog {

    public String serverIP = "darkdustry.net";
    public int serverPort = 3917;

    public ManageRoomsDialog() {
        super("@manage.name");
        addCloseButton();

        cont.table(rooms -> {
            float w = mobile ? 550f : 750f;
            rooms.defaults().width(w);

            Table list = new Table();
            list.defaults().width(w).padBottom(8f);
            list.update(() -> list.getCells().filter(cell -> cell.get() != null));
            rooms.add(list).row();

            rooms.field("darkdustry.net:3917", address -> {
                int semicolon = address.indexOf(':');

                serverIP = address.substring(0, semicolon);
                serverPort = Strings.parseInt(address.substring(semicolon + 1));
            }).maxTextLength(100).valid(address -> address.contains(":")).row();

            rooms.button("@manage.create", () -> {
                try {
                    list.add(new Room()).row();
                } catch (Throwable ignored) {
                    ui.showErrorMessage(ignored.getMessage());
                }
            }).disabled(button -> list.getChildren().size >= 4).padTop(8f);
        }).height(550f).row();

        cont.labelWrap("@manage.tooltip").labelAlign(2, 8).padTop(16f).width(400f).get().getStyle().fontColor = Color.lightGray;

        ui.paused.shown(this::fixPausedDialog);
    }

    private void fixPausedDialog() {
        var root = ui.paused.cont;

        if (mobile) {
            root.row().buttonRow("@manage.name", Icon.planet, this::show).colspan(3).disabled(button -> !net.server());
            return;
        }

        root.row();
        root.button("@manage.name", Icon.planet, this::show).colspan(2).width(450f).disabled(button -> !net.server()).row();

        int index = state.isCampaign() || state.isEditor() ? 5 : 7;
        root.getCells().insert(index, root.getCells().remove(index + 1));
    }

    public class Room extends Table {

        public Client client;
        public String link;

        public Room() throws IOException {
            client = ClajIntegration.createRoom(serverIP, serverPort, link -> this.link = link, this::close);

            table(Tex.underline, cont -> {
                cont.label(() -> link).growX().left().fontScale(.7f).ellipsis(true);
            }).growX();

            table(btns -> {
                btns.defaults().size(48f).padLeft(8f);

                btns.button(Icon.copy, Styles.clearNonei, () -> Main.copy(link));
                btns.button(Icon.cancel, Styles.clearNonei, this::close);
            });
        }

        public void close() {
            client.close();
            remove();
        }
    }
}
