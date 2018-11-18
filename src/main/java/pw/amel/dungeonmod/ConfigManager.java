package pw.amel.dungeonmod;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * Parses the config to a convent getter interface.
 */
public class ConfigManager {
    private static FileConfiguration config;

    private ConfigManager() {}

    public static void reload() {
        DungeonMod.getPlugin().saveDefaultConfig();
        DungeonMod.getPlugin().reloadConfig();
        config = DungeonMod.getPlugin().getConfig();

        doDungeonsSection(config);
        doPortalsSection(config);
    }

    private static void doDungeonsSection(ConfigurationSection config) {
        ConfigurationSection dungeonsSection = config.getConfigurationSection("dungeons");
        dungeonsSection.getValues(false).forEach((dungeon, v) -> {
            ConfigurationSection dungeonConfig = (ConfigurationSection) v;
            String type = dungeonConfig.getString("type", "DecayDungeon");
            if (!(type.equals("PersistentDungeon") || type.equals("DecayDungeon"))) {
                DungeonMod.getPlugin().getLogger().log(Level.WARNING, dungeonConfig.getCurrentPath() + ".type is neither" +
                        " PersistentDungeon nor DecayDungeon. Defaulting to DecayDungeon.");
                type = "DecayDungeon";
            }

            float spawnX = (float) dungeonConfig.getDouble("spawnX", 2);
            float spawnY = (float) dungeonConfig.getDouble("spawnY", 2);
            float spawnZ = (float) dungeonConfig.getDouble("spawnZ", 2);
            Location dungeonSpawn = new Location(null, spawnX, spawnY, spawnZ);

            float exitX = (float) dungeonConfig.getDouble("exitX", 0);
            float exitY = (float) dungeonConfig.getDouble("exitY", 128);
            float exitZ = (float) dungeonConfig.getDouble("exitZ", 0);
            String exitWorld = dungeonConfig.getString("exitWorld", "world");

            int maxX = dungeonConfig.getInt("maxX");
            int maxY = dungeonConfig.getInt("maxY");
            int maxZ = dungeonConfig.getInt("maxZ");

            boolean generateBedrockBox = dungeonConfig.getBoolean("generateBedrockBox", true);

            Location dungeonExit = new Location(DungeonMod.getPlugin().getServer().getWorld(exitWorld), exitX, exitY, exitZ);

            if (type.equals("PersistentDungeon")) {
                dungeons.put(dungeon, new PersistentDungeon(dungeonSpawn, dungeonExit, dungeon, generateBedrockBox, maxX, maxY, maxZ));
            } else if (type.equals("DecayDungeon")) {
                int variance = dungeonConfig.getInt("breakTimeVarianceSeconds", 10);
                int avgTime = dungeonConfig.getInt("breakAvgTimeSeconds", 10);

                dungeons.put(dungeon, new DecayDungeon(dungeonSpawn, dungeonExit, dungeon,
                        generateBedrockBox,variance * 20, avgTime * 20, maxX, maxY, maxZ));
            }
        });
    }

    private static void doPortalsSection(ConfigurationSection rootConfig) {
        ConfigurationSection portalsSection = rootConfig.getConfigurationSection("portals");
        portalsSection.getValues(false).forEach((name, v) -> {
            ConfigurationSection config = (ConfigurationSection) v;

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
            Dungeon dungeon = getDungeon(dungeonName);
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
            boolean isEntry = entryOrExit.equals("entry");

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

            Location point1 = new Location(world, x1, y1, z1);
            Location point2 = new Location(world, x2, y2, z2);

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

            Location spawnLocation = !useDungeonSpawnExit ? new Location(spawnWorld, spawnX, spawnY, spawnZ) :
                    (isEntry ? dungeon.getSpawnLocation() : dungeon.getExitLocation());

            boolean cancelEvent = config.getBoolean("cancelEvent", false);

            String type = config.getString("type");
            if (type == null) {
                DungeonMod.getPlugin().getLogger().log(Level.SEVERE, config.getCurrentPath() +
                        ".type is not set.");
                throw new IllegalArgumentException();
            }

            PortalConstructor constructor = portalConstructors.get(type);
            if (constructor == null) {
                DungeonMod.getPlugin().getLogger().log(Level.SEVERE, type + " is not a valid portal type.");
                throw new IllegalArgumentException();
            }

            constructor.newPortal(name, point1, point2, spawnLocation, isEntry, cancelEvent,dungeon, config);
        });
    }

    private static HashMap<String, PortalConstructor> portalConstructors = new HashMap<>();

    private static HashMap<String, Dungeon> dungeons = new HashMap<>();

    public static Dungeon getDungeon(String name) {
        if (name == null)
            return null;
        name = name.replaceAll("^dungeon_", "");
        return dungeons.get(name);
    }

    public static void addPortalConstructor(String type, PortalConstructor constructor) {
        portalConstructors.put(type, constructor);
    }

    @FunctionalInterface
    public interface PortalConstructor {
        void newPortal(String name,
                       Location point1, Location point2, Location spawnPoint,
                       boolean isEntry, boolean cancelEvent,
                       Dungeon dungeon, ConfigurationSection config);
    }
}
