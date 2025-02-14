package schema.ui.dialogs;

import arc.scene.style.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;

/** Dialog that has a title header and buttons footer. */
public class BaseDialog extends Dialog {

    public BaseDialog(String name) {
        super(name);
        setFillParent(true);

        this.margin(0f).getCells().each(c -> c.pad(0f));
        cont.margin(4f).defaults().pad(4f);

        title.setAlignment(Align.center);
        titleTable.getCell(title).pad(4f);

        titleTable.row();
        titleTable.image().growX().height(4f).pad(0f).color(Pal.accent);

        buttons.image().growX().height(4f).pad(0f).color(Pal.accent);
        buttons.row();
    }

    /** Adds a button to the dialog footer. */
    public void addButton(String text, Drawable icon, Runnable clicked) {
        buttons.button(text, icon, schema.ui.Style.tbd, clicked).size(196f, 48f).pad(8f, 4f, 8f, 4f);
    }

    /** Adds a button that closes the dialog. */
    public void addCloseButton() {
        addButton("@back", Icon.left, this::hide);
        closeOnBack();
    }
}
