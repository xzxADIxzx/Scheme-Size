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

import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;

public class MobileInput512 extends MobileInput{

    @Override
    public void drawTop(){
        if(mode == schematicSelect){
            drawSelection(lineStartX, lineStartY, lastLineX, lastLineY, 512);
        }
    }
}