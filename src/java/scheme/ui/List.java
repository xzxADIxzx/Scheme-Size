package scheme.ui;

import arc.func.*;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Button;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

import static mindustry.Vars.*;

public class List<T> {

    public T selected;

    public Cons<Cons<T>> iterator;
    public Func<T, String> title;
    public Func<T, TextureRegion> texture;
    public Func<T, Color> color;
    public Cons<T> onChanged = value -> {};

    public Cell<ScrollPane> pane;
    public Table list = new Table();

    public List(Cons<Cons<T>> iterator, Func<T, String> title, Func<T, TextureRegion> texture, Func<T, Color> color) {
        this.iterator = iterator;
        this.title = title;
        this.texture = texture;
        this.color = color;
    }

    public void build(Table parent) {
        pane = parent.pane(list).size(288f, mobile ? 540f : 630f).scrollX(false);
    }

    public void rebuild() {
        list.clear();
        list.defaults().size(264f, 74f).padBottom(16f);

        iterator.get(item -> {
            Button check = new Button(Styles.cleart);
            check.changed(() -> set(item));

            check.add(new IconTable(check::isChecked, texture.get(item))).size(74f);
            check.table(t -> {
                t.labelWrap(title.get(item)).growX().row();
                t.image().height(4f).color(color.get(item)).growX().bottom().padTop(4f);
            }).size(170f, 74f).pad(10f);

            list.add(check).checked(button -> selected == item).row();
        });
    }

    public T get() {
        return selected;
    }

    public void set(T item) {
        selected = item;
        onChanged.get(item);
    }

    public static class IconTable extends Table {

        public Boolp active;

        public IconTable(Boolp active, TextureRegion region) {
            this.active = active;
            image(region).scaling(Scaling.bounded).pad(8f).grow();
        }

        @Override
        public void draw() {
            super.draw();
            Draw.color(active.get() ? Pal.accent : Pal.gray, parentAlpha);
            Lines.stroke(Scl.scl(4f));
            Lines.rect(x, y, width, height);
            Draw.reset();
        }
    }
}
