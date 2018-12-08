package pw.amel.dungeonmod.portal;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import pw.amel.dungeonmod.ConfigManager;

import java.util.ArrayList;

public class EntityDamagePortal implements Listener, ConfigManager.PortalConstructor {
    @Override
    public void newPortal(ConfigurationSection config) {
        portals.add(new DamagePortal(config));
    }

    @Override
    public void removeAllPortals() {
        portals.clear();
    }

    private ArrayList<DamagePortal> portals = new ArrayList<>();

    @EventHandler(ignoreCancelled = true)
    private void entityDamageEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        Player damager = (Player) event.getDamager();

        for (DamagePortal portal : portals) {
            if (event.getDamage() < portal.minDamage || event.getDamage() > portal.maxDamage)
                continue;

            Location checkLocation = event.getEntity().getLocation();

            if (!portal.isInArea(checkLocation))
                continue;

            System.out.println("damage" + event.getDamage());
            System.out.println("health" + ((Damageable) event.getEntity()).getHealth());
            System.out.println("kill" + portal.kill);

            if (portal.kill)
                if (event.getEntity() instanceof Damageable &&
                        event.getDamage() <= ((Damageable) event.getEntity()).getHealth())
                    continue;

            if (portal.type != null)
                if (event.getEntity().getType() != portal.type)
                    continue;

            if (portal.item != null)
                if (!damager.getInventory().getItemInMainHand().isSimilar(portal.item))
                    continue;

            if (portal.name != null)
                if (portal.name.equals(event.getEntity().getCustomName()))
                    continue;

            if (portal.shouldCancelEvent() && portal.kill) {
                Damageable damageEntity = (Damageable) event.getEntity();
                damageEntity.setHealth(damageEntity.getMaxHealth());
            }

            portal.trigger(damager, event);
            break;
        }
    }

    private class DamagePortal extends PortalData {
        public DamagePortal(ConfigurationSection config) {
            super(config);

            kill = config.getBoolean("kill", false);
            minDamage = config.getInt("minDamage", 0);
            maxDamage = config.getInt("maxDamage", Integer.MAX_VALUE);
            item = config.getItemStack("item", null);
            name = config.getString("nameTag", null);

            String typeStr = config.getString("entityType", null);
            if (typeStr == null) {
                type = null;
            } else {
                type = EntityType.valueOf(typeStr.toUpperCase().trim());
            }
        }

        boolean kill;
        int minDamage;
        int maxDamage;
        ItemStack item;
        EntityType type;
        String name;
    }
}
