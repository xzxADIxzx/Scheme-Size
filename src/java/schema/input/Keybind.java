package schema.input;

import arc.input.*;

import static arc.Core.*;

/** List of all keybinds of the mod, including new ones and those that replace vanilla binds. */
public enum Keybind {

    // region binds

    move_x(0, KeyCode.a, KeyCode.d, "movement"),
    move_y(0, KeyCode.s, KeyCode.w),
    pan_mv(0, KeyCode.mouseForward),
    mouse_mv(0, KeyCode.mouseBack),
    teleport(3, KeyCode.mouseBack),
    boost(0, KeyCode.shiftLeft),
    look_at(0, KeyCode.altLeft),
    respawn(0, KeyCode.v),
    despawn(3, KeyCode.v),
    pick_cargo(0, KeyCode.leftBracket),
    drop_cargo(0, KeyCode.rightBracket),

    build_bd(0, KeyCode.mouseLeft, "building"),
    break_bd(0, KeyCode.mouseRight),
    rebuild(0, KeyCode.b),
    replace(3, KeyCode.mouseLeft),
    select(0, KeyCode.mouseLeft),
    deselect(0, KeyCode.q),
    hexblock(3, KeyCode.q),
    pause_bd(0, KeyCode.e),
    clear_bd(0, KeyCode.c),
    drop(3, KeyCode.mouseMiddle),
    pick(0, KeyCode.r),
    rotate(3, KeyCode.r),
    sel_schematic(0, KeyCode.t),
    hex_schematic(3, KeyCode.f),
    new_schematic(0, KeyCode.f),
    flip_x(0, KeyCode.z),
    flip_z(0, KeyCode.x),

    command(0, KeyCode.shiftLeft, "command"),
    control(0, KeyCode.controlLeft),
    select_all_units(0, KeyCode.g),
    select_all_factories(0, KeyCode.h),

    menu(0, KeyCode.escape, "view"),
    pause(0, KeyCode.space),
    sector_map(0, KeyCode.m),
    planet_map(0, KeyCode.n),
    research(0, KeyCode.j),
    display_xray(3, KeyCode.x),
    display_prod(3, KeyCode.c),
    block_info(0, KeyCode.f1),
    tgl_menus(0, KeyCode.backtick),
    tgl_power_lines(0, KeyCode.f5),
    tgl_block_status(0, KeyCode.f6),
    tgl_block_health(0, KeyCode.f7),
    tgl_fullscreen(0, KeyCode.f11);

    // endregion

    /** List of all keybinds used for loading, conflict resolving and so on. */
    public static final Keybind[] all = values();

    /** All of the following keybinds are assumed to belong to the same category unless they have their own. */
    public final String category;
    /** Default mask of the keybind that is assigned in the constructor. */
    public final Keymask defaultMask;
    /** Default values of the keybind that are assigned in the constructor. */
    public final KeyCode defaultKey, defaultMin, defaultMax;

    /** The keybind cannot be pressed unless the mask key is held down. */
    private Keymask mask;
    /** Primary key of the keybind and axis keys that are used in pair. */
    private KeyCode key, min, max;
    /** If there is a keybind with the same primary key, but different mask, the last is marked in this list. */
    private boolean[] conflicts = new boolean[3];

    private Keybind(int mask, KeyCode key, String category) {
        this.category = category;
        this.defaultMask = Keymask.all[mask];
        this.defaultKey = key;

        this.defaultMin = this.defaultMax = KeyCode.unset;
    }

    private Keybind(int mask, KeyCode key) { this(mask, key, (String) null); }

    private Keybind(int mask, KeyCode min, KeyCode max, String category) {
        this.category = category;
        this.defaultMask = Keymask.all[mask];
        this.defaultMin = min;
        this.defaultMax = max;

        this.defaultKey = KeyCode.unset;
    }

    private Keybind(int mask, KeyCode min, KeyCode max) { this(mask, min, max, null); }

    @Override
    public String toString() { return name().replace('_', '-'); }

    /** Returns the formatted values of the bind aka the primary and axis keys. */
    public String formatKeys() { return single()
        ? (key == KeyCode.unset ? "[disabled]unset" : key.toString())
        : (min == KeyCode.unset ? "[disabled]unset" : min.toString() + "[red]/[]" + max.toString()); }

    /** Returns the formatted mask name. */
    public String formatMask() { return Keymask.names[mask.ordinal()]; }

    /** Returns the current mask. */
    public int mask() { return mask.ordinal(); }

    // region state

    /** Whether any of the conflicting masks are held down. */
    public boolean conflicting() {
        for (int i = 1; i < Keymask.all.length; i++) if (conflicts[i - 1] && Keymask.all[i].down.get()) return true;
        return false;
    }

    /** Whether the bind is held down. */
    public boolean down() { return mask.down.get() && input.keyDown(key) && !conflicting(); }

    /** Whether the bind was just pressed. */
    public boolean tap() { return mask.down.get() && input.keyTap(key) && !conflicting(); }

    /** Whether the bind was just released. */
    public boolean release() { return input.keyRelease(key); }
    
    /** Whether the bind is a single key / <b>not</b> an axis. */
    public boolean single() { return defaultKey != KeyCode.unset; }

    /** Returns a value between -1 and 1. */
    public float axis() { return mask.down.get() ? (input.keyDown(max) ? 1f : 0f) - (input.keyDown(min) ? 1f : 0f) : 0f; }

    // endregion
    // region rebinding

    /** Saves the bind in {@link arc.Core#settings settings}. */
    public void save() {
        if (single())
            settings.put("schema-keybind-" + this + "-key", key.ordinal());
        else {
            settings.put("schema-keybind-" + this + "-min", min.ordinal());
            settings.put("schema-keybind-" + this + "-max", max.ordinal());
        }
        settings.put("schema-keybind-" + this + "-mask", mask.ordinal());
    }

    /** Loads the bind from {@link arc.Core#settings settings}. */
    public void load() {
        if (single())
            key = KeyCode.all[settings.getInt("schema-keybind-" + this + "-key", defaultKey.ordinal())];
        else {
            min = KeyCode.all[settings.getInt("schema-keybind-" + this + "-min", defaultMin.ordinal())];
            max = KeyCode.all[settings.getInt("schema-keybind-" + this + "-max", defaultMax.ordinal())];
        }
        mask = Keymask.all[settings.getInt("schema-keybind-" + this + "-mask", defaultMask.ordinal())];
    }

    /** Resets the bind to its default values. */
    public void reset() {
        if (single())
            settings.remove("schema-keybind-" + this + "-key");
        else {
            settings.remove("schema-keybind-" + this + "-min");
            settings.remove("schema-keybind-" + this + "-max");
        }
        settings.remove("schema-keybind-" + this + "-mask");

        mask = defaultMask;
        key = defaultKey;
        min = defaultMin;
        max = defaultMax;
    }

    /** Sets the values of the bind to {@link KeyCode#unset}. */
    public void clear() {
        mask = Keymask.unset;
        key = min = max = KeyCode.unset;
    }

    /** Changes the mask of the bind. */
    public void rebind(int mask) { this.mask = Keymask.all[mask]; }

    /** Changes the primary key of the bind. */
    public void rebind(KeyCode key) {
        if (!single()) throw new UnsupportedOperationException("Keybind " + this + " is an axis bind and cannot be rebinded to a single key");

        this.key = key;
    }

    /** Changes the axis keys of the bind. */
    public void rebind(KeyCode min, KeyCode max) {
        if (single()) throw new UnsupportedOperationException("Keybind " + this + " is a single key bind and cannot be rebinded to an axis");

        this.min = min;
        this.max = max;
    }

    /** Updates the list of conflicting masks. Returns the amount of conflicts found. */
    public int resolveConflicts() {
        int amount = 0;
        for (int i = 0; i < conflicts.length; i++) conflicts[i] = false;

        for (var bind : all) if (bind.key == key && bind.mask != mask && bind.mask != Keymask.unset) {
            amount++;
            conflicts[bind.mask.ordinal() - 1] = true;
        }
        return amount;
    }

    // endregion
}
