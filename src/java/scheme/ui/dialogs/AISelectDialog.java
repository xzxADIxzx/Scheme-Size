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
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import scheme.ui.List;

import static arc.Core.*;
import static mindustry.Vars.*;

import com.github.bsideup.jabel.Desugar;

public class AISelectDialog extends ListDialog {

    public AIController ai;
    public List<UnitAI> list;

    public AISelectDialog() {
        super("@select.ai");

        Events.run(WorldLoadEvent.class, this::deselect);
        Seq<UnitAI> units = Seq.with(
                new UnitAI(null, null),
                new UnitAI(UnitTypes.mono, new MinerAI()),
                new UnitAI(UnitTypes.poly, new BuilderAI()),
                new UnitAI(UnitTypes.mega, new RepairAI()),
                new UnitAI(UnitTypes.oct, new DefenderAI()),
                new UnitAI(UnitTypes.crawler, new SuicideAI()),
                new UnitAI(UnitTypes.dagger, new GroundAI()),
                new UnitAI(UnitTypes.flare, new FlyingAI()),
                new UnitAI(UnitTypes.gamma, null));

        list = new List<>(units::each, UnitAI::name, UnitAI::icon, unit -> Pal.accent);
        players.selected = player; // do it once

        cont.table(cont -> {
            players.build(cont);
            players.onChanged = player -> list.set(units.peek());

            list.build(cont);
            list.onChanged = unit -> ai = unit.ai;
        }).row();

        cont.labelWrap("@select.ai.tooltip").labelAlign(2, 8).padTop(16f).width(400f).get().getStyle().fontColor = Color.lightGray;
    }

    public void update() {
        ai.unit(player.unit());
        ai.updateUnit();
    }

    public void select() {
        players.rebuild();
        list.rebuild();

        show(scene); // call Dialog.show bypassing ListDialog.show
    }

    public void deselect() {
        ai = null;
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
