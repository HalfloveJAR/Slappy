package me.Halflove.Slappy;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SlappyCommands implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("slappy"))
            if (sender.hasPermission("admin")) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.RED + "/s joinlobby, joinqueue, leavelobby, leavequeue, leavegame, create, delete, setspawn, setfallback, debug, maps, forcestart, endgame");
                } else if (args[0].equalsIgnoreCase("joinlobby")) {
                    MatchmakingManager.joinLobby((Player)sender, true);
                } else if (args[0].equalsIgnoreCase("joinqueue")) {
                    MatchmakingManager.joinQueue((Player)sender, true);
                } else if (args[0].equalsIgnoreCase("leavelobby")) {
                    MatchmakingManager.leaveLobby((Player)sender, true);
                } else if (args[0].equalsIgnoreCase("leavequeue")) {
                    MatchmakingManager.leaveQueue((Player)sender, true);
                } else if (args[0].equalsIgnoreCase("create")) {
                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.RED + "/s create (map)");
                    } else {
                        MatchmakingManager.createMap(args[1], (Player)sender);
                    }
                } else if (args[0].equalsIgnoreCase("delete")) {
                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.RED + "/s delete (map)");
                    } else {
                        MatchmakingManager.deleteMap(args[1], (Player)sender);
                    }
                } else if (args[0].equalsIgnoreCase("setspawn")) {
                    if (args.length != 3) {
                        sender.sendMessage(ChatColor.RED + "/s setspawn (map) (spawn)");
                    } else {
                        MatchmakingManager.setSpawn(args[1], (Player)sender, Integer.parseInt(args[2]));
                    }
                } else if (args[0].equalsIgnoreCase("setfallback")) {
                    MatchmakingManager.setFallback((Player)sender);
                } else if (args[0].equalsIgnoreCase("debug")) {
                    sender.sendMessage(MatchmakingManager.countdown + " countdown");
                    sender.sendMessage(MatchmakingManager.ingame + " ingame");
                    sender.sendMessage(MatchmakingManager.inlobby + " inlobby");
                    sender.sendMessage(MatchmakingManager.lobbies + " lobbies");
                    sender.sendMessage(MatchmakingManager.maps + " maps");
                    sender.sendMessage(MatchmakingManager.queue + " queue");
                    sender.sendMessage(MatchmakingManager.unavailable + " unavailable");
                    sender.sendMessage(String.valueOf(MatchmakingManager.maps.size()) + " maps size");
                    sender.sendMessage(String.valueOf(MatchmakingManager.unavailable.size()) + " unv size");
                    sender.sendMessage(MatchmakingManager.gametimer + " game timer");
                } else if (args[0].equalsIgnoreCase("maps")) {
                    sender.sendMessage(MatchmakingManager.maps + " maps");
                } else if (args[0].equalsIgnoreCase("leavegame")) {
                    MatchmakingManager.losePlayer((Player)sender);
                } else if (args[0].equalsIgnoreCase("forcestart")) {
                    if (args.length == 1) {
                        if (MatchmakingManager.isLobby((Player)sender)) {
                            Player player = (Player)sender;
                            MatchmakingManager.forcestartGame(MatchmakingManager.inlobby.get(player.getName()));
                            sender.sendMessage(ChatColor.GREEN + "Successfully shortened the start time!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "You're not in a lobby...");
                            Player player = (Player)sender;
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 5.0F, 1.0F);
                        }
                    } else if (MatchmakingManager.maps.contains(args[1])) {
                        MatchmakingManager.forcestartGame(args[1]);
                        sender.sendMessage(ChatColor.GREEN + "Successfully forcestarted the map " + args[1]);
                    } else {
                        sender.sendMessage(ChatColor.RED + "That map doesn't exist");
                    }
                } else if (args[0].equalsIgnoreCase("endgame")) {
                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.RED + "/s endgame (map)");
                    } else if (MatchmakingManager.maps.contains(args[1])) {
                        MatchmakingManager.tiePlayer(args[1]);
                        sender.sendMessage(ChatColor.GREEN + "Successfully ended the map " + args[1]);
                    } else {
                        sender.sendMessage(ChatColor.RED + "That map doesn't exist");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "/s joinlobby, joinqueue, leavelobby, leavequeue, leavegame, create, delete, setspawn, setfallback, debug, maps, forcestart, endgame");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Wait, you're not an admin... You can't do that!");
            }
        return true;
    }
}

