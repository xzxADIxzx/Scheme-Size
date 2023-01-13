package scheme.ui.dialogs;

import arc.files.Fi;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.layout.Table;
import mindustry.game.Schematic;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SchematicsDialog.SchematicImage;
import mindustry.world.blocks.logic.LogicDisplay;
import scheme.SchemeVars;
import scheme.tools.ImageParser;
import scheme.tools.ImageParser.Config;
import scheme.ui.TextSlider;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ImageParserDialog extends BaseDialog {

    public Fi file;
    public Schematic last;

    public Table image;
    public Config config = new Config();

    public ImageParserDialog() {
        super("@parser.name");
        addCloseButton();

        cont.table(t -> image = t).row();

        new TextSlider(1f, 10f, 1f, 1f, value -> {
            app.post(this::rebuild);
            return bundle.format("parser.row", config.rows = value);
        }).build(cont).width(450f).row();

        new TextSlider(1f, 10f, 1f, 1f, value -> {
            app.post(this::rebuild);
            return bundle.format("parser.column", config.columns = value);
        }).build(cont).width(450f).row();

        cont.check("@parser.filter", true, value -> {
            app.post(this::rebuild);
            config.filter = value;
        }).left().row();

        buttons.button("@parser.import", () -> {
            SchemeVars.schemas.imported(last);
            hide();
        }).disabled(button -> last == null);

        cont.table(table -> {
            var style = new ImageButtonStyle(Styles.clearNoneTogglei) {{
                up = Tex.pane;
            }};

            content.blocks().each(block -> {
                if (block instanceof LogicDisplay display)
                    table.button(new TextureRegionDrawable(display.uiIcon), style, 30f, () -> {
                        app.post(this::rebuild);
                        config.display = display;
                    }).size(48f).padRight(4f).checked(button -> config.display == display);
            });
        }).left().row();
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
