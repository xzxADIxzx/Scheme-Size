package mindustry.ui.fragments;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.entities.abilities.*;
import mindustry.core.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.input.BuildingTools.*;
import mindustry.world.blocks.power.*;
import mindustry.ui.*;
import mindustry.scheme.*;

import static arc.Core.*;
import static mindustry.Vars.*;

// Last Update - Oct 12, 2021
public class ModHudFragment extends Fragment {

    private ImageButton flipMobile;
    private TextField size;
    private float maxShield;
    private Element block;
    private Building node;

    public boolean shownMobile = false;
    public boolean shownBT = false;
    public boolean checked = false;

    @Override
    public void build(Group parent) {
        Events.run(UnitChangeEvent.class, () -> {
            maxShield = -1;
            player.unit().abilities.each(ability -> {
                if (ability instanceof ForceFieldAbility field) maxShield = field.max;
            });
        });

        Events.run(WorldLoadEvent.class, () -> {
            updateBlock();

            if (node != null) {
                node = world.build(node.pos());
                if (node != null && node.block instanceof PowerBlock == false) node = null;
            }
        });

        Events.run(UnlockEvent.class, this::updateBlock);

        parent.fill(cont -> {
            cont.name = "shieldbar";
            cont.top().left();

            cont.visible(() -> ui.hudfrag.shown && !state.isEditor());

            // TODO: test click on mobile
            float dif = Scl.scl() % .5f == 0 ? 0f : 1f;
            cont.add(new HexBar(() -> player.unit().shield / maxShield, icon -> {
                icon.image(() -> player.icon()).scaling(Scaling.bounded).grow().maxWidth(54f);
            })).size(92.2f + dif / 2, 80f).padLeft(18.2f - dif);
        });

        if (mobile || Core.settings.getBool("mobilemode")) parent.fill(cont -> {
            cont.name = "buttons";
            cont.top().left();

            cont.visible(() -> ui.hudfrag.shown);
            cont.update(() -> {
                cont.marginTop((mobile ? 201f : 132f) + (state.isEditor() ? 13f : 0f));
            }); // mobile have additional buttons

            float dsize = 65f, bsize = dsize - 1.5f, isize = dsize - 28f;
            ImageButtonStyle style = new ImageButtonStyle() {{
                up = Tex.wavepane;
                down = Styles.flatDown;
                over = Styles.flatOver;
            }};

            cont.table(select -> {
                select.defaults().size(bsize).left();

                Drawable look = Core.atlas.drawable("status-disarmed");
                Drawable tele = Core.atlas.drawable("status-overdrive");

                flipMobile = select.button(Icon.downOpen, style, this::toggleMobile).get();
                flipMobile.name = "flip";

                select.button(Icon.admin, style, isize - 12f, SchemeUtils::showSecret);
                select.button(look,       style, isize, SchemeSize.input::toggleLookAt);
                select.button(tele,       style, isize, () -> SchemeUtils.teleport(Core.camera.position));
                select.button(Icon.lock,  style, isize, SchemeSize.input::toggleFreePan).get().image().color(Pal.gray).width(4).height(bsize).padRight(-dsize + 1.5f + isize);
            }).left().row();

            cont.table(select -> {
                select.defaults().size(bsize).left();

                Drawable team = Core.atlas.drawable("team-derelict");
                Drawable kill = Core.atlas.drawable("status-blasted");

                select.button(Icon.effect, style, isize, SchemeUtils::placeCore);
                select.button(team,        style, isize, SchemeUtils::changeTeam);
                select.button(kill,        style, isize, () -> SchemeUtils.kill(player));
                select.button(Icon.logic,  style, isize, () -> SchemeSize.ai.select(true));
                select.button(Icon.map,    style, isize, SchemeSize.renderset::show).get().image().color(Pal.gray).width(4).height(bsize).padRight(-dsize + 1.5f + isize);
            }).left().visible(() -> shownMobile).row();

            cont.table(select -> {
                select.defaults().size(bsize).left();

                Drawable effe = Core.atlas.drawable("status-corroded");

                select.button(Icon.units,      style, isize, SchemeUtils::changeUnit);
                select.button(effe,            style, isize, SchemeUtils::changeEffect);
                select.button(Icon.production, style, isize, SchemeUtils::changeItem);
                select.button(Icon.add,        style, isize, SchemeUtils::spawnUnit);
                select.button(Icon.info,       style, isize, SchemeUtils::history).get().image().color(Pal.gray).width(4).height(bsize).padRight(-dsize + 1.5f + isize);
            }).left().visible(() -> shownMobile).row();
        });

        // building tools
        parent.fill(cont -> {
            cont.name = "buildingtools";
            cont.bottom().right();

            float bsize = 46f;
            BuildingTools bt = SchemeSize.input.bt;

            ImageButtonStyle style = new ImageButtonStyle() {{
                down = Styles.flatDown;
                up = Styles.none;
                over = Styles.flatOver;
            }};

            ImageButtonStyle check = new ImageButtonStyle() {{
                down = Styles.flatDown;
                up = Styles.none;
                over = Styles.flatOver;
                checked = Styles.flatDown;
            }};

            TextFieldStyle input = new TextFieldStyle() {{
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

                    ctrl.button(Icon.cancel, style, () -> {
                        SchemeSize.input.block = null;
                        bt.plan.clear();
                    }).visible(bt::isPlacing).row();
                    ctrl.add(size).row();
                    ctrl.button(Icon.up, style, () -> bt.resize(1)).row();
                    ctrl.image(Icon.resize).row();
                    ctrl.button(Icon.down, style, () -> bt.resize(-1)).row();
                });

                pad.image().color(Pal.gray).width(4f).pad(4f).fillY();

                pad.table(edit -> {
                    edit.name = "mapeditor";
                    edit.defaults().size(bsize).bottom().right();

                    edit.button(Icon.redo, style, SchemeSize.input::flushLastRemoved).tooltip("@keycomb.return").row();
                    edit.button(Icon.paste, check, () -> bt.setMode(Mode.calc)).checked(t -> bt.mode == Mode.calc).row();
                    edit.button(Icon.pencil, style, () -> SchemeSize.tile.select(true, null)).padTop(bsize).row();
                    edit.button(Icon.editor, check, () -> bt.setMode(Mode.edit)).checked(t -> bt.mode == Mode.edit).row();
                });

                pad.image().color(Pal.gray).width(4f).pad(4f).fillY();

                pad.table(mode -> {
                    mode.name = "modes";
                    mode.defaults().size(bsize).bottom().right();

                    mode.button(Icon.fill, check, () -> bt.setMode(Mode.fill)).checked(t -> bt.mode == Mode.fill).row();
                    mode.button(Icon.grid, check, () -> bt.setMode(Mode.square)).checked(t -> bt.mode == Mode.square).row();
                    mode.button(Icon.commandRally, check, () -> bt.setMode(Mode.circle)).checked(t -> bt.mode == Mode.circle).row();
                    mode.button(Icon.link, check, () -> bt.setMode(Mode.replace)).checked(t -> bt.mode == Mode.replace).row();
                    mode.button(Icon.power, check, () -> bt.setMode(Mode.power)).checked(t -> bt.mode == Mode.power).row();
                }).row();
            }).height(254f).visible(() -> shownBT && ui.hudfrag.shown && !ui.minimapfrag.shown()).update(t -> {
                if (block != null) t.setTranslation(-block.getWidth() + Scl.scl(4), 0);
            });
        });

        // power display
        getCoreItems().table(cont -> {
            cont.name = "energydisplay";
            cont.background(Styles.black6).margin(8f, 8f, 8f, 0f);

            Bar power = new Bar(
                    () -> Core.bundle.format("bar.powerbalance", node != null ? (node.power.graph.getPowerBalance() >= 0 ? "+" : "") + UI.formatAmount((long) (node.power.graph.getPowerBalance() * 60)) : "+0"),
                    () -> node != null && node.added ? Pal.powerBar : Pal.adminChat,
                    () -> node != null ? node.power.graph.getSatisfaction() : 0);

            Bar stored = new Bar(
                    () -> Core.bundle.format("bar.powerstored", node != null ? UI.formatAmount((long) node.power.graph.getLastPowerStored()) : 0,
                                                                node != null ? UI.formatAmount((long) node.power.graph.getLastCapacity()) : 0),
                    () -> node != null && node.added ? Pal.powerBar : Pal.adminChat,
                    () -> node != null ? Mathf.clamp(node.power.graph.getLastPowerStored() / node.power.graph.getLastCapacity()) : 0);

            ImageButtonStyle style = new ImageButtonStyle() {{
                down = Styles.flatDown;
                up = Styles.none;
                over = Styles.flatOver;
                checked = Styles.flatDown;
            }};

            cont.table(bars -> {
                bars.add(power).height(18f).growX().row();
                bars.add(stored).height(19f).growX().padTop(8f).row();
            }).growX();
            cont.button(Icon.edit, style, () -> checked = !checked).checked(t -> checked).size(44f, 44f).padLeft(8f);
        }).fillX().visible(() -> Core.settings.getBool("coreitems") && !mobile && ui.hudfrag.shown);
    }

    public void resize(int amount) {
        size.setText(String.valueOf(amount));
    }

    private void toggleMobile() {
        if (flipMobile != null) {
            flipMobile.getStyle().imageUp = shownMobile ? Icon.downOpen : Icon.upOpen;
        }

        shownMobile = !shownMobile;
    }

    public void toggleBT() {
        shownBT = !shownBT;
    }

    private Table getCoreItems() {
        return (Table) ((Table) ui.hudGroup.getChildren().get(4)).getChildren().get(1);
    }

    public void updateBlock() {
        app.post(() -> { // waiting for blockfrag rebuild
            block = ((Table) ui.hudGroup.getChildren().get(9)).getChildren().get(0);
        });
    }

    public void updateNode(Building build) {
        if (checked) {
            checked = false;
            node = build;
        }
    }

    public class HexBar extends Table {
        public final Floatp amount;
        public float last, blink, value;
        public float sw = Scl.scl(25.8f);

        public HexBar(Floatp amount, Cons<Table> cons) {
            super(cons);
            this.amount = amount;
        }

        @Override
        public void draw() {
            float next = amount.get();

            if (next == 0f) return;
            if (next < last) blink = 1f;

            if (Float.isNaN(next) || Float.isInfinite(next)) next = 1f;
            if (Float.isNaN(value) || Float.isInfinite(value)) value = 1f;

            blink = Mathf.lerpDelta(blink, 0f, 0.2f);
            value = Mathf.lerpDelta(value, next, 0.15f);
            last = next;

            drawInner(Pal.darkerGray, 1f); // draw a gray background over the standard one
            if (value > 0) drawInner(Tmp.c1.set(Pal.accent).lerp(Color.white, blink), value);

            Drawf.shadow(x + width / 2f, y + height / 2f, height * 1.13f, parentAlpha);
            Draw.reset();

            super.draw();
        }

        public void drawInner(Color color, float fract) {
            fract = Mathf.clamp(fract);
            Draw.color(color, parentAlpha);

            float f1 = Math.min(fract * 2f, 1f), f2 = (fract - 0.5f) * 2f;
            float bh = height / 2f, mw = width - sw;

            float dx = sw * f1;
            float dy = bh * f1 + y;
            Fill.quad(
                    x + sw, y,
                    x + mw, y,
                    x + dx + mw, dy,
                    x - dx + sw, dy);

            if (f2 < 0) return;

            dx = sw * f2;
            dy = height * fract + y;
            Fill.quad(
                    x, y + bh,
                    x + width, y + bh,
                    x + width - dx, dy,
                    x + dx, dy);
        }
    }
}