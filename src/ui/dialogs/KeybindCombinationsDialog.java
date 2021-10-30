package mindustry.ui.dialogs;

import arc.*;
import arc.graphics.*;
import mindustry.graphics.*;

public class KeybindCombinationsDialog extends BaseDialog{

	public String main;

	public KeybindCombinationsDialog(){
		super("@keycomb.name");
		addCloseButton();

		main = Core.bundle.get("keycomb.main");

		template("@keycomb.view_comb", "keybind.block_info.name");
		template("@keycomb.teleport", "keybind.select.name");
		template("@keycomb.self_dest", "keybind.respawn.name");
		template("@keycomb.spawn_unit", "keybind.change_unit.name");
		template("@keycomb.toggle_bt", "keybind.deselect.name");
	}

	private void template(String name, String comb){
		String sec = Core.bundle.get(comb);
		cont.add(name, Color.white).left().padRight(20).padLeft(8);
		cont.add(main + " + " + sec, Pal.accent).left().minWidth(90).padRight(20);
		cont.row();
	}
}