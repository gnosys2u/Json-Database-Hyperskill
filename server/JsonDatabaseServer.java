package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static utils.Util.*;

public class JsonDatabaseServer {
    private final static String SERVER_ADDRESS = "127.0.0.1";
    private final static int SERVER_PORT = 23456;

    private final static int SERVER_ACCEPT_TIMEOUT_MILLIS = 50;

    private static boolean exitNow;
    private JsonDatabase database;

    public static void commandExit() {
        exitNow = true;
    }

    public JsonDatabaseServer() {
        exitNow = false;
        database = new JsonDatabase();
    }

    public void run() {

        try (ServerSocket server = new ServerSocket(SERVER_PORT, 50, InetAddress.getByName(SERVER_ADDRESS))) {

            exitNow = false;
            server.setSoTimeout(SERVER_ACCEPT_TIMEOUT_MILLIS);
            while (!exitNow) {
                try {
                    Socket sessionSocket = server.accept();
                    Session session = new Session(sessionSocket, database);
                    session.start();
                } catch (SocketTimeoutException ex) {
                    // this is expected, it allows exitNow to work
                } catch (Exception ex) {
                    logit("accept:" + ex.getMessage());
                }
            }
            database.writeDatabase();
            logit("exiting");

        } catch (Exception ex) {
            logit("run:" + ex.getMessage());
        }

    }

    class Session extends Thread {
        private final Socket socket;
        private final JsonDatabase database;

        public Session(Socket socketForClient, JsonDatabase database) {
            this.socket = socketForClient;
            this.database = database;
        }

        public void run() {
            try (DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output  = new DataOutputStream(socket.getOutputStream())) {

                String message = input.readUTF();
                prlnLogged("Received " + message);
                JsonObject messageOut = processCommand(message);
                output.writeUTF(messageOut.toString());
                prlnLogged("Sent: " + messageOut.toString());
            } catch (Exception ex) {
                logit("Session:run:" + ex.getMessage());
            }
        }

        private void processGet(JsonObject command, JsonObject result) throws Exception {
            JsonElement key = command.get("key");
            if (key != null) {
                if (key.isJsonPrimitive()) {
                    String keyString =  key.getAsString();
                    if (database.containsKey(keyString)) {
                        result.add("value", database.get(keyString));
                        result.addProperty("response", "OK");
                    }
                } else if (key.isJsonArray()) {
                    result.add("value", database.getPath(key.getAsJsonArray()));
                    result.addProperty("response", "OK");
                } else {
                    throw new Exception("processGet: invalid key");
                }
            }

        }

        private void processSet(JsonObject command, JsonObject result) throws Exception {
            JsonElement key = command.get("key");
            JsonElement value = command.get("value");
            if (key == null) {
                throw new Exception("processSet: missing key");
            } else if (value == null) {
                throw new Exception("processSet: missing value");
            }

            if (key.isJsonPrimitive()) {
                String keyString =  key.getAsString();
                database.put(keyString, value);
                result.addProperty("response", "OK");
            } else if (key.isJsonArray()) {
                database.putPath(key.getAsJsonArray(), value);
                result.addProperty("response", "OK");
            } else {
                throw new Exception("processGet: invalid key");
            }
        }

        private void processDelete(JsonObject command, JsonObject result) throws Exception {
            JsonElement key = command.get("key");
            if (key == null) {
                throw new Exception("processSet: missing key");
            }

            if (key.isJsonPrimitive()) {
                String keyString =  key.getAsString();
                database.remove(keyString);
                result.addProperty("response", "OK");
            } else if (key.isJsonArray()) {
                database.removePath(key.getAsJsonArray());
                result.addProperty("response", "OK");
            } else {
                throw new Exception("processGet: invalid key");
            }
        }

        private JsonObject processCommand(String message) {

            JsonObject command = new Gson().fromJson(message, JsonObject.class);
            JsonObject result = new JsonObject();

            result.addProperty("response", "ERROR");

            String commandType = command.getAsJsonPrimitive("type").getAsString();

            if ("exit".equals(commandType)) {
                commandExit();
                result.addProperty("response", "OK");
            } else {
                if (command.has("key")) {
                    try {
                        if ("get".equals(commandType)) {
                            processGet(command, result);
                        } else if ("set".equals(commandType)) {
                            processSet(command, result);
                        } else if ("delete".equals(commandType)) {
                            processDelete(command, result);
                        }
                    } catch (Exception ex) {
                        logit("processCommand:" + ex.getMessage());
                    }
                } else {
                    result.addProperty("reason", "Key missing");
                }

            }

            /*
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder
                    .serializeNulls()                           // show fields which are null
                    .create();
             */
            return result;
        }

    }
}
