package schema.ui.fragments;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import schema.ui.Style;
import schema.ui.hud.*;

import static mindustry.Vars.*;
import static schema.Main.*;

/// Fragment that displays the controlled unit and its configuration, core items and power grids, the sector minimap and wave information.
public class HudFragment extends Table
{
    public UnitInfo unit = new UnitInfo();
    public CoreInfo core = new CoreInfo();
    public WaveInfo wave = new WaveInfo();

    /// Notification content labels.
    private Label[] nots = new Label[Notification.all.length];
    /// Notification points in time.
    private float[] time = new float[Notification.all.length];
    /// Icon of the other notifications.
    private Image notificationIcon;

    /// Whether the fragment is visible.
    public boolean shown = true;

    /// Builds the fragment and overrides the original.
    public void build(Group parent)
    {
        Events.run(Trigger.teamCoreDamage, () -> show(Notification.cores, 3f));
        Events.on(WaveEvent.class, e ->
        {
            nextGuardian(w -> show(Notification.guardian, String.valueOf(w), 5f));
        });
        Events.on(SectorCaptureEvent.class, e -> show(Notification.captured, e.sector.name(), 5f));
        Events.on(SectorInvasionEvent.class, e -> show(Notification.attacked, e.sector.name(), 5f));

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
                c.background(Style.find("panel-h-shape"));

                if (type == Notification.other)
                    notificationIcon = c.image().scaling(Scaling.fit).get();
                nots[type.ordinal()] = c.add("").style(Style.outline).get();

                if (type == Notification.cores)
                {
                    c.parent.tapped(() -> insys.poseCam(Tmp.v1.set(control.lastDamagedCore)));
                    c.parent.update(() ->
                    {
                        nots[type.ordinal()].color.set(Color.orange).lerp(Color.scarlet, Mathf.absin(2f, 1f));
                    });
                    c.parent.addListener(new HandCursorListener());
                }
                if (type == Notification.guardian)
                {
                    c.parent.tapped(() -> nextGuardian(wavy::show));
                    c.parent.addListener(new HandCursorListener());
                }
            },
            true, () -> Time.time <= time[type.ordinal()]).row();
        }
        ).colspan(3);

        // remove gaps between subfragments
        getCell(core).pad(0f, -4f, -4f, -4f);

        unit.build();
        core.build();
        wave.build();
    }

    // region control

    /// Shows a notification for the given duration.
    public void show(Notification not, String sectors, float duration)
    {
        if (duration != 0f && Time.time >= time[not.ordinal()]) Sounds.uiNotify.play();

        nots[not.ordinal()].setText(not.format(sectors));
        time[not.ordinal()] = Time.time + duration * 60f;
    }

    /// Shows a notification for the given duration.
    public void show(Notification not, float duration) { show(not, null, duration); }

    /// Iterates a few waves until a guard is found.
    public void nextGuardian(Intc wave)
    {
        for (int i = state.wave; i <= Math.min(state.wave + 9, state.rules.winWave > 0 ? state.rules.winWave : Integer.MAX_VALUE); i++)
        {
            int j = i - 1;
            int d = i - state.wave + 1;
            if
            (
                d == 1 | d == 2 | d == 5 | d == 10 &&
                state.rules.spawns.contains(s -> s.effect == StatusEffects.boss && s.getSpawned(j) > 0)
            )
            {
                wave.get(d);
                break;
            }
        }
    }

    // endregion
    // region agent

    /// Creates a new agent.
    public Agent agent() { return new Agent(); }

    /// Agent redirecting method calls from the original component.
    public class Agent extends mindustry.ui.fragments.HudFragment
    {
        /// Last time a notification was shown.
        private long last;

        /// Schedules a notification in a line.
        private void schedule(Runnable show)
        {
            if (Time.timeSinceMillis(last) > 3500)
            {
                last = Time.millis();
                show.run();
            }
            else
            {
                last += 3500;
                Time.run((last - Time.millis()) / 1000f * 60f, show);
            }
        }

        @Override
        public void showToast(Drawable icon, float size, String text)
        {
            schedule(() ->
            {
                notificationIcon.setDrawable(icon);
                show(Notification.other, text, 3f);
            });
        }

        @Override
        public void showUnlock(UnlockableContent content)
        {
            schedule(() ->
            {
                notificationIcon.setDrawable(content.uiIcon);
                show(Notification.other, "@unlocked", 3f);
            });
        }

        @Override
        public boolean hasToast() { return Time.timeSinceMillis(last) < 3500; }
    }

    // endregion
}
