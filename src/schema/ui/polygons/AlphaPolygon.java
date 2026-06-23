package schema.ui.polygons;

import arc.func.*;
import arc.math.geom.*;
import mindustry.gen.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Fragment that displays the alpha selection wheel.
public class AlphaPolygon extends Polygon
{
    @Override
    public void show(Vec2 position)
    {
        clear();

        add(Iconc.lock,    true, set(() -> alpha.lock = camera.unproject(x, y).cpy()));
        add(Iconc.refresh, true, set(() -> alpha.spin = camera.unproject(x, y).cpy()));
        add(Iconc.cancel,  true, set(() -> { }));

        indexer.getAllPresentOres().each(alpha::mineable, i -> add(i.emoji(), false, set(() -> alpha.mine = i)));

        Groups.player.each(alpha::assistable, p -> add(p.coloredName(), false, set(() -> alpha.assist = p)));

        super.show(position);
    }

    private Intc set(Runnable setter)
    {
        return _ ->
        {
            alpha.reset();
            setter.run();
            hide();
        };
    }
}
