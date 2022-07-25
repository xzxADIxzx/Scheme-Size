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
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
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
            cont.add(new HexBar(() -> player.unit().shield / units.maxShield, icon -> {
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
                    setMove(mode, follow);
                    setMove(mode, none);
                    setMove(mode, circle);
                }).row();
                pad.table(mode -> {
                    setBuild(mode, help);
                    setBuild(mode, none);
                    setBuild(mode, destroy);
                }).row();
                pad.labelWrap(GammaAI.tooltip).labelAlign(2, 8).pad(8f, 0f, 8f, 0f).width(150f).get().getStyle().fontColor = Color.lightGray;
            }).width(150f).margin(0f).update(pad -> pad.setTranslation(0f, settings.getBool("minimap") ? -235f : 0f)).row();
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
                    mode.button(Icon.pencil, style, tile::show).tooltip("@select.tile").padTop(92f).row();
                    setMode(mode, Icon.pick, Mode.pick);
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
                }).visible(() -> true).update(mode -> {
                    mode.setTranslation(Scl.scl(building.fliped ? 0f: -87f), 0);
                });
            }).height(254f).update(pad -> { // more magic numbers to the god of magic numbers
                if (block != null) pad.setTranslation(Scl.scl(building.fliped ? 4f : 178f) - block.getWidth(), 0f);
                pad.setWidth(Scl.scl(building.fliped ? 244f : 70f));
            });
        });

        if (!settings.getBool("mobilebuttons")) return;

        // TODO: refactor
        parent.fill(cont -> { // Mobile Buttons
            cont.name = "mobilebuttons";
            cont.top().left();

            float dsize = 65f, bsize = dsize - 1.5f, isize = dsize - 28f;
            ImageButtonStyle style = new ImageButtonStyle(){{
                up = Tex.wavepane;
                down = Styles.flatDown;
                over = Styles.flatOver;
            }};

            cont.visible(() -> ui.hudfrag.shown && !ui.minimapfrag.shown());
            cont.update(() -> {
                cont.marginTop((mobile ? 201f : 132f) + (state.isEditor() ? 29f : 0f));
            }); // mobile have additional buttons

            cont.image().color(Pal.gray).height(4f).width(bsize * 5f + 2.5f).row();

            cont.table(select -> {
                select.defaults().size(bsize).left();

                Drawable look = atlas.drawable("status-disarmed");
                Drawable tele = atlas.drawable("status-overdrive");

                select.add(mobiles).get().setStyle(style);
                mobiles.flip();
                mobiles.flip(); // TODO: fix it later

                select.button(Icon.admin, style, isize - 12f, () -> adminscfg.show());
                if (mobile) select.button(look, style, isize, () -> {}); // disable shooting
                else select.button(Icon.book, style, keycomb::show); // show keybind comb
                select.button(tele,       style, isize, () -> admins.teleport());
                select.button(Icon.lock,  style, isize, m_input::lockMovement).get().image().color(Pal.gray).width(4).height(bsize).padRight(-dsize + 1.5f + isize);
            }).left().row();

            cont.table(select -> {
                select.defaults().size(bsize).left();

                Drawable team = atlas.drawable("team-derelict");
                Drawable kill = atlas.drawable("status-blasted");

                select.button(Icon.effect, style, isize, () -> admins.placeCore());
                select.button(team,        style, isize, () -> admins.manageTeam());
                select.button(kill,        style, isize, () -> admins.despawn());
                select.button(Icon.logic,  style, isize, ai::select);
                select.button(Icon.image,    style, isize, rendercfg::show).get().image().color(Pal.gray).width(4).height(bsize).padRight(-dsize + 1.5f + isize);
            }).left().visible(() -> mobiles.fliped).row();

            cont.table(select -> {
                select.defaults().size(bsize).left();

                Drawable effe = atlas.drawable("status-corroded");

                select.button(Icon.units,      style, isize, () -> admins.manageUnit());
                select.button(Icon.add,        style, isize, () -> admins.spawnUnits());
                select.button(effe,            style, isize, () -> admins.manageEffect());
                select.button(Icon.production, style, isize, () -> admins.manageItem());
                select.button(Icon.info,       style, isize, render::toggleHistory).get().image().color(Pal.gray).width(4).height(bsize).padRight(-dsize + 1.5f + isize);
            }).left().visible(() -> mobiles.fliped).row();
        });
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

    private void setMode(Table table, Drawable icon, Mode mode) {
        table.button(icon, check, () -> build.setMode(mode)).checked(t -> build.mode == mode).row();
    }

    private void setMove(Table table, Updater move) {
        table.button(move.icon, check, () -> GammaAI.move = move).checked(t -> GammaAI.move == move).tooltip(move.tooltip()).size(50f);
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
}
