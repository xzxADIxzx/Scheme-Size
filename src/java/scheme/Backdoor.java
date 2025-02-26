package scheme;

import arc.math.Mathf;

import static mindustry.Vars.*;

/** Just local meme. */
public class Backdoor {

    public static void load() {
        Main.log("Loading backdoor...");
        Main.log("Fetched 42 terabytes from " + random());
        Main.log("It's a joke :D");
    }

    /** Generates a random URL address. */
    private static String random() {
        StringBuilder url = new StringBuilder(ghApi).append("/");
        for (int i = 0; i < Mathf.random(8, 16); i++) url.append(Mathf.random(9));
        return url.toString();
    }
}
