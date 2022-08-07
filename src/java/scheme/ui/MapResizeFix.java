package scheme.ui;

import arc.func.Prov;
import arc.scene.Action;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.editor.MapResizeDialog;

import static arc.Core.*;
import static arc.scene.actions.Actions.*;

@SuppressWarnings("rawtypes")
public class MapResizeFix {

    public static Prov<Action> show;
    public static Runnable fix;

    public static void load() {
        // hah, that was pretty easy
        MapResizeDialog.maxSize = 5000;

        // this is an exact copy of the show action from the UI
        show = () -> sequence(alpha(0f), fadeIn(0.1f));

        Dialog.setShowAction(() -> {
            app.post(() -> {
                Log.info(scene.root.getChildren().peek());
                if (scene.root.getChildren().peek() instanceof MapResizeDialog dialog) fix(dialog);
            });
            return show.get();
        });
    }

    private static void fix(MapResizeDialog dialog) {
        dialog.shown(fix = () -> {
            Seq<Cell> cells = getCells(dialog);
            cells.each(cell -> cell.maxTextLength(4));
        });

        fix.run(); // apply for first time
        Dialog.setShowAction(show); // just for optimization
    }

    private static Seq<Cell> getCells(Table table) {
        return ((Table) ((Table) table.getChildren().get(1)).getChildren().get(0)).getCells();
    }
}
