package schema.tools;

import arc.*;
import arc.struct.*;
import mindustry.entities.abilities.*;
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

    /** Item capacity of the unit of the local player. */
    public int capacity;

    /** Maximum health of the force field. */
    private float maxShield;
    /** Current shield or null if absent. */
    private ForceFieldAbility fldShield;
    /** Current shield or null if absent. */
    private ShieldArcAbility arcShield;

    public Units() {
        Events.on(UnitChangeEvent.class, e -> {
            if (e.player != player) return;

            coreUnit = player.unit() != null && coreUnits.contains(player.unit().type);
            capacity = player.unit() != null ? player.unit().type.itemCapacity : -1;

            maxShield = -1f;
            fldShield = null;
            arcShield = null;

            if (player.unit() != null) for (var ability : player.unit().abilities) {

                if (ability instanceof ForceFieldAbility fld) {
                    maxShield = fld.max;
                    fldShield = fld;
                    break;
                }
                if (ability instanceof ShieldArcAbility arc) {
                    maxShield = arc.max;
                    arcShield = arc;
                    break;
                }
            }
        });

        // the units from this sequence have different movement type
        coreUnits = content.blocks().select(b -> b instanceof CoreBlock).<CoreBlock>as().map(b -> b.unitType);
    }

    /** Returns the current health of the force field. */
    public float shield() { return fldShield != null ? player.unit().shield : arcShield != null ? arcShield.data : 0f; }

    /** Returns the percentage health of the force field. */
    public float shieldf() { return shield() / maxShield; }
}
