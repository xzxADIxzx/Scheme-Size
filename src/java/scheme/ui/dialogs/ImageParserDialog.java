package scheme.ui.dialogs;

import arc.files.Fi;
import arc.scene.ui.layout.Table;
import mindustry.game.Schematic;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SchematicsDialog.SchematicImage;
import scheme.SchemeVars;
import scheme.tools.ImageParser;
import scheme.tools.ImageParser.Config;
import scheme.ui.TextSlider;

import static arc.Core.*;

public class ImageParserDialog extends BaseDialog {

    public Fi file;
    public Schematic last;

    public Table image;
    public Config config = new Config();

    public ImageParserDialog() {
        super("@parser.name");
        addCloseButton();

        cont.defaults().width(450f);
        cont.table(t -> image = t).row();

        new TextSlider(1f, 10f, 1f, 1f, value -> {
            app.post(this::rebuild);
            return bundle.format("parser.row", config.rows = value);
        }).build(cont).row();

        new TextSlider(1f, 10f, 1f, 1f, value -> {
            app.post(this::rebuild);
            return bundle.format("parser.column", config.columns = value);
        }).build(cont).row();

        cont.check("@parser.filter", true, value -> {
            app.post(this::rebuild);
            config.filter = value;
        }).width(0f).left();

        buttons.button("@parser.import", () -> {
            SchemeVars.schemas.imported(last);
            hide();
        }).disabled(button -> last == null);
    }

    public void rebuild() {
        if (file == null) return; // don't do this

        last = ImageParser.parseSchematic(file, config);
        if (last == null) return; // an error occurred while parsing the schema

        image.clear();
        image.add(new SchematicImage(last)).size(450f);
    }

    public void show(Fi file) {
        this.file = file;

        rebuild();
        show();
    }
}
