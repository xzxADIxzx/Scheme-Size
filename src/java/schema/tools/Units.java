package schema.tools;

import arc.*;
import arc.struct.*;
import mindustry.game.EventType.*;
import mindustry.type.*;
import mindustry.world.blocks.storage.*;

import static mindustry.Vars.*;

/** Utility class that helps fetch info about units. */
public class Units {

    /** Units that are commonly spawn by core. */
    public Seq<UnitType> coreUnits;
    /** Whether the unit of the local player was spawn by core. */
    public boolean coreUnit;

    public Units() {
        Events.on(UnitChangeEvent.class, e -> {
            if (e.player != player) return;

            coreUnit = player.unit() != null && coreUnits.contains(player.unit().type);
            // TODO cache more info
        });

        // the units from this sequence have different movement type
        coreUnits = content.blocks().select(b -> b instanceof CoreBlock).<CoreBlock>as().map(b -> b.unitType);
    }
}
