package mindustry.ui.dialogs;

import arc.*;
import arc.func.*;
import arc.scene.ui.layout.*;
import mindustry.ui.fragments.*;
import mindustry.gen.*;
import mindustry.game.*;

public class TeamSelectDialog extends BaseDialog{

	public Cons2<Player, Team> callback;

	private Table team = new Table();
	private PlayerSelectFragment list = new PlayerSelectFragment();

	public TeamSelectDialog(String name){
		super(name);
		addCloseButton();

		template("team-derelict", Team.derelict);
		template("team-sharded", Team.sharded);
		template("team-crux", Team.crux);
		template("status-electrified-ui", Team.green);
		template("status-spore-slowed-ui", Team.purple);
		template("status-wet-ui", Team.blue);

		list.build(cont);
		cont.add(team).padLeft(16f);
	}

	private void template(String icon, Team team){
		var draw = Core.atlas.drawable(icon);
		this.team.button(draw, () -> {
			callback.get(list.select(), team);
			hide();
		}).size(64).row();
	}

	public void select(Cons2<Player, Team> callback){
		this.callback = callback;
		list.rebuild();
		show();
	}
}