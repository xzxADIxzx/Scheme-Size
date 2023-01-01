package scheme.ui.dialogs;

import arc.graphics.Color;
import arc.net.Client;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import scheme.ClajIntegration;
import scheme.Main;

import static mindustry.Vars.*;

import java.io.IOException;

public class ManageRoomsDialog extends BaseDialog {

    public static final String serverIP = "darkdustry.net";
    public static final int serverPort = 10000;

    public ManageRoomsDialog() {
        super("@manage.name");
        addCloseButton();

        Table list = new Table();
        list.defaults().width(750f).padBottom(8f);

        shown(() -> list.getCells().filter(cell -> cell.get() != null));

        cont.table(rooms -> {
            rooms.defaults().width(750f);

            rooms.add(list).row();
            rooms.button("@manage.create", () -> {
                try {
                    list.add(new Room()).row();
                } catch (Throwable ignored) {
                    ui.showErrorMessage(ignored.getMessage());
                }
            }).disabled(button -> list.getChildren().size >= 8);
        }).height(550f).row();

        cont.labelWrap("@manage.tooltip").labelAlign(2, 8).padTop(16f).width(400f).get().getStyle().fontColor = Color.lightGray;

        ui.paused.shown(this::fixPausedDialog);
    }

    private void fixPausedDialog() {
        var root = ui.paused.cont;

        root.row();
        root.button("@manage.name", Icon.planet, this::show).colspan(2).width(450).disabled(button -> !net.server()).row();

        int index = 5; // TODO mobile index
        if (!state.isCampaign() && !state.isEditor()) index += 2;

        root.getCells().insert(index, root.getCells().remove(index + 1));
    }

    public class Room extends Table {

        public Client client;
        public String link;

        public Room() throws IOException {
            client = ClajIntegration.createRoom(serverIP, serverPort, link -> this.link = link, this::close);

            table(Tex.underline, cont -> {
                cont.label(() -> link).growX().left().fontScale(.7f);
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
