package mindustry.ui.dialogs;

import arc.scene.ui.*;
import arc.scene.event.*;
import arc.graphics.*;
import mindustry.ui.*;

import static arc.Core.*;

public class SecretConfigDialog extends BaseDialog{
	
	private Label description;
	
	public boolean enabled = settings.getBool("enabledsecret", false);
	public boolean isAdmin = settings.getBool("adminssecret", false);
	public boolean usejs = settings.getBool("usejs", true);

	public SecretConfigDialog(){
		super("@secret.name");
		addCloseButton();
		closeOnBack(callback);

		new Table(table -> {
			table.touchable = Touchable.disabled;

			Label text = table.labelWrap("").style(Styles.outlineLabel).padLeft(33f).growX().left().get();
			Slider lever = new Slider(0, 1, 1, false);
			slider.moved(value -> {
				enabled = value.get() == 1;
				text.setText(bundle.format("@secret.use.name", enabled ? "@secret.use.enabled" : "@secret.use.disabled"));
			});
			slider.setValue(enabled ? 1 : 0);
			slider.change();

			cont.stack(lever, table).width(320).row()
		});

		cont.label("@secret.who.name").padTop(16f).row();
		cont.table(table -> {
			table.check("@secret.who.server", value -> isAdmin = !value).disabled(() -> !enabled).checked(() -> !isadmin).left().row();
			table.check("@secret.who.admin", value -> isAdmin = value).disabled(() -> !enabled).checked(() -> isadmin).left().row();
		}).left().row();

		cont.label("@secret.way.name").padTop(16f).row();
		cont.table(table -> {
			table.check("@secret.way.js", value -> { usejs = value; update(); }).disabled(() -> !enabled || !isAdmin).checked(() -> usejs).left().row();
			table.check("@secret.way.secret", value -> { usejs = !value; update(); }).disabled(() -> !enabled || !isAdmin).checked(() -> !usejs).left().row();
		}).left().row();

		description = cont.labelWrap("").labelAlign(2, 8).padTop(16f).size(320f, 120f).get();
		description.getStyle().fontColor = Color.lightGray;
		update();
	}
	
	private void callback(){
		settings.put("enabledsecret", enabled);
		settings.put("adminssecret", isAdmin);
		settings.put("usejs", usejs);
	}

	private void update(){
		description.setText(enabled && isAdmin ? (usejs ? "@secret.way.js.description" : "@secret.way.secret.description") : "");
	}
}