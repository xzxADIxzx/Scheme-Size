package scheme.ui.dialogs;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Dialog;
import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.ui.dialogs.BaseDialog;
import scheme.ui.List;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ListDialog extends BaseDialog {

    public List<Player> players;
    public List<Team> teams;

    public ListDialog(String title) {
        super(title);
        addCloseButton();

        players = new List<>(Groups.player::each, Player::coloredName, Player::icon, player -> player.team().color);
        teams = new List<>(cons -> Structs.each(cons, Team.baseTeams), Team::localized, ListDialog::texture, team -> team.color);
    }

    @Override
    public Dialog show() {
        players.set(player);
        return super.show();
    }

    public void addPlayer() {
        players.build(cont);
        players.pane.left();
    }

    public void addTeam() {
        teams.build(cont);
        teams.pane.right();

        // not via ListFragment.set because some dialogs are closed after selecting a team
        players.onChanged = player -> teams.selected = player.team();
    }

    public void addEmpty() {
        cont.table().width(288f);
    }

    public static TextureRegion texture(Team team) {
        return atlas.find(new String[] {
                "team-derelict",
                "team-sharded",
                "team-crux",
                "team-malis",
                "status-electrified-ui",
                "status-wet-ui"
        }[team.id]);
    }
}
