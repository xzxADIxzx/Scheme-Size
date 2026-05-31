package schema.ui.fragments;

import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import schema.ui.*;
import schema.ui.hud.*;

/// Fragment that displays the core and power grid info, controlled unit and its configuration, minimap and wave info.
public class HudFragment extends Table
{
    public UnitInfo unit = new UnitInfo();
    public CoreInfo core = new CoreInfo();
    public WaveInfo wave = new WaveInfo();

    /// Notification content labels.
    private Label[] nots = new Label[Notification.all.length];
    /// Notification points in time.
    private float[] time = new float[Notification.all.length];

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
        table(cont ->
        {
            cont.defaults().width(480f).maxHeight(32f);

            for (var type : Notification.all) cont.collapser(c ->
            {
                nots[type.ordinal()] = c.background(Style.find("panel-h-shape")).add("").style(Style.outline).get();
            },
            true, () -> Time.time < time[type.ordinal()]).row();
        }
        ).colspan(3);

        // remove gaps between subfragments
        getCell(core).pad(0f, -4f, -4f, -4f);

        // unit.build();
        core.build();
        // wave.build();

        // TODO paused/pause disabled/waiting for players/saving notifications
        // TODO leave drop point warning?
        // TODO agent: setHudText, toggleHudText, showToast, hasToast, showUnlock
    }

    /// Shows a notification for the given duration.
    public void show(Notification not, String sectors, float duration)
    {
        nots[not.ordinal()].setText(not.format(sectors));
        time[not.ordinal()] = Time.time + duration * 60f;
    }

    /// Shows a notification for the given duration.
    public void show(Notification not, float duration) { show(not, null, duration); }
}
