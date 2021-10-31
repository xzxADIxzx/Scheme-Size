package mindustry.ui.dialogs;

import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.style.*;
import arc.struct.*;
import mindustry.ui.*;
import mindustry.ui.fragments.*;
import mindustry.gen.*;
import mindustry.ctype.*;

import static mindustry.Vars.*;

public class ContentSelectDialog<T extends UnlockableContent> extends BaseDialog{

	public Cons3<Player, T, Floatp> callback;
	public Stringf format;

	private Cell label;
	private Cell slider;
	private PlayerSelectFragment list = new PlayerSelectFragment();

	public ContentSelectDialog(String name, Seq<T> content, float min, float max, float step, Stringf format){
		super(name);
		this.format = format;
		addCloseButton();

		var label = new Label("", Styles.outlineLabel);
		var slider = new Slider(min, max, step, false);
		slider.moved(value -> {
			label.setText(format.get(value));
		});
		slider.change();

		var table = new Table();
		content.each(item -> {
			if(item.isHidden()) return;

			var drawable = new TextureRegionDrawable(item.icon(Cicon.tiny));
			table.button(drawable, () -> { 
				callback.get(list.select(), item, () -> slider.getValue());
				hide(); 
			}).size(mobile ? 52f : 64f);

			if(item.id % mobile ? 5 : 10 == mobile ? 4 : 9) table.row();
		});

		list.build(cont);
		list.get().left();

		cont.table(t -> {
			t.add(table).row();
			this.label = t.add(label).center().padTop(16f);
			t.row();
			this.slider = t.add(slider).fillX();
		}).growX();
		cont.table().width(288f).right();
	}

	public void select(boolean showSL, boolean showP, Cons3<Player, T, Floatp> callback){
		this.callback = callback;
		label.visible(showSL);
		slider.visible(showSL);
		list.get().visible(showP);
		list.rebuild();
		show();
	}

	public interface Stringf{
        String get(float f);
    }
}