package schema.ui.fragments;

import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.fragments.*;

/** Fragment that displays the block configuration overlay. */
public class ConfigFragment extends Table {

    /** Building that is being configured at the moment. */
    private Building selected;

    public ConfigFragment() { touchable = Touchable.enabled; }

    /** Builds the fragment and override the original one. */
    public void build(Group parent) {
        parent.addChild(this);

        setTransform(true);
        update(() -> {
            if (selected != null) selected.updateTableAlign(this);
        });
    }

    // region config

    /** Shows the fragment with a simple animation. */
    public void show(Building build) {
        if (selected != null) selected.onConfigureClosed();
        if (build.configTapped()) {
            selected = build;
            visible = true;

            clear();
            build.buildConfiguration(this);
            pack();
            actions(Actions.scaleTo(0f, 1f), Actions.scaleTo(1f, 1f, .06f));
        }
    }

    /** Hides the fragment with a simple animation. */
    public void hide() {
        if (selected != null) selected.onConfigureClosed();
        actions(Actions.scaleTo(0f, 1f, .06f), Actions.run(this::hideImmediately));
    }

    /** Immediately hides the fragment without any animation. */
    public void hideImmediately() {
        selected = null;
        visible = false;
    }

    public boolean shown() { return visible && selected != null; }

    public Building selected() { return selected; }

    // endregion
    // region render

    // endregion
    // region agent

    /** Returns the agent of this fragment. */
    public Agent getAgent() { return new Agent(); }

    /** Agent that redirects method calls from the original fragment to the new one. */
    public class Agent extends BlockConfigFragment {

        @Override
        public void hideConfig() { hide(); }

        @Override
        public void forceHide() { hideImmediately(); }

        @Override
        public boolean isShown() { return shown(); }

        @Override
        public Building getSelected() { return selected; }
    }

    // endregion
}
