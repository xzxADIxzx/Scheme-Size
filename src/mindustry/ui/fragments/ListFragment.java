package mindustry.ui.fragments;

import arc.util.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.ui.*;
import mindustry.graphics.*;

public class ListFragment<T> {

    public T selected;

    public Cons<Cons<T>> iterator;
    public Func<T, String> title;
    public Func<T, TextureRegion> texture;
    public Func<T, Color> color;
    public Cons<T> onChanged = t -> {};

    public Cell<ScrollPane> pane;
    public Table list = new Table();

    public ListFragment(Cons<Cons<T>> iterator, Func<T, String> title, Func<T, TextureRegion> texture, Func<T, Color> color) {
        this.iterator = iterator;
        this.title = title;
        this.texture = texture;
        this.color = color;
    }

    public void build(Table parent) {
        pane = parent.pane(list).size(288f, 540f).scrollX(false);
    }

    public void rebuild() {
        list.clear();
        iterator.get(item -> {
            Button check = new Button(Styles.transt);
            check.changed(() -> set(item));

            Table icon = new Table() {
                @Override
                public void draw() {
                    super.draw();
                    Draw.color(check.isChecked() ? Pal.accent : Pal.gray);
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(4f));
                    Lines.rect(x, y, width, height);
                    Draw.reset();
                }
            };
            icon.add(new Image(texture.get(item)).setScaling(Scaling.fill)).pad(8f).grow();

            check.add(icon).size(74f);
            check.table(t -> {
                t.labelWrap(title.get(item)).growX().row();
                t.image().height(4f).color(color.get(item)).growX().bottom().padTop(4f);
            }).size(170f, 74f).pad(10f);

            list.add(check).checked(button -> selected == item).size(264f, 74f).padBottom(16f).row();
        });
    }

    public T get() {
        return selected;
    }

    public void set(T item) {
        selected = item;
        onChanged.get(item);
    }
}