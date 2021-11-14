package mindustry.ui.dialogs;

import arc.*;
import arc.util.*;
import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.style.*;
import arc.graphics.g2d.*;
import mindustry.*;
import mindustry.ui.*;
import mindustry.ui.fragments.*;
import mindustry.ai.types.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.content.*;
import mindustry.entities.units.*;

import static mindustry.Vars.*;

public class AISelectDialog extends BaseDialog{

	private AIController ai;
	private Table content = new Table();
	private PlayerSelectFragment list = new PlayerSelectFragment();

	public AISelectDialog(String name){
		super(name);
		addCloseButton();

		hidden(() -> {
			// move to selected player
			if(ai instanceof DefenderAI && list.select() != player) ai = new DefenderAI(){
				@Override
				public void updateTargeting(){
					if(retarget()) target = list.select().unit();
				}
			};

			updateUnit();
		});

		template(null, null);
		template(UnitTypes.mono, new MinerAI());
		template(UnitTypes.poly, new BuilderAI());
		template(UnitTypes.mega, new RepairAI());
		template(UnitTypes.oct, new DefenderAI());
		template(UnitTypes.crawler, new SuicideAI());
		
		list.build(cont);
		cont.add(content).padLeft(16f).row();
		cont.labelWrap("You can select player for some ai like oct or poly...\nTo deselect ai press [accent]alternative + change ai[]");

		Events.on(WorldLoadEvent.class, event -> ai = null);
		Events.on(UnitChangeEvent.class, event -> updateUnit());
	}

	private void template(UnitType icon, AIController ai){
		var draw = icon != null ? new TextureRegionDrawable(icon.icon(Cicon.tiny)) : Icon.none;
		content.button(draw, () -> {
			this.ai = ai;
			hide();
		}).size(64).row();
	}

	public boolean select(boolean show){
		if(show){
			list.rebuild();
			show();
		}else{
			if(ai == null) return false;
			ai.updateUnit();
		}
		return true;
	}

	public void updateUnit(){
		if(ai != null) ai.unit(player.unit());
	}
}