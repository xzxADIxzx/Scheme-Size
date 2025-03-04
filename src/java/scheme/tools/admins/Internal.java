package scheme.tools.admins;

import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Prop;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;
import scheme.tools.RainbowTeam;

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
        });
    }

    public void spawnUnits() {
        if (unusable()) return;
        unit.select(true, true, true, (target, team, unit, amount) -> {
            if (amount == 0f) {
                Groups.unit.each(u -> u.team == team && u.type == unit, u -> u.spawnedByCore(true));
                return;
            }

            if (!canCreate(team, unit)) return;
            for (int i = 0; i < amount; i++) unit.spawn(team, target);
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
        team.select((target, team) -> {
            if (team != null) {
                RainbowTeam.remove(target);
                target.team(team);
            } else
                RainbowTeam.add(target, target::team);
        });
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
                for (int y = sy; y <= ey; y++)
                    edit(floor, block, overlay, x, y);
        });
    }

    public void brush(int x, int y, int radius) {
        if (unusable()) return;
        tile.select((floor, block, overlay) -> Geometry.circle(x, y, radius, (cx, cy) -> edit(floor, block, overlay, cx, cy)));
    }

    public void flush(Seq<BuildPlan> plans) {
        plans.each(plan -> {
            if (plan.block.isFloor() && !plan.block.isOverlay())
                edit(plan.block, null, null, plan.x, plan.y);
            else if (plan.block instanceof Prop)
                edit(null, plan.block, null, plan.x, plan.y);
            else if (plan.block.isOverlay())
                edit(null, null, plan.block, plan.x, plan.y);
        });
    }

    public boolean unusable() {
        boolean admin = net.client() && !settings.getBool("adminsalways");
        if (!settings.getBool("adminsenabled")) {
            ui.showInfoFade(disabled);
            return true;
        } else if (admin) ui.showInfoFade(unabailable);
        return admin;
    }

    private static void edit(Block floor, Block block, Block overlay, int x, int y) {
        Tile tile = world.tile(x, y);
        if (tile == null) return;

        if ((floor != null && tile.floor() != floor) || (overlay != null && tile.overlay() != overlay))
            tile.setFloorNet(floor == null ? tile.floor() : floor, overlay == null ? tile.overlay() : overlay);

        if (block != null && tile.block() != block) tile.setNet(block);
    }
}
