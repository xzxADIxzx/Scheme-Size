package schema.ui.hud;

import arc.scene.*;
import arc.scene.ui.layout.*;

/** Fragment that displays the core and power grid info, controlled unit and its ai config, minimap and wave info. */
public class HudFragment extends Table {

    /** Whether the fragment is visible. */
    public boolean shown;

    /** Builds the fragment and override the original one. */
    public void build(Group parent) {
        parent.addChild(this);
        parent.removeChild(parent.find("overlaymarker"));
        parent.removeChild(parent.find("coreinfo"));
        parent.removeChild(parent.find("minimap/position"));

        setFillParent(true);
        visible(() -> shown);
    }
}
