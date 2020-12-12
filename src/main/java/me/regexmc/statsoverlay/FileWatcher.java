package me.regexmc.statsoverlay;

import me.regexmc.statsoverlay.utils.CellRenderer;
import me.regexmc.statsoverlay.utils.Multithreading;
import me.regexmc.statsoverlay.utils.NumberUtils;
import me.regexmc.statsoverlay.utils.hypixel.ExpCalculator;
import me.regexmc.statsoverlay.utils.hypixel.ILeveling;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileWatcher extends Thread {
    public void run() {
        final Path path = Paths.get(Main.roamingPath + Main.configHandler.getClient().getLogPath());
        try {
            String logFile = path + "\\latest.log";
            String oldContent = IOUtils.toString(new FileReader(logFile));

            while (true) {
                Thread.sleep(250);

                if (!Main.configHandler.getEnabled()) return;

                String newContent = IOUtils.toString(new FileReader(logFile));

                if (!oldContent.equals(newContent)) {
                    String changes = newContent.substring(oldContent.length()).trim();
                    List<String> lineChanges = Arrays.asList(changes.split("\n"));

                    lineChanges.forEach(s -> {
                        //String regex = "ONLINE: (((\\w{2,16}(,|$) ?){8}$)|((\\w{2,16}(,|$) ?){12}$)|((\\w{2,16}(,|$) ?){16}$))";
                        //Pattern pattern = Pattern.compile(regex);
                        Pattern pattern = Main.patternHashMap.get("WHO");
                        Matcher matcher = pattern.matcher(s);
                        if (matcher.find()) {
                            if (!Main.configHandler.getValidKey()) {
                                return;
                            }

                            try {
                                String apiKeyInfoURL = "https://api.hypixel.net/key?key=" + Main.configHandler.getKey();
                                JSONObject apiKeyInfoJSON = Main.readJsonFromUrl(apiKeyInfoURL);
                                int queriesLastMinute = apiKeyInfoJSON.getJSONObject("record").getInt("queriesInPastMin");
                                int limit = apiKeyInfoJSON.getJSONObject("record").getInt("limit");
                                if (limit - queriesLastMinute < 20) {
                                    JOptionPane.showMessageDialog(null, (queriesLastMinute + 1) + "Queries in last minute, will not get stats.");
                                    return;
                                }
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }

                            String whoMessage = matcher.group(1);

                            Main.usernames = Arrays.asList(whoMessage.replaceAll(" ", "").split(","));
                            AtomicInteger finishedCount = new AtomicInteger();
                            int totalCount = Main.usernames.size();

                            Main.players.clear();
                            DefaultTableModel model = (DefaultTableModel) Main.table_Players.getModel();
                            model.setRowCount(0);

                            //most of the delay is before here
                            for (int i = 0; i < Main.usernames.size(); i++) {
                                String username = Main.usernames.get(i);
                                Multithreading.runAsync(() -> {
                                    String key = Main.configHandler.getKey();
                                    try {
                                        String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
                                        JSONObject mojangAPIJson = Main.readJsonFromUrl(url);
                                        String UUID = mojangAPIJson.getString("id");

                                        url = "https://api.hypixel.net/player?key=" + key + "&uuid=" + UUID;
                                        JSONObject playerJSON = Main.readJsonFromUrl(url);
                                        JSONObject bedwarsJSON = playerJSON.getJSONObject("player").getJSONObject("stats").getJSONObject("Bedwars");

                                        double hypixelLevel = NumberUtils.round(ILeveling.getLevel(playerJSON.getJSONObject("player").optDouble("networkExp")), 3);
                                        int level = ExpCalculator.getLevelForExp(bedwarsJSON.optInt("Experience"));
                                        int winstreak = bedwarsJSON.optInt("winstreak");
                                        int wins = bedwarsJSON.optInt("wins_bedwars");
                                        int losses = bedwarsJSON.optInt("losses_bedwars");
                                        double WL = losses == 0 ? wins : NumberUtils.round((double) wins / losses, 3);
                                        int kills = bedwarsJSON.optInt("kills_bedwars");
                                        int deaths = bedwarsJSON.optInt("deaths_bedwars");
                                        double KD = deaths == 0 ? kills : NumberUtils.round((double) kills / deaths, 3);
                                        int finalKills = bedwarsJSON.optInt("final_kills_bedwars");
                                        int finalDeaths = bedwarsJSON.optInt("final_deaths_bedwars");
                                        double FKD = finalDeaths == 0 ? finalKills : NumberUtils.round((double) finalKills / finalDeaths, 3);

                                        Main.players.put(level, new String[]{"(" + hypixelLevel + ") {" + winstreak + "} " + username, String.valueOf(level), String.valueOf(wins), String.valueOf(losses), String.valueOf(WL), String.valueOf(kills), String.valueOf(deaths), String.valueOf(KD), String.valueOf(finalKills), String.valueOf(finalDeaths), String.valueOf(FKD)});
                                        finishedCount.getAndIncrement();

                                        if (finishedCount.get() == totalCount) {
                                            finishedCount.incrementAndGet();

                                            Main.players.forEach((key1, value) -> model.addRow(value));
                                            Main.updateRowHeights();
                                        }
                                    } catch (Exception e) {
                                        finishedCount.getAndIncrement();
                                        model.addRow(new String[]{username, "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"});
                                        if (finishedCount.get() == totalCount) {
                                            finishedCount.incrementAndGet();

                                            Main.players.forEach((key1, value) -> model.addRow(value));
                                            Main.updateRowHeights();
                                        }
                                    }
                                });
                            }

                            Enumeration<TableColumn> tableColumnEnumeration = Main.table_Players.getColumnModel().getColumns();
                            while (tableColumnEnumeration.hasMoreElements()) {
                                TableColumn column = tableColumnEnumeration.nextElement();
                                column.setCellRenderer(new CellRenderer());
                            }
                        }
                    });
                }

                oldContent = newContent;
            }
        } catch (IOException | InterruptedException ioException) {
            ioException.printStackTrace();
        }
    }
}
