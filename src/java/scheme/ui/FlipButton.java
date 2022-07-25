package scheme.ui;

import arc.scene.ui.ImageButton;
import mindustry.gen.Icon;

public class FlipButton extends ImageButton {

    public boolean fliped;

    public FlipButton() {
        super(Icon.downOpen, HudFragment.style);
        clicked(this::flip);
        resizeImage(Icon.downOpen.imageSize());
    }

    public void flip() {
        setChecked(fliped = !fliped);
        getStyle().imageUp = fliped ? Icon.upOpen : Icon.downOpen;
    }
}
