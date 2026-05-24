package schema.ui.dialogs;

import arc.scene.style.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;

/// Dialog with a title header and button footer.
public class BaseDialog extends Dialog
{
    public BaseDialog(String name)
    {
        super(name);

        setFillParent(true);
        hidden(Sounds.uiBack::play);

        this.margin(0f).getCells().each(c -> c.pad(0f));
        cont.margin(4f).defaults().pad(4f);

        title.setAlignment(Align.center);
        titleTable.getCell(title).pad(8f);
        titleTable.row();
        titleTable.image().growX().height(4f).pad(0f).color(Pal.accent);

        buttons.image().growX().height(4f).pad(0f).color(Pal.accent);
        buttons.row();
    }

    /// Adds a button to the dialog's footer.
    public void addButton(String text, Drawable icon, float width, Runnable clicked)
    {
        buttons.button(text, icon, schema.ui.Style.tbd, clicked).size(width, 48f).pad(8f, 4f, 8f, 4f);
    }

    /// Adds a button that closes the dialog.
    public void addCloseButton()
    {
        addButton("@back", Icon.left, 196f, this::hide);
        closeOnBack();
    }
}
