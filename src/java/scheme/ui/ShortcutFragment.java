package scheme.ui;

import arc.scene.Group;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Scaling;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SchematicsDialog.SchematicImage;

import static mindustry.Vars.*;

public class ShortcutFragment {

    public boolean visible;

    public int lastIndex;
    public Runnable rebuild;
    public Runnable onShown;

    public void build(Group parent) {
        parent.fill(cont -> {
            cont.name = "tagselection";
            cont.add(/* selection */ "").visible(() -> visible).size(24f); // buttons size

            /*
            for (int i = 0; i < 6; i++) {
                String key = "shortcut-tag-" + i;
                selection.add(settings.getString(key, TagSelectDialog.none), button -> {
                    tag.show(button.getText().toString(), tag -> {
                        button.setText(tag);
                        settings.put(key, tag);

                        selection.updateAlignment();
                    });
                    hide();
                });
            }

            selection.updateAlignment();
            */
        });

        parent.fill(cont -> {
            cont.name = "schematicselection";
            cont.visible(() -> visible);

            cont.pane(list -> rebuild = () -> {
                String tag = /* selection.buttons[lastIndex].getText().toString() */ "";
                schematics.all().each(schematic -> schematic.labels.contains(tag), schematic -> {
                    list.button(button -> button.stack(
                            new SchematicImage(schematic).setScaling(Scaling.fit),
                            new Table(table -> table.top().table(Styles.black3, title -> {
                                title.add(schematic.name(), Styles.outlineLabel, .60f).width(72f).labelAlign(Align.center).get().setEllipsis(true);
                            }).pad(4f).width(72f))
                    ), () -> {
                        control.input.useSchematic(schematic);
                        hide();
                    }).margin(0f).pad(2f).size(80f);

                    if (list.getChildren().size % 4 == 0) list.row();
                });
            }).size(362f, 336f).update(pane -> {
                /*
                Tmp.r1.setSize(pane.getWidth(), pane.getHeight()).setPosition(pane.translation.x + pane.x, pane.translation.y + pane.y).grow(8f);
                selection.updatable = lastIndex == -1 || !Tmp.r1.contains(input.mouse());

                if (selection.selectedIndex == lastIndex) return;
                lastIndex = selection.selectedIndex;
                pane.getWidget().clear();

                if (lastIndex == -1) return;
                rebuild.run(); // rebuild schematics list

                Vec2 offset = selection.vertices[lastIndex].cpy().setLength(HexSelection.size * 1.5f);
                offset.sub(offset.x > 0 ? 0 : pane.getWidth(), offset.y > 0 ? 0 : offset.y == 0 ? pane.getHeight() / 2 : pane.getHeight());
                pane.setTranslation(selection.x + offset.x - pane.x, selection.y + offset.y - pane.y);
                */
            }).with(pane -> onShown = pane.getWidget()::clear);
        });
    }

    public void show(int x, int y) {
        // selection.setPosition(x, y);
        onShown.run(); // clear pane
        visible = true;
    }

    public void hide() {
        lastIndex = -1;
        visible = false;
    }
}
