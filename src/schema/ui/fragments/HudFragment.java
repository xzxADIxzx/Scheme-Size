package schema.ui.fragments;

import arc.scene.*;
import arc.scene.ui.layout.*;
import schema.ui.hud.*;

/// Fragment that displays the core and power grid info, controlled unit and its configuration, minimap and wave info.
public class HudFragment extends Table {

    public UnitInfo unit = new UnitInfo();
    public CoreInfo core = new CoreInfo();
    public WaveInfo wave = new WaveInfo();

    /// Whether the fragment is visible.
    public boolean shown = true;

    /// Builds the fragment and overrides the original.
    public void build(Group parent)
    {
        parent.addChild(this);

        setFillParent(true);
        visible(() -> shown);

        top().add(unit, core, wave);
        row();
        // TODO notifications, see below

        core.build();

        // TODO paused/pause disabled/waiting for players/saving notifications
        // TODO leave drop point warning?
    }
}
