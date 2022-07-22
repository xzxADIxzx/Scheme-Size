package scheme;

import mindustry.gen.Call;

import static mindustry.Vars.*;

import arc.math.Mathf;

/** Just local meme. */
public class Backdoor {

    public static void load() {
        Main.log("Loading backdoor...");

        // Call.serverPacketReliable("WhoIsUsingSS", null);
        netClient.addPacketHandler("AreYouUsingSS", (callback) -> {
            Call.sendChatMessage("I am using Scheme Size btw");
        });

        Main.log("Fetched 42 terabytes from " + random());
        Main.log("It's a joke :D");
    }

    /** Generates a random URL address. */
    public static String random() {
        StringBuilder url = new StringBuilder(ghApi).append("/").append(SchemeUpdater.repo).append("/");
        for (int i = 0; i < Mathf.random(8, 16); i++) url.append(Mathf.random(9));
        return url.toString();
    }
}
