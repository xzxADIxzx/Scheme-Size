package mindustry.admins;

import arc.math.geom.*;
import mindustry.entities.Units;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;

import static mindustry.Vars.*;

public interface AdminsUtilsHandler {

    public void changeUnit();

    public void spawnUnit();

    public void changeEffect();

    public void changeItem();

    public void changeTeam();

    public void placeCore();

    public void teleport(Vec2 pos);

    public void kill(Player player);

    public void editWorld(int sx, int sy, int ex, int ey);

    public static int fix(Team team, Item item, int amount) {
        var items = team.core().items;
        return amount == 0 ? -items.get(item) : items.get(item) + amount < 0 ? -items.get(item) : amount;
    }

    public static boolean noCore(Team team) {
        boolean no = team.core() == null;
        if (no) ui.showInfoFade("@nocore");
        return no;
    }

    public static boolean noCap(Team team) {
        boolean no = Units.getCap(team) == 0;
        if (no) ui.showInfoFade("@nocap");
        return no;
    }
}
