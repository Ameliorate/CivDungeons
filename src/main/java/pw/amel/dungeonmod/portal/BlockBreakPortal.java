package pw.amel.dungeonmod.portal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import pw.amel.dungeonmod.ConfigManager;
import pw.amel.dungeonmod.DungeonMod;

import java.util.ArrayList;

public class BlockBreakPortal implements Listener, ConfigManager.PortalConstructor {
    @Override
    public void newPortal(ConfigurationSection config) {
        portals.add(new BlockPortal(config));
    }

    @Override
    public void removeAllPortals() {
        portals.clear();
    }

    private ArrayList<BlockPortal> portals = new ArrayList<>();

    @EventHandler(priority = EventPriority.NORMAL)
    private void blockBreakEvent(BlockBreakEvent event) {
        for (BlockPortal portal : portals) {
            Location checkLocation = event.getBlock().getLocation();

            if (!DungeonMod.isInArea(checkLocation, portal.getPoint1(), portal.getPoint2()))
                continue;

            Material clickedMaterial = event.getBlock().getType();

            if (clickedMaterial == portal.material) {
                portal.afterDelay(() -> event.getPlayer().teleport(portal.getSpawnPoint()));
                if (portal.shouldCancelEvent())
                    event.setCancelled(true);
                break;
            }
        }
    }

    private class BlockPortal extends PortalData {
        public BlockPortal(ConfigurationSection config) {
            super(config);
            material = Material.matchMaterial(config.getString("material", "AIR"));
        }

        Material material;
    }
}
