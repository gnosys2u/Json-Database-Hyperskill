package client;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static utils.Util.*;

public class JsonDatabaseClient {
    private final static String SERVER_ADDRESS = "127.0.0.1";
    private final static int SERVER_PORT = 23456;
    private final static String CLIENT_DATA_DIRECTORY = "src/client/data/";

    public JsonDatabaseClient() {
    }

    public void run(ClientArgs args) {

        try (Socket socket = new Socket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            String messageOut = null;

            if (args.commandFile != null) {

                String path = CLIENT_DATA_DIRECTORY + args.commandFile;
                messageOut = readFileAsString(path);
                logit(path + " contents:" + messageOut);

            } else {
                String commandType = args.commandType.toLowerCase();
                String commandKey = null;
                String commandValue = null;

                switch (commandType) {
                    case "set":
                        commandKey = args.commmandKey;
                        commandValue = args.commandValue;
                        break;

                    case "get":
                        commandKey = args.commmandKey;
                        break;

                    case "delete":
                        commandKey = args.commmandKey;
                        break;

                    case "exit":
                        break;

                    default:
                        commandType = null;
                        logit("Unrecognized command: " + args.commandType);
                        break;
                }

                if (commandType != null) {
                    Map<String, String> command = new HashMap<>();
                    command.put("type", commandType);
                    if (commandKey != null) {
                        command.put("key", commandKey);
                    }

                    if (commandValue != null) {
                        command.put("value", commandValue);
                    }

                    messageOut = new Gson().toJson(command);
                }
            }

            if (messageOut != null) {
                output.writeUTF(messageOut);
                prlnLogged("Sent: " + messageOut);

                String message = input.readUTF();
                prlnLogged("Received: " + message);
            } else {
                logit("nothing to send to server!");
            }

        } catch (Exception ex) {
            logit(ex.getMessage());
        }

    }
}
