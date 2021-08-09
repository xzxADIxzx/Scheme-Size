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

import static arc.Core.*;
import static mindustry.Vars.net;
import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;

import java.lang.Math;

public class DesktopInput512 extends DesktopInput{

    @Override
    public void drawTop(){
        Lines.stroke(1f);
        int cursorX = tileX512(Core.input.mouseX());
        int cursorY = tileY512(Core.input.mouseY());

        if(mode == breaking){
            int size = settings.getInt("breaksize");
            drawBreakSelection(selectX, selectY, cursorX, cursorY, size);

            // Show Size
            if(settings.getBool("breakshow")){
                NormalizeResult normalized = Placement.normalizeArea(selectX, selectY, cursorX, cursorY, 0, false, size);
                int sizeX = normalized.x2 - normalized.x + 1;
                int sizeY = normalized.y2 - normalized.y + 1;
                String strSizeX = sizeX == size ? "[accent]" + Integer.toString(sizeX) + "[]" : Integer.toString(sizeX);
                String strSizeY = sizeY == size ? "[accent]" + Integer.toString(sizeY) + "[]" : Integer.toString(sizeY);
                String info = strSizeX + ", " + strSizeY;
                ui.showLabel(info, 0.02f, cursorX * 8 + 16, cursorY * 8 - 16);
            }
        }

        if(Core.input.keyDown(Binding.schematic_select) && !Core.scene.hasKeyboard() && mode != breaking){
            int size = settings.getInt("copysize");
            drawSelection(schemX, schemY, cursorX, cursorY, size);

            // Show Size
            if(settings.getBool("copyshow")){
                NormalizeResult normalized = Placement.normalizeArea(schemX, schemY, cursorX, cursorY, 0, false, size);
                int sizeX = normalized.x2 - normalized.x + 1;
                int sizeY = normalized.y2 - normalized.y + 1;
                String strSizeX = sizeX == size ? "[accent]" + Integer.toString(sizeX) + "[]" : Integer.toString(sizeX);
                String strSizeY = sizeY == size ? "[accent]" + Integer.toString(sizeY) + "[]" : Integer.toString(sizeY);
                String info = strSizeX + ", " + strSizeY;
                ui.showLabel(info, 0.02f, cursorX * 8 + 16, cursorY * 8 - 16);
            }
        }

        Draw.reset();
    }

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
}