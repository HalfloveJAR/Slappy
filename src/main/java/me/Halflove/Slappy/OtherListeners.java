package me.Halflove.Slappy;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class OtherListeners implements Listener {
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity e = event.getEntity();
        if (e instanceof Player) {
            Player player = (Player)e;
            if (!MatchmakingManager.isGame(player)) {
                event.setCancelled(true);
            } else {
                event.setDamage(0.0D);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler
    public void changeItem(PlayerItemHeldEvent event) {
        if (MatchmakingManager.isGame(event.getPlayer()))
            event.getPlayer().getInventory().setHeldItemSlot(4);
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().hasPermission("mod")) {
            event.setCancelled(true);
        } else if (MatchmakingManager.isGame((Player)event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void swapHands(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (MatchmakingManager.pregame.containsKey(event.getPlayer().getName()))
            event.setCancelled(true);
    }
}
