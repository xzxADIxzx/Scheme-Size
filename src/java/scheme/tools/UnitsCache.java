package scheme.tools;

import arc.Events;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import mindustry.content.StatusEffects;
import mindustry.entities.abilities.Ability;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.game.EventType.*;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

import static arc.Core.*;
import static mindustry.Vars.*;

public class UnitsCache {

    public float maxShield;
    public int maxAccepted;

    public ShieldArcAbility ability;
    public Seq<Unit> cache = new Seq<>();

    public float waveHealth;
    public float waveShield;

    public ObjectIntMap<UnitType> waveUnits = new ObjectIntMap<>();
    public ObjectIntMap<UnitType> waveBosses = new ObjectIntMap<>();

    public void load() {
        Events.run(WorldLoadEvent.class, () -> app.post(this::refresh));
        Events.run(UnitSpawnEvent.class, this::refresh);
        Events.run(UnitCreateEvent.class, this::refresh);
        Events.run(UnitUnloadEvent.class, this::refresh);
        Events.run(UnitChangeEvent.class, this::refresh);

        Events.on(UnitChangeEvent.class, event -> {
            if (event.player != player) return;
            maxAccepted = player.unit().type.itemCapacity;

            maxShield = -1;
            ability = null;
            for (Ability ability : player.unit().abilities) if (ability instanceof ForceFieldAbility field) {
                maxShield = field.max;
                break;
            } else if (ability instanceof ShieldArcAbility shield) {
                maxShield = shield.max;
                this.ability = shield;
                break;
            }
        });
    }

    public float shield() {
        return ability == null ? player.unit().shield : ability.data;
    }

    public void refresh() {
        if (!cache.isEmpty()) cache();
    }

    public void refreshWaveInfo() {
		waveHealth = waveShield = 0;
        waveUnits.clear();
        waveBosses.clear();

        state.rules.spawns.each(group -> group.type != null, group -> {
            int amount = group.getSpawned(state.wave - 1);
            if (amount == 0) return;

            waveHealth += group.type.health * amount;
            waveShield += group.getShield(state.wave - 1);
            waveUnits.put(group.type, amount);
            if (group.effect == StatusEffects.boss) waveBosses.put(group.type, amount);
        });
    }

    public void cache() {
        Groups.draw.each(drawc -> drawc instanceof Unit, drawc -> {
            Groups.draw.remove(drawc);
            cache.add((Unit) drawc);
        });
    }

    public void uncache() {
        cache.each(Unit::isAdded, Groups.draw::add);
        cache.clear();
    }
}
