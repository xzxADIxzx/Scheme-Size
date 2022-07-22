package scheme.ui.dialogs;

import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.net.Administration.TraceInfo;

import static arc.Core.*;
import static mindustry.Vars.*;

public class TraceDialog extends mindustry.ui.dialogs.TraceDialog {

    public TraceInfo last;

    public TraceDialog() {
        super();
        buttons.button("@trace.copy", Icon.copy, () -> {
            ui.showCustomConfirm("@trace.copy", "", "@trace.copy.id", "@trace.copy.ip", () -> copy(last.uuid), () -> copy(last.ip));
        });
    }

    @Override
    public void show(Player player, TraceInfo info) {
        super.show(player, info);
        last = info;
    }

    private void copy(String content) {
        app.setClipboardText(content);
        ui.showInfoFade("@copied");
        hide();
    }
}
