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
import mindustry.logic.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.power.ImpactReactor.*;
import mindustry.world.blocks.power.NuclearReactor.*;

import static mindustry.Vars.*;

public class AdditionalRenderer{

    public Seq<Building> build = new Seq<>();
    public TilesQuadtree tiles;
    public float opacity = .5f;

    public boolean xray;
    public boolean grid;
    public boolean unit;
    public boolean radius;
    public boolean raduni;

    public AdditionalRenderer(){
        Events.on(WorldLoadEvent.class, event -> {
            tiles = new TilesQuadtree(new Rect(0, 0, world.unitWidth(), world.unitHeight()));
            world.tiles.eachTile(tile -> tiles.insert(tile));
        });

        renderer.addEnvRenderer(0, this::draw);
    }

    private void draw(){
        Draw.color(Color.white, opacity);
        Rect bounds = Core.camera.bounds(Tmp.r1).grow(tilesize);

        if(xray) tiles.intersect(bounds, tile -> {
            if(tile.build != null){
                tile.floor().drawBase(tile);
                tile.overlay().drawBase(tile);
            }
        });

        if(grid){
            int size = tilesize * Math.max(Mathf.round(bounds.area() / 524288), 1);
            int sx = Mathf.round(bounds.x, size);
            int sy = Mathf.round(bounds.y, size);
            int ex = Mathf.round(bounds.x + bounds.width, size);
            int ey = Mathf.round(bounds.y + bounds.height, size);

            Draw.z(Layer.blockUnder);
            Lines.stroke(1f, Pal.darkMetal);

            int segmentsX = Math.abs(sy - ey) >> 2;
            for(var x = sx - 4; x < ex; x += size)
                Lines.dashLine(x, sy - 6, x, ey - 6, segmentsX);

            int segmentsY = Math.abs(sx - ex) >> 2;
            for(var y = sy - 4; y < ey; y += size)
                Lines.dashLine(sx - 6, y, ex - 6, y, segmentsY);
        }

        Draw.z(Layer.overlayUI);

        build.clear();
        if(radius) tiles.intersect(bounds, tile -> {
            if(tile.build == null || build.contains(tile.build)) return;
            else build.add(tile.build);

            if(tile.build instanceof Ranged r)
                Drawf.dashCircle(r.x(), r.y(), r.range(), r.team().color);
            if(tile.build instanceof NuclearReactorBuild nrb)
                Drawf.dashCircle(nrb.x, nrb.y, ((NuclearReactor)nrb.block).explosionRadius * tilesize, Pal.thoriumPink);
            if(tile.build instanceof ImpactReactorBuild irb)
                Drawf.dashCircle(irb.x, irb.y, ((ImpactReactor)irb.block).explosionRadius * tilesize, Pal.meltdownHit);
        });

        if(unit || raduni) Groups.draw.draw(draw -> {
            if(draw instanceof Unit u && u != player.unit()){
                if(raduni) Drawf.circles(u.x, u.y, u.range(), u.team.color);
                if(unit){
                    Tmp.v1.set(u.aimX(), u.aimY()).sub(u.x, u.y);
                    Tmp.v2.set(Tmp.v1).setLength(u.hitSize);
                    Lines.stroke(2, u.team.color);
                    Lines.lineAngle(u.x + Tmp.v2.x, u.y + Tmp.v2.y, Tmp.v1.angle(), Tmp.v1.len() - Tmp.v2.len());

                    drawHealthBar(u, Pal.darkishGray, 1);
                    drawHealthBar(u, Pal.health, u.health / u.maxHealth);
                }
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

    static class TilesQuadtree extends QuadTree<Tile>{

        public TilesQuadtree(Rect bounds){
            super(bounds);
        }

        @Override
        public void hitbox(Tile tile){
            var floor = tile.floor();
            tmp.setCentered(tile.worldx(), tile.worldy(), floor.clipSize, floor.clipSize);
        }
    }
}