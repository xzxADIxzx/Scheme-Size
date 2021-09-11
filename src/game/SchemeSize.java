package mindustry.game;

import arc.*;
import arc.util.*;
import arc.input.*;
import arc.input.InputDevice.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.KeyBinds.*;
import mindustry.mod.*;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.input.*;

public class SchemeSize extends Mod{

    public SchemeSize(){
        Events.on(ClientLoadEvent.class, e -> {
            // wait 10 secs, because... idk
            Time.runTask(10f, () -> {
                // Change schematics
                Vars.schematics = new Schematics512();
                Vars.schematics.loadSync();

                // Change input
                if(!Vars.mobile){
                    Vars.control.setInput(new DesktopInput512());
                }

                // Add settings
                var game = Vars.ui.settings.game;
                game.checkPref("secret", false);
                game.sliderPref("maxzoommul", 4, 4, 8, 1, i -> i / 4f + "x");
                game.sliderPref("minzoommul", 4, 4, 8, 1, i -> i / 4f + "x");
                game.sliderPref("copysize", 512, 32, 512, 32, i -> Core.bundle.format("setting.blocks", i));
                game.sliderPref("breaksize", 512, 32, 512, 32, i -> Core.bundle.format("setting.blocks", i));
                game.checkPref("copyshow", true);
                game.checkPref("breakshow", true);
                game.getCells().get(11).visible(false); // Hide secret

                // Add zoom scale
                Stack elementMax = (Stack)game.getCells().get(12).get();
                Stack elementMin = (Stack)game.getCells().get(13).get();
                Slider sliderMax = (Slider)elementMax.getChildren().get(0);
                Slider sliderMin = (Slider)elementMin.getChildren().get(0);
                sliderMax.changed(() -> { Vars.renderer.maxZoom = sliderMax.getValue() / 4f * 6f; });
                sliderMin.changed(() -> { Vars.renderer.minZoom = 1 / (sliderMin.getValue() / 4f) * 1.5f; });
                Vars.renderer.maxZoom = sliderMax.getValue() / 4f * 6f; // Apply zoom
                Vars.renderer.minZoom = 1f / (sliderMin.getValue() / 4f) * 1.5f;

                // Add keybind
                // var keybinds = new Array();
                // Binding.values().forEach(item -> keybinds.splice(0, 0, item));
                // keybinds.reverse();
                // keybinds.splice(3, 0, new Bind());
                // Core.keybinds.setDefaults(keybinds);
                Core.keybinds.setDefaults(ExBinding.values());

                // Add logs
                // Log.info(Vars.schematics);
                // Log.info(Vars.control.input);
            });
        });
    }

    public enum ExBinding implements KeyBind{
        move_x(new Axis(KeyCode.a, KeyCode.d), "general"),
        move_y(new Axis(KeyCode.s, KeyCode.w)),
        mouse_move(KeyCode.mouseBack),
        pan(KeyCode.mouseForward),

        boost(KeyCode.shiftLeft),
        control(KeyCode.controlLeft),
        respawn(KeyCode.v),
        select(KeyCode.mouseLeft),
        deselect(KeyCode.mouseRight),
        break_block(KeyCode.mouseRight),

        pickupCargo(KeyCode.leftBracket),
        dropCargo(KeyCode.rightBracket),

        command(KeyCode.g),

        clear_building(KeyCode.q),
        pause_building(KeyCode.e),
        rotate(new Axis(KeyCode.scroll)),
        rotateplaced(KeyCode.r),
        diagonal_placement(KeyCode.controlLeft),
        pick(KeyCode.mouseMiddle),

        schematic_select(KeyCode.f),
        schematic_flip_x(KeyCode.z),
        schematic_flip_y(KeyCode.x),
        schematic_menu(KeyCode.t),

        category_prev(KeyCode.comma, "blocks"),
        category_next(KeyCode.period),

        block_select_left(KeyCode.left),
        block_select_right(KeyCode.right),
        block_select_up(KeyCode.up),
        block_select_down(KeyCode.down),
        block_select_01(KeyCode.num1),
        block_select_02(KeyCode.num2),
        block_select_03(KeyCode.num3),
        block_select_04(KeyCode.num4),
        block_select_05(KeyCode.num5),
        block_select_06(KeyCode.num6),
        block_select_07(KeyCode.num7),
        block_select_08(KeyCode.num8),
        block_select_09(KeyCode.num9),
        block_select_10(KeyCode.num0),

        zoom(new Axis(KeyCode.scroll), "view"),
        menu(Core.app.isAndroid() ? KeyCode.back : KeyCode.escape),
        fullscreen(KeyCode.f11),
        pause(KeyCode.space),
        minimap(KeyCode.m),
        research(KeyCode.b),
        planet_map(KeyCode.n),
        block_info(KeyCode.f1),
        toggle_menus(KeyCode.c),
        screenshot(KeyCode.p),
        toggle_power_lines(KeyCode.f5),
        toggle_block_status(KeyCode.f6),
        toggle_display_items(KeyCode.f7),
        player_list(KeyCode.tab, "multiplayer"),
        chat(KeyCode.enter),
        chat_history_prev(KeyCode.up),
        chat_history_next(KeyCode.down),
        chat_scroll(new Axis(KeyCode.scroll)),
        chat_mode(KeyCode.tab),
        console(KeyCode.f8),
        ;

        private final KeybindValue defaultValue;
        private final String category;

        Binding(KeybindValue defaultValue, String category){
            this.defaultValue = defaultValue;
            this.category = category;
        }

        Binding(KeybindValue defaultValue){
            this(defaultValue, null);
        }

        @Override
        public KeybindValue defaultValue(DeviceType type){
            return defaultValue;
        }

        @Override
        public String category(){
            return category;
        }
    }
}