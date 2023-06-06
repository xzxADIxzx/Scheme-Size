package scheme.ui;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.*;
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
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.net.Packets.AdminAction;
import mindustry.ui.Styles;
import scheme.ServerIntegration;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

/** Last update - Sep 15, 2022 */
public class PlayerListFragment extends mindustry.ui.fragments.PlayerListFragment {

    public boolean show;
    public TextField search;

    @Override
    public void build(Group parent) {
        super.build(parent);
        ui.hudGroup.getChildren().remove(11);

        search = getSearch();
        Table pane = getPane(), menu = getMenu();

        pane.row();
        pane.check("@trace.type.show", value -> show = value).pad(10f).padBottom(0f).left().row();
        menu.getCells().get(1).padLeft(10f).padRight(10f); // looks better

        pane.getCells().insert(3, pane.getCells().remove(4));
        pane.row(); // idk why but it crashes without it
    }

    @Override
    public void rebuild() {
        if (TooltipLocker.locked || search == null) return; // tooltips may bug during rebuild

        content.clear();
        float h = 50f, bs = h / 2f;

        Seq<Player> players = new Seq<>();
        Groups.player.copy(players);

        players.sort(Structs.comps(Structs.comparing(Player::team), Structs.comparingBool(p -> !p.admin)));
        if (search.getText().length() > 0) players.filter(p -> Strings.stripColors(p.name().toLowerCase()).contains(search.getText().toLowerCase()));

        if (players.isEmpty()) content.add("@players.notfound").padBottom(6f).width(350f).maxHeight(h + 14);
        else for (Player user : players) {
            if (user.con == null && net.server() && !user.isLocal()) return;
            ClickListener listener = new ClickListener();

            Table table = new Table() {
                @Override
                public void draw() {
                    super.draw();
                    Draw.colorMul(user.team().color, listener.isOver() ? 1.3f : 1f);
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(4f));
                    Lines.rect(x, y, width, height);
                    Draw.reset();
                }
            };

            table.margin(8f);
            table.add(new Image(user.icon()).setScaling(Scaling.bounded)).grow();
            table.touchable = Touchable.enabled;

            table.addListener(listener); // viewing players is always available
            table.addListener(new HandCursorListener());
            table.addListener(new TooltipLocker(user.id));

            table.tapped(() -> {
                if (user.dead()) return;

                m_input.observe(user);
                ui.showInfoFade(bundle.format("viewplayer", user.name), 1f);
            });

            Table button = new Table();
            button.left().margin(5f).marginBottom(10f);
            button.background(show && ServerIntegration.isModded(user.id) ? Tex.underlineOver : Tex.underline);

            button.add(table).size(h);
            button.labelWrap(user.coloredName()).style(Styles.outlineLabel).width(170f).pad(10f);
            button.add().grow();

            var style = new ImageButtonStyle() {{
                down = up = Styles.none;
                imageCheckedColor = Pal.accent;
                imageDownColor = Pal.accent;
                imageUpColor = Color.white;
                imageOverColor = Color.lightGray;
            }};

            var ustyle = new ImageButtonStyle() {{
                down = up = Styles.none;
                imageDownColor = Pal.accent;
                imageUpColor = Color.white;
                imageOverColor = Color.lightGray;
            }};

            if (!user.isLocal()) {
                button.add().growY();
                button.table(t -> {
                    t.defaults().size(35f, h);

                    t.button(Icon.logic, ustyle, () -> ai.gotoppl(user));
                    t.button(Icon.copy, ustyle, () -> {
                        app.setClipboardText(user.coloredName());
                        ui.showInfoFade("@copied");
                    });
                    t.button(atlas.drawable("status-blasted"), ustyle, () -> admins.despawn(user));
                }).padRight(12f).size(105f, h);
            }

            if (user.admin && !(!user.isLocal() && net.server())) button.image(Icon.admin).size(h);

            if ((net.server() || player.admin) && !user.isLocal() && (!user.admin || net.server())) {
                button.add().growY();
                button.table(t -> {
                    t.defaults().size(bs);

                    t.button(Icon.hammerSmall, ustyle,
                    () -> ui.showConfirm("@confirm", bundle.format("confirmban",  user.name()), () -> Call.adminRequest(user, AdminAction.ban)));
                    t.button(Icon.cancelSmall, ustyle,
                    () -> ui.showConfirm("@confirm", bundle.format("confirmkick",  user.name()), () -> Call.adminRequest(user, AdminAction.kick)));

                    t.row();

                    t.button(Icon.adminSmall, style, () -> {
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

                    t.button(Icon.zoomSmall, ustyle, () -> Call.adminRequest(user, AdminAction.trace));

                }).padRight(12f).size(bs + 10f, bs);
            } else if (!user.isLocal() && !user.admin && net.client() && Groups.player.size() >= 3 && player.team() == user.team()) {
                button.add().growY();
                button.button(Icon.hammer, ustyle,
                        () -> ui.showTextInput("@votekick.reason", bundle.format("votekick.reason.message", user.name()), "",
                        reason -> Call.sendChatMessage("/votekick #" + user.id + " " + reason)))
                .size(h);
            }

            content.add(button).width(350f + 117f).height(h + 14f);
            content.row();
        }
    }

    private Table getPane() {
        return ((Table) ((Table) ui.hudGroup.find("playerlist")).getChildren().get(0));
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
            this(ServerIntegration.tooltip(id));
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