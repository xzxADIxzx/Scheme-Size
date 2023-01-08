package scheme.ui.dialogs;

import arc.func.Cons2;
import arc.graphics.Color;
import arc.scene.ui.Button;
import arc.util.Time;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.ui.Styles;
import scheme.ui.List.IconTable;

import static arc.Core.*;

public class TeamSelectDialog extends ListDialog {

    public Cons2<Player, Team> callback;

    public TeamSelectDialog() {
        super("@select.team");

        addPlayer();
        addTeam();

        teams.onChanged = team -> {
            callback.get(players.get(), team);
            hide();
        };
    }

    public void select(Cons2<Player, Team> callback) {
        players.rebuild();
        teams.rebuild();

        Button check = new Button(Styles.cleart);
        check.changed(() -> teams.set(null));

        check.add(new IconTable(check::isChecked, atlas.find("status-overclock-ui"))).size(74f);
        check.table(t -> {
            t.labelWrap("@team.rainbow.name").growX().row();
            t.image().height(4f).growX().bottom().padTop(4f).update(image -> image.setColor(Color.HSVtoRGB(Time.time % 360f, 100f, 100f)));
        }).size(170f, 74f).pad(10f);

        teams.list.add(check).checked(button -> teams.selected == null).row();

        this.callback = callback;
        show();
    }
}
