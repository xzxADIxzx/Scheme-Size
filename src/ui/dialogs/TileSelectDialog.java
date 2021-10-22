package mindustry.ui.dialogs;

import arc.util.*;
import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.style.*;
import arc.graphics.g2d.*;
import mindustry.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.content.*;
import mindustry.graphics.*;

public class TileSelectDialog extends BaseDialog{

	public Cons3<Floor, Block, Floor> callback;

	private int cat = 0;
	private Table category = new Table();
	private Table content = new Table();

	private Floor floor = Blocks.air.asFloor();
	private Block block = Blocks.air;
	private Floor overlay = Blocks.air.asFloor();

	private Image floorImg;
	private Image blockImg;
	private Image overlayImg;

	public TileSelectDialog(String name){
		super(name);
		addCloseButton();

		cont.add(category).size(288f, 270f).left();
		cont.add(content).growX();
		cont.table().width(288f).right();

		floorImg = template("@tile.floor", 0);
		blockImg = template("@tile.block", 1);
		overlayImg = template("@tile.overlay", 2);

		content.table(floor -> {
			Vars.content.blocks().each(b -> {
				if(!(b instanceof Floor) || b instanceof OreBlock ||  b.id < 1) return;
				var drawable = new TextureRegionDrawable(b.icon(Cicon.full));
				floor.button(drawable, () -> { 
					this.floor = b.asFloor();
				}).size(64f);

				if(floor.getChildren().count(i -> true) % 10 == 9) floor.row();
			});
		}).visible(() -> cat == 0);

		content.table(block -> {
			Vars.content.blocks().each(b -> {
				if(!(b instanceof StaticWall)) return;
				var drawable = new TextureRegionDrawable(b.icon(Cicon.full));
				block.button(drawable, () -> { 
					this.block = b;
				}).size(64f);

				if(block.getChildren().count(i -> true) % 10 == 9) block.row();
			});
		}).visible(() -> cat == 1);

		content.table(overlay -> {
			Vars.content.blocks().each(b -> {
				if(!(b instanceof OreBlock)) return;
				var drawable = new TextureRegionDrawable(b.icon(Cicon.full));
				overlay.button(drawable, () -> { 
					this.overlay = b.asFloor();
				}).size(64f);

				if(overlay.getChildren().count(i -> true) % 10 == 9) overlay.row();
			});
		}).visible(() -> cat == 2);
	}

	private Image template(String name, int cat){
		Button check = new Button(Styles.transt);
		check.changed(() -> this.cat = cat);
		Image img;

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
		icon.add(img = new Image().setScaling(Scaling.bounded)).pad(8f).grow();

		check.add(icon).size(74f);
		check.table(t -> {
			t.labelWrap(name).growX().row();
			t.image().height(4f).color(Pal.gray).growX().bottom().padTop(4f);
		}).size(170f, 74f).pad(10f);

		category.add(check).checked(t -> this.cat == cat).size(264f, 74f).padBottom(16f).row();
		return img;
	}

	public void select(boolean show, Cons3<Floor, Block, Floor> callback){
		this.callback = callback;
		if(show) show();
		else callback.get(floor, block, overlay);

		floorImg.setDrawable(floor.icon(Cicon.full));
		blockImg.setDrawable(block.icon(Cicon.full));
		overlayImg.setDrawable(overlay.icon(Cicon.full));
	}
}