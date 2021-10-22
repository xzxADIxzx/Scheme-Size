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

	private Floor floor = BLocks.air;
	private Block block = BLocks.air;
	private Floor overlay = BLocks.air;

	private Image floorImg;
	private Image blockImg;
	private Image overlayImg;

	public TileSelectDialog(String name){
		super(name);
		addCloseButton();

		cont.add(category).size(288f, 270f).left();
		cont.add(content).growX();
		cont.table().width(288f).right();

		template(floorImg, "@tile.floor", 0);
		template(blockImg, "@tile.block", 1);
		template(overlayImg, "@tile.overlay", 2);

		content.table(overlay -> {
			Vars.content.blocks().each(block -> {
				if(block instanceof OreBlock == false) return;
				var drawable = new TextureRegionDrawable(block.icon(Cicon.full));
				overlay.button(drawable, () -> { 
					this.overlay = block.asFloor();
				}).size(64f);
				if(overlay.getChildren().count(i -> true)) overlay.row();
			});
		}).visible(() -> cat == 2);
	}

	private void template(Image img, String name, int cat){
		Button check = new Button(Styles.transt);
		check.changed(() -> this.cat = cat);

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