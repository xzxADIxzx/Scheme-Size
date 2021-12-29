package mindustry.ui.dialogs;

import arc.*;
import arc.util.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.scene.event.*;
import arc.scene.style.*;
import mindustry.ai.types.*;
import mindustry.ai.formations.*;
import mindustry.ai.formations.patterns.*;
import mindustry.ui.fragments.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.game.EventType.*;
import mindustry.content.*;
import mindustry.entities.units.*;

import static mindustry.Vars.*;

public class AISelectDialog extends BaseDialog{

	private AIController ai;
	private Table content = new Table();
	private PlayerSelectFragment list = new PlayerSelectFragment();
	private Runnable onHidden;

	public AISelectDialog(String name){
		super(name);
		addCloseButton();

		hidden(onHidden = () -> {
			if(ai instanceof BuilderAI && list.select() != player) ai = new FormationAI(
				list.select().unit(),
				new Formation(new Vec3(), new CircleFormation())
			){
				@Override
				public void updateUnit(){
					super.updateUnit();
					updateTargeting();
					updateMovement();
				}

				@Override
				public void updateTargeting(){
					if(retarget()) leader = list.select().unit();
				}

				@Override
				public void updateMovement(){
					player.boosting = leader.isFlying();
					if(unit.dst(leader) < formationSize()) return;

					Tmp.v1.set(leader.x, leader.y).sub(unit.x, unit.y);
					float length = Mathf.clamp((unit.dst(leader) - formationSize()) / 40f, -1f, 1f);

					Tmp.v1.setLength(unit.speed() * length * Time.delta);
					unit.vel.set(Tmp.v1);
				}
			};

			if(ai instanceof RepairAI && list.select() != player) ai = new CursorAI(){
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

			if(ai instanceof CircleAI c) c.init(list.select());

			updateUnit();
		});

		template(null, null, false);
		template(UnitTypes.mono, new MinerAI(), false);
		template(UnitTypes.poly, new BuilderAI(), true);
		template(UnitTypes.mega, new RepairAI(), true);
		template(UnitTypes.oct, new DefenderAI(), true);
		template(UnitTypes.crawler, new SuicideAI(), false);
		template(UnitTypes.dagger, new GroundAI(), false);
		template(UnitTypes.flare, new FlyingAI(), false);
		template(UnitTypes.gamma, new CircleAI(), true);
		
		cont.table(table -> {
			list.build(table);
			table.add(content).padLeft(16f);
		}).row();
		cont.labelWrap("@aiselect.tooltip").labelAlign(2, 8).padTop(16f).width(368f);
		
		Events.on(WorldLoadEvent.class, event -> ai = null);
		Events.on(UnitChangeEvent.class, event -> updateUnit());
	}

	private void template(UnitType icon, AIController ai, boolean show){
		var draw = icon != null ? new TextureRegionDrawable(icon.uiIcon) : Icon.none;
		content.button(draw, () -> {
			list.get().touchable(show ? Touchable.enabled : Touchable.disabled);
			this.ai = ai;
			updateUnit();
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

	public void gotoppl(Player ppl){
		ai = new DefenderAI();
		list.player = ppl;
		onHidden.run();;
	}
}