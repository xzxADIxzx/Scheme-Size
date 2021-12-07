package mindustry.ui.dialogs;

import arc.*;
import arc.KeyBinds.*;
import arc.scene.ui.*;
import arc.graphics.*;
import mindustry.input.*;
import mindustry.graphics.*;

public class KeybindCombinationsDialog extends BaseDialog{

	public String main;
	public boolean show;

	public KeybindCombinationsDialog(){
		super("@keycomb.name");
		addCloseButton();

		main = Core.bundle.get("keycomb.main");

		template("@keycomb.view_comb", Binding.block_info);
		template("@keycomb.teleport", Binding.select);
		template("@keycomb.self_dest", Binding.respawn);
		template("@keycomb.spawn_unit", ModBinding.change_unit);
		template("@keycomb.reset_ai", ModBinding.change_ai);
		template("@keycomb.free_pan", Binding.pan);

		cont.label(() -> "@category.bt.name").color(Color.gray).padTop(10f).row();
		cont.image().color(Color.gray).fillX().height(3f).padBottom(6f).row();

		template("@keycomb.toggle_bt", Binding.deselect);
		template("@keycomb.return", Binding.schematic_menu);

		cont.check("show keycodes", value -> show = value);
	}

	private void template(String name, KeyBind key){
		String sec = Core.bundle.get("keybind." + key.name() + ".name");
		Label label = new Label(() -> show ? sec.toString() : main + " + " + sec);

		label.setColor(Pal.accent);

		cont.add(name, Color.white).left().padRight(20f).padLeft(8f);
		cont.add(label).left().minWidth(90f).padRight(20f).row();
	}
}