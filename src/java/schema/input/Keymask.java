package schema.input;

import arc.func.*;

import static arc.Core.*;

/** Keymasks are used for advanced keybinds that require their <i>mask</i> key to be pressed alongside with the <i>primary</i> key. */
public enum Keymask {

    // region masks

    unset(() -> true),
    shift(input::shift),
    ctrl(input::ctrl),
    alt(input::alt);

    // endregion

    /** List of all keymasks used for loading. */
    public static final Keymask[] all = values();
    /** List of formatted names of the keymasks. */
    public static final String[] names = { "[disabled]unset", "Shift", "Ctrl", "Alt" };

    /** Whether the mask is held down. */
    public Boolp down;

    private Keymask(Boolp down) { this.down = down; }
}
