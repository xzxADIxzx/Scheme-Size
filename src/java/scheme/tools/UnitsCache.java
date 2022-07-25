package scheme.tools;

import arc.Events;
import arc.struct.Seq;
import mindustry.entities.abilities.Ability;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.game.EventType.*;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

import static arc.Core.*;
import static mindustry.Vars.*;

public class UnitsCache {

    public float maxShield;
    public int maxAccepted;

    public Seq<Unit> cache = new Seq<>();

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
            for (Ability ability : player.unit().abilities) if (ability instanceof ForceFieldAbility field) {
                maxShield = field.max;
                break;
            }
        });
    }

    public void refresh() {
        if (!cache.isEmpty()) cache();
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
