package scheme.ui;

import arc.func.Cons;
import arc.input.KeyCode;
import arc.scene.ui.TextArea;
import arc.scene.ui.TextField;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.TextField.TextFieldFilter;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Reflect;
import arc.util.Strings;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.input.Binding;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ConsoleFragment extends Table {

    public Seq<Table> tabs;
    public ConsoleTab last;

    public Table cont;
    public ResizableArea chat;

    public Seq<String> history = Reflect.get(ui.consolefrag, "history");
    public int position;

    public ConsoleFragment() {
        scene.add(this);
        scene.root.removeChild(ui.consolefrag);

        this.visibility = ui.consolefrag.visibility;
        ui.consolefrag.visibility = null; // hacky

        update(() -> {
            if (input.keyTap(KeyCode.tab) && !scene.hasKeyboard()) showTab(last.next());

            // don't ask, please
            ui.consolefrag.setPosition(-5f, -5f);
            ui.consolefrag.getChildren().get(1).setWidth(graphics.getWidth() - Scl.scl(26f));

            if (last != ConsoleTab.multiline || scene.getKeyboardFocus() != chat.area) return;

            if (input.keyTap(Binding.chat_history_prev) && (last != ConsoleTab.multiline || input.shift()) && position < history.size - 1) {
                if (position == 0) history.set(0, chat.getText());
                chat.setText(history.get(++position));
            }

            if (input.keyTap(Binding.chat_history_next) && (last != ConsoleTab.multiline || input.shift()) && position > 0)
                chat.setText(history.get(--position));
        });
    }

    public void build() {
        margin(4f).setFillParent(true);

        var style = new TextButtonStyle() {{
            font = Fonts.def;
            down = Styles.flatDown;
            up = Styles.black5;
            over = Styles.flatOver;
        }};

        table(tabs -> {
            tabs.defaults().height(28f).growX();

            for (ConsoleTab tab : ConsoleTab.values())
                tabs.button("@console." + tab, style, () -> showTab(tab));

            tabs.getCells().get(1).pad(0f, 4f, 0f, 4f);
        }).growX().padBottom(4f).row();

        table(cont -> this.cont = cont.bottom().left()).grow();

        // region tabs

        tabs = Seq.with(ui.consolefrag);

        tabs.add(new Table(cont -> {
            cont.setFillParent(true);
            cont.bottom().defaults().growX();

            Table list = new Table(Styles.black5);
            list.margin(4f).defaults().growX().padBottom(3f);

            Seq<String> messages = Reflect.get(ui.consolefrag, "messages");
            cont.pane(list).update(pane -> {
                int current = list.getChildren().size;
                if (current == messages.size) return;

                for (int i = current; i < messages.size; i++)
                    list.labelWrap(messages.get(messages.size - i - 1)).row();
            }).padBottom(4f).row();

            cont.table(input -> {
                input.add(chat = new ResizableArea()).growX().padRight(4f);
                input.button("@console.multiline.send", style, () -> {
                    TextField field = Reflect.get(ui.consolefrag, "chatfield");
                    field.setText(chat.getText());

                    Reflect.invoke(ui.consolefrag, "sendMessage");
                }).width(100f).fillY();
            });
        }));

        tabs.add(new Table(cont -> {
            cont.setFillParent(true);
            cont.top().defaults().growX();

            Table list = new Table();
            list.defaults().growX().padBottom(4f);
            list.update(() -> list.getCells().removeAll(cell -> cell.get() == null));

            cont.pane(list).row();
            cont.button("@console.schedule.new", style, () -> {
                list.add(new TaskButton()).row();
            }).height(28f);
        }));

        // endregion

        showTab(ConsoleTab.classic);
    }

    public void showTab(ConsoleTab tab) {
        this.last = tab;

        cont.clear();
        cont.add(tabs.get(tab.id()));
    }

    public enum ConsoleTab {
        classic, multiline, schedule;

        public int id() {
            return Seq.with(values()).indexOf(this);
        }

        public ConsoleTab next() {
            return values()[(id() + 1) % 3];
        }
    }

    public class TaskButton extends Table {

        public Task task;
        public float interval = 1f;
        public String output;
        public Runnable restart;

        public TaskButton() {
            super(Styles.black5);
            margin(4f).defaults().growX().padBottom(4f);

            add(new ResizableArea("// write your code here", code -> {
                if (task != null) task.cancel();
                task = Timer.schedule(() -> output = mods.getScripts().runConsole(code), 0f, interval);
            })).with(area -> restart = area.area::change).row();

            table(input -> {
                input.left();
                input.add("@console.schedule.interval");

                input.field("1.0", TextFieldFilter.floatsOnly, num -> {
                    interval = Strings.parseFloat(num, 1f);
                    if (interval < 0.01f) interval = 1f;

                    restart.run(); // update task
                }).update(field -> {
                    if (scene.getKeyboardFocus() != field) field.setText(String.valueOf(interval));
                }).width(200f).tooltip("@console.schedule.tooltip").row();
            }).row();

            labelWrap(() -> bundle.format("console.schedule.output", output != null && output.contains("\n") ? "\n" : " ", output)).row();

            button("@console.schedule.cancel", Styles.cleart, () -> {
                if (task != null) task.cancel();
                remove();
            }).padBottom(0f);
        }
    }

    public class ResizableArea extends Table {

        public TextArea area;
        public int last;

        public ResizableArea() {
            super(Styles.black5);

            this.area = new TextArea("");
            rebuild();
        }

        public ResizableArea(String text, Cons<String> listener) {
            this.area = new TextArea(text);
            this.area.changed(() -> {
                listener.get(area.getText());
            });

            rebuild();
        }

        public void rebuild() {
            last = area.getLines();
            area.setPrefRows(Math.max(last + 1, 3));

            int pos = area.getCursorPosition();

            clear();
            add(area).grow().update(area -> {
                if (area.getLines() != last) rebuild();
            });

            area.setCursorPosition(pos);
            scene.setKeyboardFocus(area);
        }

        public void setText(String text) {
            area.setText(text);
            area.setCursorPosition(text.length());
        }

        public String getText() {
            return area.getText();
        }
    }
}
