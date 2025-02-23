package schema.ui.fragments;

import arc.input.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import schema.input.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/** Fragment that is displayed when the {@link schema.Main#insys input system} is in unit command mode. */
public class CommandFragment extends Table {

    /** Amount of units that were controlled when the fragment was rebuild. */
    private int lastAmountOfUnits;

    /** Builds the fragment and override the original one. */
    public void build(Group parent) {
        parent.addChild(this);
        parent.removeChild(Reflect.get(ui.hudfrag.blockfrag, "toggler"));

        name = "Command Fragment";

        setFillParent(true);
        rebuild();
        update(() -> {
            if (lastAmountOfUnits != insys.controlledUnitsAmount()) {
                lastAmountOfUnits = insys.controlledUnitsAmount();
                rebuild();
            }
        }).visible(insys::controlling);
    }

    /** Rebuilds the fragment in order to update the list of units */
    public void rebuild() {
        bottom().clear();
        table(atlas.drawable("schema-panel-bottom"), cont -> {
            cont.margin(12f, 12f, 4f, 12f);
            cont.defaults().pad(4f);

            if (lastAmountOfUnits == 0)
                cont.add("@cmnd.no-units").height(48f);
            else {
                var counts = insys.controlledUnitsAmountByType();
                for (int i = 0; i < counts.length; i++) if (counts[i] > 0) {

                    var type = content.unit(i);
                    int count = counts[i];

                    cont.table(t -> {
                        t.add(new ItemImage(type.uiIcon, count)).size(32f, 32f).tooltip(type.localizedName);

                        t.clicked(KeyCode.mouseLeft, () -> insys.freeUnits(u -> u.type != type));
                        t.clicked(KeyCode.mouseRight, () -> insys.freeUnits(u -> u.type == type));

                        t.hovered(() -> t.background(atlas.drawable("schema-button-over")));
                        t.exited(() -> t.background(null));
                    }).size(48f, 48f);
                }

                cont.image().growY().width(4f).color(Pal.accent);
                cont.add(bundle.format("cmnd.clear", Keybind.deselect.format()));
            }
        }).bottom().touchable(Touchable.enabled);
    }
}
