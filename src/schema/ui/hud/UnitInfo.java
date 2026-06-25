package schema.ui.hud;

import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.graphics.*;
import schema.ui.*;
import schema.ui.elements.*;

import static mindustry.Vars.*;
import static schema.Main.*;

/// Subfragment that displays the controlled unit and its configuration.
public class UnitInfo extends Table
{
    public UnitInfo() { super(Style.find("panel-l-shape")); }

    /// Builds the fragment.
    public void build()
    {
        margin(4f, 12f, 12f, 4f);
        defaults().pad(4f);

        stack
        (
            new Element()
            {
                @Override
                public void draw() { Drawf.shadow(x + 32f, y + 32f, 80f); }
            },
            new Table(t -> t.image(() -> player.icon()).scaling(Scaling.bounded))
        )
        .size(64f);

        stack
        (
            new Table(t -> t.right().add(new Polybar(units::healthRel, Pal.health, () -> true                                            )).growY().width(12f)),
            new Table(t -> t.right().add(new Polybar(units::shieldRel, Pal.accent, () -> units.shieldAbs() > 0f && units.paylodAbs() > 0f)).growY().width( 8f)),
            new Table(t -> t.right().add(new Polybar(units::shieldRel, Pal.accent, () -> units.shieldAbs() > 0f                          )).growY().width( 4f)),
            new Table(t -> t.right().add(new Polybar(units::paylodRel, Pal.items,  () -> units.paylodAbs() > 0f                          )).growY().width( 4f)),
            new Table(t -> t.right().add(new Polybar(units::roundsRel, Pal.ammo,   () -> units.roundsAbs() > 0f                          )).growY().width( 4f))
        )
        .size(12f, 64f);
    }
}
