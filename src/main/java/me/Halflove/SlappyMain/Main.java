package me.Halflove.SlappyMain;

import me.Halflove.Slappy.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Main extends JavaPlugin implements Listener {
    public SettingsManager settings = SettingsManager.getInstance();

    public static Plugin plugin;

    public void onEnable() {
        plugin = (Plugin)this;
        Bukkit.getPluginManager().registerEvents(this, (Plugin)this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, new Runnable() {
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (!player.hasPermission("mod"))
                        Main.refreshQueue(player);
                }
            }
        },  1L, 100L);
        getLogger().info("Version " + getDescription().getVersion() + " has been enabled.");
        this.settings.setup((Plugin)this);
        registerEvents();
        registerCommands();
        (new BukkitRunnable() {
            public void run() {
                for (String map : SettingsManager.getConfig().getStringList("maps")) {
                    MatchmakingManager.lobbies.put(map, Integer.valueOf(0));
                    List<String> unavailable = SettingsManager.getConfig().getStringList("unavailable");
                    unavailable.clear();
                    SettingsManager.getConfig().set("unavailable", unavailable);
                    SettingsManager.saveConfig();
                }
            }
        }).runTaskLater(plugin, 2L);
    }

    public static void refreshQueue(final Player player) {
        if (MatchmakingManager.isQueued(player)) {
            if (!MatchmakingManager.isLobby(player) &&
                    !MatchmakingManager.isGame(player))
                if (!MatchmakingManager.maps.isEmpty() || MatchmakingManager.unavailable.size() == MatchmakingManager.maps.size()) {
                    player.sendMessage(ChatColor.GRAY + "Refreshing queue...");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
                    (new BukkitRunnable() {
                        public void run() {
                            MatchmakingManager.leaveQueue(player, false);
                            MatchmakingManager.joinQueue(player, false);
                        }
                    }).runTaskLater(plugin, 1L);
                } else if (player != null) {
                    MatchmakingManager.joinQueue(player, false);
                }
        } else if (!MatchmakingManager.isLobby(player) &&
                !MatchmakingManager.isGame(player) &&
                !MatchmakingManager.postgame.containsKey(player.getName())) {
            MatchmakingManager.joinQueue(player, false);
        }
    }

    public void onDisable() {
        getLogger().info("Version " + getDescription().getVersion() + " has been disabled.");
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (MatchmakingManager.inlobby.containsKey(player.getName()) || MatchmakingManager.ingame.containsKey(player.getName()))
                MatchmakingManager.forceQuit(player);
        }
        MatchmakingManager.countdown.clear();
        MatchmakingManager.postgame.clear();
        MatchmakingManager.ingame.clear();
        MatchmakingManager.inlobby.clear();
        MatchmakingManager.playerslot.clear();
        MatchmakingManager.queue.clear();
        List<String> unavailable = SettingsManager.getConfig().getStringList("unavailable");
        unavailable.clear();
        SettingsManager.getConfig().set("unavailable", unavailable);
        SettingsManager.saveConfig();
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents((Listener)new JoinListener(), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new MoveListener(), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new OtherListeners(), (Plugin)this);
        //Bukkit.getPluginManager().registerEvents((Listener)new ClassSelector(), (Plugin)this);
    }

    public void registerCommands() {
        getCommand("slappy").setExecutor((CommandExecutor)new SlappyCommands());
    }
}