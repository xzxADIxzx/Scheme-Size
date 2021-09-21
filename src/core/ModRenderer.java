package mindustry.core;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.async.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.storage.CoreBlock.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ModRenderer extends Renderer{

	private Color clearColor = new Color(0f, 0f, 0f, 1f);

	// @Override
 //    public void update(){
 //        Color.white.set(1f, 1f, 1f, 1f);

 //        float dest = Mathf.clamp(Mathf.round(getScale(), 0.5f), minScale(), maxScale());
 //        camerascale = Mathf.lerpDelta(camerascale, dest, 0.1f);
 //        if(Mathf.equal(camerascale, dest, 0.001f)) camerascale = dest;
 //        laserOpacity = settings.getInt("lasersopacity") / 100f;
 //        bridgeOpacity = settings.getInt("bridgeopacity") / 100f;
 //        animateShields = settings.getBool("animatedshields");
 //        drawStatus = Core.settings.getBool("blockstatus");

 //        if(landTime > 0){
 //            if(!state.isPaused()){
 //                updateLandParticles();
 //            }

 //            if(!state.isPaused()){
 //                landTime -= Time.delta;
 //            }
 //            float fin = landTime / coreLandDuration;
 //            if(!launching) fin = 1f - fin;
 //            camerascale = landInterp.apply(minZoomScl, Scl.scl(4f), fin);
 //            weatherAlpha = 0f;

 //            //snap camera to cutscene core regardless of player input
 //            if(landCore != null){
 //                camera.position.set(landCore);
 //            }
 //        }else{
 //            weatherAlpha = Mathf.lerpDelta(weatherAlpha, 1f, 0.08f);
 //        }

 //        camera.width = graphics.getWidth() / camerascale;
 //        camera.height = graphics.getHeight() / camerascale;

 //        if(state.isMenu()){
 //            landTime = 0f;
 //            graphics.clear(Color.black);
 //        }else{
 //            if(shakeTime > 0){
 //                float intensity = shakeIntensity * (settings.getInt("screenshake", 4) / 4f) * 0.75f;
 //                camShakeOffset.setToRandomDirection().scl(Mathf.random(intensity));
 //                camera.position.add(camShakeOffset);
 //                shakeIntensity -= 0.25f * Time.delta;
 //                shakeTime -= Time.delta;
 //                shakeIntensity = Mathf.clamp(shakeIntensity, 0f, 100f);
 //            }else{
 //                camShakeOffset.setZero();
 //                shakeIntensity = 0f;
 //            }

 //            if(pixelator.enabled()){
 //                pixelator.drawPixelate();
 //            }else{
 //                draw();
 //            }

 //            camera.position.sub(camShakeOffset);
 //        }
 //    }

	@Override
    public void draw(){
        Events.fire(Trigger.preDraw);

        camera.update();

        if(Float.isNaN(camera.position.x) || Float.isNaN(camera.position.y)){
            camera.position.set(player);
        }

        graphics.clear(clearColor);
        Draw.reset();

        if(Core.settings.getBool("animatedwater") || animateShields){
            effectBuffer.resize(graphics.getWidth(), graphics.getHeight());
        }

        Draw.proj(camera);

        blocks.checkChanges();
        blocks.floor.checkChanges();
        blocks.processBlocks();

        Draw.sort(true);

        Events.fire(Trigger.draw);

        if(pixelator.enabled()){
            pixelator.register();
        }

        Draw.draw(Layer.background, this::drawBackground);
        Draw.draw(Layer.floor, blocks.floor::drawFloor);
        Draw.draw(Layer.block - 1, blocks::drawShadows);
        Draw.draw(Layer.block - 0.09f, () -> {
            blocks.floor.beginDraw();
            blocks.floor.drawLayer(CacheLayer.walls);
            blocks.floor.endDraw();
        });

        Draw.drawRange(Layer.blockBuilding, () -> Draw.shader(Shaders.blockbuild, true), Draw::shader);

        //render all matching environments
        for(var renderer : envRenderers){
            if((renderer.env & state.rules.environment) == renderer.env){
                renderer.renderer.run();
            }
        }

        if(state.rules.lighting){
            Draw.draw(Layer.light, lights::draw);
        }

        if(enableDarkness){
            Draw.draw(Layer.darkness, blocks::drawDarkness);
        }

        if(bloom != null){
            bloom.resize(graphics.getWidth(), graphics.getHeight());
            Draw.draw(Layer.bullet - 0.02f, bloom::capture);
            Draw.draw(Layer.effect + 0.02f, bloom::render);
        }

        Draw.draw(Layer.plans, overlays::drawBottom);

        if(animateShields && Shaders.shield != null){
            Draw.drawRange(Layer.shields, 1f, () -> effectBuffer.begin(Color.clear), () -> {
                effectBuffer.end();
                effectBuffer.blit(Shaders.shield);
            });

            Draw.drawRange(Layer.buildBeam, 1f, () -> effectBuffer.begin(Color.clear), () -> {
                effectBuffer.end();
                effectBuffer.blit(Shaders.buildBeam);
            });
        }

        Draw.draw(Layer.overlayUI, overlays::drawTop);
        Draw.draw(Layer.space, this::drawLanding);

        // x-ray
        blocks.drawBlocks();

        Groups.draw.draw(Drawc::draw);

        Draw.reset();
        Draw.flush();
        Draw.sort(false);

        Events.fire(Trigger.postDraw);
    }

    @Override
    private void drawBackground(){
        //nothing to draw currently
    }

}