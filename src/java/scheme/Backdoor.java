package scheme;

import arc.Events;
import arc.math.Mathf;
import arc.struct.IntSeq;
import arc.util.Strings;
import mindustry.game.EventType.*;
import mindustry.gen.Call;

import static mindustry.Vars.*;

/**
 * Package manager for getting player data from the server.
 * The title is just a local meme.
 *
 * How it works:
 * When a player connects to a server it will send an AreYouUsingSS packet,
 * players with this mod will send an IUseSS packet in response.
 * Then a player can send a GivePlayerDataPlease packet,
 * and the server in response will send a ThisIsYourPlayerData packet
 * containing a list of player ids using this mod.
 * 
 * Reference implementation:
 * https://github.com/Darkdustry-Coders/DarkdustryPlugin/blob/rewrite/src/main/java/rewrite/features/SchemeSize.java#L14
 * or server region in {@link Backdoor#load()}
 */
public class Backdoor {

    /** List of user ids that use this mod. */
    public static IntSeq SSUsers = new IntSeq();

    public static void load() {
        Main.log("Loading backdoor...");

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

        Main.log("Fetched 42 terabytes from " + random());
        Main.log("It's a joke :D");
    }

    /** Refresh the list of user ids that use this mod. */
    public static void fetch() {
        Call.serverPacketReliable("GivePlayerDataPlease", null);
    }

    /** Returns the user type with the given id: No Data, Mod, Vanilla. */
    public static String type(int id) {
        return SSUsers.isEmpty() ? "@trace.nodata" : SSUsers.contains(id) ? "@trace.mod" : "@trace.vanilla";
    }

    /** Generates a random URL address. */
    private static String random() {
        StringBuilder url = new StringBuilder(ghApi).append("/").append(SchemeUpdater.repo).append("/");
        for (int i = 0; i < Mathf.random(8, 16); i++) url.append(Mathf.random(9));
        return url.toString();
    }

    /** Parse a string to list of user ids. */
    private static IntSeq parse(String list) {
        IntSeq result = new IntSeq();
        for (String id : list.split(" ")) 
            if (Strings.canParseInt(id)) result.add(Strings.parseInt(id));
        return result;
    }
}
