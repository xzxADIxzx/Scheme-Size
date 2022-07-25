package scheme.ui;

import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SettingsMenuDialog.StringProcessor;

public class TextSlider extends Table {

    public Label label;
    public Slider slider;

    public TextSlider(float min, float max, float step, float def, StringProcessor processor) {
        touchable = Touchable.disabled;

        label = labelWrap("").style(Styles.outlineLabel).padLeft(8f).growX().left().get();
        slider = new Slider(min, max, step, false);

        slider.moved(value -> label.setText(processor.get((int) value)));
        slider.setValue(def);
        slider.change();
    }

    public Cell<Stack> build(Table table) {
        return table.stack(slider, this);
    }
}
