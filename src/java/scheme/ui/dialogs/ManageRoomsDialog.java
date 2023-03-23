package scheme.ui.dialogs;

import arc.graphics.Color;
import arc.net.Client;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import scheme.ClajIntegration;
import scheme.Main;
import scheme.ui.FlipButton;

import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

import java.io.IOException;

public class ManageRoomsDialog extends BaseDialog {

    public String serverIP;
    public int serverPort;

    public Table list;
    public TextField field;
    public FlipButton flip;
    public boolean valid;

    public ManageRoomsDialog() {
        super("@manage.name");
        addCloseButton();

        cont.defaults().width(mobile ? 550f : 750f);

        cont.table(list -> {
            list.defaults().growX().padBottom(8f);
            list.update(() -> list.getCells().filter(cell -> cell.get() != null)); // remove closed rooms

            this.list = list;
        }).row();

        cont.table(url -> {
            url.field(clajURLs.first(), this::setURL).maxTextLength(100).valid(this::validURL).with(f -> field = f).growX();
            url.add(flip = new FlipButton()).size(48f).padLeft(8f);
        }).row();

        cont.collapser(list -> {
            clajURLs.each(url -> {
                list.button(url, Styles.cleart, () -> setURL(url)).height(32f).growX().row();
            });
        }, true, () -> flip.fliped).row();

        cont.button("@manage.create", () -> {
            try {
                list.add(new Room()).row();
            } catch (Exception ignored) {
                ui.showErrorMessage(ignored.getMessage());
            }
        }).disabled(b -> list.getChildren().size >= 4 || !valid).row();

        cont.labelWrap("@manage.tooltip").labelAlign(2, 8).padTop(16f).width(400f).get().getStyle().fontColor = Color.lightGray;

        setURL(clajURLs.first());
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

    // region URL

    public void setURL(String url) {
        field.setText(url);

        int semicolon = url.indexOf(':');
        serverIP = url.substring(0, semicolon);
        serverPort = Strings.parseInt(url.substring(semicolon + 1));
    }

    public boolean validURL(String url) {
        return valid = url.contains(":") && Strings.canParseInt(url.substring(url.indexOf(':') + 1));
    }

    // endregion

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
