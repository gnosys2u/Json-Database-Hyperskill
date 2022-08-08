package server;

import com.google.gson.*;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static utils.Util.*;

public class JsonDatabase {

    private final static String DATABASE_FILENAME = "src/server/data/db.json";

    //private Map<String, String> database;
    private JsonObject database;
    private Lock readLock;
    private Lock writeLock;

    public JsonDatabase() {
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();

        readDatabase();
    }

    private void readDatabase() {
        writeLock.lock();
        try {
            String db = readFileAsString(DATABASE_FILENAME);
            database = new Gson().fromJson(db, JsonObject.class);
        } catch (Exception ex) {
            logit("readDatabase:" + ex.getMessage());
        }
        writeLock.unlock();
    }

    public boolean containsKey(String key) {
        readLock.lock();
        boolean result = database.has(key);
        readLock.unlock();
        return result;
    }

    public JsonElement get(String key) {
        readLock.lock();
        JsonElement result = database.get(key);
        readLock.unlock();
        return result;
    }

    public JsonElement getPath(JsonArray path) {
        JsonElement result = null;
        readLock.lock();
        JsonObject targetLevel = database;
        int pathLength = path.size();
        String leafName = null;
        for (int i = 0; i < pathLength - 1; i++) {
            JsonElement pathElement = path.get(i);
            leafName = pathElement.getAsString();
            if (targetLevel.has(leafName)) {
                targetLevel = targetLevel.getAsJsonObject(leafName);
            } else {
                targetLevel = null;
                logit("getPath cant find " + leafName);
                break;
            }
        }

        if (targetLevel != null) {
            leafName = path.get(pathLength - 1).getAsString();
            result = targetLevel.get(leafName);
        }
        readLock.unlock();
        return result;
    }

    public void put(String key, JsonElement value) {
        writeLock.lock();
        database.add(key, value);
        writeDatabase();
        writeLock.unlock();
    }

    public void putPath(JsonArray path, JsonElement value) {
        writeLock.lock();
        JsonObject targetLevel = database;
        int pathLength = path.size();
        String leafName = null;
        for (int i = 0; i < pathLength - 1; i++) {
            JsonElement pathElement = path.get(i);
            leafName = pathElement.getAsString();
            if (targetLevel.has(leafName)) {
                targetLevel = targetLevel.getAsJsonObject(leafName);
            } else {
                logit("adding object " + leafName);
                JsonObject newLeaf = new JsonObject();
                targetLevel.add(leafName, newLeaf);
                targetLevel = newLeaf;
            }
        }
        leafName = path.get(pathLength - 1).getAsString();
        targetLevel.add(leafName, value);
        writeDatabase();
        writeLock.unlock();
    }

    public boolean remove(String key) {
        boolean result = false;
        writeLock.lock();
        if (database.has(key)) {
            database.remove(key);
            writeDatabase();
            result = true;
        }
        writeLock.unlock();
        return result;
    }

    public boolean removePath(JsonArray path) {
        logit("removePath");
        boolean result = false;
        writeLock.lock();

        JsonObject targetLevel = database;
        int pathLength = path.size();
        String leafName = null;
        for (int i = 0; i < pathLength - 1; i++) {
            JsonElement pathElement = path.get(i);
            leafName = pathElement.getAsString();
            logit("removePath entering " + leafName);
            if (targetLevel.has(leafName)) {
                targetLevel = targetLevel.getAsJsonObject(leafName);
            } else {
                logit("removePath cant find " + leafName);
                targetLevel = null;
                break;
            }
        }

        if (targetLevel != null) {
            leafName = path.get(pathLength - 1).getAsString();
            if (targetLevel.has(leafName)) {
                targetLevel.remove(leafName);
                writeDatabase();
                result = true;
            } else {
                logit("removePath - targetLevel has no " + leafName);
            }
        }

        writeLock.unlock();
        logit("removePath done");
        return result;
    }

    public void writeDatabase() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder
                .serializeNulls()                           // show fields which are null
                .create();
        String dbAsString = gson.toJson(database);
        try (FileWriter fileWriter = new FileWriter(DATABASE_FILENAME)) {
            fileWriter.write(dbAsString);
            logit("writeDatabase:" + dbAsString);
        } catch (Exception ex) {
            logit("writeDatabase:" + ex.getMessage());
        }

    }
}
