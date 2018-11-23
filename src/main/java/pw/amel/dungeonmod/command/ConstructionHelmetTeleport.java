package pw.amel.dungeonmod.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pw.amel.dungeonmod.ConfigManager;
import pw.amel.dungeonmod.Dungeon;

public class ConstructionHelmetTeleport implements Listener {
    @EventHandler(ignoreCancelled = true)
    private void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getCurrentItem().getType() != Material.GOLD_HELMET)
            return;
        InventoryAction click = event.getAction();
        if (!event.getCurrentItem().hasItemMeta() || !event.getCurrentItem().getItemMeta().hasDisplayName() ||
                !event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Construction Helmet"))
            return;
        else if (click == InventoryAction.UNKNOWN || click == InventoryAction.NOTHING)
            return;

        event.setCancelled(true);
        event.getClickedInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));

        Player player = (Player) event.getWhoClicked();
        player.updateInventory();

        String worldName = player.getLocation().getWorld().getName();
        Dungeon dungeon = ConfigManager.getDungeon(worldName);
        if (dungeon == null)
            return;
        dungeon.teleportPlayerToExit(player);
    }
}
