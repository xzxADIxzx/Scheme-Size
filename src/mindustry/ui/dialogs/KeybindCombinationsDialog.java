package mindustry.ui.dialogs;

import arc.*;
import arc.KeyBinds.*;
import arc.graphics.*;
import mindustry.input.*;
import mindustry.graphics.*;

public class KeybindCombinationsDialog extends BaseDialog{

	public String main;
	public String code;

	public KeybindCombinationsDialog(){
		super("@keycomb.name");
		addCloseButton();

		cont.marginLeft(24f);
	}

	/**
	 * DO NOT CALL BEFORE MDO BINDING HAS BEEN INITIALIZED
	 */
	public void init(){
		main = Core.bundle.get("keycomb.main");
		code = Core.keybinds.get(ModBinding.alternative).key.toString();

		template("@keycomb.view_comb", Binding.block_info);
		template("@keycomb.teleport", Binding.select);
		template("@keycomb.self_dest", Binding.respawn);
		template("@keycomb.spawn_unit", ModBinding.change_unit);
		template("@keycomb.reset_ai", ModBinding.change_ai);
		template("@keycomb.free_pan", Binding.pan);
		template("@keycomb.graphics", ModBinding.renderset);

		cont.label(() -> "@category.bt.name").color(Color.gray).padTop(10f).row();
		cont.image().color(Color.gray).fillX().height(3f).padBottom(6f).row();

		template("@keycomb.toggle_bt", Binding.deselect);
		template("@keycomb.return", Binding.schematic_menu);
	}

	private void template(String name, KeyBind bind){
		String key = Core.keybinds.get(bind).key.toString();
		String sec = Core.bundle.get("keybind." + bind.name() + ".name");

		cont.add(name, Color.white).left().padRight(20f);
		cont.add("", Pal.accent).left().minWidth(340f).padRight(20f).update(label -> {
			label.setText(label.hasMouse() ? code + " + " + key : main + " + " + sec);
		}).row();
	}
}