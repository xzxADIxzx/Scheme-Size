package scheme.ui.dialogs;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Cons3;
import arc.func.Prov;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OverlayFloor;
import mindustry.world.blocks.environment.StaticWall;
import scheme.ui.List;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

import com.github.bsideup.jabel.Desugar;

public class TileSelectDialog extends BaseDialog {

    public static final int row = mobile ? 8 : 10;
    public static final float size = mobile ? 54f : 64f;

    public Table blocks = new Table();

    public Block floor;
    public Block block;
    public Block overlay;
    public List<Folder> list;

    public TileSelectDialog() {
        super("@select.tile");
        addCloseButton();

        Seq<Folder> folders = Seq.with(
                new Folder("select.floor", () -> floor, b -> b instanceof Floor && !(b instanceof OverlayFloor), b -> floor = b),
                new Folder("select.block", () -> block, b -> b instanceof StaticWall, b -> block = b),
                new Folder("select.overlay", () -> overlay, b -> b instanceof OverlayFloor, b -> overlay = b));

        list = new List<>(folders::each, Folder::name, Folder::icon, folder -> Pal.accent);
        list.onChanged = this::rebuild;
        list.set(folders.first());
        list.rebuild();
        
        list.build(cont);
        cont.add(blocks).growX();
        cont.table().width(288f);
    }

    public void rebuild(Folder folder) {
        blocks.clear();
        blocks.table(table -> {
            table.defaults().size(size);

            table.button(Icon.none, () -> folder.callback(null));
            table.button(Icon.line, () -> folder.callback(Blocks.air));

            content.blocks().each(folder::pred, block -> {
                TextureRegionDrawable drawable = new TextureRegionDrawable(block.uiIcon);
                table.button(drawable, () -> folder.callback(block));

                if (table.getChildren().count(i -> true) % row == 0) table.row();
            });
        });
    }

    public void select(Cons3<Floor, Block, Floor> callback) {
        callback.get(floor != null ? floor.asFloor() : null, block, overlay != null ? overlay.asFloor() : null);
    }

    public void select(int x, int y) {
        Tile tile = world.tile(x, y);
        floor = tile.floor();
        block = tile.block();
        overlay = tile.overlay();
        list.rebuild();
    }

    @Desugar
    public record Folder(String name, Prov<Block> block, Boolf<Block> pred, Cons<Block> callback) {

        public String name() {
            Block selected = block.get();
            return bundle.format(name, selected == null ? bundle.get("none") : selected.localizedName);
        }

        public TextureRegion icon() {
            Block selected = block.get();
            return selected == null ? Icon.none.getRegion() : selected == Blocks.air ? Icon.line.getRegion() : selected.uiIcon;
        }

        public boolean pred(Block block) {
            return pred.get(block) && block.id > 1;
        }

        public void callback(Block block) {
            callback.get(block);
            tile.list.rebuild();
        }
    }
}
