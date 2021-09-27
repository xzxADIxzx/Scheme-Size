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
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
// import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.core.GameState.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.net.Packets.*;
import mindustry.type.*;
import mindustry.ui.*;

import static mindustry.Vars.*;

// Last Update - Sep 4, 2021
public class ModHudFragment extends HudFragment{

    private static final float dsize = 65f;

    // public final PlacementFragment blockfrag = new PlacementFragment();
    // public boolean shown = true;

    private ImageButton flip;
    // private CoreItemsDisplay coreItems = new CoreItemsDisplay();

    // private Table lastUnlockTable;
    // private Table lastUnlockLayout;
    // private long lastToast;

    @Override
    public void build(Group parent){
        // parent.children.get(5).clear();

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

            wavesMain.row();

            addInfoTable(wavesMain.table().width(dsize * 5f + 4f).left().get());

            editorMain.name = "editor";
            editorMain.table(Tex.buttonEdge4, t -> {
                //t.margin(0f);
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
    }

    // private void scheduleToast(Runnable run){
    //     long duration = (int)(3.5 * 1000);
    //     long since = Time.timeSinceMillis(lastToast);
    //     if(since > duration){
    //         lastToast = Time.millis();
    //         run.run();
    //     }else{
    //         Time.runTask((duration - since) / 1000f * 60f, run);
    //         lastToast += duration;
    //     }
    // }

    // public void showToast(String text){
    //     showToast(Icon.ok, text);
    // }

    // public void showToast(Drawable icon, String text){
    //     showToast(icon, -1, text);
    // }

    // public void showToast(Drawable icon, float size, String text){
    //     if(state.isMenu()) return;

    //     scheduleToast(() -> {
    //         Sounds.message.play();

    //         Table table = new Table(Tex.button);
    //         table.update(() -> {
    //             if(state.isMenu() || !ui.hudfrag.shown){
    //                 table.remove();
    //             }
    //         });
    //         table.margin(12);
    //         var cell = table.image(icon).pad(3);
    //         if(size > 0) cell.size(size);
    //         table.add(text).wrap().width(280f).get().setAlignment(Align.center, Align.center);
    //         table.pack();

    //         //create container table which will align and move
    //         Table container = Core.scene.table();
    //         container.top().add(table);
    //         container.setTranslation(0, table.getPrefHeight());
    //         container.actions(Actions.translateBy(0, -table.getPrefHeight(), 1f, Interp.fade), Actions.delay(2.5f),
    //         //nesting actions() calls is necessary so the right prefHeight() is used
    //         Actions.run(() -> container.actions(Actions.translateBy(0, table.getPrefHeight(), 1f, Interp.fade), Actions.remove())));
    //     });
    // }

    // /** Show unlock notification for a new recipe. */
    // public void showUnlock(UnlockableContent content){
    //     //some content may not have icons... yet
    //     //also don't play in the tutorial to prevent confusion
    //     if(state.isMenu()) return;

    //     Sounds.message.play();

    //     //if there's currently no unlock notification...
    //     if(lastUnlockTable == null){
    //         scheduleToast(() -> {
    //             Table table = new Table(Tex.button);
    //             table.update(() -> {
    //                 if(state.isMenu()){
    //                     table.remove();
    //                     lastUnlockLayout = null;
    //                     lastUnlockTable = null;
    //                 }
    //             });
    //             table.margin(12);

    //             Table in = new Table();

    //             //create texture stack for displaying
    //             Image image = new Image(content.uiIcon);
    //             image.setScaling(Scaling.fit);

    //             in.add(image).size(8 * 6).pad(2);

    //             //add to table
    //             table.add(in).padRight(8);
    //             table.add("@unlocked");
    //             table.pack();

    //             //create container table which will align and move
    //             Table container = Core.scene.table();
    //             container.top().add(table);
    //             container.setTranslation(0, table.getPrefHeight());
    //             container.actions(Actions.translateBy(0, -table.getPrefHeight(), 1f, Interp.fade), Actions.delay(2.5f),
    //             //nesting actions() calls is necessary so the right prefHeight() is used
    //             Actions.run(() -> container.actions(Actions.translateBy(0, table.getPrefHeight(), 1f, Interp.fade), Actions.run(() -> {
    //                 lastUnlockTable = null;
    //                 lastUnlockLayout = null;
    //             }), Actions.remove())));

    //             lastUnlockTable = container;
    //             lastUnlockLayout = in;
    //         });
    //     }else{
    //         //max column size
    //         int col = 3;
    //         //max amount of elements minus extra 'plus'
    //         int cap = col * col - 1;

    //         //get old elements
    //         Seq<Element> elements = new Seq<>(lastUnlockLayout.getChildren());
    //         int esize = elements.size;

    //         //...if it's already reached the cap, ignore everything
    //         if(esize > cap) return;

    //         //get size of each element
    //         float size = 48f / Math.min(elements.size + 1, col);

    //         lastUnlockLayout.clearChildren();
    //         lastUnlockLayout.defaults().size(size).pad(2);

    //         for(int i = 0; i < esize; i++){
    //             lastUnlockLayout.add(elements.get(i));

    //             if(i % col == col - 1){
    //                 lastUnlockLayout.row();
    //             }
    //         }

    //         //if there's space, add it
    //         if(esize < cap){

    //             Image image = new Image(content.uiIcon);
    //             image.setScaling(Scaling.fit);

    //             lastUnlockLayout.add(image);
    //         }else{ //else, add a specific icon to denote no more space
    //             lastUnlockLayout.image(Icon.add);
    //         }

    //         lastUnlockLayout.pack();
    //     }
    // }

    // public void showLaunch(){
    //     float margin = 30f;

    //     Image image = new Image();
    //     image.color.a = 0f;
    //     image.touchable = Touchable.disabled;
    //     image.setFillParent(true);
    //     image.actions(Actions.delay((coreLandDuration - margin) / 60f), Actions.fadeIn(margin / 60f, Interp.pow2In), Actions.delay(6f / 60f), Actions.remove());
    //     image.update(() -> {
    //         image.toFront();
    //         ui.loadfrag.toFront();
    //         if(state.isMenu()){
    //             image.remove();
    //         }
    //     });
    //     Core.scene.add(image);
    // }

    // public void showLand(){
    //     Image image = new Image();
    //     image.color.a = 1f;
    //     image.touchable = Touchable.disabled;
    //     image.setFillParent(true);
    //     image.actions(Actions.fadeOut(35f / 60f), Actions.remove());
    //     image.update(() -> {
    //         image.toFront();
    //         ui.loadfrag.toFront();
    //         if(state.isMenu()){
    //             image.remove();
    //         }
    //     });
    //     Core.scene.add(image);
    // }

    private void toggleMenus(){
        if(flip != null){
            flip.getStyle().imageUp = shown ? Icon.downOpen : Icon.upOpen;
        }

        shown = !shown;
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
                Draw.color(color);

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

        table.stack(
        new Element(){
            @Override
            public void draw(){
                Draw.color(Pal.darkerGray);
                Fill.poly(x + width/2f, y + height/2f, 6, height / Mathf.sqrt3);
                Draw.reset();
                Drawf.shadow(x + width/2f, y + height/2f, height * 1.13f);
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
                }
            });

            t.add(new SideBar(() -> player.unit().healthf(), () -> true, true)).width(bw).growY().padRight(pad);
            t.image(() -> player.icon()).scaling(Scaling.bounded).grow().maxWidth(54f);
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

}