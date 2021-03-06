package mindustry.ui.dialogs;

import arc.util.*;
import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.style.*;
import arc.graphics.g2d.*;
import mindustry.*;
import mindustry.ui.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.content.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class TileSelectDialog extends BaseDialog{

	public Cons3<Floor, Block, Floor> callback;

	private int selected = 0;
	private Table category = new Table();
	private Table content = new Table();

	private Block floor;
	private Block block;
	private Block overlay;

	private Image floorImg;
	private Image blockImg;
	private Image overlayImg;

	private int row = mobile ? 8 : 10;

	public TileSelectDialog(String name){
		super(name);
		addCloseButton();

		cont.add(category).size(288f, 270f).left();
		cont.add(content).growX();
		cont.table().width(288f).right();

		floorImg = template("@tile.floor", 0, b -> !(b instanceof Floor) || b instanceof OverlayFloor, b -> floor = b);
		blockImg = template("@tile.block", 1, b -> !(b instanceof StaticWall), b -> block = b);
		overlayImg = template("@tile.overlay", 2, b -> !(b instanceof OverlayFloor), b -> overlay = b);
	}

	private void rebuild(Boolf<Block> skip, Cons<Block> callback){
		content.clear();
		content.table(table -> {
			table.button(Icon.none, () -> { 
				callback.get(null);
				updateimg();
			}).size(64f);
			table.button(Icon.line, () -> { 
				callback.get(Blocks.air);
				updateimg();
			}).size(64f);

			Vars.content.blocks().each(block -> {
				if(skip.get(block) || block.id < 2) return;

				var drawable = new TextureRegionDrawable(block.uiIcon);
				table.button(drawable, () -> { 
					callback.get(block);
					updateimg();
				}).size(mobile ? 58f : 64f);

				if((table.getChildren().count(i -> true) - 1) % row == row - 1) table.row();
			});
		});
	}

	private Image template(String name, int select, Boolf<Block> skip, Cons<Block> callback){
		Button check = new Button(Styles.transt);
		check.changed(() -> {
			selected = select;
			rebuild(skip, callback);
		});

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

		category.add(check).checked(t -> selected == select).size(264f, 74f).padBottom(16f).row();

		if(selected == select) check.change();
		return img;
	}

	private void updateimg(){
		floorImg.setDrawable(getIcon(floor));
		blockImg.setDrawable(getIcon(block));
		overlayImg.setDrawable(getIcon(overlay));
	}

	private TextureRegionDrawable getIcon(Block block){
		// bruh
		return block == null ? new TextureRegionDrawable(Icon.none) : block == Blocks.air ? new TextureRegionDrawable(Icon.line) : new TextureRegionDrawable(block.uiIcon);
	}

	private Floor asFloor(Block block){
		return block == null ? null : block.asFloor();
	}

	public void select(boolean show, Cons3<Floor, Block, Floor> callback){
		this.callback = callback;
		if(show) show();
		else callback.get(asFloor(floor), block, asFloor(overlay));
		updateimg();
	}
}