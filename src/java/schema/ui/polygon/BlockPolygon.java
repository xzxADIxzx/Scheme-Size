package schema.ui.polygon;

import arc.scene.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

import static mindustry.Vars.*;

import static mindustry.type.Category.*;
import static mindustry.type.Category.logic;

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
        });
    }

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
