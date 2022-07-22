package mindustry.ui.fragments;

import arc.*;
import arc.util.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.event.*;
import arc.struct.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.ui.*;
import mindustry.gen.*;
import mindustry.net.*;
import mindustry.net.Packets.*;
import mindustry.game.EventType.*;
import mindustry.input.*;
import mindustry.scheme.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;
import static mindustry.scheme.SchemeVars.*;

// Last Update - Oct 21, 2021
public class ModPlayerListFragment extends PlayerListFragment{
    private boolean visible = false;
    private boolean show = false;
    private Interval timer = new Interval();
    private TextField search;
    private Seq<Player> players = new Seq<>();

    @Override
    public void build(Group parent){
        parent.fill(cont -> {
            cont.visible(() -> visible);
            cont.update(() -> {
                if(!(net.active() && state.isGame())){
                    visible = false;
                    return;
                }

                if(visible && timer.get(20)){
                    rebuild();
                    content.pack();
                    content.act(Core.graphics.getDeltaTime());
                    //hacky
                    Core.scene.act(0f);
                }
            });

            cont.table(Tex.buttonTrans, pane -> {
                pane.label(() -> Core.bundle.format(Groups.player.size() == 1 ? "players.single" : "players", Groups.player.size()));
                pane.row();

                search = pane.field(null, text -> rebuild()).grow().pad(8).name("search").maxTextLength(maxNameLength).get();
                search.setMessageText(Core.bundle.get("players.search"));

                pane.row();
                pane.pane(content).grow().scrollX(false);
                pane.row();

                pane.table(menu -> {
                    menu.defaults().height(50f);
                    menu.name = "menu";

                    menu.check("@playerlist.alwaysshow", s -> show = s).left().row();
                    menu.table(submenu -> {
                        submenu.defaults().growX().height(50f).fillY();
                        submenu.name = "submenu";

                        submenu.button("@server.bans", ui.bans::show).disabled(b -> net.client());
                        submenu.button("@server.admins", ui.admins::show).disabled(b -> net.client()).padLeft(12f).padRight(12f);
                        submenu.button("@close", this::toggle);
                    }).growX();
                }).margin(0f).pad(10f).growX();

            }).touchable(Touchable.enabled).margin(14f).minWidth(360f);
        });

        rebuild();

        Events.on(ClientLoadEvent.class, event -> {
            var child = parent.getChildren();
            var table = child.get(12);
            table.clear();
            table.remove();
            var menu = child.get(13);
            menu.remove();
            parent.addChildAt(12, menu);
        });
    }

    @Override
    public void rebuild(){
        content.clear();

        players.clear();
        Groups.player.copy(players);

        players.sort(Structs.comps(Structs.comparing(Player::team), Structs.comparingBool(p -> !p.admin)));
        if(search.getText().length() > 0){
            players.filter(p -> Strings.stripColors(p.name().toLowerCase()).contains(search.getText().toLowerCase()));
        }

        float h = 74f;
        float bs = 37f;

        if(players.isEmpty()){
            content.add(Core.bundle.format("players.notfound")).padBottom(6).width(350f).maxHeight(h + 14);
        }else for(var user : players){
            NetConnection connection = user.con;
            if(connection == null && net.server() && !user.isLocal()) return;

            Table button = new Table();
            button.left();
            button.margin(5).marginBottom(10);

            Table table = new Table(){
                @Override
                public void draw(){
                    super.draw();
                    Draw.color(Pal.gray);
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(4f));
                    Lines.rect(x, y, width, height);
                    Draw.reset();
                }
            };
            table.margin(8);
            table.add(new Image(user.icon()).setScaling(Scaling.bounded)).grow();

            String name = "[#" + user.color().toString().toUpperCase() + "]" + user.name();

            button.add(table).size(h);
            button.labelWrap(name).width(170f).pad(10);
            button.add().grow();

            var style = new ImageButtonStyle(){{
                down = Styles.none;
                up = Styles.none;
                imageCheckedColor = Pal.accent;
                imageDownColor = Pal.accent;
                imageUpColor = Color.white;
                imageOverColor = Color.lightGray;
            }};

            var ustyle = new ImageButtonStyle(){{
                down = Styles.none;
                up = Styles.none;
                imageDownColor = Pal.accent;
                imageUpColor = Color.white;
                imageOverColor = Color.lightGray;
            }};

            if(!user.isLocal()){
                button.add().growY();
                button.table(t -> {
                    t.defaults().size(bs);

                    t.button(Icon.logic, ustyle, () -> ai.gotoppl(user));

                    t.button(Icon.copy, ustyle, () -> {
                        Core.app.setClipboardText(name);
                        ui.showInfoFade("@copied");
                    });

                    t.row();

                    t.button(Icon.eyeSmall, ustyle, () -> {
                        Core.camera.position.set(user.x, user.y);
                        if(m_input instanceof ModDesktopInput di) di.panning = true;
                        else m_input.toggleFreePan();
                    });

                    t.button(Core.atlas.drawable("status-blasted"), ustyle, () -> SchemeUtils.kill(user));

                }).padRight(12).padLeft(16).size(bs + 10f, bs);
            }

            if(user.admin && !(!user.isLocal() && net.server())){
                button.image(Icon.admin).size(h);
            }

            if((net.server() || player.admin) && !user.isLocal() && (!user.admin || net.server())){
                button.add().growY();
                button.table(t -> {
                    t.defaults().size(bs);

                    t.button(Icon.hammer, ustyle,
                    () -> ui.showConfirm("@confirm", Core.bundle.format("confirmban",  user.name()), () -> Call.adminRequest(user, AdminAction.ban)));
                    t.button(Icon.cancel, ustyle,
                    () -> ui.showConfirm("@confirm", Core.bundle.format("confirmkick",  user.name()), () -> Call.adminRequest(user, AdminAction.kick)));

                    t.row();

                    t.button(Icon.admin, style, () -> {
                        if(net.client()) return;

                        String id = user.uuid();

                        if(user.admin){
                            ui.showConfirm("@confirm", Core.bundle.format("confirmunadmin",  user.name()), () -> {
                                netServer.admins.unAdminPlayer(id);
                                user.admin = false;
                            });
                        }else{
                            ui.showConfirm("@confirm", Core.bundle.format("confirmadmin",  user.name()), () -> {
                                netServer.admins.adminPlayer(id, user.usid());
                                user.admin = true;
                            });
                        }
                    }).update(b -> b.setChecked(user.admin))
                        .disabled(b -> net.client())
                        .touchable(() -> net.client() ? Touchable.disabled : Touchable.enabled)
                        .checked(user.admin);

                    t.button(Icon.zoom, ustyle, () -> Call.adminRequest(user, AdminAction.trace));

                }).padRight(12).padLeft(16).size(bs + 10f, bs);
            }else if(!user.isLocal() && !user.admin && net.client() && Groups.player.size() >= 3 && player.team() == user.team()){ //votekick
                button.add().growY();
                button.button(Icon.hammer, ustyle,
                () -> {
                    ui.showConfirm("@confirm", Core.bundle.format("confirmvotekick",  user.name()), () -> {
                        Call.sendChatMessage("/votekick " + user.name());
                    });
                }).size(h);
            }

            content.add(button).padBottom(-6).width(350f + h).maxHeight(h + 14);
            content.row();
            content.image().height(4f).color(state.rules.pvp || show ? user.team().color : Pal.gray).growX();
            content.row();
        }

        content.marginBottom(5);
    }

    @Override
    public void toggle(){
        visible = !visible;
        if(visible){
            rebuild();
        }else{
            Core.scene.setKeyboardFocus(null);
            search.clearText();
        }
    }

}