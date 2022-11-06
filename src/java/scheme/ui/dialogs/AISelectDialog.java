package scheme.ui.dialogs;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import mindustry.ai.types.*;
import mindustry.content.UnitTypes;
import mindustry.entities.units.AIController;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import scheme.ai.GammaAI;
import scheme.ai.GammaAI.Updater;
import scheme.ui.List;

import static arc.Core.*;
import static mindustry.Vars.*;

import com.github.bsideup.jabel.Desugar;

public class AISelectDialog extends ListDialog {

    public AIController ai;
    public List<UnitAI> list;

    public AISelectDialog() {
        super("@select.ai");

        hidden(() -> {
            if (ai instanceof GammaAI gamma) gamma.cache();
            if (ai instanceof MissileAI missile) missile.shooter = player.unit();
        });

        Events.run(WorldLoadEvent.class, this::deselect);
        Events.on(BlockBuildBeginEvent.class, event -> {
            if (ai instanceof GammaAI gamma && GammaAI.build == Updater.destroy && event.unit.getPlayer() == gamma.target)
                gamma.block(event.tile, event.breaking); // crutch but no way
        });

        Seq<UnitAI> units = Seq.with(
                new UnitAI(null, null),
                new UnitAI(UnitTypes.mono, new MinerAI()),
                new UnitAI(UnitTypes.poly, new BuilderAI()),
                new UnitAI(UnitTypes.mega, new RepairAI()),
                new UnitAI(UnitTypes.oct, new DefenderAI()),
                new UnitAI(UnitTypes.crawler, new SuicideAI()),
                new UnitAI(UnitTypes.dagger, new GroundAI()),
                new UnitAI(UnitTypes.flare, new FlyingAI()),
                new UnitAI(UnitTypes.renale, new HugAI()),
                new UnitAI(content.unit(64), new MissileAI()),
                new UnitAI(UnitTypes.gamma, new GammaAI()));

        list = new List<>(units::each, UnitAI::name, UnitAI::icon, unit -> Pal.accent);
        players.selected = player; // do it once

        cont.table(cont -> {
            players.build(cont);
            players.onChanged = player -> list.set(units.peek());

            players.pane.padRight(16f);

            list.build(cont);
            list.onChanged = unit -> ai = unit.ai;
        }).row();

        cont.labelWrap("@select.ai.tooltip").labelAlign(2, 8).padTop(16f).width(400f).get().getStyle().fontColor = Color.lightGray;
    }

    public void update() {
        ai.unit(player.unit());
        ai.updateUnit();
        player.shooting = player.unit().isShooting;
    }

    public void select() {
        players.rebuild();
        list.rebuild();

        show(scene); // call Dialog.show bypassing ListDialog.show
    }

    public void deselect() {
        ai = null;
    }

    public void gotoppl(Player player) {
        players.set(player);
        ((GammaAI) ai).cache();
    }

    @Desugar
    public record UnitAI(UnitType type, AIController ai) {

        public String name() {
            return type != null ? type.localizedName : "@keycomb.reset_ai";
        }

        public TextureRegion icon() {
            return type != null ? type.uiIcon : Icon.none.getRegion();
        }
    }
}
