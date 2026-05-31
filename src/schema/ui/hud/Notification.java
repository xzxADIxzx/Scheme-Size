package schema.ui.hud;

import static arc.Core.*;

/// List of all types of HUD notifications.
public enum Notification
{
    // region types

    Waiting("hud.empty"),
    Rules("hud.rules"),
    Pause("hud.pause"),
    Build("hud.build"),
    Guardian("hud.sector-guardian"),
    Captured("hud.sector-captured"),
    Attacked("hud.sector-attacked"),
    Lost("hud.sector-lost"),
    Other(null);

    // endregion

    /// List of all types.
    public static final Notification[] all = values();

    /// Content of the notification.
    public final String content;

    private Notification(String content)
    {
        this.content = content;
    }

    /// Formats the content string.
    public String format(String sectors)
    {
        if (content == null)
            return sectors;
        if (sectors == null)
            return bundle.get(content);
        else
            return bundle.format(content, sectors);
    }
}
