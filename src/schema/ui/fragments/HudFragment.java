package schema.ui.fragments;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.gen.*;
import schema.ui.*;
import schema.ui.hud.*;

import static mindustry.Vars.*;
import static schema.Main.*;

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
        Events.run(EventType.Trigger.teamCoreDamage, () -> show(Notification.cores, 3f));

        parent.addChild(this);

        setFillParent(true);
        update(() ->
        {
            if (state.isPaused()) show(netServer.isWaitingForPlayers() ? Notification.waits : Notification.pause, 0f);
            if (!insys.building) show(Notification.build, 0f);
        }
        ).visible(() -> shown);

        top().add(unit, core, wave);
        row();
        table(cont ->
        {
            cont.defaults().width(480f).maxHeight(32f);

            for (var type : Notification.all) cont.collapser(c ->
            {
                nots[type.ordinal()] = c.background(Style.find("panel-h-shape")).add("").style(Style.outline).get();

                if (type != Notification.cores) return;

                c.parent.tapped(() -> insys.poseCam(Tmp.v1.set(control.lastDamagedCore)));
                c.parent.addListener(new HandCursorListener());
                c.parent.update(() ->
                {
                    nots[type.ordinal()].color.set(Color.orange).lerp(Color.scarlet, Mathf.absin(2f, 1f));
                });
            },
            true, () -> Time.time <= time[type.ordinal()]).row();
        }
        ).colspan(3);

        // remove gaps between subfragments
        getCell(core).pad(0f, -4f, -4f, -4f);

        // unit.build();
        core.build();
        // wave.build();

        // TODO agent: setHudText, toggleHudText, showToast, hasToast, showUnlock
    }

    /// Shows a notification for the given duration.
    public void show(Notification not, String sectors, float duration)
    {
        if (duration != 0f && Time.time >= time[not.ordinal()]) Sounds.uiNotify.play();

        nots[not.ordinal()].setText(not.format(sectors));
        time[not.ordinal()] = Time.time + duration * 60f;
    }

    /// Shows a notification for the given duration.
    public void show(Notification not, float duration) { show(not, null, duration); }
}
