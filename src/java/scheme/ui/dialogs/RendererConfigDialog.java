package scheme.ui.dialogs;

import arc.func.Boolc;
import arc.func.Boolp;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import mindustry.ui.dialogs.BaseDialog;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class RendererConfigDialog extends BaseDialog {

    public RendererConfigDialog() {
        super("@render.name");
        addCloseButton();

        partition("general.name", part -> {
            check(part, "power",  this::togglePowerLines,                      () -> settings.getInt("lasersopacity") != 0);
            check(part, "status", value -> settings.put("blockstatus", value), () -> settings.getBool("blockstatus"));
            check(part, "light",  value -> enableLight = value,                () -> enableLight);
            check(part, "dark",   value -> enableDarkness = value,             () -> enableDarkness);
            check(part, "fog",    value -> state.rules.fog = value,            () -> state.rules.fog);
        });

        cont.button("@keycomb.view_sets", () -> show(true)).width(320f).row();

        partition("add.name", part -> {
            check(part, "xray",   value -> render.xray = value);
            check(part, "hide",   render::showUnits);
            check(part, "grid",   value -> render.grid = value);
            check(part, "ruler",  value -> render.ruler = value);
            check(part, "info",   value -> render.unitInfo = value);
            check(part, "unit",   value -> render.unitRadius = value);
            check(part, "turret", value -> render.turretRadius = value);
            check(part, "reactor",value -> render.reactorRadius = value);
        });

        cont.labelWrap("@render.desc").labelAlign(2, 8).padTop(16f).width(320f).get().getStyle().fontColor = Color.lightGray;
    }

    private void partition(String title, Cons<Table> cons) {
        cont.labelWrap("@category." + title).padTop(16f).row();
        cont.table(cons).left().row();
    }

    private void check(Table table, String name, Boolc listener) {
        check(table, name, listener, null);
    }

    private void check(Table table, String name, Boolc listener, Boolp checked) {
        table.check("@render." + name, listener).left().with(check -> {
            if (check != null) check.update(() -> check.setChecked(checked.get()));
        }).row();
    }

    public void show(boolean graphics){
        if (graphics) {
            ui.settings.show(); 
            graphics().fireClick();
        } else show();
    }

    public void togglePowerLines(boolean on) {
        if (on) settings.put("lasersopacity", settings.getInt("preferredlaseropacity", 100));
        else {
            settings.put("preferredlaseropacity", settings.getInt("lasersopacity"));
            settings.put("lasersopacity", 0);
        }
    }

    private TextButton graphics() { // oh no
        return (TextButton) ((Table) ((Table) ((ScrollPane) ui.settings.getChildren().get(1)).getChildren().get(0)).getChildren().get(0)).getChildren().get(1);
    }

    @Desugar
    public record Check(String text, Boolc listener, Boolp checked) {}
}
