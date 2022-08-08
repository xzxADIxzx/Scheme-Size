package scheme.ui;

import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.TextField;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.TextField.TextFieldFilter;
import arc.scene.ui.TextField.TextFieldStyle;
import arc.scene.ui.layout.*;
import arc.util.Scaling;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import scheme.ai.GammaAI;
import scheme.ai.GammaAI.Updater;
import scheme.tools.BuildingTools.Mode;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;
import static scheme.ai.GammaAI.Updater.*;

public class HudFragment {

    /** Just a short reference to a variable with a long name. */
    public static final ImageButtonStyle style = Styles.clearNonei;
    public static final ImageButtonStyle check = Styles.clearNoneTogglei;
    public static final TextFieldStyle input = new TextFieldStyle() {{
        font = Fonts.def;
        fontColor = Color.white;
        selection = Tex.selection;
        cursor = Tex.cursor;
    }};

    public FlipButton mobiles = new FlipButton();
    public FlipButton building = new FlipButton();

    public PowerBars power = new PowerBars();
    public boolean checked;

    public TextField size;
    public Element block;

    public void build(Group parent) {
        Events.run(WorldLoadEvent.class, power::refreshNode);
        Events.run(BlockBuildEndEvent.class, power::refreshNode);
        Events.run(BlockDestroyEvent.class, power::refreshNode);
        Events.run(ConfigEvent.class, power::refreshNode);

        Events.run(WorldLoadEvent.class, this::updateBlock);
        Events.run(UnlockEvent.class, this::updateBlock);

        parent.fill(cont -> { // Shield Bar
            cont.name = "shieldbar";
            cont.top().left();

            cont.touchable = Touchable.disabled;
            cont.visible(() -> ui.hudfrag.shown && !state.isEditor());

            float dif = Scl.scl() % .5f == 0 ? 0f : 1f; // there are also a lot of magic numbers
            cont.add(new HexBar(() -> units.shield() / units.maxShield, icon -> {
                icon.image(player::icon).scaling(Scaling.bounded).grow().maxWidth(54f);
            })).size(92.2f + dif / 2, 80f).padLeft(18.2f - dif).padTop(mobile ? 69f : 0f);
        });

        getCoreItems().table(cont -> { // Power Bars
            cont.name = "powerbars";
            cont.background(Styles.black6).margin(8f, 8f, 8f, 0f);

            cont.table(bars -> {
                bars.defaults().height(18f).growX();
                bars.add(power.balance()).row();
                bars.add(power.stored()).padTop(8f);
            }).growX();
            cont.button(Icon.edit, check, () -> checked = !checked).checked(t -> checked).size(44f).padLeft(8f);
        }).fillX().visible(() -> settings.getBool("coreitems") && !mobile && ui.hudfrag.shown);

        parent.fill(cont -> { // Gamma UI
            cont.name = "gammaui";
            cont.top().right();

            cont.visible(() -> ai.ai instanceof GammaAI && ui.hudfrag.shown);

            cont.table(Tex.pane, pad -> {
                pad.defaults().growX();

                new TextSlider(40f, 340f, 20f, 80f, value -> bundle.format("gamma.range", GammaAI.range = value)).build(pad).row();
                pad.table(mode -> {
                    setMove(mode, none);
                    setMove(mode, follow);
                    setMove(mode, cursor);
                    setMove(mode, circle);
                }).row();
                pad.table(mode -> {
                    setBuild(mode, help);
                    setBuild(mode, none);
                    setBuild(mode, destroy);
                }).row();
                pad.labelWrap(GammaAI.tooltip).labelAlign(2, 8).pad(8f, 0f, 8f, 0f).width(150f).get().getStyle().fontColor = Color.lightGray;
            }).width(150f).margin(0f).update(pad -> pad.setTranslation(0f, settings.getBool("minimap") ? -Scl.scl(mobile ? 272f : 188f) : 0f)).row();
        });

        parent.fill(cont -> { // Building Tools
            cont.name = "buildingtools";
            cont.bottom().right();

            cont.visible(() -> ui.hudfrag.shown && !ui.minimapfrag.shown() && !control.input.commandMode);

            size = new TextField("8", input);
            size.setFilter(TextFieldFilter.digitsOnly);
            size.changed(() -> build.resize(size.getText()));

            cont.table(Tex.buttonEdge2, pad -> {
                partition(pad, mode -> {
                    mode.button(Icon.cancel, style, () -> {
                        control.input.block = null;
                        build.plan.clear();
                    }).visible(build::isPlacing).row();
                    mode.add(size).row();
                    mode.button(Icon.up, style, () -> build.resize(1)).row();
                    mode.image(Icon.resize).row();
                    mode.button(Icon.down, style, () -> build.resize(-1)).row();
                });

                partition(pad, mode -> {
                    mode.button(Icon.menu, style, tile::show).tooltip("@select.tile").padTop(46f).row();
                    setMode(mode, Icon.pick, Mode.pick);
                    setMode(mode, Icon.pencil, Mode.brush);
                    setMode(mode, Icon.editor, Mode.edit);
                });

                partition(pad, mode -> {
                    mode.button(Icon.redo, style, m_input::flushLastRemoved).tooltip("@keycomb.return").padBottom(46f).row();
                    setMode(mode, Icon.fill, Mode.fill);
                    setMode(mode, Icon.grid, Mode.square);
                    setMode(mode, Icon.commandRally, Mode.circle);
                });

                partition(pad, mode -> {
                    mode.add(building).row();
                    setMode(mode, Icon.upload, Mode.drop);
                    setMode(mode, Icon.link, Mode.replace);
                    setMode(mode, Icon.hammer, Mode.remove);
                    setMode(mode, Icon.power, Mode.connect);
                }).visible(() -> true).update(mode -> mode.setTranslation(Scl.scl(building.fliped ? 0f : -87f), 0f));
            }).height(254f).update(pad -> {
                if (block == null) return; // block is null before the world is loaded
                pad.setTranslation(Scl.scl(building.fliped ? 4f : 178f) - block.getWidth(), 0f);
                pad.setWidth(Scl.scl(building.fliped ? 244f : 70f)); // more magic numbers to the god of magic numbers
            });
        });

        if (!settings.getBool("mobilebuttons") && !mobile) return;

        parent.fill(cont -> { // Mobile Buttons
            cont.name = "mobilebuttons";
            cont.top().left();

            cont.visible(() -> ui.hudfrag.shown && !ui.minimapfrag.shown());

            cont.table(Tex.buttonEdge4, pad -> {
                Cons<Element> translate = element -> element.setTranslation(0f, Scl.scl(mobiles.fliped ? 0f : -62f));
                pad.image().color(Pal.gray).size(320f, 4f).update(translate::get).row();

                partition(pad, mode -> mode.add(mobiles),
                        Icon.admin, (Runnable) () -> adminscfg.show(),
                        mobile ? "disarmed" : Icon.book, mobile ? (Runnable) m_input::lockShooting : (Runnable) keycomb::show,
                        "overdrive", (Runnable) () -> admins.teleport(),
                        Icon.lock, (Runnable) () -> m_input.lockMovement()
                ).visible(() -> true).update(translate::get).row();

                partition(pad, mode -> {},
                        Icon.effect, (Runnable) () -> admins.placeCore(),
                        "boss", (Runnable) () -> admins.manageTeam(),
                        "blasted", (Runnable) () -> admins.despawn(),
                        Icon.logic, (Runnable) () -> ai.select(),
                        Icon.image, (Runnable) () -> rendercfg.show()
                ).row();

                partition(pad, mode -> {},
                        Icon.units, (Runnable) () -> admins.manageUnit(),
                        Icon.add, (Runnable) () -> admins.spawnUnits(),
                        "corroded", (Runnable) () -> admins.manageEffect(),
                        Icon.production, (Runnable) () -> admins.manageItem(),
                        Icon.info, (Runnable) () -> render.toggleHistory()
                ).row();
            }).margin(0f).update(pad -> {
                pad.setTranslation(0f, -Scl.scl((mobile ? 147f : 78f) + (state.isEditor() ? 61f : 0f) - (mobiles.fliped ? 0f : 125f)));
                pad.setHeight(Scl.scl(mobiles.fliped ? 190.5f : 63.5f));
            });
        });

        Table info = getInfoTable();
        info.update(() -> info.setTranslation(0f, -Scl.scl(mobiles.fliped ? 190.5f : 63.5f)));
    }

    public void resize(int amount) {
        size.setText(String.valueOf(amount));
    }

    private Cell<Table> partition(Table table, Cons<Table> cons) {
        if (table.hasChildren()) table.image().color(Pal.gray).width(4f).pad(4f).fillY().visible(() -> building.fliped);
        return table.table(cont -> {
            cont.defaults().size(46f).bottom().right();
            cons.get(cont);
        }).visible(() -> building.fliped);
    }

    private Cell<Table> partition(Table table, Cons<Table> cons, Object... buttons) {
        return table.table(cont -> {
            cont.defaults().size(63.5f).left();
            cons.get(cont); // special for flip button
            for (int i = 0; i < buttons.length; i++) 
                cont.button(buttons[i] instanceof String name ? atlas.drawable("status-" + name) : (Drawable) buttons[i], style, 37f, (Runnable) buttons[++i]);
        }).visible(() -> mobiles.fliped);
    }

    private void setMode(Table table, Drawable icon, Mode mode) {
        table.button(icon, check, () -> build.setMode(mode)).checked(t -> build.mode == mode).row();
    }

    private void setMove(Table table, Updater move) {
        table.button(move.icon, check, () -> {
            GammaAI.move = move;
            ((GammaAI) ai.ai).cache();
        }).checked(t -> GammaAI.move == move).tooltip(move.tooltip()).size(37.5f);
    }

    private void setBuild(Table table, Updater build) {
        table.button(build.icon, check, () -> GammaAI.build = build).checked(t -> GammaAI.build == build).tooltip(build.tooltip()).size(50f);
    }

    private void updateBlock() {
        app.post(() -> { // waiting for blockfrag rebuild
            block = ((Table) ui.hudGroup.getChildren().get(10)).getChildren().get(0);
        });
    }

    private Table getCoreItems() {
        return (Table) ((Table) ui.hudGroup.getChildren().get(5)).getChildren().get(1);
    }

    private Table getInfoTable() {
        return (Table) ((Table) getWavesMain().getChildren().get(0)).getChildren().get(1);
    }

    private Stack getWavesMain() {
        return (Stack) ((Table) ui.hudGroup.getChildren().get(4)).getChildren().get(mobile ? 1 : 0);
    }
}
