package pw.amel.dungeonmod.portal;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import pw.amel.dungeonmod.ConfigManager;
import pw.amel.dungeonmod.Dungeon;
import pw.amel.dungeonmod.DungeonMod;

import java.util.logging.Level;

public abstract class PortalData {
    public PortalData(ConfigurationSection config) {
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

        String dungeonName = config.getString("dungeon");
        dungeon = ConfigManager.getDungeon(dungeonName);
        if (dungeon == null) {
            DungeonMod.getPlugin().getLogger().log(Level.SEVERE, config.getCurrentPath() +
                    ".dungeon is not a valid dungeon.");
            throw new IllegalArgumentException();
        }

        String entryOrExit = config.getString("entryOrExit", "entry");
        if ((!entryOrExit.equals("entry")) && (!entryOrExit.equals("exit"))) {
            DungeonMod.getPlugin().getLogger().log(Level.WARNING, config.getCurrentPath() +
                    ".entryOrExit is not 'entry' or 'exit'. Defaulting to entry.");
            entryOrExit = "entry";
        }
        isEntry = entryOrExit.equals("entry");

        if (worldName != null && !isEntry) {
            DungeonMod.getPlugin().getLogger().log(Level.WARNING, config.getCurrentPath() +
                    ".world and .dungeon are both defined and .entryOrExit is 'exit'. Is this a mistake? Defaulting to .dungeon");
        }

        World world;
        if (worldName != null && isEntry) {
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

        int spawnX = config.getInt("spawnX", Integer.MAX_VALUE);
        int spawnY = config.getInt("spawnY", Integer.MAX_VALUE);
        int spawnZ = config.getInt("spawnZ", Integer.MAX_VALUE);
        String spawnWorldName = config.getString("spawnWorld");

        boolean warn = true;
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

        spawnPoint = !useDungeonSpawnExit ? new Location(spawnWorld, spawnX, spawnY, spawnZ) :
                (isEntry ? dungeon.getSpawnLocation() : dungeon.getExitLocation());

        cancelEvent = config.getBoolean("cancelEvent", false);
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

    private Location point1;
    private Location point2;
    private Location spawnPoint;
    private boolean isEntry;
    private boolean cancelEvent;
    private Dungeon dungeon;

}
