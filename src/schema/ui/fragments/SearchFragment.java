package schema.ui.fragments;

import arc.input.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import schema.input.*;
import schema.ui.*;

/// Fragment that displays a search bar.
public class SearchFragment extends Table
{
    /// Search bar.
    private TextField field;
    /// Search results.
    private Table results;

    public SearchFragment() { touchable = Touchable.enabled; }

    /// Builds the fragment and overrides the original.
    public void build(Group parent)
    {
        parent.addChild(this);

        setTransform(true);
        setFillParent(true);
        hideImmediately();

        keyDown(key ->
        {
            if (key == KeyCode.escape || key == KeyCode.back)
            {
                hide();
                return;
            }
            if (key == KeyCode.enter)
            {
                handle();
                return;
            }
            if (key == KeyCode.backspace && Keymask.any()) field.setText("");
        });

        field = field("", Style.tfs, this::handle).growX().height(40f).update(Element::requestKeyboard).visible(() -> visible).get();
        field.setAlignment(Align.center);
        results = row().table(Style.find("panel-x-shape")).get();
    }

    // region control

    /// Shows the fragment with a simple animation.
    public void show()
    {
        visible = true;

        actions(Actions.originCenter(), Actions.scaleTo(1f, 0f), Actions.scaleTo(1f, 1f, .1f));
    }

    /// Hides the fragment with a simple animation.
    public void hide()
    {
        actions(Actions.originCenter(), Actions.scaleTo(1f, 0f, .1f), Actions.run(this::hideImmediately));
    }

    /// Immediately hides the fragment.
    public void hideImmediately()
    {
        visible = false;
    }

    /// Handles text input.
    private void handle(String value)
    {
        results.margin(4f).clear();
        results.defaults().pad(4f);

        if (value.isBlank())
        {
            results.add("@search.placeholder").style(Style.outline);
            return;
        }
    }

    /// Handles text input.
    private void handle()
    {
        Time.run(.1f * 60f, () ->
        {
            field.setText("");
            handle("");
        });
        hide();
    }

    // endregion
}
