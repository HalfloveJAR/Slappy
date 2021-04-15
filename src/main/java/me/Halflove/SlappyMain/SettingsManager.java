package me.Halflove.SlappyMain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsManager {
    static SettingsManager instance = new SettingsManager();

    static Plugin p;

    static FileConfiguration config;
    static FileConfiguration data;
    static File cfile;
    static File dfile;

    public static SettingsManager getInstance() {
        return instance;
    }

    public void setup(Plugin p) {
        if (!p.getDataFolder().exists())
            p.getDataFolder().mkdir();
        cfile = new File(p.getDataFolder(), "config.yml");
        if (!cfile.exists())
            try {
                cfile.createNewFile();
            } catch (IOException e) {
                Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create config.yml!");
            }
        config = p.getConfig();
        config.options().copyDefaults(true);
        ArrayList maps = new ArrayList();
        ArrayList unavailable = new ArrayList();
        config.addDefault("unavailable", unavailable);
        config.addDefault("maps", maps);
        saveConfig();
        dfile = new File(p.getDataFolder(), "data.yml");
        if (!dfile.exists())
            try {
                dfile.createNewFile();
            } catch (IOException e) {
                Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create data.yml!");
            }
        data = (FileConfiguration) YamlConfiguration.loadConfiguration(dfile);
    }

    public static String formatTime(long secs) {
        String output;
        long seconds = secs;
        long minutes = 0L;
        while (seconds >= 60L) {
            seconds -= 60L;
            minutes++;
        }
        long hours = 0L;
        while (minutes >= 60L) {
            minutes -= 60L;
            hours++;
        }
        if (hours != 0L) {
            output = String.valueOf(hours) + " Hours " + minutes + " Minutes " + seconds + " Seconds";
        } else if (minutes != 0L) {
            if (seconds == 0L) {
                if (minutes == 1L) {
                    output = String.valueOf(minutes) + " Minute";
                } else {
                    output = String.valueOf(minutes) + " Minutes";
                }
            } else if (minutes == 1L) {
                if (seconds == 1L) {
                    output = String.valueOf(minutes) + " Minute " + seconds + " Second";
                } else {
                    output = String.valueOf(minutes) + " Minute " + seconds + " Seconds";
                }
            } else if (seconds == 1L) {
                output = String.valueOf(minutes) + " Minutes " + seconds + " Second";
            } else {
                output = String.valueOf(minutes) + " Minutes " + seconds + " Seconds";
            }
        } else if (seconds == 1L) {
            output = String.valueOf(seconds) + " Second";
        } else {
            output = String.valueOf(seconds) + " Seconds";
        }
        return output;
    }

    public static String getIp(Player player, boolean raw) {
        String output;
        if (raw) {
            output = player.getAddress().toString().substring(1).replace(".", "-");
        } else {
            output = player.getAddress().toString().substring(1).replace(".", "-").split(":")[0];
        }
        return output;
    }

    public static String getOfflineIp(OfflinePlayer player, boolean raw) {
        String output;
        if (raw) {
            output = ((Player)player).getAddress().toString().substring(1).replace(".", "-");
        } else {
            output = ((Player)player).getAddress().toString().substring(1).replace(".", "-").split(":")[0];
        }
        return output;
    }

    public static String getUUID(Player player) {
        String output = "";
        if (player.hasPlayedBefore())
            output = player.getUniqueId().toString();
        return output;
    }

    public static String getOfflineUUID(OfflinePlayer target) {
        String output = "";
        if (target.hasPlayedBefore())
            output = target.getUniqueId().toString();
        return output;
    }

    public static boolean isInt(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isMaterial(String input) {
        boolean output = false;
        if (Material.getMaterial(input) != null)
            output = true;
        return output;
    }

    public static FileConfiguration getConfig() {
        return config;
    }

    public static void saveConfig() {
        try {
            config.save(cfile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save config.yml!");
        }
    }

    public static void reloadConfig() {
        config = p.getConfig();
    }

    public static FileConfiguration getData() {
        return data;
    }

    public static void saveData() {
        try {
            data.save(dfile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save data.yml!");
        }
    }

    public static void reloadData() {
        data = (FileConfiguration)YamlConfiguration.loadConfiguration(dfile);
    }

    public static PluginDescriptionFile getDesc() {
        return p.getDescription();
    }
}
