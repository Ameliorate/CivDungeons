package pw.amel.dungeonmod.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import pw.amel.dungeonmod.ConfigManager;
import pw.amel.dungeonmod.Dungeon;

public class ConstructionHelmetTeleport implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    private void onInventoryClickEvent(InventoryClickEvent event) {
        ClickType click = event.getClick();
        if (!event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Construction Helmet"))
            return;
        else if (click != ClickType.DOUBLE_CLICK && click != ClickType.CONTROL_DROP && click != ClickType.DROP && click != ClickType.LEFT &&
                click != ClickType.NUMBER_KEY && click != ClickType.RIGHT && click != ClickType.SHIFT_LEFT && click != ClickType.SHIFT_RIGHT)
            return;

        event.setCancelled(true);
        event.getClickedInventory().getItem(event.getSlot()).setType(Material.AIR);

        Player player = (Player) event.getWhoClicked();

        String worldName = player.getLocation().getWorld().getName();
        Dungeon dungeon = ConfigManager.getDungeon(worldName);
        if (dungeon == null)
            return;
        dungeon.teleportPlayerToExit(player);
    }
}
