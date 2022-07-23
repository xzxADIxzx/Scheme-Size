package scheme.ui.dialogs;

import arc.func.Boolc;
import arc.func.Boolp;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.ui.CheckBox;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import mindustry.ui.dialogs.BaseDialog;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

import com.github.bsideup.jabel.Desugar;

public class RendererConfigDialog extends BaseDialog {

    public RendererConfigDialog() {
        super("@render.name");
        addCloseButton();

        addGroup("@category.general.name", table -> table.button("@keycomb.view_sets", () -> show(true)).width(320f),
                new Check("power",   this::togglePowerLines, () -> settings.getInt("lasersopacity") != 0),
                new Check("status",  value -> settings.put("blockstatus", value), () -> settings.getBool("blockstatus")),
                new Check("light",   value -> enableLight = value, () -> enableLight),
                new Check("dark",    value -> enableDarkness = value, () -> enableDarkness),
                new Check("fog",     value -> state.rules.fog = value, () -> state.rules.fog));

        addGroup("@category.add.name", null,
                new Check("xray",    value -> render.xray = value, null),
                new Check("hide",    render::showUnits, null),
                new Check("grid",    value -> render.grid = value, null),
                new Check("ruler",   value -> render.ruler = value, null),
                new Check("info",    value -> render.unitInfo = value, null),
                new Check("unit",    value -> render.unitRadius = value, null),
                new Check("turret",  value -> render.turretRadius = value, null),
                new Check("reactor", value -> render.reactorRadius = value, null));

        cont.labelWrap("@render.desc").labelAlign(2, 8).padTop(16f).width(320f).get().getStyle().fontColor = Color.lightGray;
    }

    private void addGroup(String title, Cons<Table> cons, Check... checks) {
        cont.label(() -> title).padTop(16f).row();
        cont.table(table -> {
            for (Check check : checks) {
                Cell<CheckBox> cell = table.check("@render." + check.text, check.listener).left();
                if (check.checked != null) cell.checked(t -> check.checked.get());
                cell.row(); // it was possible to do without if, but again the code would become larger
            }
            if (cons != null) cons.get(table);
        }).left().row(); // bruh, but this saves a huge array of code
    }

    public void show(boolean graphics){
        if (graphics) return; // TODO: show graphics settings
        else show();
    }

    public void togglePowerLines(boolean on) {
        if (on) settings.put("lasersopacity", settings.getInt("preferredlaseropacity", 100));
        else {
            settings.put("preferredlaseropacity", settings.getInt("lasersopacity"));
            settings.put("lasersopacity", 0);
        }
    }

    @Desugar
    public record Check(String text, Boolc listener, Boolp checked) {}
}
