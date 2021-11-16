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
		template("@keycomb.reset_ai", "keybind.change_ai.name");

		cont.label(() -> "@category.bt.name").color(Color.gray).padTop(10f).row();
		cont.image().color(Color.gray).fillX().height(3f).padBottom(6f).row();

		template("@keycomb.toggle_bt", "keybind.deselect.name");
		template("@keycomb.return", "keybind.schematic_menu.name");
	}

	private void template(String name, String comb){
		String sec = Core.bundle.get(comb);
		cont.add(name, Color.white).left().padRight(20f).padLeft(8f);
		cont.add(main + " + " + sec, Pal.accent).left().minWidth(90f).padRight(20f).row();
	}
}