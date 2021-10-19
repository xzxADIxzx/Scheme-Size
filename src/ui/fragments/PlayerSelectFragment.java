package mindustry.ui.fragments;

import arc.*;
import arc.util.*;
import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.graphics.g2d.*;
import mindustry.*;
import mindustry.ui.*;
import mindustry.gen.*;
import mindustry.game.*;

public class PlayerSelectFragment extends Fragment{

	private Player player;
	private Cell pane;
	private Table list = new Table();

	@Override
    public void build(Group parent){
    	pane = parent.pane(list).scrollX(false);
    }

	public void rebuild(){
		player = Vars.player;

		list.clear();
		Groups.player.each(player -> {
			Button check = new Button(Styles.transt);
			check.changed(() -> this.player = player);

			Table icon = new Table(){
                @Override
                public void draw(){
                    super.draw();
                    Draw.color(check.isChecked() ? Pal.accent : Pal.gray);
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(4f));
                    Lines.rect(x, y, width, height);
                    Draw.reset();
                }
            };
            icon.add(new Image(player.icon()).setScaling(Scaling.bounded)).pad(8f).grow();
            icon.name = player.name();

            check.add(icon).size(74f);
            check.table(t -> {
            	t.labelWrap("[#" + player.color().toString().toUpperCase() + "]" + player.name()).growX().row();
            	t.image().height(4f).color(player.team().color).growX().bottom().padTop(4f);
            }).size(170f, 74f).pad(10f);

			list.add(check).checked(t -> this.player == player).size(264f, 74f).padBottom(16f).row();
		});
	}

	public Player get(){
		return player;
	}

	public Cell getPane(){
		return pane;
	}
}