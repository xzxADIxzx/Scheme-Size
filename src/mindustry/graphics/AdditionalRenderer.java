package mindustry.graphics;

import arc.*;
import arc.util.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.game.EventType.*;
import mindustry.content.*;
import mindustry.graphics.BlockRenderer.*;
import mindustry.world.blocks.defense.turrets.BaseTurret.*;

import static mindustry.Vars.*;

public class AdditionalRenderer{

    private TextureRegion cell = Core.atlas.find("door-open");
    private Seq<Building> build = new Seq<>();
    private float grow = tilesize * (mobile ? 1 : Core.settings.getFloat("argrowsize", 16f));

    public FloorQuadtree tiles;
    public float opacity = .5f;

    public Boolean xray;
    public Boolean grid;
    public Boolean unitRadius;
    public Boolean blockRadius;

    public AdditionalRenderer(){
        Events.on(WorldLoadEvent.class, event -> {
            tiles = new FloorQuadtree(new Rect(0, 0, world.unitWidth(), world.unitHeight()));
            world.tiles.forEach(tile -> tiles.insert(tile));
        });

        renderer.addEnvRenderer(0, this::draw);
    }

    private void draw(){
        build.clear();
        Draw.color(Color.white, opacity);

        Rect bounds = Core.camera.bounds(Tmp.r1).grow(grow);
        tiles.intersect(bounds, tile -> {
            if(grid && tile.block() == Blocks.air) Draw.rect(cell, tile.x * tilesize, tile.y * tilesize, tilesize, tilesize);
            if(tile.build != null){
                if(!build.contains(tile.build)) build.add(tile.build);
                if(xray){
                    tile.floor().drawBase(tile);
                    tile.overlay().drawBase(tile);
                }
            }
        });

        build.each(build -> {
            if(grid) control.input.drawSelected(build.tileX(), build.tileY(), build.block, Pal.darkMetal);
            if(blockRadius && build instanceof BaseTurretBuild btb)
                Drawf.dashCircle(btb.x, btb.y, btb.range(), btb.team.color);
        });

        Draw.z(Layer.overlayUI);
        Groups.draw.draw(drawc -> {
            if(drawc instanceof Unit u){
                if(unitRadius) Drawf.dashCircle(u.x, u.y, u.range(), u.team.color);

                Tmp.v1.set(u.aimX(), u.aimY()).sub(u.x, u.y);
                Tmp.v2.set(Tmp.v1).setLength(u.hitSize);
                Lines.stroke(2, u.team.color);
                Lines.lineAngle(u.x + Tmp.v2.x, u.y + Tmp.v2.y, Tmp.v1.angle(), Tmp.v1.len() - Tmp.v2.len());

                drawHealthBar(u, Pal.darkishGray, 1);
                drawHealthBar(u, Pal.health, u.health / u.maxHealth);
            }
        });

        Draw.reset();
    }

    private void drawHealthBar(Unit unit, Color color, float fract){
            Draw.color(color, 1);

            float size = Mathf.sqrt(unit.hitSize) * 3f;
            float x = unit.x - size / 2f;
            float y = unit.y - size;

            float width = -size;
            float height = size * 2f;
            float stroke = width * .35f;

            float f1 = Math.min(fract * 2f, 1f), f2 = (fract - .5f) * 2f;
            float bo = -(1f - f1) * (width - stroke);

            Fill.quad(
                x, y,
                x + stroke, y,
                x + width + bo, y + size * f1,
                x + width - stroke + bo, y + size * f1
            );

            if(f2 > 0){
                float bx = x + (width - stroke) * (1f - f2);
                Fill.quad(
                    x + width, y + size,
                    x + width - stroke, y + size,
                    bx, y + height * fract,
                    bx + stroke, y + height * fract
                );
            }
    }
}