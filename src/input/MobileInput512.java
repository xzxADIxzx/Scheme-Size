package mindustry.input;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.input.GestureDetector.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.input.*;
import mindustry.input.Placement.*;

import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;

public class MobileInput512 extends MobileInput{

    @Override
    public void drawTop(){
        if(mode == schematicSelect){
            drawSelection(lineStartX, lineStartY, lastLineX, lastLineY, 512);

            // Show Size
            NormalizeResult normalized = Placement.normalizeArea(lineStartX, lineStartY, lastLineX, lastLineY, 0, false, 512);
            int sizeX = normalized.x2 - normalized.x + 1;
            int sizeY = normalized.y2 - normalized.y + 1;
            String info = Integer.toString(sizeX) + ", " + Integer.toString(sizeY);
            ui.showLabel(info, 0.02f, lastLineX * 8 + 16, lastLineY * 8 - 16);
        }
    }
}