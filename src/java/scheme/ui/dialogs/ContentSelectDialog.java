package scheme.ui.dialogs;

import arc.func.Cons4;
import arc.func.Func;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.content.UnitTypes;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.ui.Styles;

import static mindustry.Vars.*;

public class ContentSelectDialog<T extends UnlockableContent> extends ListDialog {

    public static final int row = mobile ? 8 : 10;
    public static final float size = mobile ? 54f : 64f;
    public static final Seq<UnlockableContent> specials = Seq.with(
            UnitTypes.latum, UnitTypes.renale, UnitTypes.manifold, UnitTypes.assemblyDrone,
            content.unit(53), content.unit(55), content.unit(64));

    public Cons4<Player, Team, T, Float> callback;
    public Func<Float, String> format;

    public boolean showSlider;
    public int items;

    public ContentSelectDialog(String title, Seq<T> content, int min, int max, int step, Func<Float, String> format) {
        super(title);
        this.format = format;

        Label label = new Label("", Styles.outlineLabel);
        Slider slider = new Slider(min, max, step, false);

        slider.moved(value -> label.setText(format.get(value)));
        slider.change(); // update label

        Table table = new Table(); // T::logicVisible
        content.each(item -> item.logicVisible() || specials.contains(item), item -> {
            table.button(new TextureRegionDrawable(item.uiIcon), () -> {
                callback.get(players.get(), teams.get(), item, slider.getValue());
                hide();
            }).size(size);

            if (++items % row == 0) table.row();
        });

        addPlayer();

        cont.table(cont -> {
            cont.add(table).row();
            cont.add(label).center().padTop(16f).visible(() -> showSlider).row();
            cont.table(slide -> {
                slide.button(Icon.add, () -> {
                    content.each(T::logicVisible, item -> callback.get(players.get(), teams.get(), item, slider.getValue()));
                    hide();
                }).tooltip("@select.all");
                slide.add(slider).padLeft(8f).growX();
            }).fillX().visible(() -> showSlider);
        }).growX();

        addTeam();
    }

    public void select(boolean showSlider, boolean showPlayers, boolean showTeams, Cons4<Player, Team, T, Float> callback) {
        players.pane.visible(showPlayers);
        players.rebuild();

        teams.pane.visible(showTeams);
        teams.rebuild();

        this.showSlider = showSlider;
        this.callback = callback;
        show();
    }
}
