package schema.input;

import arc.input.*;

import static arc.Core.*;

/** List of all keybinds of the mod, including new ones and those that replace vanilla binds. */
public enum Keybind {

    // region binds

    move_x(KeyCode.a, KeyCode.d, "Movement"),
    move_y(KeyCode.s, KeyCode.w),
    pan_mv(KeyCode.mouseForward),
    mouse_mv(KeyCode.mouseBack);

    // endregion

    /** All of the following keybinds are assumed to belong to the same category unless they have their own. */
    public final String category;
    /** Default values of the keybind that are assigned in the constructor. */
    public final KeyCode defaultKey, defaultMin, defaultMax;

    /** Primary key of the keybind and axis keys that are used in pair. */
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

    @Override
    public String toString() { return name().replace('_', '-'); }

    // region state

    /** Whether the bind is held down. */
    public boolean down() { return input.keyDown(key); }

    /** Whether the bind was just pressed. */
    public boolean tap() { return input.keyTap(key); }

    /** Whether the bind was just released. */
    public boolean release() { return input.keyRelease(key); }
    
    /** Whether the bind is a single key / <b>not</b> an axis. */
    public boolean single() { return defaultKey != KeyCode.unset; }

    /** Returns a value between -1 and 1. */
    public float axis() { return (input.keyDown(max) ? 1f : 0f) - (input.keyDown(min) ? 1f : 0f); }

    // endregion
    // region rebinding

    /** Saves the bind in {@link arc.Core#settings}. */
    public void save() {
        if (single())
            settings.put("schema-keybind-" + toString() + "-key", key.ordinal());
        else {
            settings.put("schema-keybind-" + toString() + "-min", min.ordinal());
            settings.put("schema-keybind-" + toString() + "-max", max.ordinal());
        }
    }

    /** Loads the bind from {@link arc.Core#settings}. */
    public void load() {
        if (single())
            key = KeyCode.all[settings.getInt("schema-keybind-" + toString() + "-key", defaultKey.ordinal())];
        else {
            min = KeyCode.all[settings.getInt("schema-keybind-" + toString() + "-min", defaultMin.ordinal())];
            max = KeyCode.all[settings.getInt("schema-keybind-" + toString() + "-max", defaultMax.ordinal())];
        }
    }

    /** Resets the bind to its default values. */
    public void reset() {
        if (single())
            settings.remove("schema-keybind-" + toString() + "-key");
        else {
            settings.remove("schema-keybind-" + toString() + "-min");
            settings.remove("schema-keybind-" + toString() + "-max");
        }

        key = defaultKey;
        min = defaultMin;
        max = defaultMax;
    }

    /** Sets the values of the bind to {@link KeyCode#unset}. */
    public void clear() {
        // TODO mask
        key = min = max = KeyCode.unset;
    }

    // endregion
}
