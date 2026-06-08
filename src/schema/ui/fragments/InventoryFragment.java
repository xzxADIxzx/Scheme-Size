package schema.ui.fragments;

import arc.input.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import schema.*;
import schema.ui.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Fragment that displays the block inventory overlay.
public class InventoryFragment extends Table
{
    /// Building that is being viewed at the moment.
    private Building selected;

    public InventoryFragment() { super(Style.find("panel-x-shape")); touchable = Touchable.enabled; }

    /// Builds the fragment and overrides the original.
    public void build(Group parent)
    {
        parent.addChild(this);

        setTransform(true);
        update(() ->
        {
            if (selected != null)
            {
                var pos = input.mouseScreen(selected.x + selected.hitSize() / 2f, selected.y + selected.hitSize() / 2f);
                setPosition(pos.x, pos.y, Align.topLeft);
            }
            setOrigin(Align.topLeft);
        }
        ).visible(() -> selected != null && selected.isValid());
    }

    // region control

    /// Shows the fragment with a simple animation.
    public void show(Building build)
    {
        if (visible && selected == build)
        {
            hide();
            return;
        }

        if (build.block.isAccessible() && build.items.total() > 0)
        {
            selected = build;
            Call.requestBlockSnapshot(build.pos());

            margin(4f).clear();
            defaults().pad(4f);

            content.items().each(build.items::has, i -> stack
            (
                new Table(t -> t.center()       .image(i.uiIcon).size(40f)),
                new Table(t -> t.bottom().left().label(() -> Tools.format(build.items.get(i), false)))
            )
            .with(s -> 
            {
                s.addListener(new HandCursorListener
                (
                    () -> !player.dead() && player.unit().acceptsItem(i) && player.unit().within(selected, itemTransferRange),
                    false
                ));
                s.clicked(KeyCode.mouseLeft,  () -> take(i, build.items.get(i)));
                s.clicked(KeyCode.mouseRight, () -> take(i, 1));

                if (children.size % 4 == 0) row();
            }
            ).size(48f).touchable(Touchable.enabled));

            pack();
            actions(Actions.scaleTo(0f, 1f), Actions.scaleTo(1f, 1f, .1f, Interp.pow4Out));
        }
    }

    /// Hides the fragment with a simple animation.
    public void hide()
    {
        actions(Actions.scaleTo(0f, 1f, .1f, Interp.pow4Out), Actions.run(this::hideImmediately));
    }

    /// Immediately hides the fragment.
    public void hideImmediately()
    {
        selected = null;
    }

    /// Requests the given amount of items.
    private void take(Item item, int amount)
    {
        amount = Math.min(amount, player.unit().maxAccepted(item));
        if (amount > 0)
            Call.requestItem(player, selected, item, amount);
    }

    // endregion
    // region agent

    /// Creates a new agent.
    public Agent agent() { return new Agent(); }

    /// Agent redirecting method calls from the original component.
    public class Agent extends mindustry.ui.fragments.BlockInventoryFragment
    {
        @Override
        public void showFor(Building tile) { show(tile); }

        @Override
        public void hide() { inv.hide(); }
    }

    // endregion
}
