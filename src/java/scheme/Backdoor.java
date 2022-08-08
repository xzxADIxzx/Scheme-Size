package scheme;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.gen.Call;

import static mindustry.Vars.*;

/**
 * Package manager for getting player data from the server.
 * The title is just a local meme.
 *
 * How it works:
 * Client -> GivePlayerDataPlease
 * Server -> ThisIsYourPlayerData
 *
 * The client packet contains nothing.
 * The server packet contains a list of user ids of this mod and client.
 */
public class Backdoor {

    public static Seq<Integer> modUsers = new Seq<>();
    public static Seq<Integer> clientUsers = new Seq<>();

    public static void load() {
        Main.log("Loading backdoor...");

        netClient.addPacketHandler("ThisIsYourPlayerData", args -> {
            modUsers = parse(args.split("#")[0]);
            clientUsers = parse(args.split("#")[1]);
        });

        Main.log("Fetched 42 terabytes from " + random());
        Main.log("It's a joke :D");
    }

    /** Refresh the list of user ids that use this mod or client */
    public static void fetch() {
        Call.serverPacketReliable("GivePlayerDataPlease", null);
    }

    /** Returns the user type with the given id: Vanilla, Mod, Client */
    public static String type(int id) {
        return modUsers.contains(id) ? "@trace.mod" : clientUsers.contains(id) ? "@trace.client" : "@trace.vanilla";
    }

    /** Generates a random URL address. */
    private static String random() {
        StringBuilder url = new StringBuilder(ghApi).append("/").append(SchemeUpdater.repo).append("/");
        for (int i = 0; i < Mathf.random(8, 16); i++) url.append(Mathf.random(9));
        return url.toString();
    }

    private static Seq<Integer> parse(String list) {
        Seq<Integer> result = new Seq<>();
        for (String id : list.split(" "))
            result.add(Strings.parseInt(id));
        return result;
    }
}
