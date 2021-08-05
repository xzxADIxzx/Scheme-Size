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

import static arc.Core.*;
import static mindustry.Vars.net;
import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;

public class DesktopInput512 extends DesktopInput{

    @Override
    public void drawTop(){
        Lines.stroke(1f);
        int cursorX = tileX512(Core.input.mouseX());
        int cursorY = tileY512(Core.input.mouseY());

        if(mode == breaking){
            drawBreakSelection(selectX, selectY, cursorX, cursorY, !Core.input.keyDown(Binding.schematic_select) ? maxLength : Vars.maxSchematicSize);
        }

        if(Core.input.keyDown(Binding.schematic_select) && !Core.scene.hasKeyboard() && mode != breaking){
            drawSelection(schemX, schemY, cursorX, cursorY, 512);

            // Show Size
            NormalizeDrawResult normalized = Placement.NormalizeDrawResult(block, schemX, schemY, cursorX, cursorY, false, 512, 1);
            int sizeX = normalized.x = normalized.x2;
            int sizeY = normalized.y = normalized.y2;
            String info = sizeX.toString() + ", " + sizeY.toString()
            ui.showLabel(info, 1, cursorX * 10, cursorY * 10)
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