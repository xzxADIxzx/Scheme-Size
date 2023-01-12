package scheme.ui.dialogs;

import arc.files.Fi;
import arc.scene.ui.layout.Table;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SchematicsDialog.SchematicImage;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicDisplay;
import scheme.SchemeVars;
import scheme.tools.ImageParser;
import scheme.ui.TextSlider;

import static arc.Core.*;

public class ImageParserDialog extends BaseDialog {

    public Fi file;
    public Schematic last;

    public Table image;

    public LogicBlock processor = (LogicBlock) Blocks.microProcessor;
    public LogicDisplay display = (LogicDisplay) Blocks.logicDisplay;
    public int rows, columns; // TODO class ParseConfig

    public ImageParserDialog() {
        super("@parser.name");
        addCloseButton();

        cont.defaults().width(550f);
        cont.table(t -> image = t).row();

        new TextSlider(1f, 10f, 1f, 1f, value -> {
            app.post(this::rebuild);
            return bundle.format("parser.row", rows = value);
        }).build(cont).row();

        new TextSlider(1f, 10f, 1f, 1f, value -> {
            app.post(this::rebuild);
            return bundle.format("parser.column", columns = value);
        }).build(cont).row();

        cont.check("@parser.filter", value -> {}).left();

        buttons.button("@parser.import", () -> {
            SchemeVars.schemas.imported(last);
            hide();
        });
    }

    public void rebuild() {
        if (file == null) return;
        last = ImageParser.parseSchematic(file, display, rows, columns);

        image.clear();
        image.add(new SchematicImage(last)).size(550f);
    }

    public void show(Fi file) {
        this.file = file;

        rebuild();
        show();
    }
}
