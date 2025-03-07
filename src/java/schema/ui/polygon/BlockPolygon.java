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

    @Override
    public void build(Group parent) {
        super.build(parent);
        for (int i = 0; i < all.length; i++) add(String.valueOf(icons[i]), true, j -> {
            draw = false;

            add(new Table(atlas.drawable("schema-panel"), list -> {
                list.margin(12f);
                list.defaults().pad(4f);

                // TODO top part of the list

                list.pane(Style.scr, pane -> {
                    pane.margin(0f, 4f, 4f, 8f);
                    pane.defaults().pad(0f, -4f, -4f, 0f);

                    eachUnlocked(categories[j], b -> {
                        var core = player.core();
                        var available = player.isBuilder() && (state.rules.infiniteResources || (core != null && core.items.has(b.requirements)));

                        var button = pane.button(new TextureRegionDrawable(b.uiIcon), Style.ibc, () -> {

                            if (input.shift() || input.ctrl() || input.alt())
                                copy(icon(b.name) + "");
                            else
                                insys.block = b;

                            list.remove();
                            hide();

                        }).size(48f).checked(insys.block == b && available).disabled(available).get();

                        button.resizeImage(32f);
                        button.image().color(!b.isPlaceable() ? Color.darkGray : !available ? Color.gray : Color.white);
                    });
                });
            }));
        });
    }

    @Override
    public void select() { if (draw) super.select(); }

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
