package scheme.ui;

import arc.scene.Group;
import mindustry.gen.Iconc;

public class ShortcutFragment {

    public HexSelection selection = new HexSelection();
    public boolean visible;

    public void build(Group parent) {
        parent.fill(cont -> {
            cont.name = "shortcut";
            cont.visible(() -> visible);

            cont.add(selection);
        });
    }

    public void show(int x, int y) {
        selection.setPosition(x, y);
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public void replace(int index, String tag) {
        selection.buttons[index].setText(tag);
    }
}
