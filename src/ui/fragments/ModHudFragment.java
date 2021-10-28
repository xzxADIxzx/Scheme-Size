package mindustry.ui.fragments;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
// import mindustry.annotations.Annotations.*;
import mindustry.entities.abilities.*;
import mindustry.content.*;
import mindustry.scheme.*;
import mindustry.core.GameState.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.input.BuildingTools.*;
import mindustry.net.Packets.*;
import mindustry.type.*;
import mindustry.ui.*;

import static mindustry.Vars.*;

// Last Update - Oct 12, 2021
public class ModHudFragment extends Fragment{

    private static final float dsize = 65f;
    private ImageButton flip;
    private ImageButton flipMobile;
    private TextField size;
    private float maxShield;
    public boolean shown = true;
    public boolean shownMobile = false;
    public boolean shownBT = false;

    @Override
    public void build(Group parent){
        Events.on(UnitChangeEvent.class, e -> {
            updateShield(player.unit());
        });

        Events.on(ClientLoadEvent.class, e -> {
            var child = parent.getChildren();
            var table = child.get(5);
            table.clear();
            table.remove();
            var menu = child.get(12);
            menu.remove();
            parent.addChildAt(5, menu);
        });

        //menu at top left
        parent.fill(cont -> {
            cont.name = "overlaymarker";
            cont.top().left();

            if(mobile){
                cont.table(select -> {
                    select.name = "mobile buttons";
                    select.left();
                    select.defaults().size(dsize).left();

                    ImageButtonStyle style = Styles.clearTransi;

                    select.button(Icon.menu, style, ui.paused::show).name("menu");
                    flip = select.button(Icon.upOpen, style, this::toggleMenus).get();
                    flip.name = "flip";

                    select.button(Icon.paste, style, ui.schematics::show)
                    .name("schematics");

                    select.button(Icon.pause, style, () -> {
                        if(net.active()){
                            ui.listfrag.toggle();
                        }else{
                            state.set(state.is(State.paused) ? State.playing : State.paused);
                        }
                    }).name("pause").update(i -> {
                        if(net.active()){
                            i.getStyle().imageUp = Icon.players;
                        }else{
                            i.setDisabled(false);
                            i.getStyle().imageUp = state.is(State.paused) ? Icon.play : Icon.pause;
                        }
                    });

                    select.button(Icon.chat, style,() -> {
                        if(net.active() && mobile){
                            if(ui.chatfrag.shown()){
                                ui.chatfrag.hide();
                            }else{
                                ui.chatfrag.toggle();
                            }
                        }else if(state.isCampaign()){
                            ui.research.show();
                        }else{
                            ui.database.show();
                        }
                    }).name("chat").update(i -> {
                        if(net.active() && mobile){
                            i.getStyle().imageUp = Icon.chat;
                        }else if(state.isCampaign()){
                            i.getStyle().imageUp = Icon.tree;
                        }else{
                            i.getStyle().imageUp = Icon.book;
                        }
                    });

                    select.image().color(Pal.gray).width(4f).fillY();
                });

                cont.row();
                cont.image().height(4f).color(Pal.gray).fillX();
                cont.row();
            }

            cont.update(() -> {
                if(Core.input.keyTap(Binding.toggle_menus) && !ui.chatfrag.shown() && !Core.scene.hasDialog() && !(Core.scene.getKeyboardFocus() instanceof TextField)){
                    Core.settings.getBoolOnce("ui-hidden", () -> {
                        ui.announce(Core.bundle.format("showui",  Core.keybinds.get(Binding.toggle_menus).key.toString(), 11));
                    });
                    toggleMenus();
                }
            });

            Table wavesMain, editorMain;

            cont.stack(wavesMain = new Table(), editorMain = new Table()).height(wavesMain.getPrefHeight())
            .name("waves/editor");

            wavesMain.visible(() -> shown && !state.isEditor());
            wavesMain.top().left().name = "waves";

            wavesMain.table(s -> {
                //wave info button with text
                s.add(makeStatusTable()).grow().name("status");

                //table with button to skip wave
                s.button(Icon.play, Styles.righti, 30f, () -> {
                    if(net.client() && player.admin){
                        Call.adminRequest(player, AdminAction.wave);
                    }else{
                        logic.skipWave();
                    }
                }).growY().fillX().right().width(40f).disabled(b -> !canSkipWave()).name("skip");
            }).width(dsize * 5 + 4f).name("statustable");

            if(mobile){
                float bsize = dsize - 1.5f;
                float isize = dsize - 28f;

                ImageButtonStyle style = new ImageButtonStyle(){{
                    up = Tex.wavepane;
                    down = Styles.flatDown;
                    over = Styles.flatOver;
                }};

                wavesMain.row();
                wavesMain.table(select -> {
                    select.defaults().size(bsize).left();

                    Drawable flip = Icon.downOpen;
                    Drawable crtm = Icon.eye;
                    Drawable look = Core.atlas.drawable("status-disarmed");
                    Drawable tele = Core.atlas.drawable("status-overdrive");
                    Drawable port = Icon.lock;

                    flipMobile = select.button(flip, style, this::toggleMobile).get();
                    flipMobile.name = "flip";

                    select.button(crtm, style, isize - 12f, SchemeUtils::toggleCoreItems).name("crtm");
                    select.button(look, style, isize, SchemeSize.input::toggleMobileDisWpn).name("look");
                    select.button(tele, style, isize, () -> SchemeUtils.teleport(Core.camera.position)).name("tele");
                    select.button(port, style, isize, SchemeSize.input::toggleMobilePanCam).name("port").get(
                    ).image().color(Pal.gray).width(4).height(bsize).padRight(-dsize + 1.5f + isize);
                }).left().name("mod buttons").row();

                wavesMain.table(select -> {
                    select.defaults().size(bsize).left();

                    Drawable core = Icon.effect;
                    Drawable team = Core.atlas.drawable("team-derelict");
                    Drawable kill = Core.atlas.drawable("status-blasted");
                    Drawable hist = Icon.info;

                    select.button(core, style, isize, SchemeUtils::placeCore).name("core");
                    select.button(team, style, isize, SchemeUtils::changeTeam).name("team");
                    select.button(kill, style, isize, SchemeUtils::selfDest).name("kill");
                    select.button(hist, style, isize, SchemeUtils::history).name("hist").get(
                    ).image().color(Pal.gray).width(4).height(bsize).padRight(-dsize + 1.5f + isize);
                }).left().name("mod buttons").visible(() -> shownMobile).row();

                wavesMain.table(select -> {
                    select.defaults().size(bsize).left();

                    Drawable unit = Icon.units;
                    Drawable effe = Core.atlas.drawable("status-corroded");
                    Drawable item = Icon.production;
                    Drawable spwn = Icon.add;

                    select.button(unit, style, isize, SchemeUtils::changeUnit).name("unit");
                    select.button(effe, style, isize, SchemeUtils::changeEffect).name("effe");
                    select.button(item, style, isize, SchemeUtils::changeItem).name("item");
                    select.button(spwn, style, isize, SchemeUtils::spawnUnit).name("spwn").get(
                    ).image().color(Pal.gray).width(4).height(bsize).padRight(-dsize + 1.5f + isize);
                }).left().name("mod buttons").visible(() -> shownMobile).row();
            }

            wavesMain.row();

            addInfoTable(wavesMain.table().width(dsize * 5f + 4f).left().get());

            editorMain.name = "editor";
            editorMain.table(Tex.buttonEdge4, t -> {
                t.name = "teams";
                t.add("@editor.teams").growX().left();
                t.row();
                t.table(teams -> {
                    teams.left();
                    int i = 0;
                    for(Team team : Team.baseTeams){
                        ImageButton button = teams.button(Tex.whiteui, Styles.clearTogglePartiali, 40f, () -> Call.setPlayerTeamEditor(player, team))
                        .size(50f).margin(6f).get();
                        button.getImageCell().grow();
                        button.getStyle().imageUpColor = team.color;
                        button.update(() -> button.setChecked(player.team() == team));

                        if(++i % 3 == 0){
                            teams.row();
                        }
                    }
                }).left();
            }).width(dsize * 5 + 4f);
            editorMain.visible(() -> shown && state.isEditor());

            //fps display
            cont.table(info -> {
                info.name = "fps/ping";
                info.touchable = Touchable.disabled;
                info.top().left().margin(4).visible(() -> Core.settings.getBool("fps") && shown);
                IntFormat fps = new IntFormat("fps");
                IntFormat ping = new IntFormat("ping");
                IntFormat tps = new IntFormat("tps");
                IntFormat mem = new IntFormat("memory");
                IntFormat memnative = new IntFormat("memory2");

                info.label(() -> fps.get(Core.graphics.getFramesPerSecond())).left().style(Styles.outlineLabel).name("fps");
                info.row();

                if(android){
                    info.label(() -> memnative.get((int)(Core.app.getJavaHeap() / 1024 / 1024), (int)(Core.app.getNativeHeap() / 1024 / 1024))).left().style(Styles.outlineLabel).name("memory2");
                }else{
                    info.label(() -> mem.get((int)(Core.app.getJavaHeap() / 1024 / 1024))).left().style(Styles.outlineLabel).name("memory");
                }
                info.row();

                info.label(() -> ping.get(netClient.getPing())).visible(net::client).left().style(Styles.outlineLabel).name("ping").row();
                info.label(() -> tps.get(state.serverTps == -1 ? 60 : state.serverTps)).visible(net::client).left().style(Styles.outlineLabel).name("tps").row();

            }).top().left();
        });

        // building tools
        parent.fill(cont -> {
            cont.name = "buildingtools";
            cont.bottom().right();

            float bsize = 46f;
            BuildingTools bt = SchemeSize.input.bt;
            var block = ((Group)parent.getChildren().get(7)).getChildren().get(0);

            ImageButtonStyle style = new ImageButtonStyle(){{
                down = Styles.flatDown;
                up = Styles.none;
                over = Styles.flatOver;
            }};

            ImageButtonStyle check = new ImageButtonStyle(){{
                down = Styles.flatDown;
                checked = Styles.flatDown;
                up = Styles.none;
                over = Styles.flatOver;
            }};

            TextFieldStyle input = new TextFieldStyle(){{
                font = Fonts.def;
                fontColor = Color.white;
                selection = Tex.selection;
                cursor = Tex.cursor;
            }};

            size = new TextField("8", input);
            size.setFilter(TextFieldFilter.digitsOnly);
            size.changed(() -> bt.resize(size.getText()));

            cont.table(Tex.buttonEdge2, pad -> {
                pad.name = "padding";

                pad.table(ctrl -> {
                    ctrl.name = "controls";
                    ctrl.defaults().size(bsize).bottom().right();

                    ctrl.button(Icon.cancel, style, bt.plan::clear).visible(bt::isPlacing).name("cancel").row();
                    ctrl.add(size).row();
                    ctrl.button(Icon.up, style, () -> bt.resize(1)).name("sizeup").row();
                    ctrl.image(Icon.resize).name("resize").row();
                    ctrl.button(Icon.down, style, () -> bt.resize(-1)).name("sizedown").row();
                });

                pad.image().color(Pal.gray).width(4f).pad(4f).fillY();

                pad.table(edit -> {
                    edit.name = "mapeditor";
                    edit.defaults().size(bsize).bottom().right();

                    edit.button(Icon.pencil, style, () -> SchemeSize.tile.select(true, null)).name("select").row();
                    edit.button(Icon.editor, check, () -> bt.setMode(Mode.edit)).checked(t -> bt.mode == Mode.edit).height(bsize * 4).name("edit").row();
                });

                pad.image().color(Pal.gray).width(4f).pad(4f).fillY();

                pad.table(mode -> {
                    mode.name = "modes";
                    mode.defaults().size(bsize).bottom().right();

                    mode.button(Icon.fill, check, () -> bt.setMode(Mode.fill)).checked(t -> bt.mode == Mode.fill).name("fill").row();
                    mode.button(Icon.grid, check, () -> bt.setMode(Mode.square)).checked(t -> bt.mode == Mode.square).name("square").row();
                    mode.button(Icon.commandRally, check, () -> bt.setMode(Mode.circle)).checked(t -> bt.mode == Mode.circle).name("circle").row();
                    mode.button(Icon.defense, check, () -> bt.setMode(Mode.replace)).checked(t -> bt.mode == Mode.replace).name("replace").row();
                    mode.button(Icon.link, check, () -> bt.setMode(Mode.wall)).checked(t -> bt.mode == Mode.wall).name("wall").row();
                }).row();
            }).height(254f).padRight(310f).visible(() -> shownBT && shown && !ui.minimapfrag.shown()).update(t -> t.setTranslation(393 - block.getWidth(), 0));
        });
    }

    public void resize(int amount){
        size.setText(String.valueOf(amount));
    }

    private void toggleMenus(){
        if(flip != null){
            flip.getStyle().imageUp = shown ? Icon.downOpen : Icon.upOpen;
        }

        shown = !shown;
        ui.hudfrag.shown = shown;
    }

    private void toggleMobile(){
        if(flipMobile != null){
            flipMobile.getStyle().imageUp = shownMobile ? Icon.downOpen : Icon.upOpen;
        }

        shownMobile = !shownMobile;
    }

    public void toggleBT(){
        shownBT = !shownBT;
    }

    private Table makeStatusTable(){
        Table table = new Table(Tex.wavepane);

        StringBuilder ibuild = new StringBuilder();

        IntFormat wavef = new IntFormat("wave");
        IntFormat wavefc = new IntFormat("wave.cap");
        IntFormat enemyf = new IntFormat("wave.enemy");
        IntFormat enemiesf = new IntFormat("wave.enemies");
        IntFormat enemycf = new IntFormat("wave.enemycore");
        IntFormat enemycsf = new IntFormat("wave.enemycores");
        IntFormat waitingf = new IntFormat("wave.waiting", i -> {
            ibuild.setLength(0);
            int m = i/60;
            int s = i % 60;
            if(m > 0){
                ibuild.append(m);
                ibuild.append(":");
                if(s < 10){
                    ibuild.append("0");
                }
            }
            ibuild.append(s);
            return ibuild.toString();
        });

        table.touchable = Touchable.enabled;

        StringBuilder builder = new StringBuilder();

        table.name = "waves";

        table.marginTop(0).marginBottom(4).marginLeft(4);

        class SideBar extends Element{
            public final Floatp amount;
            public final boolean flip;
            public final Boolp flash;

            float last, blink, value;

            public SideBar(Floatp amount, Boolp flash, boolean flip){
                this.amount = amount;
                this.flip = flip;
                this.flash = flash;

                setColor(Pal.health);
            }

            @Override
            public void draw(){
                float next = amount.get();

                if(Float.isNaN(next) || Float.isInfinite(next)) next = 1f;

                if(next < last && flash.get()){
                    blink = 1f;
                }

                blink = Mathf.lerpDelta(blink, 0f, 0.2f);
                value = Mathf.lerpDelta(value, next, 0.15f);
                last = next;

                if(Float.isNaN(value) || Float.isInfinite(value)) value = 1f;

                drawInner(Pal.darkishGray, 1f);
                drawInner(Tmp.c1.set(color).lerp(Color.white, blink), value);
            }

            void drawInner(Color color, float fract){
                if(fract < 0) return;

                fract = Mathf.clamp(fract);
                if(flip){
                    x += width;
                    width = -width;
                }

                float stroke = width * 0.35f;
                float bh = height/2f;
                Draw.color(color, parentAlpha);

                float f1 = Math.min(fract * 2f, 1f), f2 = (fract - 0.5f) * 2f;

                float bo = -(1f - f1) * (width - stroke);

                Fill.quad(
                x, y,
                x + stroke, y,
                x + width + bo, y + bh * f1,
                x + width - stroke + bo, y + bh * f1
                );

                if(f2 > 0){
                    float bx = x + (width - stroke) * (1f - f2);
                    Fill.quad(
                    x + width, y + bh,
                    x + width - stroke, y + bh,
                    bx, y + height * fract,
                    bx + stroke, y + height * fract
                    );
                }

                Draw.reset();

                if(flip){
                    width = -width;
                    x -= width;
                }
            }
        }

        class Bar extends Table{
            public final Floatp amount;
            public final Boolp flash;

            float last, blink, value;

            public Bar(Floatp amount, Boolp flash, Cons<Table> cons){
                super(cons);
                this.amount = amount;
                this.flash = flash;

                setColor(Pal.health);
            }

            @Override
            public void draw(){
                float next = amount.get();

                if(Float.isNaN(next) || Float.isInfinite(next)) next = 1f;

                if(next < last && flash.get()){
                    blink = 1f;
                }

                blink = Mathf.lerpDelta(blink, 0f, 0.2f);
                value = Mathf.lerpDelta(value, next, 0.15f);
                last = next;

                if(Float.isNaN(value) || Float.isInfinite(value)) value = 1f;

                drawInner(Tmp.c1.set(color).lerp(Color.white, blink), value);
                Drawf.shadow(x + width/2f, y + height/2f, height * 1.13f, parentAlpha); // bar draw over shadow... so it's look bad
                super.draw();
            }

            public void drawInner(Color color, float fract){
                if(fract < 0) return;
                fract = Mathf.clamp(fract);

                float bh = height/2f;
                Draw.color(color, parentAlpha);

                float f1 = Math.min(fract * 2f, 1f), f2 = (fract - 0.5f) * 2f;
                float stroke = width - (width * 0.35f);
                float dif = stroke * f1;

                Fill.quad(
                x, y,
                x + width, y,
                x + width + dif, y + bh * f1,
                x - dif, y + bh * f1
                );

                if(f2 > 0){
                    float diftop = stroke * f2;
                    Fill.quad(
                    x - dif, y + bh,
                    x + dif + width, y + bh,
                    x - diftop + width + stroke, y + height * fract,
                    x - (stroke - diftop), y + height * fract
                    );
                }

                Draw.reset();
            }
        }

        table.stack(
        new Element(){
            @Override
            public void draw(){
                Draw.color(Pal.darkerGray, parentAlpha);
                Fill.poly(x + width/2f, y + height/2f, 6, height / Mathf.sqrt3);
                Draw.reset();
            }
        },
        new Table(t -> {
            float bw = 40f;
            float pad = -20;
            t.margin(0);
            t.clicked(() -> {
                if(!player.dead() && mobile){
                    Call.unitClear(player);
                    control.input.recentRespawnTimer = 1f;
                    control.input.controlledType = null;

                    SchemeSize.hudfrag.updateShield(player.unit());
                }
            });

            t.add(new SideBar(() -> player.unit().healthf(), () -> true, true)).width(bw).growY().padRight(pad);
            t.add(new Bar(() -> maxShield == -1 ? 0f : player.unit().shield / maxShield, () -> true, b -> {
                b.image(() -> player.icon()).scaling(Scaling.bounded).grow().maxWidth(54f);
            }).marginLeft(-7).marginRight(-7)).scaling(Scaling.bounded).grow().maxWidth(40).update(b -> {
                b.color.set(Pal.accent);
            });
            t.add(new SideBar(() -> player.dead() ? 0f : player.displayAmmo() ? player.unit().ammof() : player.unit().healthf(), () -> !player.displayAmmo(), false)).width(bw).growY().padLeft(pad).update(b -> {
                b.color.set(player.displayAmmo() ? player.dead() || player.unit() instanceof BlockUnitc ? Pal.ammo : player.unit().type.ammoType.color() : Pal.health);
            });

            t.getChildren().get(1).toFront();
        })).size(120f, 80).padRight(4);

        table.labelWrap(() -> {
            builder.setLength(0);

            if(!state.rules.waves && state.rules.attackMode){
                int sum = Math.max(state.teams.present.sum(t -> t.team != player.team() ? t.cores.size : 0), 1);
                builder.append(sum > 1 ? enemycsf.get(sum) : enemycf.get(sum));
                return builder;
            }

            if(!state.rules.waves && state.isCampaign()){
                builder.append("[lightgray]").append(Core.bundle.get("sector.curcapture"));
            }

            if(!state.rules.waves){
                return builder;
            }

            if(state.rules.winWave > 1 && state.rules.winWave >= state.wave && state.isCampaign()){
                builder.append(wavefc.get(state.wave, state.rules.winWave));
            }else{
                builder.append(wavef.get(state.wave));
            }
            builder.append("\n");

            if(state.enemies > 0){
                if(state.enemies == 1){
                    builder.append(enemyf.get(state.enemies));
                }else{
                    builder.append(enemiesf.get(state.enemies));
                }
                builder.append("\n");
            }

            if(state.rules.waveTimer){
                builder.append((logic.isWaitingWave() ? Core.bundle.get("wave.waveInProgress") : (waitingf.get((int)(state.wavetime/60)))));
            }else if(state.enemies == 0){
                builder.append(Core.bundle.get("waiting"));
            }

            return builder;
        }).growX().pad(8f);

        table.row();

        return table;
    }

    private void addInfoTable(Table table){
        table.name = "infotable";
        table.left();

        var count = new float[]{-1};
        table.table().update(t -> {
            if(player.unit() instanceof Payloadc payload){
                if(count[0] != payload.payloadUsed()){
                    payload.contentInfo(t, 8 * 2, 275f);
                    count[0] = payload.payloadUsed();
                }
            }else{
                count[0] = -1;
                t.clear();
            }
        }).growX().visible(() -> player.unit() instanceof Payloadc p && p.payloadUsed() > 0).colspan(2);
        table.row();

        Bits statuses = new Bits();

        table.table().update(t -> {
            t.left();
            Bits applied = player.unit().statusBits();
            if(!statuses.equals(applied)){
                t.clear();

                if(applied != null){
                    for(StatusEffect effect : content.statusEffects()){
                        if(applied.get(effect.id) && !effect.isHidden()){
                            t.image(effect.uiIcon).size(iconMed).get()
                            .addListener(new Tooltip(l -> l.label(() ->
                                effect.localizedName + " [lightgray]" + UI.formatTime(player.unit().getDuration(effect))).style(Styles.outlineLabel)));
                        }
                    }

                    statuses.set(applied);
                }
            }
        }).left();
    }

    private boolean canSkipWave(){
        return state.rules.waves && ((net.server() || player.admin) || !net.active()) && state.enemies == 0 && !spawner.isSpawning();
    }

    public void updateShield(Unit on){
        maxShield = -1;
        on.abilities.each((a) -> {
            maxShield = a instanceof ForceFieldAbility ffa ? ffa.max : maxShield;
        });
    }
}