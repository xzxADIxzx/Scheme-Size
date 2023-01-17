package scheme.ui;

import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Reflect;
import mindustry.editor.MapResizeDialog;

import static mindustry.Vars.*;

@SuppressWarnings("rawtypes")
public class MapResizeFix {

    public static void load() {
        MapResizeDialog.maxSize = 5000; // hah, that was pretty easy
        MapResizeDialog dialog = Reflect.get(ui.editor, "resizeDialog");

        dialog.shown(() -> {
            Seq<Cell> cells = getCells(dialog);
            cells.each(cell -> cell.maxTextLength(4));
        });
    }

    private static Seq<Cell> getCells(Table table) {
        return ((Table) ((Table) table.getChildren().get(1)).getChildren().get(0)).getCells();
    }
}
