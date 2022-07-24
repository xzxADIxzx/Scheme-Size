package scheme.tools.admins;

import arc.math.geom.Position;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.type.UnitType;
import scheme.ui.dialogs.AdminsConfigDialog;

import static arc.Core.*;
import static mindustry.Vars.*;

public interface AdminsTools {

    public String disabled = bundle.format("admins.notenabled", AdminsConfigDialog.keybind());
    public String available = bundle.format("admins.notavailable", AdminsConfigDialog.keybind());

    public void manageUnit();

    public void spawnUnits();

    public void manageEffect();

    public void manageItem();

    public void manageTeam();

    public void placeCore();

    public void despawn(Player target);

    public default void despawn() {
        despawn(player);
    }

    public void teleport(Position pos);

    public default void teleport() {
        teleport(camera.position);
    }

    public default void look() {
        for (int i = 0; i < 10; i++) player.unit().lookAt(input.mouseWorld());
    }

    public void edit(int sx, int sy, int ex, int ey);

    public boolean unusable();

    public default int fixAmount(Item item, Float amount) {
        int items = player.core().items.get(item);
        return amount == 0f || items + amount < 0 ? -items : amount.intValue();
    }

    public default boolean canCreate(Team team, UnitType type) {
        boolean can = Units.canCreate(team, type);
        if (!can) ui.showInfoFade("@admins.nounit");
        return can;
    }

    public default boolean hasCore(Team team) {
        boolean has = team.core() != null;
        if (!has) ui.showInfoFade("@admins.nocore");
        return has;
    }
}
