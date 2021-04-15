package me.Halflove.Slappy;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!e.getPlayer().hasPermission("mod"))
            MatchmakingManager.joinQueue(e.getPlayer(), false);
        if (!e.getPlayer().hasPermission("admin"))
            e.getPlayer().setGameMode(GameMode.ADVENTURE);
        e.setJoinMessage(null);
        e.getPlayer().teleport(MatchmakingManager.getFallback());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (MatchmakingManager.isGame(player))
            MatchmakingManager.losePlayer(player);
        MatchmakingManager.leaveLobby(player, false);
        MatchmakingManager.leaveQueue(player, false);
        MatchmakingManager.rebalanceLobbies();
        e.setQuitMessage(null);
    }
}
