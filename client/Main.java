package client;

import com.beust.jcommander.JCommander;

import static utils.Util.*;

public class Main {
    public static void main(String[] argv) {
        setLogitPrefix("Client:");
        prlnLogged("Client started!");

        StringBuilder sb = new StringBuilder();
        for (String arg : argv) {
            sb.append(arg + " ");
        }
        logit("args:" + sb.toString());

        ClientArgs args = new ClientArgs();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        JsonDatabaseClient client = new JsonDatabaseClient();
        client.run(args);
    }
}
