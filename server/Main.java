package server;

import static utils.Util.*;

public class Main {

    public static void main(String[] args)
    {
        setLogitPrefix("Server:");
        logit("##############################################");
        prlnLogged("Server started!");
        JsonDatabaseServer server = new JsonDatabaseServer();
        server.run();
    }


}
