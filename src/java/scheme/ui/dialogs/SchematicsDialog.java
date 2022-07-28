package scheme.ui.dialogs;

import arc.scene.ui.Dialog;
import scheme.moded.ModedBinding;

import static arc.Core.*;

public class SchematicsDialog extends mindustry.ui.dialogs.SchematicsDialog {

    @Override
    public Dialog show() {
        if (input.keyDown(ModedBinding.alternative)) return null;
        else return super.show(); // do not show SchematicsDialog if the keybind combination is pressed
    }
}
