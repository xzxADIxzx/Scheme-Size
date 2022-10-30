package scheme.ui.dialogs;

import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SchematicsDialog;
import scheme.moded.ModedBinding;

import static arc.Core.*;

public class SchemasDialog extends SchematicsDialog {

    @Override
    public Dialog show() {
        if (input.keyDown(ModedBinding.alternative)) return null;
        else return super.show(); // do not show SchematicsDialog if the keybind combination is pressed
    }
}
