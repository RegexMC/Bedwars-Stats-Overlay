package me.regexmc.statsoverlay.config;

import lc.kra.system.keyboard.event.GlobalKeyEvent;
import me.regexmc.statsoverlay.utils.Clients;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class ConfigHandler {
    private final File configFile;
    private JSONObject configJson;

    public ConfigHandler(Path launchDirectory) throws IOException {
        configFile = new File(launchDirectory + "StatsOverlayConfig.json");
        if (!configFile.exists()) {
            configFile.createNewFile();
            configJson = new JSONObject();
            configJson.put("key", "");
            configJson.put("enabled", true);
            configJson.put("client", Clients.FORGE);
            configJson.put("background-color", "#696969");
            configJson.put("background-image", "NONE");
            configJson.put("opacity", 100);
            configJson.put("valid-key", false);
            configJson.put("hotkey", GlobalKeyEvent.VK_TAB);
        } else {
            configJson = new JSONObject(IOUtils.toString(new FileReader(configFile)));
        }
        write();
    }

    public boolean getEnabled() {
        return this.configJson.getBoolean("enabled");
    }

    public void setEnabled(boolean enabled) {
        replace("enabled", enabled);
    }

    public boolean getValidKey() {
        return this.configJson.getBoolean("valid-key");
    }

    public void setValidKey(boolean valid) {
        replace("valid-key", valid);
    }

    public String getKey() {
        return this.configJson.getString("key");
    }

    public void setKey(String key) {
        replace("key", key);
    }

    public Clients getClient() {
        return this.configJson.getEnum(Clients.class, "client");
    }

    public void setClient(Clients client) {
        replace("client", client);
    }

    public String getBackgroundColor() {
        return this.configJson.getString("background-color");
    }

    public void setBackgroundColor(String color) {
        replace("background-color", color);
    }

    public int getOpacity() {
        return this.configJson.getInt("opacity");
    }

    public void setOpacity(int opacity) {
        replace("opacity", opacity);
    }

    public int getHotKey() {
        return this.configJson.getInt("hotkey");
    }

    public void setHotKey(int hotKey) {
        replace("hotkey", hotKey);
    }

    public void write() throws IOException {
        FileWriter writer = new FileWriter(configFile);
        writer.write(configJson.toString());
        writer.close();
    }

    private void replace(String key, Object value) {
        JSONObject configJson = this.configJson;
        if (configJson.has(key)) configJson.remove(key);
        configJson.put(key, value);
        this.configJson = configJson;
    }
}
