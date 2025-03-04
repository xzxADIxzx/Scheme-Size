package scheme.ui.dialogs;

import arc.scene.ui.Dialog;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectIntMap;
import arc.util.Scaling;
import mindustry.gen.Icon;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import scheme.Main;

import static arc.Core.*;
import static mindustry.Vars.*;

public class WaveApproachingDialog extends BaseDialog {

    public Label health;
    public Label shield;

    public Table enemies;
    public Table bosses;

    public WaveApproachingDialog() {
        super("@approaching.name");
        addCloseButton();

        setFillParent(false); // no sense in full screen dialog
        cont.add().width(350f).row(); // set min width

        cont.add("").with(l -> health = l).left().row();
        cont.add("").with(l -> shield = l).left().row();

        cont.add("@approaching.enemies").left();
        cont.button(Icon.copySmall, Styles.clearNonei, () -> copyUnits(null /* units.waveUnits */)).row();
        cont.table(t -> enemies = t).padLeft(16f).left().row();

        cont.add("@approaching.bosses").left();
        cont.button(Icon.copySmall, Styles.clearNonei, () -> copyUnits(null /* units.waveBosses */)).row();
        cont.table(t -> bosses = t).padLeft(16f).left().row();
    }

    @Override
    public Dialog show() {
        // units.refreshWaveInfo();
        title.setText(bundle.format("approaching.name", String.valueOf(state.wave)));

        health.setText(bundle.format("approaching.health", 0f /* units.waveHealth */));
        shield.setText(bundle.format("approaching.shield", 0f /* units.waveShield */));

        addUnits(enemies, null /* units.waveUnits */);
        addUnits(bosses, null /* units.waveBosses */);

        return super.show();
    }

    private void addUnits(Table table, ObjectIntMap<UnitType> units) {
        table.clear();

        if (units.isEmpty()) table.add("@none");
        else for (var entry : units) {
            table.stack(
                    new Image(entry.key.uiIcon).setScaling(Scaling.fit),
                    new Table(pad -> pad.bottom().left().add(String.valueOf(entry.value)))
            ).size(32f).padRight(8f);
        }
    }

    private void copyUnits(ObjectIntMap<UnitType> units) {
        StringBuilder builder = new StringBuilder();

        if (units.isEmpty()) builder.append(bundle.get("none"));
        else for (var entry : units)
            builder.append(entry.value).append(entry.key.emoji()).append(" ");

        Main.copy(builder.toString());
        hide();
    }
}
