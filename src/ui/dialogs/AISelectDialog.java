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
			if(ai instanceof DefenderAI && list.select() != player) ai = new DefenderAI(){
				@Override
				public void updateTargeting(){
					target = list.select();
				}
			}
		});

		template(null, null);
		template(UnitTypes.mono, new MinerAI());
		template(UnitTypes.poly, new BuilderAI());
		template(UnitTypes.mega, new RepairAI());
		template(UnitTypes.oct, new DefenderAI());
		template(UnitTypes.crawler, new SuicideAI());
		
		list.build(cont);
		cont.add(content).padLeft(16f);

		Events.on(WorldLoadEvent.class, event -> ai = null);
		Events.on(UnitChangeEvent.class, event -> {
			if(ai != null) ai.unit(Vars.player.unit());
		});
	}

	private void template(UnitType icon, AIController ai){
		var draw = icon != null ? new TextureRegionDrawable(icon.icon(Cicon.tiny)) : Icon.none;
		content.button(draw, () -> {
			if(ai != null) ai.unit(player.unit());
			this.ai = ai;
		}).size(64).row();
	}

	public boolean select(boolean show, Boolf2<Player, AIController> callback){
		if(show){
			list.rebuild();
			show();
		}else{
			callback.get(list.select(), ai);
			return ai != null;
		}
		return false;
	}
}