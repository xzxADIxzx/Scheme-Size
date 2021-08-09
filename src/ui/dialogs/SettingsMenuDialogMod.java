package mindustry.ui.dialogs;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.input.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.content.TechTree.*;
import mindustry.core.GameState.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.ui.*;

import java.io.*;
import java.util.zip.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class SettingsMenuDialogMod extends SettingsMenuDialog{
	public SettingsTable scheme;

	void rebuildMenuMod(){
        menu.row();
        menu.button("@settings.scheme", style, () -> visible(3));
    }

	public void addModSettings(){
		scheme.sliderPref("copysize", 512, 32, 512, 32, i -> i + " blocks");
        scheme.sliderPref("breaksize", 512, 32, 512, 32, i -> i + " blocks");
        scheme.checkPref("copyshow", true);
        scheme.checkPref("destshow", true);
        // TODO: Add bundles
	}
}