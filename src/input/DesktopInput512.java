package mindustry.input;

import arc.*;
import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.input.*;
import mindustry.input.Placement.*;
import mindustry.content.*;

import static arc.Core.*;
import static mindustry.Vars.net;
import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;

import java.lang.Math;

public class DesktopInput512 extends DesktopInput, InputHandler{

    @Override
    public void drawTop(){
        Lines.stroke(1f);
        int cursorX = tileX512(Core.input.mouseX());
        int cursorY = tileY512(Core.input.mouseY());

        if(mode == breaking){
            int size = settings.getInt("breaksize") - 1;
            drawBreakSelection(selectX, selectY, cursorX, cursorY, Core.input.keyDown(Binding.schematic_select) ? settings.getInt("copysize") - 1 : size);

            // Show Size
            if(settings.getBool("breakshow")){
                NormalizeResult normalized = Placement.normalizeArea(selectX, selectY, cursorX, cursorY, 0, false, size);
                int sizeX = normalized.x2 - normalized.x + 1;
                int sizeY = normalized.y2 - normalized.y + 1;
                String strSizeX = sizeX - 1 == size ? "[accent]" + Integer.toString(sizeX) + "[]" : Integer.toString(sizeX);
                String strSizeY = sizeY - 1 == size ? "[accent]" + Integer.toString(sizeY) + "[]" : Integer.toString(sizeY);
                String info = strSizeX + ", " + strSizeY;
                ui.showLabel(info, 0.02f, cursorX * 8 + 16, cursorY * 8 - 16);
            }
        }

        if(Core.input.keyDown(Binding.schematic_select) && !Core.scene.hasKeyboard() && mode != breaking){
            int size = settings.getInt("copysize") - 1;
            drawSelection(schemX, schemY, cursorX, cursorY, size);

            // Show Size
            if(settings.getBool("copyshow")){
                NormalizeResult normalized = Placement.normalizeArea(schemX, schemY, cursorX, cursorY, 0, false, size);
                int sizeX = normalized.x2 - normalized.x + 1;
                int sizeY = normalized.y2 - normalized.y + 1;
                String strSizeX = sizeX - 1 == size ? "[accent]" + Integer.toString(sizeX) + "[]" : Integer.toString(sizeX);
                String strSizeY = sizeY - 1 == size ? "[accent]" + Integer.toString(sizeY) + "[]" : Integer.toString(sizeY);
                String info = strSizeX + ", " + strSizeY;
                ui.showLabel(info, 0.02f, cursorX * 8 + 16, cursorY * 8 - 16);
            }
        }

        Draw.reset();
    }

    @Override
    public void update(){
        super.update();

        if(net.active() && Core.input.keyTap(Binding.player_list) && (scene.getKeyboardFocus() == null || scene.getKeyboardFocus().isDescendantOf(ui.listfrag.content) || scene.getKeyboardFocus().isDescendantOf(ui.minimapfrag.elem))){
            ui.listfrag.toggle();
        }

        boolean panCam = false;
        float camSpeed = (!Core.input.keyDown(Binding.boost) ? panSpeed : panBoostSpeed) * Time.delta;

        if(input.keyDown(Binding.pan) && !scene.hasField() && !scene.hasDialog()){
            panCam = true;
            panning = true;
        }

        if((Math.abs(Core.input.axis(Binding.move_x)) > 0 || Math.abs(Core.input.axis(Binding.move_y)) > 0 || input.keyDown(Binding.mouse_move)) && (!scene.hasField())){
            panning = false;
        }

        if(((player.dead() || state.isPaused()) && !ui.chatfrag.shown()) && !scene.hasField() && !scene.hasDialog()){
            if(input.keyDown(Binding.mouse_move)){
                panCam = true;
            }

            Core.camera.position.add(Tmp.v1.setZero().add(Core.input.axis(Binding.move_x), Core.input.axis(Binding.move_y)).nor().scl(camSpeed));
        }else if(!player.dead() && !panning){
            Core.camera.position.lerpDelta(player, Core.settings.getBool("smoothcamera") ? 0.08f : 1f);
        }

        if(panCam){
            Core.camera.position.x += Mathf.clamp((Core.input.mouseX() - Core.graphics.getWidth() / 2f) * panScale, -1, 1) * camSpeed;
            Core.camera.position.y += Mathf.clamp((Core.input.mouseY() - Core.graphics.getHeight() / 2f) * panScale, -1, 1) * camSpeed;
        }

        shouldShoot = !scene.hasMouse();

        if(!scene.hasMouse()){
            if(Core.input.keyDown(Binding.control) && Core.input.keyTap(Binding.select)){
                Unit on = selectedUnit();
                var build = selectedControlBuild();
                if(on != null){
                    Call.unitControl(player, on);
                    shouldShoot = false;
                    recentRespawnTimer = 1f;
                }else if(build != null){
                    Call.buildingControlSelect(player, build);
                    recentRespawnTimer = 1f;
                }
            }
        }

        if(!player.dead() && !state.isPaused() && !scene.hasField() && !renderer.isCutscene()){
            updateMovement(player.unit());

            if(Core.input.keyTap(Binding.respawn)){
                controlledType = null;
                recentRespawnTimer = 1f;
                Call.unitClear(player);
            }
        }

        if(Core.input.keyRelease(Binding.select)){
            player.shooting = false;
        }

        if(state.isGame() && !scene.hasDialog() && !(scene.getKeyboardFocus() instanceof TextField)){
            if(Core.input.keyTap(Binding.minimap)) ui.minimapfrag.toggle();
            if(Core.input.keyTap(Binding.planet_map) && state.isCampaign()) ui.planet.toggle();
            if(Core.input.keyTap(Binding.research) && state.isCampaign()) ui.research.toggle();
        }

        if(state.isMenu() || Core.scene.hasDialog()) return;

        //zoom camera
        if((!Core.scene.hasScroll() || Core.input.keyDown(Binding.diagonal_placement)) && !ui.chatfrag.shown() && Math.abs(Core.input.axisTap(Binding.zoom)) > 0
            && !Core.input.keyDown(Binding.rotateplaced) && (Core.input.keyDown(Binding.diagonal_placement) || ((!player.isBuilder() || !isPlacing() || !block.rotate) && selectRequests.isEmpty()))){
            renderer.scaleCamera(Core.input.axisTap(Binding.zoom));
        }

        if(Core.input.keyTap(Binding.select) && !Core.scene.hasMouse()){
            Tile selected = world.tileWorld(input.mouseWorldX(), input.mouseWorldY());
            if(selected != null){
                Call.tileTap(player, selected);
            }
        }

        if(player.dead()){
            cursorType = SystemCursor.arrow;
            return;
        }

        pollInput();

        //deselect if not placing
        if(!isPlacing() && mode == placing){
            mode = none;
        }

        if(player.shooting && !canShoot()){
            player.shooting = false;
        }

        if(isPlacing() && player.isBuilder()){
            cursorType = SystemCursor.hand;
            selectScale = Mathf.lerpDelta(selectScale, 1f, 0.2f);
        }else{
            selectScale = 0f;
        }

        if(!Core.input.keyDown(Binding.diagonal_placement) && Math.abs((int)Core.input.axisTap(Binding.rotate)) > 0){
            rotation = Mathf.mod(rotation + (int)Core.input.axisTap(Binding.rotate), 4);

            if(sreq != null){
                sreq.rotation = Mathf.mod(sreq.rotation + (int)Core.input.axisTap(Binding.rotate), 4);
            }

            if(isPlacing() && mode == placing){
                updateLine(selectX, selectY);
            }else if(!selectRequests.isEmpty() && !ui.chatfrag.shown()){
                rotateRequests(selectRequests, Mathf.sign(Core.input.axisTap(Binding.rotate)));
            }
        }

        Tile cursor = tileAt512(Core.input.mouseX(), Core.input.mouseY());

        if(cursor != null){
            if(cursor.build != null){
                cursorType = cursor.build.getCursor();
            }

            if((isPlacing() && player.isBuilder()) || !selectRequests.isEmpty()){
                cursorType = SystemCursor.hand;
            }

            if(!isPlacing() && canMine512(cursor)){
                cursorType = ui.drillCursor;
            }

            if(getRequest(cursor.x, cursor.y) != null && mode == none){
                cursorType = SystemCursor.hand;
            }

            if(canTapPlayer512(Core.input.mouseWorld().x, Core.input.mouseWorld().y)){
                cursorType = ui.unloadCursor;
            }

            if(cursor.build != null && cursor.interactable(player.team()) && !isPlacing() && Math.abs(Core.input.axisTap(Binding.rotate)) > 0 && Core.input.keyDown(Binding.rotateplaced) && cursor.block().rotate && cursor.block().quickRotate){
                Call.rotateBlock(player, cursor.build, Core.input.axisTap(Binding.rotate) > 0);
            }
        }

        if(!Core.scene.hasMouse()){
            Core.graphics.cursor(cursorType);
        }

        cursorType = SystemCursor.arrow;
    }

    @Override
    public void pollInput(){
        var focus = scene.getKeyboardFocus();
        if(focus != null && focus.getClass() == TextField.class) return;

        Tile selected = tileAt512(Core.input.mouseX(), Core.input.mouseY());
        int cursorX = tileX512(Core.input.mouseX());
        int cursorY = tileY512(Core.input.mouseY());
        int rawCursorX = World.toTile(Core.input.mouseWorld().x), rawCursorY = World.toTile(Core.input.mouseWorld().y);

        //automatically pause building if the current build queue is empty
        if(Core.settings.getBool("buildautopause") && isBuilding && !player.unit().isBuilding()){
            isBuilding = false;
            buildWasAutoPaused = true;
        }

        if(!selectRequests.isEmpty()){
            int shiftX = rawCursorX - schematicX, shiftY = rawCursorY - schematicY;

            selectRequests.each(s -> {
                s.x += shiftX;
                s.y += shiftY;
            });

            schematicX += shiftX;
            schematicY += shiftY;
        }

        if(Core.input.keyTap(Binding.deselect) && !isPlacing()){
            player.unit().mineTile = null;
        }

        if(Core.input.keyTap(Binding.clear_building)){
            player.unit().clearBuilding();
        }

        if(Core.input.keyTap(Binding.schematic_select) && !Core.scene.hasKeyboard() && mode != breaking){
            schemX = rawCursorX;
            schemY = rawCursorY;
        }

        if(Core.input.keyTap(Binding.schematic_menu) && !Core.scene.hasKeyboard()){
            if(ui.schematics.isShown()){
                ui.schematics.hide();
            }else{
                ui.schematics.show();
            }
        }

        if(Core.input.keyTap(Binding.clear_building) || isPlacing()){
            lastSchematic = null;
            selectRequests.clear();
        }

        if(Core.input.keyRelease(Binding.schematic_select) && !Core.scene.hasKeyboard() && selectX == -1 && selectY == -1 && schemX != -1 && schemY != -1){
            lastSchematic = schematics.create(schemX, schemY, rawCursorX, rawCursorY);
            useSchematic(lastSchematic);
            if(selectRequests.isEmpty()){
                lastSchematic = null;
            }
            schemX = -1;
            schemY = -1;
        }

        if(!selectRequests.isEmpty()){
            if(Core.input.keyTap(Binding.schematic_flip_x)){
                flipRequests(selectRequests, true);
            }

            if(Core.input.keyTap(Binding.schematic_flip_y)){
                flipRequests(selectRequests, false);
            }
        }

        if(sreq != null){
            float offset = ((sreq.block.size + 2) % 2) * tilesize / 2f;
            float x = Core.input.mouseWorld().x + offset;
            float y = Core.input.mouseWorld().y + offset;
            sreq.x = (int)(x / tilesize);
            sreq.y = (int)(y / tilesize);
        }

        if(block == null || mode != placing){
            lineRequests.clear();
        }

        if(Core.input.keyTap(Binding.pause_building)){
            isBuilding = !isBuilding;
            buildWasAutoPaused = false;

            if(isBuilding){
                player.shooting = false;
            }
        }

        if((cursorX != lastLineX || cursorY != lastLineY) && isPlacing() && mode == placing){
            updateLine(selectX, selectY);
            lastLineX = cursorX;
            lastLineY = cursorY;
        }

        if(Core.input.keyTap(Binding.select) && !Core.scene.hasMouse()){
            BuildPlan req = getRequest(cursorX, cursorY);

            if(Core.input.keyDown(Binding.break_block)){
                mode = none;
            }else if(!selectRequests.isEmpty()){
                flushRequests(selectRequests);
            }else if(isPlacing()){
                selectX = cursorX;
                selectY = cursorY;
                lastLineX = cursorX;
                lastLineY = cursorY;
                mode = placing;
                updateLine(selectX, selectY);
            }else if(req != null && !req.breaking && mode == none && !req.initialized){
                sreq = req;
            }else if(req != null && req.breaking){
                deleting = true;
            }else if(selected != null){
                //only begin shooting if there's no cursor event
                if(!tryTapPlayer512(Core.input.mouseWorld().x, Core.input.mouseWorld().y) && !tileTapped(selected.build) && !player.unit().activelyBuilding() && !droppingItem
                    && !(tryStopMine(selected) || (!settings.getBool("doubletapmine") || selected == prevSelected && Time.timeSinceMillis(selectMillis) < 500) && tryBeginMine(selected)) && !Core.scene.hasKeyboard()){
                    player.shooting = shouldShoot;
                }
            }else if(!Core.scene.hasKeyboard()){ //if it's out of bounds, shooting is just fine
                player.shooting = shouldShoot;
            }
            selectMillis = Time.millis();
            prevSelected = selected;
        }else if(Core.input.keyTap(Binding.deselect) && isPlacing()){
            block = null;
            mode = none;
        }else if(Core.input.keyTap(Binding.deselect) && !selectRequests.isEmpty()){
            selectRequests.clear();
            lastSchematic = null;
        }else if(Core.input.keyTap(Binding.break_block) && !Core.scene.hasMouse() && player.isBuilder()){
            //is recalculated because setting the mode to breaking removes potential multiblock cursor offset
            deleting = false;
            mode = breaking;
            selectX = tileX512(Core.input.mouseX());
            selectY = tileY512(Core.input.mouseY());
            schemX = rawCursorX;
            schemY = rawCursorY;
        }

        if(Core.input.keyDown(Binding.select) && mode == none && !isPlacing() && deleting){
            BuildPlan req = getRequest(cursorX, cursorY);
            if(req != null && req.breaking){
                player.unit().plans().remove(req);
            }
        }else{
            deleting = false;
        }

        if(mode == placing && block != null){
            if(!overrideLineRotation && !Core.input.keyDown(Binding.diagonal_placement) && (selectX != cursorX || selectY != cursorY) && ((int)Core.input.axisTap(Binding.rotate) != 0)){
                rotation = ((int)((Angles.angle(selectX, selectY, cursorX, cursorY) + 45) / 90f)) % 4;
                overrideLineRotation = true;
            }
        }else{
            overrideLineRotation = false;
        }

        if(Core.input.keyRelease(Binding.break_block) && Core.input.keyDown(Binding.schematic_select) && mode == breaking){
            lastSchematic = schematics.create(schemX, schemY, rawCursorX, rawCursorY);
            schemX = -1;
            schemY = -1;
        }

        if(Core.input.keyRelease(Binding.break_block) || Core.input.keyRelease(Binding.select)){

            if(mode == placing && block != null){ //touch up while placing, place everything in selection
                flushRequests(lineRequests);
                lineRequests.clear();
                Events.fire(new LineConfirmEvent());
            }else if(mode == breaking){ //touch up while breaking, break everything in selection
                removeSelection(selectX, selectY, cursorX, cursorY, Core.input.keyDown(Binding.schematic_select) ? settings.getInt("copysize") - 1 : settings.getInt("breaksize") - 1);
                if(lastSchematic != null){
                    useSchematic(lastSchematic);
                    lastSchematic = null;
                }
            }
            selectX = -1;
            selectY = -1;

            tryDropItems(selected == null ? null : selected.build, Core.input.mouseWorld().x, Core.input.mouseWorld().y);

            if(sreq != null){
                if(getRequest(sreq.x, sreq.y, sreq.block.size, sreq) != null){
                    player.unit().plans().remove(sreq, true);
                }
                sreq = null;
            }

            mode = none;
        }

        if(Core.input.keyTap(Binding.toggle_block_status)){
            Core.settings.put("blockstatus", !Core.settings.getBool("blockstatus"));
        }

        if(Core.input.keyTap(Binding.toggle_power_lines)){
            if(Core.settings.getInt("lasersopacity") == 0){
                Core.settings.put("lasersopacity", Core.settings.getInt("preferredlaseropacity", 100));
            }else{
                Core.settings.put("preferredlaseropacity", Core.settings.getInt("lasersopacity"));
                Core.settings.put("lasersopacity", 0);
            }
        }
    }

    // public Tile tileAt512(float x, float y){
    //     // ._.
    //     return world.tile(tileX512(x), tileY512(y));
    // }

    public int tileX512(float cursorX){
        Vec2 vec = Core.input.mouseWorld(cursorX, 0);
        if(selectedBlock()){
            vec.sub(block.offset, block.offset);
        }
        return World.toTile(vec.x);
    }

    public int tileY512(float cursorY){
        Vec2 vec = Core.input.mouseWorld(0, cursorY);
        if(selectedBlock()){
            vec.sub(block.offset, block.offset);
        }
        return World.toTile(vec.y);
    }

    // public boolean canMine512(Tile tile){
    //     return !Core.scene.hasMouse()
    //         && tile.drop() != null
    //         && player.unit().validMine(tile)
    //         && !((!Core.settings.getBool("doubletapmine") && tile.floor().playerUnmineable) && tile.overlay().itemDrop == null)
    //         && player.unit().acceptsItem(tile.drop())
    //         && tile.block() == Blocks.air;
    // }

    // public boolean tryTapPlayer512(float x, float y){
    //     if(canTapPlayer512(x, y)){
    //         droppingItem = true;
    //         return true;
    //     }
    //     return false;
    // }

    // public boolean canTapPlayer512(float x, float y){
    //     // no comments
    //     return player.within(x, y, playerSelectRange) && player.unit().stack.amount > 0;
    // }
}