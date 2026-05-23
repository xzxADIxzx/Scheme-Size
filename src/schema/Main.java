package schema;

import arc.struct.*;
import mindustry.mod.*;
import schema.ui.fragments.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/// Main class of the mod that loads, initializes and stores different components of it.
public class Main extends Mod
{
    // region components

    /// List of fetched servers that host CLaJ.
    public static Seq<String> clajURLs;
    /// List of events acquired via reflection.
    public static ObjectMap<?, Seq<?>> events;

    // endregion
    // region fragments

    public static LoadingFragment loadfrag;

    // endregion

    /// Loads content such as tools, dialogs, fragments and so on.
    public void load()
    {
        loadfrag = new LoadingFragment();
    }

    /// Hooks content such as input, dialogs, fragments and so on.
    public void hook()
    {
        loadfrag.build(scene.root);

        ui.loadfrag = loadfrag.agent();
    }

    @Override
    public void init()
    {
        Tools.log("[green]=> Loading content...");
        load();

        Tools.log("[green]=> Initializing content...");
        hook();
    }
}
