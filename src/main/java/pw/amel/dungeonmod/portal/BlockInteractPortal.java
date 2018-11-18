package pw.amel.dungeonmod.portal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pw.amel.dungeonmod.ConfigManager;
import pw.amel.dungeonmod.Dungeon;
import pw.amel.dungeonmod.DungeonMod;

import java.util.ArrayList;

public class BlockInteractPortal implements Listener, ConfigManager.PortalConstructor {
    @Override
    public void newPortal(String name, Location point1, Location point2, Location spawnPoint,
                          boolean isEntry, boolean cancelEvent, Dungeon dungeon, ConfigurationSection config) {
        boolean rightClickTrigger = config.getBoolean("rightclick", true);
        boolean leftClickTrigger = config.getBoolean("leftclick", true);
        boolean physicalTrigger = config.getBoolean("physical", false);
        Material material = Material.matchMaterial(config.getString("material", "AIR"));

        portals.add(new BlockPortal(point1, point2, spawnPoint,
                isEntry, cancelEvent, dungeon,
                rightClickTrigger, leftClickTrigger, physicalTrigger,
                material));
    }

    private ArrayList<BlockPortal> portals = new ArrayList<>();

    @EventHandler(priority = EventPriority.NORMAL)
    private void playerInteractEvent(PlayerInteractEvent event) {
        for (BlockPortal portal : portals) {
            if (!portal.physicalTrigger && event.getAction() == Action.PHYSICAL)
                continue;
            if (!portal.leftClickTrigger && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
                continue;
            if (!portal.rightClickTrigger && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
                continue;

            Location checkLocation;
            if (portal.material == Material.AIR)
                checkLocation = event.getPlayer().getLocation();
            else
                checkLocation = event.getClickedBlock().getLocation();

            if (!DungeonMod.isInArea(checkLocation, portal.point1, portal.point2))
                continue;

            Material clickedMaterial;
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR)
                clickedMaterial = Material.AIR;
            else
                clickedMaterial = checkLocation.getBlock().getType();

            if (clickedMaterial == portal.material) {
                event.getPlayer().teleport(portal.spawnPoint);
                if (portal.cancelEvent)
                    event.setCancelled(true);
                break;
            }
        }
    }

    private class BlockPortal {
        public BlockPortal(Location point1, Location point2, Location spawnPoint,
                           boolean isEntry, boolean cancelEvent, Dungeon dungeon,
                           boolean rightClickTrigger, boolean leftClickTrigger, boolean physicalTrigger,
                           Material material) {
            this.point1 = point1;
            this.point2 = point2;
            this.spawnPoint = spawnPoint;
            this.isEntry = isEntry;
            this.cancelEvent = cancelEvent;
            this.dungeon = dungeon;
            this.rightClickTrigger = rightClickTrigger;
            this.leftClickTrigger = leftClickTrigger;
            this.physicalTrigger = physicalTrigger;
            this.material = material;
        }

        Location point1;
        Location point2;
        Location spawnPoint;
        boolean isEntry;
        boolean cancelEvent;
        Dungeon dungeon;
        boolean rightClickTrigger;
        boolean leftClickTrigger;
        boolean physicalTrigger;
        Material material;
    }
}
