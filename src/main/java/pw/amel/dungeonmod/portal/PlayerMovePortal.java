package pw.amel.dungeonmod.portal;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pw.amel.dungeonmod.ConfigManager;

import java.util.ArrayList;

public class PlayerMovePortal implements Listener, ConfigManager.PortalConstructor {
    @Override
    public void newPortal(ConfigurationSection config) {
        portals.add(new Portal(config));
    }

    @Override
    public void removeAllPortals() {
        portals.clear();
    }

    private ArrayList<Portal> portals = new ArrayList<>();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().getLocation().equals(event.getTo().getBlock().getLocation()))
            return;

        for (Portal portal : portals) {
            if (event.getPlayer().isSprinting() && !portal.sprinting)
                continue;
            if (event.getPlayer().isSneaking() && !portal.sneaking)
                continue;

            boolean isWalking = !event.getPlayer().isSprinting() && !event.getPlayer().isSneaking();
            if (isWalking && !portal.walking)
                continue;

            boolean moveEnter = !portal.isInArea(event.getFrom()) && portal.isInArea(event.getTo());
            boolean moveExit = portal.isInArea(event.getFrom()) && !portal.isInArea(event.getTo());
            boolean moveInside = portal.isInArea(event.getFrom()) && portal.isInArea(event.getTo());
            boolean moveOutside = !portal.isInArea(event.getFrom()) && !portal.isInArea(event.getTo());

            if (moveOutside)
                continue;
            if (moveEnter && !portal.triggerMoveEnter)
                continue;
            if (moveExit && !portal.triggerMoveExit)
                continue;
            if (moveInside && !portal.triggerMoveInside)
                continue;

            portal.afterDelay(() -> event.getPlayer().teleport(portal.getSpawnPoint()));
            if (portal.shouldCancelEvent())
                event.setCancelled(true);
            break;
        }
    }

    private class Portal extends PortalData {
        public Portal(ConfigurationSection config) {
            super(config);

            sprinting = config.getBoolean("sprinting", true);
            sneaking = config.getBoolean("sneaking", true);
            walking = config.getBoolean("walking", true);
            triggerMoveInside = config.getBoolean("triggerMoveInside", true);
            triggerMoveEnter = config.getBoolean("triggerMoveEnter", true);
            triggerMoveExit = config.getBoolean("triggerMoveExit", true);
        }

        boolean sprinting;
        boolean sneaking;
        boolean walking;
        boolean triggerMoveInside;
        boolean triggerMoveEnter;
        boolean triggerMoveExit;
    }
}
