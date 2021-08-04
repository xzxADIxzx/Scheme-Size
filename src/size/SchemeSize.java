package size;

import mindustry.mod.*;
import mindustry.Vars;
import Schematics;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

public class SchemeSize extends Mod{

    public SchemeSize(){
        
    }

    @Override
    public void loadContent(){
        Vars.schematics = new Schematics();
    }

}