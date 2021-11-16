package mindustry.ui.dialogs;

import arc.*;
import arc.util.*;
import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.event.*;
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
			if(ai instanceof BuilderAI && list.select() != player) ai = new MimicAI(){
				@Override
				public void updateTargeting(){
					if(retarget()) following = list.select().unit();
				}
			};

			if(ai instanceof DefenderAI && list.select() != player) ai = new DefenderAI(){
				@Override
				public void updateTargeting(){
					if(retarget()) target = list.select().unit();
				}
			};

			if(ai instanceof AwaitingAI) ai = new AwaitingAI(){
				@Override
				public void updateTargeting(){
					if(retarget()) target = list.select().unit();
				}
			};

			updateUnit();
		});

		template(null, null, false);
		template(UnitTypes.mono, new MinerAI(), false);
		template(UnitTypes.poly, new BuilderAI(), true);
		template(UnitTypes.mega, new RepairAI(), false);
		template(UnitTypes.oct, new DefenderAI(), true);
		template(UnitTypes.crawler, new SuicideAI(), false);
		template(UnitTypes.dagger, new GroundAI(), false);
		template(UnitTypes.flare, new FlyingAI(), false);
		
		cont.table(table -> {
			list.build(table);
			table.add(content).padLeft(16f);
		}).row();
		cont.labelWrap("You can select player for some ai like oct or poly...");
		
		Events.on(WorldLoadEvent.class, event -> ai = null);
		Events.on(UnitChangeEvent.class, event -> updateUnit());
	}

	private void template(UnitType icon, AIController ai, boolean show){
		var draw = icon != null ? new TextureRegionDrawable(icon.icon(Cicon.tiny)) : Icon.none;
		content.button(draw, () -> {
			list.get().touchable(show ? Touchable.enabled : Touchable.disabled);
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

	public void deselect(){
		ai = null;
	}

	public void updateUnit(){
		if(ai != null) ai.unit(player.unit());
	}
}