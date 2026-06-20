package schema.tools;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.storage.*;
import schema.input.*;

import static mindustry.Vars.*;

/// Utility focused on units.
public class Units
{
    /// Unit types commonly spawned by cores.
    public Seq<UnitType> coreUnits;
    /// Whether the unit was spawned by core.
    public boolean coreUnit;

    /// Item capacity of the unit.
    public int capacity;
    /// Maximum health of the shield.
    private float maxShield;
    /// Current shield or null if absent.
    private ForceFieldAbility fldShield;
    /// Current shield or null if absent.
    private ShieldArcAbility arcShield;

    /// Total health and shield of units on the next wave.
    public float waveHealth, waveShield;
    /// Total amount of units and bosses on the next wave.
    public ObjectIntMap<UnitType> waveCommon = new ObjectIntMap<>(), waveGuards = new ObjectIntMap<>();

    public Units()
    {
        Events.on(UnitChangeEvent.class, e ->
        {
            if (e.player != player) return;

            coreUnit = player.unit() != null && coreUnits.contains(player.unit().type);
            capacity = player.unit() != null ? player.unit().itemCapacity() : -1;

            maxShield = -1f;
            fldShield = null;
            arcShield = null;

            if (player.unit() != null) for (var ability : player.unit().abilities)
            {
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

        coreUnits = content.blocks().select(b -> b instanceof CoreBlock).<CoreBlock>as().map(b -> b.unitType);

        // extremely invasive, yet efficient
        Groups.draw = new EntityGroup<Drawc>(Drawc.class, false, false, Reflect.get(Groups.draw, "indexer"))
        {
            @Override
            public void draw(Cons<Drawc> cons)
            {
                if (Keybind.display_unit.down()) super.draw(d ->
                {
                    if (!(d instanceof Unit u) || u.isPlayer()) cons.get(d);
                });
                else super.draw(cons);
            }
        };
    }

    /// Returns the absolute health of the shield.
    public float shieldAbs() { return fldShield != null ? player.unit().shield : arcShield != null ? arcShield.data : 0f; }

    /// Returns the relative health of the shield.
    public float shieldRel() { return shieldAbs() / maxShield; }

    /// Refreshes information about the next wave.
    public void refreshWaveInfo(int wave)
    {
        waveHealth = waveShield = 0f;
        waveCommon.clear();
        waveGuards.clear();

        state.rules.spawns.each(g -> g.type != null, g ->
        {
            int amount = g.getSpawned(wave - 1);
            if (amount == 0) return;

            if (state.isCampaign())
            {
                var sm = amount * state.getPlanet().campaignRules.difficulty.enemySpawnMultiplier;
                amount = Math.max(1, g.effect == StatusEffects.boss ? (int) sm : Mathf.round(sm));
            }
            amount *= g.type.flying ? spawner.countFlyerSpawns() : spawner.countGroundSpawns();

            waveHealth += g.type.health * amount;
            waveShield += g.getShield(wave - 1) * amount;

            (g.effect == StatusEffects.boss ? waveGuards : waveCommon).put(g.type, amount);
        });
    }

    /// Slices the sequence of units into batches.
    public void slice(Seq<Unit> units, Boolf<Unit> pred, Cons2<int[], Boolean> cons)
    {
        int max = 192;
        var seq = units.mapInt(Unit::id, pred);

        if (seq.size > max)
            seq.chunked(max, c -> cons.get(c, c[c.length - 1] == seq.peek()));
        else
            cons.get(seq.toArray(), true);
    }
}
