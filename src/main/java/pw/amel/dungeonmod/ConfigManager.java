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

            constructor.newPortal(config);
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
        void newPortal(ConfigurationSection config);
    }
}
