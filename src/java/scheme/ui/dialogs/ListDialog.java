package mindustry.ui.dialogs;

import arc.graphics.g2d.*;
import arc.scene.ui.Dialog;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.fragments.*;

import static arc.Core.*;

public class ListDialog extends BaseDialog {

    public ListFragment<Player> players;
    public ListFragment<Team> teams;

    public ListDialog(String title) {
        super(title);
        addCloseButton();

        players = new ListFragment<>(Groups.player::each, Player::coloredName, Player::icon, player -> player.team().color);
        teams = new ListFragment<>(cons -> Structs.each(cons, Team.baseTeams), Team::localized, ListDialog::texture, team -> team.color);
    }

    @Override
    public Dialog show() {
        players.set(Vars.player);
        return super.show();
    }

    public void addPlayer() {
        players.build(cont);
        players.pane.left();
    }

    public void addTeam() {
        teams.build(cont);
        teams.pane.right();

        players.onChanged = player -> teams.selected = player.team();
    }

    public void addEmpty() {
        cont.table().width(288f);
    }

    // TODO: team icons are too big, but in V7 anuke will add new icons for all commands
    public static TextureRegion texture(Team team) {
        return atlas.find(new String[] {
                "team-derelict",
                "team-sharded",
                "status-boss-ui",
                "status-electrified-ui",
                "status-spore-slowed-ui",
                "status-wet-ui"
        }[team.id]);
    }
}
