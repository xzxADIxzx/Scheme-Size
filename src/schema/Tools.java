package schema;

import arc.*;
import arc.util.*;
import mindustry.ui.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Set of different tools for simplifying life and systematization of code.
public final class Tools
{
    /// Prints the given info into the dev console.
    public static void log(String info) { Log.infoTag("Schema", info); }

    /// Prints the given error into the dev console.
    public static void err(Throwable e) { Log.errTag("Schema", Strings.getStackTrace(e)); }

    /// Returns the icon's character by its name.
    public static char icon(String name) { return (char) Fonts.getUnicode(name); }

    /// Copies the given text into the clipboard.
    public static void copy(String text)
    {
        app.setClipboardText(text);
        ui.showInfoFade("@copied");
    }

    /// Use this **extremely carefully** as it clears all event listeners created by the given class.
    public static void clear(Class<?> target)
    {
        if (events == null)
            events = Reflect.get(Events.class, "events");

        int count = 0;
        for (var pair : events) count += pair.value.size - pair.value.removeAll(l -> l.toString().startsWith(target.getName())).size;

        log("[red] < Cleared [accent]" + count + "[] events of " + target.getSimpleName());
    }

    /// Returns the given number with a fixed amount of decimal places.
    public static StringBuilder format(float num, boolean flow)
    {
        if (num >= 100_000_000_000f)
            return Strings.fixedBuilder(num / 1_000_000_000f, 0).append("[light]b");
        if (num >= 1_000_000_000f)
            return Strings.fixedBuilder(num / 1_000_000_000f, 1).append("[light]b");

        if (num >= 100_000_000f)
            return Strings.fixedBuilder(num / 1_000_000f, 0).append("[light]m");
        if (num >= 1_000_000f)
            return Strings.fixedBuilder(num / 1_000_000f, 1).append("[light]m");

        if (num >= 100_000f)
            return Strings.fixedBuilder(num / 1_000f, 0).append("[light]k");
        if (num >= 1_000f)
            return Strings.fixedBuilder(num / 1_000f, 1).append("[light]k");

        return Strings.fixedBuilder(num, flow ? 1 : 0);
    }

    /// Returns the given number with a fixed amount of decimal places.
    public static CharSequence format(float num) { return format(num, false); }

    /// Returns the given number with a fixed amount of decimal places.
    public static CharSequence flow(float num) { return format(num, true).insert(0, num >= .1f ? "[green]+" : num <= -.1f ? "[scarlet]" : "[light]").append("[light]/s"); }
}
