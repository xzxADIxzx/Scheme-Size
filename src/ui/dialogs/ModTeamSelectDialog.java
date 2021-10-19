package mindustry.ui.dialogs;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.graphics.g2d.*;
import mindustry.*;
import mindustry.ui.*;
import mindustry.ui.fragments.*;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.graphics.*;

public class ModTeamSelectDialog extends BaseDialog{

	public Cons2<Team, Player> callback;

	private Table team = new Table();
	private MiniListFragment list = new MiniListFragment();

	public ModTeamSelectDialog(String name){
		super(name);
		addCloseButton();

		template("team-derelict", Team.derelict);
		template("team-sharded", Team.sharded);
		template("team-crux", Team.crux);
		template("status-electrified-ui", Team.green);
		template("status-spore-slowed-ui", Team.purple);
		template("status-wet-ui", Team.blue);

		list.build(cont);
		list.get().padRight(16f);
		cont.add(team);
	}

	private void template(String icon, Team team){
		var draw = Core.atlas.drawable(icon);
		this.team.button(draw, () -> {
			callback.get(team, player);
			hide();
		}).size(64).row();
	}

	public void select(Cons2<Team, Player> callback){
		this.callback = callback;
		this.player = Vars.player;
		list.rebuild();
		show();
	}
}