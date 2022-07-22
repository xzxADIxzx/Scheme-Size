package scheme.ui.dialogs;

import arc.func.Cons2;
import mindustry.game.Team;
import mindustry.gen.Player;

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

        this.callback = callback;
        show();
    }
}
