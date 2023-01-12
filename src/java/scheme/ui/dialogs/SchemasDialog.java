package scheme.ui.dialogs;

import arc.scene.style.Drawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Reflect;
import mindustry.game.Schematic;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SchematicsDialog;
import scheme.moded.ModedBinding;
import scheme.moded.ModedSchematics;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class SchemasDialog extends SchematicsDialog {

    @Override
    public Dialog show() {
        if (input.keyDown(ModedBinding.alternative)) return null;
        else return super.show(); // do not show SchematicsDialog if the keybind combination is pressed
    }

    @Override
    public void showImport() {
        new ImportDialog().show();
    }

    public void imported(Schematic schematic) {
        Reflect.invoke(SchematicsDialog.class, this, "setup", null);
        Reflect.invoke(SchematicsDialog.class, this, "checkTags", new Object[] { schematic }, Schematic.class);
        showInfo(schematic);

        schematic.removeSteamID();
        schematics.add(schematic);
    }

    public class ImportDialog extends BaseDialog {

        public ImportDialog() {
            super("@editor.export");
            addCloseButton();

            cont.pane(pane -> {
                pane.margin(10f);
                pane.table(Tex.button, t -> {
                    t.defaults().size(280f, 60f).left();

                    button(t, "copy.import", Icon.copy, () -> {
                        try {
                            imported(ModedSchematics.readBase64(app.getClipboardText()));
                            ui.showInfoFade("@schematic.saved");
                        } catch (Throwable error) {
                            ui.showException(error);
                        }
                    }).disabled(b -> app.getClipboardText() == null || !app.getClipboardText().startsWith(schematicBaseStart)).row();

                    button(t, "importfile", Icon.download, () -> platform.showFileChooser(true, schematicExtension, file -> {
                        try {
                            imported(ModedSchematics.read(file));
                        } catch (Throwable error) {
                            ui.showException(error);
                        }
                    })).row();

                    button(t, "importimage", Icon.image, () -> { // do not replace with :: because null pointer
                        platform.showFileChooser(true, "png", file -> parser.show(file));
                    });

                    if (!steam) return;

                    button("browseworkshop", Icon.book, () -> platform.openWorkshop());
                });
            });
        }

        private Cell<TextButton> button(Table table, String text, Drawable image, Runnable listener) {
            return table.button("@schematic." + text, image, Styles.flatt, () -> {
                hide();
                listener.run();
            }).marginLeft(12f);
        }
    }
}
