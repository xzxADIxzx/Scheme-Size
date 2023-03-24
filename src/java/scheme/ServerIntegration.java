package scheme;

import arc.Events;
import arc.struct.IntMap;
import arc.util.Reflect;
import arc.util.Strings;
import mindustry.game.EventType.*;
import mindustry.gen.Call;
import mindustry.io.JsonIO;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

/**
 * Package manager for getting player data from the server.
 * <p>
 * How it works:
 * When a player connects to a server it will send a SendMeSubtitle packet,
 * players with this mod will send a MySubtitle packet in response.
 * Then the server will send a Subtitles packet to all connections
 * containing a list of ids and subtitles of players who use this mod.
 * </p>
 * Reference implementation: server region in {@link ServerIntegration#load()}
 */
@SuppressWarnings("unchecked")
public class ServerIntegration {

    /** List of user ids that use this mod. */
    public static IntMap<String> SSUsers = new IntMap<>(8);

    /** Host's player id. If you're joining a headless server it will be -1. */
    public static int hostID = -1;

    /** Whether the player received subtitles from the server. */
    public static boolean hasData;

    public static void load() {
        // region Server

        Events.on(PlayerJoin.class, event -> Call.clientPacketReliable(event.player.con, "SendMeSubtitle", player == null ? null : String.valueOf(player.id)));
        Events.on(PlayerLeave.class, event -> SSUsers.remove(event.player.id));

        netServer.addPacketHandler("MySubtitle", (target, args) -> {
            SSUsers.put(target.id, args);
            Call.clientPacketReliable("Subtitles", JsonIO.write(SSUsers));
        });

        // endregion
        // region Client

        Events.run(HostEvent.class, ServerIntegration::clear);
        Events.run(ClientPreConnectEvent.class, ServerIntegration::clear);

        netClient.addPacketHandler("SendMeSubtitle", args -> {
            if (antiModIPs.contains(Reflect.<String>get(ui.join, "lastIp"))) return;

            Call.serverPacketReliable("MySubtitle", settings.getString("subtitle"));
            if (args != null) hostID = Strings.parseInt(args, -1);
        });

        netClient.addPacketHandler("Subtitles", args -> {
            SSUsers = JsonIO.read(IntMap.class, args);
            hasData = true;
        });

        // endregion
    }

    /** Clears all data about users. */
    public static void clear() {
        SSUsers.clear();
        hostID = -1;
        hasData = false;

        // put the host's subtitle so that you do not copy the int map later
        SSUsers.put(player.id, settings.getString("subtitle"));
    }

    /** Returns whether the user with the given id is using a mod. */
    public static boolean isModded(int id) {
        return SSUsers.containsKey(id) || player.id == id; // of course you are a modded player
    }

    /** Returns the user type with the given id: host, no data, mod or vanilla. */
    public static String type(int id) {
        if (hostID == id) return "trace.type.host";
        return !hasData && net.client() ? "trace.type.nodata" : isModded(id) ? "trace.type.mod" : "trace.type.vanilla";
    }

    /** Returns the user type with subtitle. */
    public static String tooltip(int id) {
        if (player.id == id) return "@trace.type.self";
        return bundle.get(type(id)) + (isModded(id) ? "\n" + SSUsers.get(id) : "");
    }
}
