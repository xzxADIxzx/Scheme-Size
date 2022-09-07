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

    /** Host's player id. If you're joining a headless server it will be -1. */
    public static int hostID = -1;

    public static void load() {
        // region Server

        Events.on(PlayerJoin.class, event -> Call.clientPacketReliable(event.player.con, "GiveYourPlayerData", null));
        Events.on(PlayerLeave.class, event -> SSUsers.remove(event.player.id));

        netServer.addPacketHandler("ThisIsMyPlayerData", (target, args) -> SSUsers.put(target.id, args.replace("|", "").replace("=", "")));
        netServer.addPacketHandler("GivePlayerDataPlease", (target, args) -> {
            var map = SSUsers.copy(); // no way ,_,
            if (player != null) map.put(player.id, settings.getString("subtitle"));
            Call.clientPacketReliable(target.con, "ThisIsYourPlayerData", (headless ? "S" : "C") + map.toString("|"));
        });

        // endregion
        // region Client

        Events.run(WorldLoadEvent.class, ServerIntegration::clear);

        netClient.addPacketHandler("GiveYourPlayerData", args -> Call.serverPacketReliable("ThisIsMyPlayerData", settings.getString("subtitle")));
        netClient.addPacketHandler("ThisIsYourPlayerData", args -> {
            clear();
            for (String data : args.substring(1).split("\\|")) {
                String[] idsub = data.split("="); // id and subtitle
                if (idsub.length < 2) continue; // server may send invalid data, but there is no sense to throw an exception, as this will close connection
                if (Strings.canParseInt(idsub[0])) SSUsers.put(Strings.parseInt(idsub[0]), idsub[1]);
            }

            if (args.startsWith("S")) return; // headless server, nobody is a host
            var keys = SSUsers.keys();
            if (keys.hasNext()) hostID = keys.next(); // last player is a host
        });

        // endregion
    }

    /** Refresh the list of user ids that use this mod. */
    public static void fetch() {
        Call.serverPacketReliable("GivePlayerDataPlease", null);
    }

    /** Clears all data about users. */
    public static void clear() {
        SSUsers.clear();
        hostID = -1;
    }

    /** Returns whether the user with the given id is using a mod. */
    public static boolean isModded(int id) {
        return SSUsers.containsKey(id) || player.id == id; // of course you are a modded player
    }

    /** Returns the user type with the given id: No Data, Mod, Vanilla. */
    public static String type(int id) {
        if (hostID == id) return "trace.type.host";
        return SSUsers.isEmpty() ? "trace.type.nodata" : isModded(id) ? "trace.type.mod" : "trace.type.vanilla";
    }

    /** Returns the user type with subtitle. */
    public static String tooltip(int id) {
        if (player.id == id) return "@trace.type.self";
        return bundle.get(type(id)) + (isModded(id) ? "\n" + SSUsers.get(id) : "");
    }
}
