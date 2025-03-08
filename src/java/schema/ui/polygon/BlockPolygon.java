package schema.ui.polygon;

import arc.func.*;
import arc.graphics.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import schema.ui.Style;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

import static mindustry.type.Category.*;
import static mindustry.type.Category.logic;
import static mindustry.type.Category.units;

/** Polygon that displays the block selection wheel. */
public class BlockPolygon extends Polygon {

    /** Reordered list of the block categories. */
    public static final Category[] categories = { distribution, liquid, production, crafting, power, defense, turret, units, effect, logic };
    /** Reordered list of character icons of the corresponding categories. */
    public static final char[] icons = new char[all.length];
    {
        for (int i = 0; i < all.length; i++)
            icons[i] = Reflect.get(Iconc.class, categories[i].name());
    }

    /** Rebuilds the list of blocks. */
    private Runnable rebuild;

    @Override
    public void build(Group parent) {
        super.build(parent);
        for (int i = 0; i < all.length; i++) add(String.valueOf(icons[i]), true, j -> {
            draw = false;

            var index = new int[1];
            var hover = new Block[2];

            rebuild = () -> {
            index[0] = 0;

            removeChild(children.find(e -> e instanceof Table));
            add(new Table(drawable("panel"), list -> {
                list.margin(12f);
                list.defaults().pad(4f);

                list.update(() -> {
                    if (hover[1] != hover[0]) {
                        hover[1] = hover[0];
                        rebuild.run();
                    }
                });

                list.table(pane -> {
                    pane.margin(0f, 0f, 4f, 4f);
                    pane.defaults().pad(0f, 0f, -4f, -4f);

                    eachUnlocked(categories[j], b -> {
                        var core = player.core();
                        var available = player.isBuilder() && (state.rules.infiniteResources || (core != null && core.items.has(b.requirements)));

                        if (index[0]++ % 4 == 0) pane.row();

                        var button = pane.button(new TextureRegionDrawable(b.uiIcon), Style.ibt, () -> {

                            if (input.shift() || input.ctrl() || input.alt())
                                copy(icon(b.name) + "");
                            else
                                insys.block = b;

                            hide();
                        }).size(48f).checked(insys.block == b).disabled(!b.isPlaceable() || !available).get();

                        if (button.isDisabled()) button.toBack();

                        button.resizeImage(32f);
                        button.getImage().setColor(!b.isPlaceable() ? Color.darkGray : !available ? Color.gray : Color.white);

                        button.hovered(() -> hover[0] = b);
                        button.exited(() -> { if (hover[0] == b) hover[0] = null; });
                    });
                });
            }) {
                @Override
                public void layout() {
                    super.layout();
                    float
                        width = 212f,
                        height = getPrefHeight();

                    setSize(width, height);
                    translation.set(width, height).scl(-.5f);
                }
            });

            };
            rebuild.run();
        });
    }

    @Override
    public void hideImmediately() { super.hideImmediately(); removeChild(children.find(e -> e instanceof Table)); }

    /** Whether the given block is unlocked. */
    public boolean unlocked(Block block) { return block.placeablePlayer && block.unlockedNow() && block.environmentBuildable(); }

    /** Iterates unlocked blocks in the given category. */
    public void eachUnlocked(Category cat, Cons<Block> cons) {
        content.blocks().select(b ->
            b.category == cat && unlocked(b) && b.isVisible()
        ).sort((b1, b2) ->
            Boolean.compare(!b1.isPlaceable(), !b2.isPlaceable())
        ).each(cons);
    }
}
