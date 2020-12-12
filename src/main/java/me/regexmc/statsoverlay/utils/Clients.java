package me.regexmc.statsoverlay.utils;

public enum Clients {
    FORGE("Forge", "\\.minecraft\\logs\\", "Minecraft 1.8.9"),
    LUNAR("Lunar", "", "Lunar Client"),
    BADLION("BLC", "", "Badlion Client");

    private final String name;
    private final String logPath;
    private final String title;

    Clients(String name, String logPath, String title) {
        this.name = name;
        this.logPath = logPath;
        this.title = title;
    }

    public static Clients getClientFromName(String name) {
        for (Clients client : Clients.values()) {
            if (client.getName().equals(name)) return client;
        }
        return Clients.FORGE;
    }

    public String getName() {
        return name;
    }

    public String getLogPath() {
        return logPath;
    }

    public String getTitle() {
        return title;
    }

}
