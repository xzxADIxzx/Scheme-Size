package scheme.ui;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.net.Packets.AdminAction;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import scheme.ServerIntegration;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

/** Last update - Jun 12, 2023 */
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
        float h = 50f;

        Seq<Player> players = new Seq<>();
        Groups.player.copy(players);

        players.sort(Structs.comps(Structs.comparing(Player::team), Structs.comparingBool(p -> !p.admin)));
        if (search.getText().length() > 0) players.removeAll(p -> !Strings.stripColors(p.name().toLowerCase()).contains(search.getText().toLowerCase()));

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

            if (user.admin) button.image(Icon.admin).size(h);

            if (net.server() || (player.admin && (!user.admin || user == player))) {
                button.add().growY();
                button.button(Icon.menu, ustyle, () -> {
                    var dialog = new BaseDialog(user.coloredName());
                    dialog.setFillParent(false);
                    dialog.title.getStyle().fontColor = Color.white;

                    var btns = dialog.buttons;
                    btns.defaults().size(220f, 55f).pad(4f);

                    if (user != player) {
                        btns.button("@player.ban", Icon.hammer, Styles.defaultt, () -> {
                            ui.showConfirm("@confirm", bundle.format("confirmban", user.name()), () -> Call.adminRequest(user, AdminAction.ban, null));
                            dialog.hide();
                        }).row();

                        btns.button("@player.kick", Icon.cancel, Styles.defaultt, () -> {
                            ui.showConfirm("@confirm", bundle.format("confirmkick", user.name()), () -> Call.adminRequest(user, AdminAction.kick, null));
                            dialog.hide();
                        }).row();

                        btns.button("@player.trace", Icon.zoom, Styles.defaultt, () -> {
                            Call.adminRequest(user, AdminAction.trace, null);
                            dialog.hide();
                        }).row();
                    }

                    btns.button("@player.team", Icon.redo, Styles.defaultt, () -> {
                        dialog.hide();

                        var select = new BaseDialog("@player.team");
                        select.setFillParent(false);

                        int i = 0;
                        for (Team team : Team.baseTeams) {
                            select.cont.button(Tex.whiteui, Styles.clearNoneTogglei, () -> {
                                Call.adminRequest(user, AdminAction.switchTeam, team);
                                select.hide();
                            }).with(b -> {
                                b.getStyle().imageUpColor = team.color;
                                b.getImageCell().size(44f);
                            }).margin(4f).checked(ib -> user.team() == team);

                            if (i++ % 3 == 2) select.cont.row();
                        }

                        select.addCloseButton();
                        select.show();
                    }).row();

                    if (!net.client() && !user.isLocal()) {
                        btns.button("@player.admin", Icon.admin, Styles.togglet, () -> {
                            dialog.hide();

                            if (user.admin) {
                                ui.showConfirm("@confirm", bundle.format("confirmunadmin", user.name()), () -> {
                                    netServer.admins.unAdminPlayer(user.uuid());
                                    user.admin = false;
                                });
                            } else {
                                ui.showConfirm("@confirm", bundle.format("confirmadmin", user.name()), () -> {
                                    netServer.admins.adminPlayer(user.uuid(), user.usid());
                                    user.admin = true;
                                });
                            }
                        }).checked(b -> user.admin).row();
                    }

                    btns.button("@back", Icon.left, dialog::hide);
                    dialog.show();
                }).size(h);
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