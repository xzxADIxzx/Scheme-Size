package schema.ui.dialogs;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import schema.*;
import schema.ui.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Dialog that displays the list of units and guardians on a specific wave.
public class WaveDialog extends BaseDialog
{
    /// Currently displayed wave.
    private int wave;
    /// Labels that display info.
    private Label health, shield;
    /// Tables that display info.
    private Table common, guards;

    public WaveDialog()
    {
        super("@wave.name");

        addCloseButton();
        addButton("@wave.prev", Icon.left,  256f, () -> rebuild(--wave), () -> wave <= 1);
        addButton("@wave.next", Icon.right, 256f, () -> rebuild(++wave), () -> wave >= state.rules.winWave - 1 && state.rules.winWave > 0);

        cont.defaults().width(512f).left();
        cont.add("").with(l -> health = l).row();
        cont.add("").with(l -> shield = l).row();

        cont.table(pane ->
        {
            pane.add("@wave.common").padRight(4f);
            pane.image().growX().height(4f).color(Pal.lightishGray);

            pane.button(Icon.copySmall, Style.ibe, () -> copy(units.waveCommon)).size(32f).row();
            pane.table(t -> common = t).left().colspan(3);
        }).row();
        cont.table(pane ->
        {
            pane.add("@wave.guards").padRight(4f);
            pane.image().growX().height(4f).color(Pal.lightishGray);

            pane.button(Icon.copySmall, Style.ibe, () -> copy(units.waveGuards)).size(32f).row();
            pane.table(t -> guards = t).left().colspan(3);
        }).row();
    }

    // region control

    /// Shows the dialog.
    public void show(int wave) { rebuild(this.wave = wave); show(); }

    /// Rebuilds the dialog.
    public void rebuild(int wave)
    {
        title.setText(bundle.format("wave.name", wave));
        units.refreshWaveInfo(wave);

        health.setText(bundle.format("wave.health", units.waveHealth));
        shield.setText(bundle.format("wave.shield", units.waveShield));

        add(common, units.waveCommon);
        add(guards, units.waveGuards);
    }

    /// Adds the units to the table.
    private void add(Table table, ObjectIntMap<UnitType> units)
    {
        table.marginLeft(16f).clear();
        table.defaults().padRight(4f);

        if (units.isEmpty())
            table.add("@none");
        else
            units.forEach(e -> table.add(StatValues.stack(e.key, e.value)));
    }

    /// Copies the units to the clipboard.
    private void copy(ObjectIntMap<UnitType> units)
    {
        var builder = new StringBuilder(units.size * 2);

        for (var u : units) builder.append(u.value).append(u.key.emoji()).append(' ');

        Tools.copy(builder.toString());
        hide();
    }

    // endregion
}
