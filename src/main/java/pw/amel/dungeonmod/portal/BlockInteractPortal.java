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
import pw.amel.dungeonmod.DungeonMod;

import java.util.ArrayList;

public class BlockInteractPortal implements Listener, ConfigManager.PortalConstructor {
    @Override
    public void newPortal(ConfigurationSection config) {

        portals.add(new BlockPortal(config));
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

            if (!DungeonMod.isInArea(checkLocation, portal.getPoint1(), portal.getPoint2()))
                continue;

            Material clickedMaterial;
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR)
                clickedMaterial = Material.AIR;
            else
                clickedMaterial = checkLocation.getBlock().getType();

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
            rightClickTrigger = config.getBoolean("rightclick", true);
            leftClickTrigger = config.getBoolean("leftclick", true);
            physicalTrigger = config.getBoolean("physical", false);
            material = Material.matchMaterial(config.getString("material", "AIR"));
        }

        boolean rightClickTrigger;
        boolean leftClickTrigger;
        boolean physicalTrigger;
        Material material;
    }
}