package org.ame.civdungeons;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Teleports the player back into the dungeon if they leave it's boundries.
 */
public class DungeonWorldBorder implements Listener {
    public DungeonWorldBorder(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    private Dungeon dungeon;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location playerLocation = event.getPlayer().getLocation();

        if (playerLocation.getWorld() != dungeon.dungeonWorld) {
            return;
        }

        if (playerLocation.getX() < 0 ||
                playerLocation.getY() < 0 ||
                playerLocation.getZ() < 0) {
            // Since dungeons are always only on positive coords, I can just check for less than 0.
            dungeon.teleportPlayerToSpawn(event.getPlayer());
            event.getPlayer().sendMessage(ChatColor.RED + "You've reached the edge of this dungeon! " +
                    "Please report how you did this to the server admins.");
        } else if (playerLocation.getX() > dungeon.getMaxX() ||
                playerLocation.getY() > dungeon.getMaxY() - 1 ||
                playerLocation.getZ() > dungeon.getMaxZ()) {
            dungeon.teleportPlayerToSpawn(event.getPlayer());
            event.getPlayer().sendMessage(ChatColor.RED + "You've reached the edge of this dungeon! " +
                    "Please report how you did this to the server admins.");
        }
    }
}
