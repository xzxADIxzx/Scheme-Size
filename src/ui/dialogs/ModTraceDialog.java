package mindustry.ui.dialogs;

import arc.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.layout.*;
import mindustry.ui.*;
import mindustry.gen.*;
import mindustry.net.Administration.*;

import static mindustry.Vars.*;

// Last Update - Mar 3, 2021
public class ModTraceDialog extends TraceDialog{

    @Override
    public void show(Player player, TraceInfo info){
        cont.clear();

        ImageButtonStyle style = new ImageButtonStyle(){{
            down = Styles.flatDown;
            up = Styles.none;
            over = Styles.flatOver;
        }};

        Table table = new Table(Tex.clear);
        table.margin(14);
        table.defaults().pad(1);

        table.defaults().left();
        table.button(Icon.copy, style, 10f, () -> copy(player.name)).size(24f, 24f).padRight(4f);
        table.add(Core.bundle.format("trace.playername", player.name));
        table.row();
        table.button(Icon.copy, style, 10f, () -> copy(info.ip)).size(24f, 24f).padRight(4f);
        table.add(Core.bundle.format("trace.ip", info.ip));
        table.row();
        table.button(Icon.copy, style, 10f, () -> copy(info.uuid)).size(24f, 24f).padRight(4f);
        table.add(Core.bundle.format("trace.id", info.uuid));
        table.row();
        table.add(Core.bundle.format("trace.modclient", info.modded));
        table.row();
        table.add(Core.bundle.format("trace.mobile", info.mobile));
        table.row();
        table.add(Core.bundle.format("trace.times.joined", info.timesJoined));
        table.row();
        table.add(Core.bundle.format("trace.times.kicked", info.timesKicked));
        table.row();

        table.add().pad(5);
        table.row();

        cont.add(table);

        show();
    }

    private void copy(String content){
        Core.app.setClipboardText(content);
        ui.showInfoFade("@copied");
    }
}