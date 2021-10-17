package mindustry.ui.dialogs;

import arc.util.*;
import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.graphics.g2d.*;
import mindustry.ui.*;
import mindustry.gen.*;
import mindustry.game.*;

public class ModTeamSelectDialog extends BaseDialog{

	public Cons2<Team, Player> callback;
	public Player player;

	private Table list = new Table();
	private Table team = new Table();

	public ModTeamSelectDialog(String name){
		super(name);
		addCloseButton();

		cont.add(list).padRight(16);
	}

	private void rebuild(){
		list.clear();
		Groups.player.each(player -> {
			CheckBox check = new CheckBox(player.name, Styles.cleart);
			check.checked(() -> this.player == player);
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

			list.add(check).row();
		});
	}

	public void select(Cons2<Team, Player> callback){
		this.callback = callback;
		rebuild();
		show();
	}
}