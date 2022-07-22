package mindustry.ui.dialogs;

import arc.func.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;

import static mindustry.Vars.*;

public class ContentSelectDialog<T extends UnlockableContent> extends ListDialog {

    public static final int row = mobile ? 8 : 10;
    public static final float size = mobile ? 54f : 64f;

    public Cons4<Player, Team, T, Floatp> callback;
    public Func<Float, String> format;

    public boolean showSlider;

    public ContentSelectDialog(String title, Seq<T> content, int min, int max, int step, Func<Float, String> format) {
        super(title);
        this.format = format;

        Label label = new Label("", Styles.outlineLabel);
        Slider slider = new Slider(min, max, step, false);

        slider.moved(value -> label.setText(format.get(value)));
        slider.change(); // update label

        Table table = new Table();
        content.each(T::logicVisible, item -> {
            table.button(new TextureRegionDrawable(item.uiIcon), () -> {
                callback.get(players.get(), teams.get(), item, slider::getValue);
                hide();
            }).size(size);

            if (item.id % row == row - 1) table.row();
        });

        addPlayer();

        cont.table(t -> {
            t.add(table).row();
            t.add(label).center().padTop(16f).visible(() -> showSlider).row();
            t.table(s -> {
                s.button(Icon.add, () -> {
                    content.each(T::logicVisible, item -> callback.get(players.get(), teams.get(), item, slider::getValue));
                    hide();
                });
                s.add(slider).padLeft(8f).growX();
            }).fillX().visible(() -> showSlider);
        }).growX();

        addTeam();
    }

    public void select(boolean showSlider, boolean showPlayers, boolean showTeams, Cons4<Player, Team, T, Floatp> callback) {
        players.pane.visible(showPlayers);
        players.rebuild();

        teams.pane.visible(showTeams);
        teams.rebuild();

        this.showSlider = showSlider;
        this.callback = callback;
        show();
    }
}
