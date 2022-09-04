package scheme;

import arc.Events;
import arc.struct.ObjectMap;
import arc.util.Strings;
import mindustry.game.EventType.*;
import mindustry.gen.Call;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * Package manager for getting player data from the server.
 * <p>
 * How it works:
 * When a player connects to a server it will send an GiveYourPlayerData packet,
 * players with this mod will send an ThisIsMyPlayerData packet in response.
 * Then a player can send a GivePlayerDataPlease packet,
 * and the server in response will send a ThisIsYourPlayerData packet
 * containing a list of ids and subtitles of players who use this mod.
 * </p>
 * Reference implementation:
 * https://github.com/Darkdustry-Coders/DarkdustryPlugin/blob/rewrite/src/main/java/darkdustry/features/SchemeSize.java
 * or server region in {@link ServerIntegration#load()}
 */
public class ServerIntegration {

    /** List of user ids that use this mod. */
    public static ObjectMap<Integer, String> SSUsers = new ObjectMap<>();

    public static void load() {
        // region Server

        Events.on(PlayerJoin.class, event -> Call.clientPacketReliable(event.player.con, "GiveYourPlayerData", null));
        Events.on(PlayerLeave.class, event -> SSUsers.remove(event.player.id));

        netServer.addPacketHandler("ThisIsMyPlayerData", (player, args) -> SSUsers.put(player.id, args.replace("|", "").replace("=", "")));
        netServer.addPacketHandler("GivePlayerDataPlease", (player, args) -> {
            Call.clientPacketReliable(player.con, "ThisIsYourPlayerData", SSUsers.toString("|"));
        });

        // endregion
        // region Client

        netClient.addPacketHandler("GiveYourPlayerData", args -> Call.serverPacketReliable("ThisIsMyPlayerData", settings.getString("subtitle")));
        netClient.addPacketHandler("ThisIsYourPlayerData", args -> {
            SSUsers.clear();
            for (String data : args.split("\\|")) {
                String id = data.split("=")[0];
                if (Strings.canParseInt(id)) SSUsers.put(Strings.parseInt(id), data.split("=")[1]);
            }
        });

        // endregion
    }

    /** Refresh the list of user ids that use this mod. */
    public static void fetch() {
        Call.serverPacketReliable("GivePlayerDataPlease", null);
    }

    public static boolean isModded(int id) {
        return SSUsers.containsKey(id) || player.id == id; // of course you are a modded player
    }

    /** Returns the user type with the given id: No Data, Mod, Vanilla. */
    public static String type(int id) {
        return SSUsers.isEmpty() ? "trace.type.nodata" : isModded(id) ? "trace.type.mod" : "trace.type.vanilla";
    }

    /** Returns the user type with subtitle. */
    public static String tooltip(int id) {
        if (player.id == id) return "@trace.type.self";
        return bundle.get(type(id)) + (isModded(id) ? "\n" + SSUsers.get(id) : "");
    }
}
