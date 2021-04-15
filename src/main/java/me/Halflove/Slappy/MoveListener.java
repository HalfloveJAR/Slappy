package me.Halflove.Slappy;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (MatchmakingManager.isGame(player) &&
                e.getTo().getBlock().isLiquid())
            MatchmakingManager.losePlayer(player);
    }
}
