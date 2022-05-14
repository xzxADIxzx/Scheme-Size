package mindustry.ui.dialogs;

import arc.graphics.*;
import mindustry.scheme.SchemeVars;

import static arc.Core.*;
import static mindustry.Vars.*;

public class RenderSettingsDialog extends BaseDialog{

	public boolean shown;

	public RenderSettingsDialog(){
		super("@render.name");
		addCloseButton();

        cont.label(() -> "@render.bin.name").padTop(16f).row();
		cont.table(table -> {
            table.check("@render.bin.power", this::togglePowerLines).checked(t -> settings.getInt("lasersopacity") != 0).left().row();
			table.check("@render.bin.status", value -> settings.put("blockstatus", value)).checked(t -> settings.getBool("blockstatus")).left().row();
			table.check("@render.bin.light", value -> enableLight = value).checked(t -> enableLight).left().row();
			table.check("@render.bin.dark", value -> enableDarkness = value).checked(t -> enableDarkness).left().row();
			table.button("@keycomb.graphics", this::showGraphics).width(320f).row();
		}).left().row();

        cont.label(() -> "@render.add.name").padTop(16f).row();
		cont.table(table -> {
			table.check("@render.add.xray", value -> SchemeVars.renderer.xray = value).left().row();
			table.check("@render.add.hide", value -> SchemeVars.renderer.showUnits(value)).left().row();
			table.check("@render.add.grid", value -> SchemeVars.renderer.grid = value).left().row();
			table.check("@render.add.info", value -> SchemeVars.renderer.info = value).left().row();
			table.check("@render.add.raduni", value -> SchemeVars.renderer.raduni = value).left().row();
			table.check("@render.add.radius", value -> SchemeVars.renderer.radius = value).left().row();
		}).left().row();

		cont.labelWrap("@render.add.description").labelAlign(2, 8).padTop(16f).size(320f, 120f).get().getStyle().fontColor = Color.lightGray;
	}

	public void showGraphics(){
		SchemeVars.settings.show();
		SchemeVars.settings.visible(1);
		shown = true;
	}

    private void togglePowerLines(boolean on){
        if(on){
            settings.put("lasersopacity", settings.getInt("preferredlaseropacity", 100));
        }else{
            settings.put("preferredlaseropacity", settings.getInt("lasersopacity"));
            settings.put("lasersopacity", 0);
        }
    }
}