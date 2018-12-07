package pw.amel.dungeonmod.portal;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import pw.amel.dungeonmod.Dungeon;
import pw.amel.dungeonmod.DungeonMod;

import java.util.logging.Level;

public abstract class PortalData {
    public PortalData(ConfigurationSection config) {
        doEntryExit(config);
        doDungeon(config);
        doLocation(config);
        doSpawn(config);

        cancelEvent = config.getBoolean("cancelEvent", false);
        delaySeconds = (float) config.getDouble("delaySeconds", 0);
    }

    private void doEntryExit(ConfigurationSection config) {
        String entryOrExit = config.getString("entryOrExit", "entry");
        if ((!entryOrExit.equals("entry")) && (!entryOrExit.equals("exit"))) {
            DungeonMod.getPlugin().getLogger().log(Level.WARNING, config.getCurrentPath() +
                    ".entryOrExit is not 'entry' or 'exit'. Defaulting to entry.");
            entryOrExit = "entry";
        }
        isEntry = entryOrExit.equals("entry");
    }

    private void doDungeon(ConfigurationSection config) {
        String dungeonName = config.getString("dungeon");
        dungeon = DungeonMod.getConfigManager().getDungeon(dungeonName);
        if (dungeon == null) {
            DungeonMod.getPlugin().getLogger().log(Level.SEVERE, config.getCurrentPath() +
                    ".dungeon is not a valid dungeon.");
            throw new IllegalArgumentException();
        }
    }

    private void doLocation(ConfigurationSection config) {
        int x1 = config.getInt("x1");
        int y1 = config.getInt("y1");
        int z1 = config.getInt("z1");
        int x2 = config.getInt("x2", Integer.MAX_VALUE);
        int y2 = config.getInt("y2", Integer.MAX_VALUE);
        int z2 = config.getInt("z2", Integer.MAX_VALUE);

        if (x2 == Integer.MAX_VALUE)
            x2 = x1;
        if (y2 == Integer.MAX_VALUE)
            y2 = y1;
        if (z2 == Integer.MAX_VALUE)
            z2 = z1;

        String worldName = config.getString("world");

        if (worldName != null && isExit()) {
            DungeonMod.getPlugin().getLogger().log(Level.WARNING, config.getCurrentPath() +
                    ".world and .dungeon are both defined and .entryOrExit is 'exit'. Is this a mistake? Defaulting to .dungeon");
        }

        World world;
        if (worldName != null && isEntry()) {
            world = DungeonMod.getPlugin().getServer().getWorld(worldName);
            if (world == null) {
                DungeonMod.getPlugin().getLogger().log(Level.SEVERE, config.getCurrentPath() +
                        ".world is not a valid world.");
                throw new IllegalArgumentException();
            }
        } else {
            world = dungeon.getDungeonWorld();
        }

        point1 = new Location(world, x1, y1, z1);
        point2 = new Location(world, x2, y2, z2);
    }

    private void doSpawn(ConfigurationSection config) {
        int spawnX = config.getInt("spawnX", Integer.MAX_VALUE);
        int spawnY = config.getInt("spawnY", Integer.MAX_VALUE);
        int spawnZ = config.getInt("spawnZ", Integer.MAX_VALUE);
        String spawnWorldName = config.getString("spawnWorld");

        boolean warn = true;

        if (spawnWorldName != null && isEntry()) {
            DungeonMod.getPlugin().getLogger().log(Level.WARNING, config.getCurrentPath() +
                    ".spawnWorld is defined and .entryOrExit is 'entry'. Is this a mistake? Defaulting to .dungeon.");
            spawnWorldName = null;
        }

        boolean useDungeonSpawnExit = false;
        if (spawnX == Integer.MAX_VALUE && spawnY == Integer.MAX_VALUE && spawnZ == Integer.MAX_VALUE && spawnWorldName == null) {
            warn = false;
        }

        if (spawnX == Integer.MAX_VALUE || spawnY == Integer.MAX_VALUE || spawnZ == Integer.MAX_VALUE || spawnWorldName == null) {
            if (warn) {
                DungeonMod.getPlugin().getLogger().log(Level.WARNING, config.getCurrentPath() +
                        ".spawnX, .spawnY, .spawnZ, and .spawnWorld must all be set to set the spawn point. " +
                        "Defaulting to dungeon's default spawn point.");
            }
            useDungeonSpawnExit = true;
        }

        World spawnWorld = null;
        if (!useDungeonSpawnExit)
            spawnWorld = DungeonMod.getPlugin().getServer().getWorld(spawnWorldName);

        if (useDungeonSpawnExit)
            spawnPoint = isEntry ? dungeon.getSpawnLocation() : dungeon.getExitLocation();
        else
            spawnPoint = new Location(spawnWorld, spawnX, spawnY, spawnZ);
    }

    /**
     * Checks if a location is inside the two other locations
     * @param target The target location that is expected to be inside the cuboid formed by the other two points.
     * @param point1 One of the points that forms the cuboid that target is expected to be inside.
     * @param point2 The other of two points that forms the cuboid that the target may be inside.
     * @return If target was inside the cuboid that is formed by point1 and point2.
     */
    public static boolean isInArea(Location target, Location point1, Location point2){
        Location t = target;
        Location p1 = point1;
        Location p2 = point2;
        // Don't try to mind the source code too much. I got it from the internet and it should work.
        return p1.getWorld().getName().equals(p2.getWorld().getName()) &&
                t.getWorld().getName().equals(p1.getWorld().getName()) &&
                (t.getBlockX() >= p1.getBlockX() &&
                        t.getBlockX() <= p2.getBlockX() ||
                        t.getBlockX() <= p1.getBlockX() &&
                                t.getBlockX() >= p2.getBlockX()) &&
                (t.getBlockZ() >= p1.getBlockZ() &&
                        t.getBlockZ() <= p2.getBlockZ() ||
                        t.getBlockZ() <= p1.getBlockZ() &&
                                t.getBlockZ() >= p2.getBlockZ()) &&
                (t.getBlockY() >= p1.getBlockY() &&
                        t.getBlockY() <= p2.getBlockY() ||
                        t.getBlockY() <= p1.getBlockY() &&
                                t.getBlockY() >= p2.getBlockY());
    }

    /**
     * Checks if the target location is inside the area formed by point1 and point2.
     * @param target The location to check that may be inside the area of this portal.
     * @return If the target location was inside the cuboid formed by point1 and point2.
     */
    public boolean isInArea(Location target) {
        return isInArea(target, getPoint1(), getPoint2());
    }

    public Location getPoint2() {
        return point2;
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public boolean isEntry() {
        return isEntry;
    }

    public boolean isExit() {
        return !isEntry;
    }

    public boolean shouldCancelEvent() {
        return cancelEvent;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public Location getPoint1() {
        return point1;
    }

    public Float getDelaySeconds() {
        return delaySeconds;
    }

    public void afterDelay(Runnable ranAfterDelaySeconds) {
        if (delaySeconds != 0)
            DungeonMod.getPlugin().getServer().getScheduler()
                .runTaskLater(DungeonMod.getPlugin(), ranAfterDelaySeconds,(long) (delaySeconds * 20));
        else
            ranAfterDelaySeconds.run();
    }

    /**
     * Sends the player through the portal, using the proper delay and other effects as specified in the config.
     * @param player The player that is going through the portal, or has "triggered" this portal.
     * @param event The event that resulted in the triggering of this portal.
     */
    public void trigger(Player player, Cancellable event) {
        afterDelay(() -> player.teleport(getSpawnPoint()));
        if (shouldCancelEvent())
            event.setCancelled(true);
    }

    private Location point1;
    private Location point2;
    private Location spawnPoint;
    private boolean isEntry;
    private boolean cancelEvent;
    private Dungeon dungeon;
    private Float delaySeconds;
}
