package scheme;

import arc.Events;
import arc.struct.IntSeq;
import arc.util.Strings;
import mindustry.game.EventType.*;
import mindustry.gen.Call;

import static mindustry.Vars.*;

/**
 * Package manager for getting player data from the server.
 * <p>
 * How it works:
 * When a player connects to a server it will send an AreYouUsingSS packet,
 * players with this mod will send an IUseSS packet in response.
 * Then a player can send a GivePlayerDataPlease packet,
 * and the server in response will send a ThisIsYourPlayerData packet
 * containing a list of player ids using this mod.
 * </p>
 * Reference implementation:
 * https://github.com/Darkdustry-Coders/DarkdustryPlugin/blob/rewrite/src/main/java/darkdustry/features/SchemeSize.java
 * or server region in {@link ServerIntegration#load()}
 */
public class ServerIntegration {

    /** List of user ids that use this mod. */
    public static IntSeq SSUsers = new IntSeq();

    public static void load() {
        // region Server

        Events.on(PlayerJoin.class, event -> Call.clientPacketReliable(event.player.con, "AreYouUsingSS", null));
        Events.on(PlayerLeave.class, event -> SSUsers.removeValue(event.player.id));

        netServer.addPacketHandler("IUseSS", (player, args) -> SSUsers.add(player.id));
        netServer.addPacketHandler("GivePlayerDataPlease", (player, args) -> {
            Call.clientPacketReliable(player.con, "ThisIsYourPlayerData", SSUsers.toString(" "));
        });

        // endregion
        // region Client

        netClient.addPacketHandler("AreYouUsingSS", args -> Call.serverPacketReliable("IUseSS", null));
        netClient.addPacketHandler("ThisIsYourPlayerData", args -> SSUsers = parse(args));

        // endregion
    }

    /** Refresh the list of user ids that use this mod. */
    public static void fetch() {
        Call.serverPacketReliable("GivePlayerDataPlease", null);
    }

    /** Returns the user type with the given id: No Data, Mod, Vanilla. */
    public static String type(int id) {
        return SSUsers.isEmpty() ? "@trace.nodata" : SSUsers.contains(id) ? "@trace.mod" : "@trace.vanilla";
    }

    /** Parse a string to list of user ids. */
    private static IntSeq parse(String list) {
        IntSeq result = new IntSeq();
        for (String id : list.split(" "))
            if (Strings.canParseInt(id)) result.add(Strings.parseInt(id));
        return result;
    }
}
