package scheme.ui.dialogs;

import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.*;

public class ManageRoomsDialog extends BaseDialog {

    public ManageRoomsDialog() {
        super("@manage.name");
        addCloseButton();

        ui.paused.shown(this::fixPausedDialog);
    }

    private void fixPausedDialog() {
        var root = ui.paused.cont;

        root.row();
        root.button("manage.name", Icon.planet, this::show).colspan(2).width(450).row();

        int index = 5;
        if (!state.isCampaign() && !state.isEditor()) index += 2;

        root.getCells().insert(index, root.getCells().remove(index + 1));
    }
}
