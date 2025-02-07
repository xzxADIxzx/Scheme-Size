package schema.input;

import arc.input.*;

/** List of all keybinds of the mod, including new ones and those that replace vanilla binds. */
public enum Keybind {

    // region binds

    move_x(KeyCode.a, KeyCode.d, "Movement"),
    move_y(KeyCode.s, KeyCode.w),
    pan_mv(KeyCode.mouseForward),
    mouse_mv(KeyCode.mouseBack);

    // endregion

    /** All of the following keybinds are assumed to belong to the same category unless they have their own. */
    private final String category;
    /** Default values of the bind that are assigned in the constructor. */
    private final KeyCode defaultKey, defaultMin, defaultMax;

    /** Primary key of the bind and axis keys that are used in pair. */
    private KeyCode key, min, max;

    private Keybind(KeyCode key, String category) {
        this.category = category;
        this.defaultKey = key;

        this.defaultMin = this.defaultMax = KeyCode.unset;
    }

    private Keybind(KeyCode key) { this(key, (String) null); }

    private Keybind(KeyCode min, KeyCode max, String category) {
        this.category = category;
        this.defaultMin = min;
        this.defaultMax = max;

        this.defaultKey = KeyCode.unset;
    }

    private Keybind(KeyCode min, KeyCode max) { this(min, max, null); }
}
