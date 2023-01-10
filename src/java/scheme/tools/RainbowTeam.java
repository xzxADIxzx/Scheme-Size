package scheme.tools;

import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Timer;
import mindustry.game.Team;
import mindustry.game.EventType.*;
import mindustry.gen.Groups;
import mindustry.gen.Player;

/** Just for fun :D */
public class RainbowTeam {

    public static IntMap<Cons<Team>> members = new IntMap<>();
    public static Seq<Team> rainbow;

    public static void load() {
        Events.run(WorldLoadEvent.class, members::clear);
        Timer.schedule(() -> {
            if (members.isEmpty()) return;

            for (var entry : members)
                if (Groups.player.getByID(entry.key) == null) members.remove(entry.key);

            Team team = rainbow.get(Mathf.floor(Time.time / 6f % rainbow.size));
            members.forEach(entry -> entry.value.get(team));
        }, 0f, .3f);

        rainbow = Seq.with(Team.all);
        rainbow.filter(team -> {
            int[] hsv = Color.RGBtoHSV(team.color);
            return hsv[1] > 64 && hsv[2] > 84;
        }).sort(team -> {
            int[] hsv = Color.RGBtoHSV(team.color);
            return hsv[0] * 1000 + hsv[1];
        });
    }

    public static void add(Player target, Cons<Team> cons) {
        members.put(target.id, cons);
    }

    public static void remove(Player target) {
        members.remove(target.id);
    }
}
