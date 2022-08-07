package scheme.tools.admins;

import arc.math.geom.Position;
import mindustry.content.Blocks;
import mindustry.gen.Player;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class Internal implements AdminsTools {

    public void manageUnit() {
        if (unusable()) return;
        unit.select(false, true, false, (target, team, unit, amount) -> {
            if (!canCreate(team, unit)) return;
            target.unit().spawnedByCore(true);
            target.unit(unit.spawn(team, target));
            units.refresh();
        });
    }

    public void spawnUnits() {
        if (unusable()) return;
        unit.select(true, true, true, (target, team, unit, amount) -> {
            if (!canCreate(team, unit)) return;
            for (int i = 0; i < amount; i++) unit.spawn(team, target);
            units.refresh();
        });
    }

    public void manageEffect() {
        if (unusable()) return;
        effect.select(true, true, false, (target, team, effect, amount) -> {
            if (amount == 0f) target.unit().unapply(effect);
            else target.unit().apply(effect, amount);
        });
    }

    public void manageItem() {
        if (unusable()) return;
        item.select(true, false, true, (target, team, item, amount) -> {
            if (!hasCore(team)) return;
            team.core().items.add(item, fixAmount(item, amount));
        });
    }

    public void manageTeam() {
        if (unusable()) return;
        team.select((target, team) -> target.team(team));
    }

    public void placeCore() {
        if (unusable()) return;
        Tile tile = player.tileOn();
        if (tile != null) tile.setNet(tile.build instanceof CoreBuild ? Blocks.air : Blocks.coreShard, player.team(), 0);
    }

    public void despawn(Player target) {
        if (unusable()) return;
        target.unit().spawnedByCore(true);
        target.clearUnit();
    }

    public void teleport(Position pos) {
        player.unit().set(pos); // it's always available
    }

    public void fill(int sx, int sy, int ex, int ey) {
        if (unusable()) return;
        tile.select((floor, block, overlay) -> {
            for (int x = sx; x <= ex; x++)
                for (int y = sy; y <= ey; y++) {
                    Tile tile = world.tile(x, y);
                    if (tile == null) continue;

                    tile.setFloorNet(floor == null ? tile.floor() : floor, overlay == null ? tile.overlay() : overlay);
                    if (block != null) tile.setNet(block);
            }
        });
    }

    public boolean unusable() {
        if (!settings.getBool("adminsenabled")) {
            ui.showInfoFade(disabled);
            return true;
        } else if (net.client()) ui.showInfoFade(unabailable);
        return net.client();
    }
}
