package schema.input;

import arc.func.*;

import static arc.Core.*;

/// List of all keymasks needed for advanced keybinds requiring both the *mask* and the *primary* keys to be pressed.
public enum Keymask
{
    // region masks

    unset(() -> true  ),
    shift(input::shift),
    ctrl (input::ctrl ),
    alt  (input::alt  );

    // endregion

    /// List of all keymasks.
    public static final Keymask[] all = values();
    /// List of names for UI.
    public static final String[] names = { "[heavy]unset", "Shift", "Ctrl", "Alt" };

    /// Whether the mask is held down.
    public Boolp down;

    /// Whether any mask is held down.
    public static boolean any() { return input.shift() || input.ctrl() || input.alt(); }

    private Keymask(Boolp down) { this.down = down; }
}
