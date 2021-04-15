package me.Halflove.Slappy;

import me.Halflove.SlappyMain.Main;
import me.Halflove.SlappyMain.SettingsManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MatchmakingManager implements Listener {
    public static Random mapselect = new Random();

    static int taskID = 0;

    public static HashMap<String, Boolean> queue = new HashMap<>();

    public static HashMap<String, Boolean> postgame = new HashMap<>();

    public static HashMap<String, Boolean> pregame = new HashMap<>();

    public static HashMap<String, Boolean> lost = new HashMap<>();

    public static HashMap<String, String> inlobby = new HashMap<>();

    public static HashMap<String, String> ingame = new HashMap<>();

    public static HashMap<String, Integer> lobbies = new HashMap<>();

    public static HashMap<String, Integer> countdown = new HashMap<>();

    public static HashMap<String, Integer> gametimer = new HashMap<>();

    public static HashMap<String, Integer> playerslot = new HashMap<>();

    public static List<String> maps = SettingsManager.getConfig().getStringList("maps");

    public static List<String> unavailable = SettingsManager.getConfig().getStringList("unavailable");

    public static void joinQueue(final Player player, boolean msg) {
        if (!isQueued(player) && !isLobby(player) && !isGame(player)) {
            queue.put(player.getName(), Boolean.valueOf(true));
            player.teleport(getFallback());
            if (msg)
                player.sendMessage(ChatColor.GREEN + "Joined queue");
            (new BukkitRunnable() {
                public void run() {
                    MatchmakingManager.joinLobby(player, true);
                }
            }).runTaskLater(Main.plugin, 1L);
        } else if (msg) {
            player.sendMessage(ChatColor.RED + "You're already in a queue");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 5.0F, 1.0F);
        }
    }

    public static void leaveQueue(Player player, boolean msg) {
        if (isQueued(player)) {
            queue.remove(player.getName());
            player.teleport(getFallback());
            if (msg)
                player.sendMessage(ChatColor.GREEN + "Left queue");
        } else if (msg) {
            player.sendMessage(ChatColor.RED + "You're not in a queue");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 5.0F, 1.0F);
        }
    }

    public static boolean isQueued(Player player) {
        boolean output = false;
        if (queue.containsKey(player.getName()))
            output = true;
        return output;
    }

    public static boolean isLobby(Player player) {
        boolean output = false;
        if (inlobby.containsKey(player.getName()))
            output = true;
        return output;
    }

    public static boolean isGame(Player player) {
        boolean output = false;
        if (ingame.containsKey(player.getName()))
            output = true;
        return output;
    }

    public static void joinLobby(Player player, boolean msg) {
        if (queue.containsKey(player.getName())) {
            int unv = unavailable.size();
            int mps = maps.size();
            if (mps != unv) {
                while (!inlobby.containsKey(player.getName())) {
                    setBestLobby(player);
                    String selected = inlobby.get(player.getName());
                    if (!unavailable.contains(selected) || selected != null || !selected.equals("null")) {
                        inlobby.put(player.getName(), selected);
                        int players = 0;
                        if (lobbies.containsKey(selected))
                            players = ((Integer)lobbies.get(selected)).intValue();
                        lobbies.put(selected, Integer.valueOf(players + 1));
                        leaveQueue(player, false);
                        if (msg)
                            player.sendMessage(ChatColor.GREEN + "Joined lobby " + selected);
                        String world = SettingsManager.getConfig().getString("map." + selected + ".lobby.world");
                        double x = SettingsManager.getConfig().getDouble("map." + selected + ".lobby.x");
                        double y = SettingsManager.getConfig().getDouble("map." + selected + ".lobby.y");
                        double z = SettingsManager.getConfig().getDouble("map." + selected + ".lobby.z");
                        float pitch = (float)SettingsManager.getConfig().getLong("map." + selected + ".lobby.pitch");
                        float yaw = (float)SettingsManager.getConfig().getLong("map." + selected + ".lobby.yaw");
                        player.teleport(new Location(Bukkit.getServer().getWorld(world), x, y, z, yaw, pitch));
                        for (Player others : Bukkit.getOnlinePlayers()) {
                            if (inlobby.containsKey(others.getName()) && ((String)inlobby.get(others.getName())).equals(selected)) {
                                others.sendMessage(ChatColor.YELLOW + player.getName() + " joined. Now Waiting: " + getPlayers(selected) + "/4 (2 Required to begin)");
                                others.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 5.0F, 1.0F);
                                if (players + 1 >= 2 && !countdown.containsKey(selected)) {
                                    others.sendMessage(ChatColor.GREEN + "Minimum player requirement reached, beginning countdown");
                                    countdown.put(selected, Integer.valueOf(61));
                                    startCountdown(selected);
                                }
                            }
                        }
                        if (players + 1 == 4) {
                            unavailable.add(selected);
                            SettingsManager.getConfig().set("unavailable", unavailable);
                            SettingsManager.saveConfig();
                            if (!countdown.containsKey(selected))
                                startCountdown(selected);
                            if (countdown.containsKey(selected) && (
                                    (Integer)countdown.get(selected)).intValue() > 15) {
                                countdown.put(selected, Integer.valueOf(15));
                                for (Player others : Bukkit.getOnlinePlayers()) {
                                    if (inlobby.containsKey(others.getName()) && ((String)inlobby.get(others.getName())).equals(selected))
                                        others.sendMessage(ChatColor.GREEN + "Reached 4 players, countdown time reduced");
                                }
                            }
                        }
                        continue;
                    }
                    if (msg) {
                        player.sendMessage(ChatColor.RED + "There are no available lobbies");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 5.0F, 1.0F);
                    }
                }
            } else if (msg) {
                player.sendMessage(ChatColor.RED + "There are no available lobbies");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 5.0F, 1.0F);
            }
        } else if (msg) {
            player.sendMessage(ChatColor.RED + "You're not in a queue");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 5.0F, 1.0F);
        }
    }

    public static void leaveLobby(Player player, boolean msg) {
        if (inlobby.containsKey(player.getName())) {
            String map = inlobby.get(player.getName());
            int players = ((Integer)lobbies.get(map)).intValue();
            lobbies.put(map, Integer.valueOf(players - 1));
            inlobby.remove(player.getName());
            unavailable.remove(map);
            SettingsManager.getConfig().set("unavailable", unavailable);
            SettingsManager.saveConfig();
            player.teleport(getFallback());
            rebalanceLobbies();
            if (pregame.containsKey(player.getName()))
                pregame.remove(player.getName());
            if (msg)
                player.sendMessage(ChatColor.GREEN + "Left lobby " + map);
            for (Player others : Bukkit.getOnlinePlayers()) {
                if (inlobby.containsKey(others.getName()) && ((String)inlobby.get(others.getName())).equals(map))
                    others.sendMessage(ChatColor.RED + player.getName() + " left");
            }
            if (((Integer)lobbies.get(map)).intValue() < 2 &&
                    countdown.containsKey(map)) {
                countdown.remove(map);
                for (Player others : Bukkit.getOnlinePlayers()) {
                    if (inlobby.containsKey(others.getName()) && ((String)inlobby.get(others.getName())).equals(map))
                        others.sendMessage(ChatColor.RED + "The countdown timer has been reset");
                }
            }
        } else if (msg) {
            player.sendMessage(ChatColor.RED + "You're not in a lobby");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 5.0F, 1.0F);
        }
    }

    public static void setBestLobby(Player player) {
        String output = "";
        boolean random = true;
        ArrayList<Integer> numbers = new ArrayList<>();
        for (String lobs : lobbies.keySet()) {
            if (((Integer)lobbies.get(lobs)).intValue() > 0)
                random = false;
            if (!unavailable.contains(lobs))
                numbers.add(lobbies.get(lobs));
        }
        int maxValue = Integer.MIN_VALUE;
        for (Integer i : numbers) {
            if (i.intValue() > maxValue)
                maxValue = i.intValue();
        }
        if (random) {
            int select = mapselect.nextInt(maps.size());
            output = maps.get(select);
            inlobby.put(player.getName(), output);
        } else {
            for (String lobs : lobbies.keySet()) {
                if (((Integer)lobbies.get(lobs)).intValue() == maxValue) {
                    output = lobs;
                    inlobby.put(player.getName(), output);
                }
            }
        }
    }

    public static void forceQuit(Player player) {
        leaveQueue(player, false);
        leaveLobby(player, false);
        if (getFallback() != null)
            player.teleport(getFallback());
        leaveGame(player, true);
        player.sendMessage(ChatColor.RED + "Your lobby has been forcefully closed");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5.0F, 1.0F);
    }

    public static void rebalanceLobbies() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isQueued(player)) {
                player.sendMessage(ChatColor.GRAY + "Migrating lobbies...");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
                (new BukkitRunnable() {
                    public void run() {
                        MatchmakingManager.leaveQueue(player, false);
                        MatchmakingManager.joinQueue(player, false);
                    }
                }).runTaskLater(Main.plugin, 10L);
                continue;
            }
            if (inlobby.containsKey(player.getName()) &&
                    getPlayers((String)inlobby.get(player.getName())) <= 1 &&
                    !ingame.containsKey(player.getName())) {
                player.sendMessage(ChatColor.GRAY + "Migrating lobbies...");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
                String map = "";
                if (inlobby.containsKey(player.getName()))
                    map = inlobby.get(player.getName());
                if (unavailable.contains(map)) {
                    unavailable.add(map);
                    SettingsManager.getConfig().set("unavailable", unavailable);
                    SettingsManager.saveConfig();
                }
                if (pregame.containsKey(player.getName()))
                    pregame.remove(player.getName());
                (new BukkitRunnable() {
                    public void run() {
                        MatchmakingManager.leaveGame(player, false);
                        MatchmakingManager.leaveLobby(player, false);
                        MatchmakingManager.joinQueue(player, false);
                    }
                }).runTaskLater(Main.plugin, 10L);
            }
        }
    }

    public static int getPlayers(String lobby) {
        return ((Integer)lobbies.get(lobby)).intValue();
    }

    public static boolean isFull(String lobby) {
        boolean output = false;
        if (((Integer)lobbies.get(lobby)).intValue() == 4)
            output = true;
        return output;
    }

    public static void createMap(String name, Player player) {
        if (!maps.contains(name)) {
            maps.add(name);
            SettingsManager.getConfig().set("maps", maps);
            SettingsManager.getConfig().set("map." + name + ".name", name);
            SettingsManager.getConfig().set("map." + name + ".lobby.world", player.getLocation().getWorld().getName());
            SettingsManager.getConfig().set("map." + name + ".lobby.x", Double.valueOf(player.getLocation().getX()));
            SettingsManager.getConfig().set("map." + name + ".lobby.y", Double.valueOf(player.getLocation().getY()));
            SettingsManager.getConfig().set("map." + name + ".lobby.z", Double.valueOf(player.getLocation().getZ()));
            SettingsManager.getConfig().set("map." + name + ".lobby.pitch", Float.valueOf(player.getLocation().getPitch()));
            SettingsManager.getConfig().set("map." + name + ".lobby.yaw", Float.valueOf(player.getLocation().getYaw()));
            SettingsManager.saveConfig();
            lobbies.put(name, Integer.valueOf(0));
            player.sendMessage(ChatColor.GREEN + "Successfully created " + name);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0F, 1.0F);
        } else {
            player.sendMessage(ChatColor.RED + "That map already exists");
        }
    }

    public static void setSpawn(String name, Player player, int spawn) {
        if (maps.contains(name)) {
            if (spawn >= 1 && spawn <= 4) {
                SettingsManager.getConfig().set("map." + name + ".spawn" + spawn + ".world", player.getLocation().getWorld().getName());
                SettingsManager.getConfig().set("map." + name + ".spawn" + spawn + ".x", Double.valueOf(player.getLocation().getX()));
                SettingsManager.getConfig().set("map." + name + ".spawn" + spawn + ".y", Double.valueOf(player.getLocation().getY()));
                SettingsManager.getConfig().set("map." + name + ".spawn" + spawn + ".z", Double.valueOf(player.getLocation().getZ()));
                SettingsManager.getConfig().set("map." + name + ".spawn" + spawn + ".pitch", Float.valueOf(player.getLocation().getPitch()));
                SettingsManager.getConfig().set("map." + name + ".spawn" + spawn + ".yaw", Float.valueOf(player.getLocation().getYaw()));
                SettingsManager.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set spawn " + spawn + " for map " + name);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0F, 1.0F);
            } else {
                player.sendMessage(ChatColor.RED + "You can only set spawn for 1-4");
            }
        } else {
            player.sendMessage(ChatColor.RED + "That map doesn't exists");
        }
    }

    public static void deleteMap(String name, Player player) {
        if (maps.contains(name)) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                if ((inlobby.containsKey(players.getName()) && ((String)inlobby.get(players.getName())).equals(name)) || (ingame.containsKey(players.getName()) && ((String)ingame.get(players.getName())).equals(name)))
                    forceQuit(players);
            }
            if (MatchmakingManager.unavailable.contains(name)) {
                List<String> unavailable = SettingsManager.getConfig().getStringList("unavailable");
                unavailable.remove(name);
                SettingsManager.getConfig().set("unavailable", unavailable);
                SettingsManager.saveConfig();
            }
            maps.remove(name);
            SettingsManager.getConfig().set("maps", maps);
            SettingsManager.getConfig().set("map." + name, null);
            SettingsManager.saveConfig();
            lobbies.remove(name);
            countdown.remove(name);
            player.sendMessage(ChatColor.GREEN + "Successfully deleted " + name);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0F, 1.0F);
        } else {
            player.sendMessage(ChatColor.RED + "That map doesn't exists");
        }
    }

    public static void startCountdown(final String map) {
        if (!countdown.containsKey(map) || ((Integer)countdown.get(map)).intValue() == 0)
            countdown.put(map, Integer.valueOf(61));
        if (countdown.containsKey(map))
            (new BukkitRunnable() {
                public void run() {
                    MatchmakingManager.countdown.put(map, Integer.valueOf(((Integer)MatchmakingManager.countdown.get(map)).intValue() - 1));
                    if (MatchmakingManager.countdown.containsKey(map)) {
                        if (((Integer)MatchmakingManager.countdown.get(map)).intValue() == 0) {
                            MatchmakingManager.countdown.remove(map);
                            cancel();
                            MatchmakingManager.startGame(map);
                            return;
                        }
                        if (((Integer)MatchmakingManager.countdown.get(map)).intValue() % 15 == 0 || ((Integer)MatchmakingManager.countdown.get(map)).intValue() <= 5)
                            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                                if (MatchmakingManager.isLobby(player) && ((String)MatchmakingManager.inlobby.get(player.getName())).equals(map)) {
                                    player.sendMessage(ChatColor.YELLOW + "Game starting in " + SettingsManager.formatTime(((Integer)MatchmakingManager.countdown.get(map)).intValue()));
                                    //TitleAPI.sendTitle(player, Integer.valueOf(0), Integer.valueOf(40), Integer.valueOf(15), "&a&lGame Starting", "&7In " + SettingsManager.formatTime(((Integer)MatchmakingManager.countdown.get(map)).intValue()));
                                }
                            }
                        if (((Integer)MatchmakingManager.countdown.get(map)).intValue() == 6) {
                            if(!unavailable.contains(map)) {
                                MatchmakingManager.unavailable.add(map);
                                SettingsManager.getConfig().set("unavailable", MatchmakingManager.unavailable);
                                SettingsManager.saveConfig();
                            }
                            int players = 0;
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                String pmap = "";
                                if (MatchmakingManager.isLobby(player))
                                    pmap = MatchmakingManager.inlobby.get(player.getName());
                                if (pmap.equals(map)) {
                                    String world1 = SettingsManager.getConfig().getString("map." + map + ".spawn1.world");
                                    double x1 = SettingsManager.getConfig().getDouble("map." + map + ".spawn1.x");
                                    double y1 = SettingsManager.getConfig().getDouble("map." + map + ".spawn1.y");
                                    double z1 = SettingsManager.getConfig().getDouble("map." + map + ".spawn1.z");
                                    float pitch1 = (float)SettingsManager.getConfig().getLong("map." + map + ".spawn1.pitch");
                                    float yaw1 = (float)SettingsManager.getConfig().getLong("map." + map + ".spawn1.yaw");
                                    Location one = new Location(Bukkit.getServer().getWorld(world1), x1, y1, z1, yaw1, pitch1);
                                    String world2 = SettingsManager.getConfig().getString("map." + map + ".spawn2.world");
                                    double x2 = SettingsManager.getConfig().getDouble("map." + map + ".spawn2.x");
                                    double y2 = SettingsManager.getConfig().getDouble("map." + map + ".spawn2.y");
                                    double z2 = SettingsManager.getConfig().getDouble("map." + map + ".spawn2.z");
                                    float pitch2 = (float)SettingsManager.getConfig().getLong("map." + map + ".spawn.2.pitch");
                                    float yaw2 = (float)SettingsManager.getConfig().getLong("map." + map + ".spawn.2.yaw");
                                    Location two = new Location(Bukkit.getServer().getWorld(world2), x2, y2, z2, yaw2, pitch2);
                                    String world3 = SettingsManager.getConfig().getString("map." + map + ".spawn3.world");
                                    double x3 = SettingsManager.getConfig().getDouble("map." + map + ".spawn3.x");
                                    double y3 = SettingsManager.getConfig().getDouble("map." + map + ".spawn3.y");
                                    double z3 = SettingsManager.getConfig().getDouble("map." + map + ".spawn3.z");
                                    float pitch3 = (float)SettingsManager.getConfig().getLong("map." + map + ".spawn3.pitch");
                                    float yaw3 = (float)SettingsManager.getConfig().getLong("map." + map + ".spawn3.yaw");
                                    Location three = new Location(Bukkit.getServer().getWorld(world3), x3, y3, z3, yaw3, pitch3);
                                    String world4 = SettingsManager.getConfig().getString("map." + map + ".spawn4.world");
                                    double x4 = SettingsManager.getConfig().getDouble("map." + map + ".spawn4.x");
                                    double y4 = SettingsManager.getConfig().getDouble("map." + map + ".spawn4.y");
                                    double z4 = SettingsManager.getConfig().getDouble("map." + map + ".spawn4.z");
                                    float pitch4 = (float)SettingsManager.getConfig().getLong("map." + map + ".spawn4.pitch");
                                    float yaw4 = (float)SettingsManager.getConfig().getLong("map." + map + ".spawn4.yaw");
                                    Location four = new Location(Bukkit.getServer().getWorld(world4), x4, y4, z4, yaw4, pitch4);
                                    players++;
                                    MatchmakingManager.playerslot.put(player.getName(), Integer.valueOf(players));
                                    MatchmakingManager.pregame.put(player.getName(), Boolean.valueOf(true));
                                    if (((Integer)MatchmakingManager.playerslot.get(player.getName())).intValue() == 1) {
                                        player.teleport(one);
                                        continue;
                                    }
                                    if (((Integer)MatchmakingManager.playerslot.get(player.getName())).intValue() == 2) {
                                        player.teleport(two);
                                        continue;
                                    }
                                    if (((Integer)MatchmakingManager.playerslot.get(player.getName())).intValue() == 3) {
                                        player.teleport(three);
                                        continue;
                                    }
                                    if (((Integer)MatchmakingManager.playerslot.get(player.getName())).intValue() == 4)
                                        player.teleport(four);
                                }
                            }
                        }
                    } else {
                        cancel();
                    }
                }
            }).runTaskTimer(Main.plugin, 0L, 20L);
    }

    public static void startGame(String map) {
        gametimer.put(map, Integer.valueOf(240));
        startGameTimer(map);
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard ig = manager.getNewScoreboard();
        Team team = ig.registerNewTeam("slappy");
        Objective objective = ig.registerNewObjective("test", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Slappy - " + map);
        Score space1 = objective.getScore("");
        space1.setScore(3);
        int players = 0;
        ArrayList<Player> plist = new ArrayList<>();
        List<String> mlist = maps;
        for (Player player : Bukkit.getOnlinePlayers()) {
            String pmap = "";
            if (isLobby(player))
                pmap = inlobby.get(player.getName());
            if (pmap.equals(map)) {
                if (pregame.containsKey(player.getName()))
                    pregame.remove(player.getName());
                team.addPlayer((OfflinePlayer)player);
                team.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Slappy - " + map);
                Score score = objective.getScore(ChatColor.WHITE + player.getName());
                score.setScore(2);
                player.setScoreboard(ig);
                gameInventory(player);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F);
                inlobby.remove(player.getName());
                ingame.put(player.getName(), map);
                players++;
                playerslot.put(player.getName(), Integer.valueOf(players));
                player.sendMessage(ChatColor.GOLD + "Game starting!");
                //TitleAPI.sendTitle(player, Integer.valueOf(0), Integer.valueOf(30), Integer.valueOf(10), "&2&lGame Starting", "");
            }
        }
        Score space2 = objective.getScore(" ");
        space2.setScore(0);
        countdown.remove(map);
    }

    public static void startGameTimer(final String map) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        taskID = scheduler.scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
            public void run() {
                if (MatchmakingManager.gametimer.containsKey(map)) {
                    if (((Integer)MatchmakingManager.gametimer.get(map)).intValue() == 0) {
                        Bukkit.getScheduler().cancelTask(MatchmakingManager.taskID);
                        MatchmakingManager.tiePlayer(map);
                        MatchmakingManager.gametimer.remove(map);
                        return;
                    }
                    if (((Integer)MatchmakingManager.gametimer.get(map)).intValue() % 30 == 0 || ((Integer)MatchmakingManager.gametimer.get(map)).intValue() <= 5)
                        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                            if (MatchmakingManager.isGame(player) && ((String)MatchmakingManager.ingame.get(player.getName())).equals(map))
                                player.sendMessage(ChatColor.YELLOW + "Time left: " + SettingsManager.formatTime(((Integer)MatchmakingManager.gametimer.get(map)).intValue()));
                        }
                    int time = ((Integer)MatchmakingManager.gametimer.get(map)).intValue();
                    MatchmakingManager.gametimer.put(map, Integer.valueOf(time - 1));
                } else {
                    Bukkit.getScheduler().cancelTask(MatchmakingManager.taskID);
                }
            }
        }, 1L, 20L);
    }

    public static void forcestartGame(String map) {
        if (countdown.containsKey(map) && (
                (Integer)countdown.get(map)).intValue() < 16)
            countdown.put(map, Integer.valueOf(16));
    }

    public static void updateScoreboard(String map) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard ig = manager.getNewScoreboard();
        Team team = ig.registerNewTeam("slappy");
        Objective objective = ig.registerNewObjective("test", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Slappy - " + map);
        Score space1 = objective.getScore("");
        space1.setScore(3);
        for (Player player : Bukkit.getOnlinePlayers()) {
            String pmap = "";
            if (isGame(player))
                pmap = ingame.get(player.getName());
            if (pmap.equals(map)) {
                team.addPlayer((OfflinePlayer)player);
                team.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Slappy - " + map);
                Score score = objective.getScore(ChatColor.WHITE + player.getName());
                score.setScore(2);
                player.setScoreboard(manager.getNewScoreboard());
                player.setScoreboard(ig);
            }
        }
        Score space2 = objective.getScore(" ");
        space2.setScore(0);
    }

    public static void updateScoreboardPlayer(String map, Player sender) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard ig = manager.getNewScoreboard();
        Team team = ig.registerNewTeam("slappy");
        Objective objective = ig.registerNewObjective("test", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Slappy - " + map);
        Score space1 = objective.getScore("");
        space1.setScore(3);
        for (Player player : Bukkit.getOnlinePlayers()) {
            String pmap = "";
            if (isGame(player))
                pmap = ingame.get(player.getName());
            if (pmap.equals(map)) {
                team.addPlayer((OfflinePlayer)player);
                team.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Slappy - " + map);
                if (player.getName().equalsIgnoreCase(sender.getName())) {
                    Score score = objective.getScore(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + player.getName());
                    score.setScore(1);
                } else {
                    Score score = objective.getScore(ChatColor.WHITE + player.getName());
                    score.setScore(2);
                }
                player.setScoreboard(manager.getNewScoreboard());
                player.setScoreboard(ig);
            }
        }
        Score space2 = objective.getScore(" ");
        space2.setScore(0);
    }

    public static void leaveGame(Player player, boolean complete) {
        if (isGame(player)) {
            String map = ingame.get(player.getName());
            updateScoreboardPlayer(map, player);
            int players = ((Integer)lobbies.get(map)).intValue();
            if (!complete) {
                int newplayers = players - 1;
                lobbies.put(map, Integer.valueOf(newplayers));
                testWinners(map, newplayers);
            } else {
                resetMap(map);
            }
            ingame.remove(player.getName());
            if (lost.containsKey(player.getName()))
                lost.remove(player.getName());
            player.teleport(getFallback());
            otherInventory(player);
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            player.setScoreboard(manager.getNewScoreboard());
        }
    }

    public static void tiePlayer(String map) {
        if (lobbies.containsKey(map))
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (ingame.containsKey(player.getName()) && (
                        (String)ingame.get(player.getName())).equals(map) &&
                        !lost.containsKey(player.getName())) {
                    player.sendMessage(ChatColor.YELLOW + "Time limit reached; Draw!");
                    leaveGame(player, true);
                    player.playSound(player.getLocation(), Sound.ENTITY_PIG_HURT, 1.0F, 1.0F);
                }
            }
    }

    public static void losePlayer(Player player) {
        if (isGame(player)) {
            String map = ingame.get(player.getName());
            int players = ((Integer)lobbies.get(map)).intValue();
            if (players > 1) {
                player.sendMessage(ChatColor.RED + "You lost");
                player.playSound(player.getLocation(), Sound.ENTITY_PIG_HURT, 1.0F, 1.0F);
                lost.put(player.getName(), Boolean.valueOf(true));
                leaveGame(player, false);
            }
        }
    }

    public static void testWinners(String map, int players) {
        if (lobbies.containsKey(map) &&
                players <= 1)
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (ingame.containsKey(player.getName()) && (
                        (String)ingame.get(player.getName())).equals(map) &&
                        !lost.containsKey(player.getName()))
                    winPlayer(player);
            }
    }

    public static void winPlayer(Player player) {
        if (isGame(player)) {
            leaveGame(player, true);
            player.sendMessage(ChatColor.GOLD + "You win!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        }
    }

    public static void resetMap(String map) {
        lobbies.put(map, Integer.valueOf(0));
        unavailable.remove(map);
        SettingsManager.getConfig().set("unavailable", unavailable);
        SettingsManager.saveConfig();
    }

    public static void setFallback(Player player) {
        SettingsManager.getConfig().set("fallback.world", player.getLocation().getWorld().getName());
        SettingsManager.getConfig().set("fallback.x", Double.valueOf(player.getLocation().getX()));
        SettingsManager.getConfig().set("fallback.y", Double.valueOf(player.getLocation().getY()));
        SettingsManager.getConfig().set("fallback.z", Double.valueOf(player.getLocation().getZ()));
        SettingsManager.getConfig().set("fallback.pitch", Float.valueOf(player.getLocation().getPitch()));
        SettingsManager.getConfig().set("fallback.yaw", Float.valueOf(player.getLocation().getYaw()));
        SettingsManager.saveConfig();
        player.sendMessage(ChatColor.GREEN + "Set fallback location");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0F, 1.0F);
    }

    public static Location getFallback() {
        String w = SettingsManager.getConfig().getString("fallback.world");
        double x = SettingsManager.getConfig().getDouble("fallback.x");
        double y = SettingsManager.getConfig().getDouble("fallback.y");
        double z = SettingsManager.getConfig().getDouble("fallback.z");
        float pi = (float)SettingsManager.getConfig().getLong("fallback.pitch");
        float ya = (float)SettingsManager.getConfig().getLong("fallback.yaw");
        Location output = new Location(Bukkit.getServer().getWorld(w), x, y, z, ya, pi);
        return output;
    }

    public static void gameInventory(Player player) {
        ItemStack fish = new ItemStack(Material.COD);
        ItemMeta f = fish.getItemMeta();
        f.setDisplayName(ChatColor.BLUE + "Slappy Fish");
        ArrayList<String> fl = new ArrayList<>();
        fl.add("");
        fl.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "Ew, smelly");
        fl.add("");
        f.setLore(fl);
        fish.setItemMeta(f);
        fish.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
        player.getInventory().clear();
        player.getInventory().setItem(4, fish);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999999, 2));
        player.getInventory().setHeldItemSlot(4);
        player.setHealth(20.0D);
        player.setGameMode(GameMode.ADVENTURE);
    }

    public static void otherInventory(Player player) {
        player.getInventory().clear();
        player.removePotionEffect(PotionEffectType.SPEED);
        player.getInventory().setHeldItemSlot(0);
        player.setHealth(20.0D);
        player.setGameMode(GameMode.ADVENTURE);
    }
}
