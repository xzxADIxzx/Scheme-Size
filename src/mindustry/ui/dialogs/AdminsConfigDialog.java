package mindustry.ui.dialogs;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.event.*;
import arc.graphics.*;
import mindustry.ui.*;

import static arc.Core.*;

public class AdminsConfigDialog extends BaseDialog{
	
	public boolean enabled = settings.getBool("enabledsecret", false);
	public boolean isAdmin = settings.getBool("adminssecret", false);
	public boolean usejs = settings.getBool("usejs", true);

	public AdminsConfigDialog(){
		super("@secret.name");
		addCloseButton();

		hidden(() -> {
			settings.put("enabledsecret", enabled);
			settings.put("adminssecret", isAdmin);
			settings.put("usejs", usejs);
		});

		new Table(table -> {
			table.touchable = Touchable.disabled;

			Label text = table.labelWrap("").style(Styles.outlineLabel).padLeft(33f).growX().left().get();
			Slider lever = new Slider(0, 1, 1, false);
			lever.moved(value -> {
				enabled = value == 1;
				text.setText(bundle.format("secret.use.name", bundle.get(enabled ? "secret.use.enabled" : "secret.use.disabled")));
			});
			lever.setValue(enabled ? 1 : 0);
			lever.change();

			cont.stack(lever, table).width(320).row();
		});

		cont.label(() -> "@secret.who.name").padTop(16f).row();
		cont.table(table -> {
			table.check("@secret.who.server", value -> isAdmin = !value).disabled(t -> !enabled).checked(t -> !isAdmin).left().row();
			table.check("@secret.who.admin", value -> isAdmin = value).disabled(t -> !enabled).checked(t -> isAdmin).left().row();
		}).left().row();

		cont.label(() -> "@secret.way.name").padTop(16f).row();
		cont.table(table -> {
			table.check("@secret.way.js", value -> usejs = value).disabled(t -> !enabled || !isAdmin).checked(t -> usejs).left().row();
			table.check("@secret.way.secret", value -> usejs = !value).disabled(t -> !enabled || !isAdmin).checked(t -> !usejs).left().row();
		}).left().row();

		cont.labelWrap("").labelAlign(2, 8).padTop(16f).size(320f, 120f).update(t -> {
			t.setText(enabled && isAdmin ? (usejs ? "@secret.way.js.description" : "@secret.way.secret.description") : "");
		}).get().getStyle().fontColor = Color.lightGray;;
	}
}