package mindustry.ui.dialogs;

import static arc.Core.*;
import static mindustry.scheme.SchemeSize.*;

public class RenderSettingsDialog extends BaseDialog{

	public RenderSettingsDialog(){
		super("@render.name");
		addCloseButton();

        cont.label(() -> "@render.bin.name").padTop(16f).row();
		cont.table(table -> {
            table.check("@render.bin.power", this::togglePowerLines).checked(t -> settings.getInt("lasersopacity") != 0).left().row();
			table.check("@render.bin.status", value -> settings.put("blockstatus", value)).checked(t -> settings.getBool("blockstatus")).left().row();
		}).left().row();

        cont.label(() -> "@render.add.name").padTop(16f).row();
		cont.table(table -> {
			table.check("@render.add.xray", value -> render.xray = value).checked(t -> render.xray).left().row();
			table.check("@render.add.grid", value -> render.grid = value).checked(t -> render.grid).left().row();
			table.check("@render.add.unit", value -> render.unit = value).checked(t -> render.unit).left().row();
			table.check("@render.add.radius", value -> render.radius = value).checked(t -> render.radius).left().row();
		}).left().row();
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