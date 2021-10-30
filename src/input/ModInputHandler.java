package mindustry.input;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.input.GestureDetector.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.formations.patterns.*;
// import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.game.Teams.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.Placement.*;
import mindustry.net.Administration.*;
import mindustry.net.*;
import mindustry.type.*;
import mindustry.ui.fragments.*;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

// Last Update - Oct 13, 2021
public class ModInputHandler extends InputHandler{

	final static float playerSelectRange = mobile ? 17f : 11f;
    final static Rect r1 = new Rect(), r2 = new Rect();

    public BuildingTools bt = new BuildingTools(this);

    public boolean mobilePanCam = false;
    public boolean mobileDisWpn = false;

    public void drawSelectionMod(int x1, int y1, int x2, int y2, int size){
        drawSelection(x1, y1, x2, y2, size);

        // Show Size
        if(Core.settings.getBool("copyshow")){
            NormalizeResult result = Placement.normalizeArea(x1, y1, x2, y2, 0, false, size);

            int sizeX = result.x2 - result.x;
            int sizeY = result.y2 - result.y;
            String strSizeX = sizeX == size ? "[accent]" + String.valueOf(++sizeX) + "[]" : String.valueOf(++sizeX);
            String strSizeY = sizeY == size ? "[accent]" + String.valueOf(++sizeY) + "[]" : String.valueOf(++sizeY);
            String info = strSizeX + ", " + strSizeY;
            ui.showLabel(info, 0.02f, x2 * tilesize + (mobile ? 0 : 16), y2 * tilesize + (mobile ? 32 : -16));
        }
    }

    public void drawBreakSelectionMod(int x1, int y1, int x2, int y2, int size){
        drawBreakSelection(x1, y1, x2, y2, size);

        // Show Size
        if(Core.settings.getBool("breakshow")){
            NormalizeResult result = Placement.normalizeArea(x1, y1, x2, y2, 0, false, size);

            int sizeX = result.x2 - result.x;
            int sizeY = result.y2 - result.y;
            String strSizeX = sizeX == size ? "[accent]" + String.valueOf(++sizeX) + "[]" : String.valueOf(++sizeX);
            String strSizeY = sizeY == size ? "[accent]" + String.valueOf(++sizeY) + "[]" : String.valueOf(++sizeY);
            String info = strSizeX + ", " + strSizeY;
            ui.showLabel(info, 0.02f, x2 * tilesize + (mobile ? 0 : 16), y2 * tilesize + (mobile ? 32 : -16));
        }
    }

    public void drawEditSelectionMod(int x1, int y1, int x2, int y2, int size){
        NormalizeDrawResult result = Placement.normalizeDrawArea(Blocks.air, x1, y1, x2, y2, false, size, 1f);

        Lines.stroke(2f);

        Draw.color(Pal.darkerMetal);
        Lines.rect(result.x, result.y - 1, result.x2 - result.x, result.y2 - result.y);
        Draw.color(Pal.darkMetal);
        Lines.rect(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
    }

    public void drawOverRequestMod(BuildPlan request){
        boolean valid = validPlace(request.x, request.y, request.block, request.rotation);

        Draw.reset();
        Draw.mixcol(!valid ? Pal.breakInvalid : Color.white, (!valid ? 0.4f : 0.24f) + Mathf.absin(Time.globalTime, 6f, 0.28f));
        Draw.alpha(1f);
        request.block.drawRequestConfigTop(request, cons -> {
            selectRequests.each(cons);
            lineRequests.each(cons);
        });
        Draw.reset();
    }

    public void showSchematicSaveMod(){
        if(lastSchematic == null) return;

        ui.showTextInput("@schematic.add", "@name", "", text -> {
            Schematic replacement = schematics.all().find(s -> s.name().equals(text));
            if(replacement != null){
                ui.showConfirm("@confirm", "@schematic.replace", () -> {
                    schematics.overwrite(replacement, lastSchematic);
                    ui.showInfoFade("@schematic.saved");
                    ui.schematics.showInfo(replacement);
                });
            }else{
                lastSchematic.tags.put("name", text);
                lastSchematic.tags.put("description", "");
                schematics.add(lastSchematic);
                ui.showInfoFade("@schematic.saved");
                ui.schematics.showInfo(lastSchematic);
                Events.fire(new SchematicCreateEvent(lastSchematic));
            }
        });
    }

    public int rawTileXMod(){
        return World.toTile(Core.input.mouseWorld().x);
    }

    public int rawTileYMod(){
        return World.toTile(Core.input.mouseWorld().y);
    }

	public int tileXMod(float cursorX){
        Vec2 vec = Core.input.mouseWorld(cursorX, 0);
        if(selectedBlock()){
            vec.sub(block.offset, block.offset);
        }
        return World.toTile(vec.x);
    }

    public int tileYMod(float cursorY){
        Vec2 vec = Core.input.mouseWorld(0, cursorY);
        if(selectedBlock()){
            vec.sub(block.offset, block.offset);
        }
        return World.toTile(vec.y);
    }

    public Tile tileAtMod(float x, float y){
        return world.tile(tileXMod(x), tileYMod(y));
    }

    public boolean tileTappedMod(@Nullable Building build){
        if(build == null){
            frag.inv.hide();
            frag.config.hideConfig();
            return false;
        }
        boolean consumed = false, showedInventory = false;

        //check if tapped block is configurable
        if(build.block.configurable && build.interactable(player.team())){
            consumed = true;
            if((!frag.config.isShown() && build.shouldShowConfigure(player)) //if the config fragment is hidden, show
            //alternatively, the current selected block can 'agree' to switch config tiles
            || (frag.config.isShown() && frag.config.getSelectedTile().onConfigureTileTapped(build))){
                Sounds.click.at(build);
                frag.config.showConfig(build);
            }
            //otherwise...
        }else if(!frag.config.hasConfigMouse()){ //make sure a configuration fragment isn't on the cursor
            //then, if it's shown and the current block 'agrees' to hide, hide it.
            if(frag.config.isShown() && frag.config.getSelectedTile().onConfigureTileTapped(build)){
                consumed = true;
                frag.config.hideConfig();
            }

            if(frag.config.isShown()){
                consumed = true;
            }
        }

        //call tapped event
        if(!consumed && build.interactable(player.team())){
            build.tapped();
        }

        //consume tap event if necessary
        if(build.interactable(player.team()) && build.block.consumesTap){
            consumed = true;
        }else if(build.interactable(player.team()) && build.block.synthetic() && (!consumed || build.block.allowConfigInventory)){
            if(build.block.hasItems && build.items.total() > 0){
                frag.inv.showFor(build);
                consumed = true;
                showedInventory = true;
            }
        }

        if(!showedInventory){
            frag.inv.hide();
        }

        return consumed;
    }

    public boolean canMineMod(Tile tile){
        return !Core.scene.hasMouse()
            && tile.drop() != null
            && player.unit().validMine(tile)
            && !((!Core.settings.getBool("doubletapmine") && tile.floor().playerUnmineable) && tile.overlay().itemDrop == null)
            && player.unit().acceptsItem(tile.drop())
            && tile.block() == Blocks.air;
    }

    public boolean tryBeginMineMod(Tile tile){
        if(canMineMod(tile)){
            player.unit().mineTile = tile;
            return true;
        }
        return false;
    }

    public boolean tryStopMineMod(Tile tile){
        if(player.unit().mineTile == tile){
            player.unit().mineTile = null;
            return true;
        }
        return false;
    }

    public boolean tryStopMineMod(){
        if(player.unit().mining()){
            player.unit().mineTile = null;
            return true;
        }
        return false;
    }

    public boolean canTapPlayerMod(float x, float y){
        return player.within(x, y, playerSelectRange) && player.unit().stack.amount > 0;
    }

    public boolean tryTapPlayerMod(float x, float y){
        if(canTapPlayerMod(x, y)){
            droppingItem = true;
            return true;
        }
        return false;
    }


    // mobile features
    public void toggleMobilePanCam(){
        mobilePanCam = !mobilePanCam;
    }

    public void toggleMobileDisWpn(){
        mobileDisWpn = !mobileDisWpn;
    }


    // helpfull methods
    public boolean isAdmin(){
        return Core.settings.getBool("adminssecret") && !Core.settings.getBool("usejs");
    }

    public void apply(){
        flushRequests(bt.plan);
        bt.plan.clear();
    }
}