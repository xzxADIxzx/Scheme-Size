package schema.ui.polygon;

import arc.scene.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;

import static mindustry.type.Category.*;

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
}
