package mindustry.ui.dialogs;

import arc.util.*;
import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.style.*;
import arc.graphics.g2d.*;
import mindustry.ui.*;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.graphics.*;

public class ModTeamSelectDialog extends BaseDialog{

	public Cons2<Team, Player> callback;
	public Player player;

	private Table list = new Table();
	private Table team = new Table();

	public ModTeamSelectDialog(String name){
		super(name);
		addCloseButton();

		template("team-derelict", Team.derelict);
		template("team-sharded", Team.sharded);
		template("team-crux", Team.crux);
		template("team-green", Team.green);
		template("team-purple", Team.purple);
		template("team-blue", Team.blue);

		cont.add(list).padRight(16);
		cont.add(team).padRight(16);
	}

	private void rebuild(){
		list.clear();
		Groups.player.each(player -> {
			CheckBox check = new CheckBox(player.name, (TextButton.TextButtonStyle)Styles.cleart);
			check.changed(() -> this.player = player);

			Table icon = new Table(){
                @Override
                public void draw(){
                    super.draw();
                    Draw.color(Pal.gray);
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(4f));
                    Lines.rect(x, y, width, height);
                    Draw.reset();
                }
            };
            icon.margin(8);
            icon.add(new Image(player.icon()).setScaling(Scaling.bounded)).grow();

            check.add(icon).size(74);
            check.label(() -> player.name);

			list.add(check).checked(() -> this.player == player).row();
		});
	}

	private void template(String icon, Team team){
		Drawable draw = Core.atlas.drawable(icon);
		team.button(draw, () -> {
			callback.get(team, player);
			dialog.hide();
		}).size(64).row();
	}

	public void select(Cons2<Team, Player> callback){
		this.callback = callback;
		rebuild();
		show();
	}
}