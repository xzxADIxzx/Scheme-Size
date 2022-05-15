package mindustry.ui.dialogs;

import arc.func.*;
import mindustry.gen.*;
import mindustry.game.*;

public class TeamSelectDialog extends ListDialog {

    public Cons2<Player, Team> callback;

    public TeamSelectDialog() {
        super("@teamselect");

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