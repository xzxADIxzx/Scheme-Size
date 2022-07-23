package scheme.ui;

import arc.scene.Group;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;

import static mindustry.Vars.*;

public class PlayerListFragment extends mindustry.ui.fragments.PlayerListFragment {

    public boolean show;

    @Override
    public void build(Group parent) {
        super.build(parent);
        ui.hudGroup.getChildren().remove(11);
        Table pane = getPane();

        pane.row();
        pane.check("@players.show", value -> show = value).left().get().setTranslation(0f, Scl.scl(-50f));
        pane.getChildren().get(2).setTranslation(0f, Scl.scl(50f));
    }

    private Table getPane() {
        return ((Table) ((Table) ui.hudGroup.getChildren().get(14)).getChildren().get(0));
    }
}

// /**
//     if(!user.isLocal()){ // это кнопки, куда ставить я уже не помню
//         button.add().growY();
//         button.table(t -> {
//             t.defaults().size(bs);

//             t.button(Icon.logic, ustyle, () -> ai.gotoppl(user)); // это включает ии гаммы и настраивает на игрока

//             t.button(Icon.copy, ustyle, () -> {
//                 Core.app.setClipboardText(name);
//                 ui.showInfoFade("@copied"); // копирует ник
//             });

//             t.row();

//             t.button(Icon.eyeSmall, ustyle, () -> { // это телепортирует камеру к игрку
//                 Core.camera.position.set(user.x, user.y);
//                 if(m_input instanceof ModDesktopInput di) di.panning = true;
//                 else m_input.toggleFreePan();
//             });

//             t.button(Core.atlas.drawable("status-blasted"), ustyle, () -> SchemeUtils.kill(user)); // это убивает игока
//             // сами onClicked не делай, их надо переписать
//         }).padRight(12).padLeft(16).size(bs + 10f, bs); // просто для размышления Vars.ui.hudGroup.getChildren().get(хз).getChildren и т.д.
//     }

//     pane.table(menu -> { // а вот сюда я вставил один единственный check
//         menu.defaults().height(50f);
//         menu.name = "menu";

//         menu.check("@playerlist.alwaysshow", s -> show = s).left().row(); // вот он
//         menu.table(submenu -> {
//             submenu.defaults().growX().height(50f).fillY();
//             submenu.name = "submenu";

//             submenu.button("@server.bans", ui.bans::show).disabled(b -> net.client());
//             submenu.button("@server.admins", ui.admins::show).disabled(b -> net.client()).padLeft(12f).padRight(12f);
//             submenu.button("@close", this::toggle); // эти кнопки уже есть в минде
//         }).growX();
//     }).margin(0f).pad(10f).growX();
// */