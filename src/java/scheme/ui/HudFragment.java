package scheme.ui;

import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextField;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.TextField.TextFieldFilter;
import arc.scene.ui.TextField.TextFieldStyle;
import arc.scene.ui.layout.*;
import arc.util.Align;
import arc.util.Interval;
import arc.util.Scaling;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import scheme.SchemeUpdater;
import scheme.ai.GammaAI;
import scheme.ai.GammaAI.Updater;
import scheme.tools.BuildingTools.Mode;
import scheme.ui.PlayerListFragment.TooltipLocker;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;
import static scheme.ai.GammaAI.Updater.*;

public class HudFragment {

    /** Just a short reference to a variable with a long name. */
    public static final ImageButtonStyle style = Styles.clearNonei, check = Styles.clearNoneTogglei;
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

    /** PlacementFragment and OverlayMarker. */
    public Element[] block = new Element[3];
    public TextField size;
    public Interval timer = new Interval();

    public void build(Group parent) {
        Events.run(WorldLoadEvent.class, power::refreshNode);
        Events.run(BlockBuildEndEvent.class, power::refreshNode);
        Events.run(BlockDestroyEvent.class, power::refreshNode);
        Events.run(ConfigEvent.class, power::refreshNode);

        Events.run(WorldLoadEvent.class, this::updateBlocks);
        Events.run(UnlockEvent.class, this::updateBlocks);

        if (mobile) {
            var button = getSchematicsButton();
            button.getListeners().remove(2);
            button.clicked(ui.schematics::show);
        }

        parent.fill(cont -> { // Shield Bar
            cont.name = "shieldbar";
            cont.top().left();

            cont.touchable = Touchable.disabled;
            cont.visible(() -> ui.hudfrag.shown && !ui.minimapfrag.shown() && !state.isEditor());

            float dif = Scl.scl() % .5f == 0 ? 0f : 1f; // there are also a lot of magic numbers
            cont.add(new HexBar(() -> units.shield() / units.maxShield, icon -> {
                icon.image(player::icon).scaling(Scaling.bounded).grow().maxWidth(54f);
            })).size(92.2f + dif / 2, 80f).padLeft(18.2f - dif).padTop(mobile ? 69f : 0f);
        });

        getCoreItems().collapser(cont -> { // Power Bars
            cont.name = "powerbars";
            cont.background(Styles.black6).margin(8f, 8f, 8f, 0f);

            cont.table(bars -> {
                bars.defaults().height(18f).growX();
                bars.add(power.balance()).row();
                bars.add(power.stored()).padTop(8f);
            }).growX();
            cont.button(Icon.edit, check, () -> checked = !checked).checked(t -> checked).size(44f).padLeft(8f);
        }, () -> settings.getBool("coreitems") && !mobile && ui.hudfrag.shown).fillX().row();

        getCoreItems().collapser(cont -> { // Schematic Layer
            cont.name = "powerbars";
            cont.background(Styles.black6).margin(8f, 8f, 8f, 0f);

            timer.reset(0, 240f);
            cont.label(() -> bundle.format("layer", bundle.get("layer." + m_schematics.layer)));
        }, true, () -> !timer.check(0, 240f) && !mobile && ui.hudfrag.shown).fillX();

        parent.fill(cont -> { // Gamma UI
            cont.name = "gammaui";
            cont.top().right();

            cont.visible(() -> ui.hudfrag.shown && !ui.minimapfrag.shown() && ai.ai instanceof GammaAI);

            cont.table(Tex.pane, pad -> {
                pad.defaults().growX();

                new TextSlider(0f, 600f, 20f, 80f, value -> bundle.format("gamma.range", GammaAI.range = value)).build(pad).row();
                new TextSlider(0f, 110f, 10f, 100f, value -> bundle.format("gamma.speed", GammaAI.speed = value)).build(pad).row();
                pad.table(mode -> {
                    setMove(mode, none);
                    setMove(mode, follow);
                    setMove(mode, cursor);
                    setMove(mode, circle);
                }).row();
                pad.table(mode -> {
                    setBuild(mode, none);
                    setBuild(mode, help);
                    setBuild(mode, destroy);
                    setBuild(mode, repair);
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
                partitionbt(pad, mode -> {
                    mode.button(Icon.cancel, style, () -> {
                        control.input.block = null;
                        build.plan.clear();
                    }).visible(build::isPlacing).row();
                    mode.add(size).row();
                    mode.button(Icon.up, style, () -> build.resize(1)).row();
                    mode.image(Icon.resize).row();
                    mode.button(Icon.down, style, () -> build.resize(-1)).row();
                });

                partitionbt(pad, mode -> {
                    mode.button(Icon.menu, style, tile::show).tooltip("@select.tile").padTop(46f).row();
                    setMode(mode, Icon.pick, Mode.pick);
                    setMode(mode, Icon.pencil, Mode.brush);
                    setMode(mode, Icon.editor, Mode.edit);
                });

                partitionbt(pad, mode -> {
                    mode.button(Icon.redo, style, m_input::flushLastRemoved).tooltip("@keycomb.return").padBottom(46f).row();
                    setMode(mode, Icon.fill, Mode.fill);
                    setMode(mode, Icon.grid, Mode.square);
                    setMode(mode, Icon.commandRally, Mode.circle);
                });

                partitionbt(pad, mode -> {
                    mode.add(building).row();
                    setMode(mode, Icon.upload, Mode.drop);
                    setMode(mode, Icon.link, Mode.replace);
                    setMode(mode, Icon.hammer, Mode.remove);
                    setMode(mode, Icon.power, Mode.connect);
                }).visible(() -> true).update(mode -> mode.setTranslation(Scl.scl(building.fliped ? 0f : -87f), 0f));
            }).height(254f).update(pad -> {
                if (block[0] == null) return; // block is null before the world is loaded
                pad.setTranslation(Scl.scl(building.fliped ? 4f : 178f) - block[0].getWidth(), 0f);
                pad.setWidth(Scl.scl(building.fliped ? 244f : 70f)); // more magic numbers to the god of magic numbers
            });
        });

        parent.fill(cont -> { // Wave Approaching
            cont.name = "waveapproaching";
            cont.bottom();

            cont.table(Styles.black6, pad -> {
                pad.add("@approaching.info").labelAlign(Align.center, Align.center).update(label -> {
                    label.setColor(Color.white.cpy().lerp(Color.scarlet, Mathf.absin(10f, 1f)));
                }).padRight(6f);
                pad.button(Icon.info, style, approaching::show).grow();
                pad.button(Icon.eyeOffSmall, style, () -> settings.put("approachenabled", false)).grow();
            }).margin(6f).padBottom(mobile ? 350f : 100f).update(pad -> {
                pad.color.a = Mathf.lerpDelta(pad.color.a, Mathf.num(
                        settings.getBool("approachenabled") && state.wavetime > 600f && state.wavetime < 1800f
                ), .1f);
                pad.touchable = pad.color.a > .001f ? Touchable.childrenOnly : Touchable.disabled; // geniusly
            }).get().color.a(0f); // hide on startup
        });

        if (!settings.getBool("mobilebuttons") && !mobile) return;

        getCommandButton(cont -> { // Shortcut and cursed schematics button
            if (!SchemeUpdater.installed("test-utils")) // hardcoded paddings
                cont.row(); // for command button

            cont.button("@schematics", Icon.paste, Styles.squareTogglet, () -> {
                if (shortfrag.visible) shortfrag.hide();
                else shortfrag.show(graphics.getWidth() - (int) Scl.scl(15f), graphics.getHeight() / 2);
            }).size(155f, 50f).margin(8f).checked(t -> shortfrag.visible);

            if (!SchemeUpdater.installed("test-utils")) cont.row();

            cont.button("@none", Icon.menu, Styles.flatBordert, () -> {
                m_schematics.nextLayer();
            }).size(155f, 50f).margin(6f).update(button -> button.setText(bundle.get("layer." + m_schematics.layer)));
        });

        parent.fill(cont -> { // Mobile Buttons
            cont.name = "mobilebuttons";
            cont.top().left();

            cont.visible(() -> ui.hudfrag.shown && !ui.minimapfrag.shown());

            cont.table(Tex.buttonEdge4, pad -> {
                partitionmb(pad, mode -> {
                    mode.add(mobiles);
                    if (mobile) setAction(mode, "disarmed", "lock_shoot.", m_input::lockShooting);
                    else setAction(mode, Icon.book, "view_comb", keycomb::show);
                    setAction(mode, "blasted",   "despawn",         () -> admins.despawn());
                    setAction(mode, "overdrive", "teleport",        () -> admins.teleport());
                    setAction(mode, Icon.lock,   "lock_move",       () -> m_input.lockMovement());
                }).visible(() -> true).update(mode -> mode.setTranslation(0f, Scl.scl(mobiles.fliped ? 0f : -63.2f))).row();

                partitionmb(pad, mode -> {
                    setAction(mode, Icon.effect, "place_core.",     () -> admins.placeCore());
                    setAction(mode, "boss",      "manage_team.",    () -> admins.manageTeam());
                    setAction(mode, Icon.logic,  "toggle_ai.",      () -> ai.select());
                    setAction(mode, Icon.admin,  "adminscfg.",      () -> adminscfg.show());
                    setAction(mode, Icon.image,  "rendercfg.",      () -> rendercfg.show());
                }).row();

                partitionmb(pad, mode -> {
                    setAction(mode, Icon.units,  "manage_unit.",    () -> admins.manageUnit());
                    setAction(mode, Icon.add,    "spawn_unit",      () -> admins.spawnUnits());
                    setAction(mode, "corroded",  "manage_effect.",  () -> admins.manageEffect());
                    setAction(mode, Icon.production,"manage_item.", () -> admins.manageItem());
                }).row();
            }).margin(0f).update(pad -> {
                if (block[1] == null) return; // waves main are not null but block is
                pad.setTranslation(0f, Scl.scl((mobiles.fliped ? 0f : 127f) - (mobile ? 69f : 0f)) - block[1].getHeight());
                pad.setHeight(Scl.scl(mobiles.fliped ? 190.8f : 63.8f));
            });
        });

        Table info = getInfoTable();
        info.update(() -> info.setTranslation(0f, -Scl.scl(mobiles.fliped ? 190.5f : 63.5f)));
    }

    public void resize(int amount) {
        size.setText(String.valueOf(amount));
    }

    public void nextLayer() {
        if (!timer.get(240f)) m_schematics.nextLayer();
    }

    private Cell<Table> partitionbt(Table table, Cons<Table> cons) {
        if (table.hasChildren()) table.image().color(Pal.gray).fillY().width(4f).pad(4f).visible(() -> building.fliped);
        return table.table(cont -> {
            cont.defaults().size(46f).bottom().right();
            cons.get(cont);
        }).visible(() -> building.fliped);
    }

    private Cell<Table> partitionmb(Table table, Cons<Table> cons) {
        return table.table(cont -> {
            cont.defaults().size(63.5f).left();
            cons.get(cont);
        }).visible(() -> mobiles.fliped);
    }

    private void setMode(Table table, Drawable icon, Mode mode) {
        table.button(icon, check, () -> build.setMode(mode)).checked(t -> build.mode == mode).with(button -> {
            button.addListener(new TooltipLocker("@tooltip." + mode));
        }).row();
    }

    private void setAction(Table table, Object icon, String tooltip, Runnable listener) {
        table.button(icon instanceof String name ? atlas.drawable("status-" + name) : (Drawable) icon, style, 37f, listener).get()
                .addListener(new TooltipLocker(tooltip.endsWith(".") ? "@keybind." + tooltip + "name" : "@keycomb." + tooltip));
    }

    private void setMove(Table table, Updater move) {
        table.button(move.icon, check, () -> {
            GammaAI.move = move;
            ((GammaAI) ai.ai).cache();
        }).checked(t -> GammaAI.move == move).tooltip(move.tooltip()).size(37.5f);
    }

    private void setBuild(Table table, Updater build) {
        table.button(build.icon, check, () -> GammaAI.build = build).checked(t -> GammaAI.build == build).tooltip(build.tooltip()).size(37.5f);
    }

    private void updateBlocks() {
        app.post(() -> { // waiting for blockfrag rebuild
            block[0] = ((Table) ui.hudGroup.getChildren().get(10)).getChildren().get(0);
            block[1] = ((Table) getWavesMain().getChildren().get(state.isEditor() ? 1 : 0)).getChildren().get(0);
        });
    }

    private Table getCoreItems() {
        return (Table) ((Table) ui.hudGroup.getChildren().get(5)).getChildren().get(1);
    }

    private Table getInfoTable() {
        return (Table) ((Table) getWavesMain().getChildren().get(0)).getChildren().get(1);
    }

    private Stack getWavesMain() {
        return (Stack) ((Table) ui.hudGroup.find("overlaymarker")).getChildren().get(mobile ? 3 : 0);
    }

    private ImageButton getSchematicsButton() {
        return (ImageButton) ((Table) ((Table) ui.hudGroup.find("overlaymarker")).getChildren().get(1)).getChildren().get(2);
    }

    private void getCommandButton(Cons<Table> cons) {
        if (mobile) Events.run(ClientLoadEvent.class, () -> { // the command button is created after the client is loaded
            cons.get((Table) control.input.uiGroup.getChildren().get(1));
        });
        else ui.hudGroup.fill(cont -> {
            cont.name = "shortcutbutton"; // it's here because there's no sense in renaming an already created table
            cont.bottom().left();

            cont.visible(() -> ui.hudfrag.shown && !ui.minimapfrag.shown());
            cont.marginBottom(SchemeUpdater.installed("test-utils") ? 120f : 0f);
            cons.get(cont);
        });
    }
}
