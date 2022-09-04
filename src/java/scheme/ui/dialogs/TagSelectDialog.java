package scheme.ui.dialogs;

import arc.func.Cons;
import arc.struct.Seq;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static arc.Core.*;

@SuppressWarnings("unchecked")
public class TagSelectDialog extends BaseDialog {

    public static final float tagh = 42f;
    public static final String none = "\uE868";

    public Cons<String> callback;
    public String current;

    public Runnable rebuild;

    public TagSelectDialog() {
        super("@select.tag");
        addCloseButton();

        cont.pane(Styles.noBarPane, list -> rebuild = () -> {
            list.left().clear();
            list.defaults().pad(2f).height(tagh);

            Seq<String> tags = settings.getJson("schematic-tags", Seq.class, String.class, Seq::new);
            tags.insert(0, none); // add none to the beginning

            tags.each(tag -> { // creating a variable is needed to bring the tag to a string
                list.button(tag, Styles.togglet, () -> {
                    callback.get(tag);
                    hide();
                }).checked(current.equals(tag)).with(button -> button.getLabel().setWrap(false));
            });
        }).fillX().height(tagh).scrollY(false);
    }

    public void show(String current, Cons<String> callback) {
        this.callback = callback;
        this.current = current;

        rebuild.run();
        show();
    }
}
