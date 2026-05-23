package schema;

import arc.struct.*;
import mindustry.mod.*;

/// Main class of the mod that loads, initializes and stores different components of it.
public class Main extends Mod
{
    // region components

    /// List of fetched servers that host CLaJ.
    public static Seq<String> clajURLs;
    /// List of events acquired via reflection.
    public static ObjectMap<?, Seq<?>> events;

    // endregion

    /// Loads content such as tools, dialogs, fragments and so on.
    public void load() { }

    /// Hooks content such as input, dialogs, fragments and so on.
    public void hook() { }

    @Override
    public void init()
    {
        Tools.log("[green]=> Loading content...");
        load();

        Tools.log("[green]=> Initializing content...");
        hook();
    }
}
