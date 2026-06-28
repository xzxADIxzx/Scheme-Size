package schema.ui.polygons;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import schema.*;
import schema.input.*;
import schema.ui.Style;

import static mindustry.Vars.*;
import static schema.Main.*;

/// Fragment that displays the block selection wheel.
public class BlockPolygon extends Polygon
{
    /// Reordered list of the block categories.
    public static final Category[] categories =
    {
        Category.distribution,
        Category.liquid,
        Category.production,
        Category.crafting,
        Category.power,
        Category.defense,
        Category.turret,
        Category.units,
        Category.effect,
        Category.logic,
    };
    /// Reordered list of the character icons.
    public static final char[] icons = new char[categories.length];
    {
        for (int i = 0; i < categories.length; i++) icons[i] = (char) Iconc.codes.get(categories[i].name());
    }

    /// List of blocks.
    private Table list;
    /// Rebuilds the list of blocks.
    private Runnable rebuild;

    /// Size of the chosen category.
    private int blocks;
    /// Hovered blocks.
    private Block hovered, tmp;

    @Override
    public void build(Group parent)
    {
        super.build(parent);
        keyDown(_ ->
        {
            if (Keybind.inspect.tap() && hovered != null) ui.content.show(hovered);
        });

        for (int i = 0; i < categories.length; i++) add(icons[i], true, j ->
        {
            draw = false;
            rebuild = () ->
            {
                removeChild(list);
                add(list = new Table(Style.find("panel-o-shape"))
                {
                    @Override
                    public void layout()
                    {
                        super.layout();
                        float
                            width  = 212f,
                            height = Mathf.round(getPrefHeight());

                        setSize(width, height);
                        translation.set(width, Mathf.ceil(blocks / 4f) * 44f + 36f).scl(-.5f);
                    }
                });

                list.update(() ->
                {
                    if (tmp != hovered)
                    {
                        tmp = hovered;
                        rebuild.run();
                    }
                });

                list.margin(12f);
                list.defaults().pad(4f);

                if (hovered != null)
                {
                    list.table(pane ->
                    {
                        pane.defaults().width(180f);
                        pane.add(hovered.localizedName).left().wrap().row();

                        for (var stack : hovered.requirements) pane.table(line ->
                        {
                            line.image(stack.item.uiIcon).size(16f);
                            line.add(stack.item.localizedName).growX().pad(0f, 2f, 0f, 2f).left().ellipsis(true).fontScale(.9f).color(Pal.lightishGray).update(l ->
                            {
                                l.setWidth(160f - line.getChildren().peek().getPrefWidth());
                            });

                            line.label(() ->
                            {
                                var core = player.core();
                                int required = Math.round(stack.amount * state.rules.buildCostMultiplier);

                                if (core == null || state.rules.infiniteResources) return "[light]*/[]" + Tools.format(required);

                                int amount = core.items.get(stack.item);
                                var color = amount < required / 2f ? "[scarlet]" : amount < required ? "[accent]" : "[white]";

                                return color + Tools.format(amount) + "[light]/[white]" + Tools.format(required);
                            }
                            ).fontScale(.9f);
                        }).row();

                        if (!hovered.isPlaceable()) pane.add(!hovered.supportsEnv(state.rules.env) ? "@block.badenv" : "@block.banned");
                    }).row();

                    list.image().growX().height(4f).color(Pal.accent).row();
                }

                blocks = 0;

                list.table(pane -> unlocked(categories[j], b ->
                {
                    pane.margin(0f, 0f, 4f, 4f);
                    pane.defaults().pad(0f, 0f, -4f, -4f);

                    var available = state.rules.infiniteResources || (player.core() != null && player.core().items.has(b.requirements));

                    if (blocks++ % 4 == 0) pane.row();

                    pane.button(new TextureRegionDrawable(b.uiIcon), Style.ibt, () ->
                    {
                        if (Keymask.any())
                            Tools.copy(Tools.icon(b.name) + "");
                        else
                            insys.block = b;

                        hide();
                    }
                    ).size(48f).checked(insys.block == b).disabled(!b.isPlaceable() || !available).with(button ->
                    {
                        if (button.isDisabled()) button.toBack();
    
                        button.resizeImage(32f);
                        button.getImage().setColor(!b.isPlaceable() ? Pal.gray : !available ? Pal.lightishGray : Color.white);

                        button.hovered(() -> hovered = b);
                        button.exited (() -> hovered = null);
                    });
                }));
            };
            rebuild.run();
        });
    }

    @Override
    public void hideImmediately() { super.hideImmediately(); removeChild(list); }

    /// Whether the block is visible and unlocked.
    public boolean unlocked(Block block) { return block.placeablePlayer && block.unlockedNowHost() && block.environmentBuildable() && block.isVisible(); }

    /// Iterates unlocked blocks in the given category.
    public void unlocked(Category cat, Cons<Block> cons)
    {
        content.blocks().select
        (
            b -> b.category == cat && unlocked(b)
        )
        .sort
        (
            (a, b) -> Boolean.compare(!a.isPlaceable(), !b.isPlaceable())
        )
        .each(cons);
    }
}
