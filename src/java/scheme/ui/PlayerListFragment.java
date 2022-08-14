package scheme.ui;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.InputEvent;
import arc.scene.event.Touchable;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.TextField;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import mindustry.input.DesktopInput;
import mindustry.net.Packets.AdminAction;
import mindustry.ui.Styles;
import scheme.ServerIntegration;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

/** Last update - Mar 11, 2022 */
public class PlayerListFragment extends mindustry.ui.fragments.PlayerListFragment {

    /** Did you forget to update the index? */
    public static final int index = settings.getBool("mobilebuttons") || mobile ? 15 : 14;

    public boolean show;
    public TextField search;

    @Override
    public void build(Group parent) {
        super.build(parent);
        ui.hudGroup.getChildren().remove(11);

        search = getSearch();
        Table pane = getPane();
        Table menu = getMenu();

        pane.row();
        pane.check("@players.show", value -> show = value).pad(10f).left().get().setTranslation(0f, Scl.scl(60f));
        menu.setTranslation(0f, Scl.scl(-50f));
        menu.getCells().get(1).padLeft(12f).padRight(12f);
    }

    @Override
    public void rebuild() {
        if (TooltipLocker.locked) return; // tooltips may bug during rebuild

        content.clear();
        content.marginBottom(5f);

        if (search == null) return;
        float h = 74f, bs = h / 2f;

        Seq<Player> players = new Seq<>();
        Groups.player.copy(players);

        players.sort(Structs.comps(Structs.comparing(Player::team), Structs.comparingBool(p -> !p.admin)));
        if (search.getText().length() > 0) players.filter(p -> Strings.stripColors(p.name().toLowerCase()).contains(search.getText().toLowerCase()));

        if (players.isEmpty()) content.add(bundle.format("players.notfound")).padBottom(6).width(350f).maxHeight(h + 14);
        else for (Player user : players) {
            if (user.con == null && net.server() && !user.isLocal()) return;
            boolean mod = ServerIntegration.SSUsers.containsKey(user.id);

            Table button = new Table();
            button.left();
            button.margin(5).marginBottom(10);

            Table table = new Table() {
                @Override
                public void draw() {
                    super.draw();
                    Draw.color(mod ? Pal.accent : Pal.gray, parentAlpha);
                    Lines.stroke(Scl.scl(4f));
                    Lines.rect(x, y, width, height);
                    Draw.reset();
                }
            };
            table.margin(8);
            table.add(new Image(user.icon()).setScaling(Scaling.bounded)).grow();
            table.addListener(new TooltipLocker(user.id));

            button.add(table).size(h);
            button.labelWrap(user.coloredName()).width(170f).pad(10);
            button.add().grow();

            ImageButtonStyle style = new ImageButtonStyle() {{
                down = up = Styles.none;
                imageCheckedColor = Pal.accent;
                imageDownColor = Pal.accent;
                imageUpColor = Color.white;
                imageOverColor = Color.lightGray;
            }};

            ImageButtonStyle ustyle = new ImageButtonStyle() {{
                down = up = Styles.none;
                imageDownColor = Pal.accent;
                imageUpColor = Color.white;
                imageOverColor = Color.lightGray;
            }};

            if (!user.isLocal()) {
                button.add().growY();
                button.table(t -> {
                    t.defaults().size(bs);

                    t.button(Icon.logic, ustyle, () -> ai.gotoppl(user));
                    t.button(Icon.copy, ustyle, () -> {
                        app.setClipboardText(user.coloredName());
                        ui.showInfoFade("@copied");
                    });

                    t.row();

                    t.button(Icon.eyeSmall, ustyle, () -> {
                        camera.position.set(user.x, user.y);
                        if (m_input instanceof DesktopInput di) di.panning = true;
                    });
                    t.button(atlas.drawable("status-blasted"), ustyle, () -> admins.despawn(user));
                }).padRight(12f).padLeft(16f).size(bs + 10f, bs);
            }

            if (user.admin && !(!user.isLocal() && net.server())) button.image(Icon.admin).size(h);

            if ((net.server() || player.admin) && !user.isLocal() && (!user.admin || net.server())) {
                button.add().growY();
                button.table(t -> {
                    t.defaults().size(bs);

                    t.button(Icon.hammer, ustyle,
                    () -> ui.showConfirm("@confirm", bundle.format("confirmban",  user.name()), () -> Call.adminRequest(user, AdminAction.ban)));
                    t.button(Icon.cancel, ustyle,
                    () -> ui.showConfirm("@confirm", bundle.format("confirmkick",  user.name()), () -> Call.adminRequest(user, AdminAction.kick)));

                    t.row();

                    t.button(Icon.admin, style, () -> {
                        if (net.client()) return;
                        if (user.admin) ui.showConfirm("@confirm", bundle.format("confirmunadmin", user.name()), () -> {
                            netServer.admins.unAdminPlayer(user.uuid());
                            user.admin = false;
                        });
                        else ui.showConfirm("@confirm", bundle.format("confirmadmin", user.name()), () -> {
                            netServer.admins.adminPlayer(user.uuid(), user.usid());
                            user.admin = true;
                        });
                    }).update(b -> b.setChecked(user.admin))
                        .disabled(b -> net.client())
                        .touchable(() -> net.client() ? Touchable.disabled : Touchable.enabled)
                        .checked(user.admin);

                    t.button(Icon.zoom, ustyle, () -> Call.adminRequest(user, AdminAction.trace));
                }).padRight(12f).padLeft(16f).size(bs + 10f, bs);
            } else if (!user.isLocal() && !user.admin && net.client() && Groups.player.size() >= 3 && player.team() == user.team()) {
                button.add().growY();
                button.button(Icon.hammer, ustyle, () -> {
                    ui.showConfirm("@confirm", bundle.format("confirmvotekick", user.name()), () -> Call.sendChatMessage("/votekick " + user.name()));
                }).size(h);
            }

            content.add(button).padBottom(-6).width(350f + h).maxHeight(h + 14f);
            content.row();
            content.image().height(4f).color(state.rules.pvp || show ? user.team().color : Pal.gray).growX();
            content.row();
        }
    }

    @Override
    public void toggle() {
        super.toggle();
        ServerIntegration.fetch();
    }

    private Table getPane() {
        return ((Table) ((Table) ui.hudGroup.getChildren().get(index)).getChildren().get(0));
    }

    private TextField getSearch() {
        return (TextField) getPane().getChildren().get(1);
    }

    private Table getMenu() {
        return (Table) getPane().getChildren().get(3);
    }

    public static class TooltipLocker extends Tooltip {

        public static boolean locked;

        public TooltipLocker(int id) {
            super(table -> table.background(Styles.black6).margin(4f).add(ServerIntegration.tooltip(id)));
        }

        public TooltipLocker(String text) {
            super(table -> table.background(Styles.black6).margin(4f).add(text));
            allowMobile = true; // why is it false by default?
        }

        @Override
        public void show(Element element, float x, float y) {
            super.show(element, x, y);
            locked = true;
        }

        @Override
        public void hide() {
            super.hide();
            locked = false;
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Element toActor) {
            if (toActor != null && toActor.isDescendantOf(event.listenerActor)) return;
            hide(); // hide on exit even if on mobile
        }
    }
}