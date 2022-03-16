package eu.koboo.minestom.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class UUIDManager {

    private UUID validateUUID(String string) {
        if (string == null || string.length() != 32) {
            return null;
        }
        if (string.contains("-")) {
            return UUID.fromString(string);
        }
        String firstSeg = string.substring(0, 8); // 8
        String secondSeg = string.substring(8, 12); // 4
        String thirthSeg = string.substring(12, 16); // 4
        String fourthSeg = string.substring(16, 20); // 4
        String fifthSeg = string.substring(20, 32); // 12
        return UUID.fromString(firstSeg + "-" + secondSeg + "-" + thirthSeg + "-" + fourthSeg + "-" + fifthSeg);
    }

    public String[] getRemoteUserData(String nameOrUuid) {
        try {
            String result = query("https://api.minetools.eu/uuid/" + nameOrUuid);
            if (result.equalsIgnoreCase("")) {
                return null;
            }
            JsonElement element = new JsonParser().parse(result);
            if (!element.isJsonObject()) {
                return null;
            }
            JsonObject object = element.getAsJsonObject();
            if (!object.has("name") || !object.has("id")) {
                return null;
            }
            String uuidString = object.get("id").getAsString();
            if(uuidString == null) {
                return null;
            }
            UUID uuid = validateUUID(uuidString);
            if (uuid == null) {
                return null;
            }
            String name = object.get("name").getAsString();
            if(name == null) {
                return null;
            }
            return new String[]{name, uuid.toString()};
        } catch (Exception e) {
            if (!(e instanceof FileNotFoundException) && !(e instanceof SocketException)) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String query(String url) throws IOException {
        StringBuilder response = new StringBuilder();
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        connection.setConnectTimeout(4500);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("User-Agent", "MinestomImpl");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }
        connection.getInputStream().close();
        connection.getOutputStream().close();
        return response.toString();
    }
}
