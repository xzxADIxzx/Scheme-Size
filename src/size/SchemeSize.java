package mindustry.game;

import arc.*;
import arc.util.*;
import mindustry.mod.*;
import mindustry.Vars;
import mindustry.game.EventType.*;

// import mindustry.*;
// import mindustry.content.*;
// import mindustry.gen.*;
// import mindustry.ui.dialogs.*;

public class SchemeSize extends Mod{

    public SchemeSize(){
        Events.on(ClientLoadEvent.class, e -> {
            //wait 10 secs, because... idk
            Time.runTask(10f, () -> {
                Vars.schematics = new Schematics64();
            });
        });
    }
}