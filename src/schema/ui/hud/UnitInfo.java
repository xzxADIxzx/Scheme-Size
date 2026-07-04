package schema.ui.hud;

import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import schema.*;
import schema.ui.*;
import schema.ui.elements.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Subfragment that displays the controlled unit and its configuration.
public class UnitInfo extends Table
{
    public UnitInfo() { super(Style.find("panel-l-shape")); }

    /// Builds the fragment.
    public void build()
    {
        margin(4f, 12f, 12f, 4f);
        defaults().pad(4f);

        stack
        (
            new Element()
            {
                @Override
                public void draw() { Drawf.shadow(x + 32f, y + 32f, 80f); }
            },
            new Table(t -> t.image(() -> player.icon()).scaling(Scaling.bounded))
        )
        .size(64f);

        stack
        (
            new Table(t -> t.right().add(new Polybar(units::healthRel, Pal.health, () -> true                                            )).growY().width(12f)),
            new Table(t -> t.right().add(new Polybar(units::shieldRel, Pal.accent, () -> units.shieldAbs() > 0f && units.paylodAbs() > 0f)).growY().width( 8f)),
            new Table(t -> t.right().add(new Polybar(units::shieldRel, Pal.accent, () -> units.shieldAbs() > 0f                          )).growY().width( 4f)),
            new Table(t -> t.right().add(new Polybar(units::paylodRel, Pal.items,  () -> units.paylodAbs() > 0f                          )).growY().width( 4f)),
            new Table(t -> t.right().add(new Polybar(units::roundsRel, Pal.ammo,   () -> units.roundsAbs() > 0f                          )).growY().width( 4f))
        )
        .size(12f, 64f);

        table(cont ->
        {
            cont.table(pane ->
            {
                final var out = new Object[7];
                final CharSequence[] display = { bundle.get("hud.noact"), bundle.get("hud.noman") };

                pane.label(() ->
                {
                    if (out[0] != alpha.assist ||
                        out[1] != alpha.lock ||
                        out[2] != alpha.drop ||
                        out[3] != alpha.plan ||
                        out[4] != alpha.mine ||
                        out[5] != alpha.spin)
                    {
                        out[0] = alpha.assist;
                        out[1] = alpha.lock;
                        out[2] = alpha.drop;
                        out[3] = alpha.plan;
                        out[4] = alpha.mine;
                        out[5] = alpha.spin;

                        if (out[0] != null) return display[0] = bundle.format("hud.act-0", alpha.assist.coloredName());
                        if (out[1] != null) return display[0] = bundle.format("hud.act-1");
                        if (out[2] != null) return display[0] = bundle.format("hud.act-2");
                        if (out[3] != null) return display[0] = bundle.format("hud.act-3");
                        if (out[4] != null) return display[0] = bundle.format("hud.act-4", alpha.mine.emoji());
                        if (out[5] != null) return display[0] = bundle.format("hud.act-5");

                        display[0] = bundle.get("hud.noact");
                    }
                    return units.coreUnit ? display[0] : display[1];
                }
                ).growX().left().ellipsis(true);
            }
            ).growX().row();
            cont.table(pane ->
            {
                final var out = new StringBuilder();
                final boolean[] display = { false };

                pane.label(() ->
                {
                    int x = display[0] ? insys.tileX() : player.tileX(),
                        y = display[0] ? insys.tileY() : player.tileY();

                    out.setLength(0);
                    out.append("[").append(x).append(", ").append(y).append("]");
                    return out;
                }
                ).growX().color(Pal.accentBack);

                pane.button(Icon.playersSmall, Style.ibc, () -> display[0] = true ).checked(_ -> display[0] == true ).size(24f);
                pane.button(Icon.unitsSmall,   Style.ibc, () -> display[0] = false).checked(_ -> display[0] == false).size(24f);
            }
            ).growX().row();
            cont.collapser(pane ->
            {
                final Bits effects = new Bits(content.statusEffects().size);
                final float[] load = { 0f };

                pane.update(() ->
                {
                    if (!player.dead() && !player.unit().statusBits().equals(effects) || units.paylodAbs() != load[0])
                    {
                        if (player.dead())
                            effects.clear();
                        else
                            effects.set(player.unit().statusBits());

                        load[0] = units.paylodAbs();

                        pane.left().clear();
                        content.statusEffects().each(e -> effects.get(e.id) && e.uiIcon.found(), e ->
                        {
                            pane.image(e.uiIcon).size(16f).scaling(Scaling.fit).tooltip(t -> t.background(Style.find("panel-x-shape")).margin(12f).label(() ->
                            {
                                return Tools.time(player.dead() ? 0f : player.unit().getDuration(e)).insert(0, " [light]").insert(0, e.localizedName);
                            }
                            ).style(Style.outline));
                        });
                        if (player.unit() instanceof Payloadc p) p.contentInfo(pane.table().grow().get(), 16f, 204f - pane.getPrefWidth());
                        pane.add().height(16f); // the element will not collapse if the table is empty
                    }
                });
            },
            true, () -> !player.dead() && !player.unit().statusBits().isEmpty() || units.paylodAbs() > 0f).growX().row();
        }
        ).growX();
    }
}
