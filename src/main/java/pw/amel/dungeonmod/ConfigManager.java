package pw.amel.dungeonmod;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
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

        ConfigurationSection dungeonsSection = config.getConfigurationSection("dungeons");

        for (String dungeon : dungeonsSection.getKeys(false)) {
            String type = dungeonsSection.getString(dungeon + ".type", "DecayDungeon");
            if (!(type.equals("PersistentDungeon") || type.equals("DecayDungeon"))) {
                DungeonMod.getPlugin().getLogger().log(Level.WARNING, "dungeons." + dungeon + ".type is neither" +
                        " PersistentDungeon nor DecayDungeon. Defaulting to DecayDungeon.");
                type = "DecayDungeon";
            }

            float spawnX = (float) dungeonsSection.getDouble(dungeon + ".spawnX", 2);
            float spawnY = (float) dungeonsSection.getDouble(dungeon + ".spawnY", 2);
            float spawnZ = (float) dungeonsSection.getDouble(dungeon + ".spawnZ", 2);
            Location dungeonSpawn = new Location(null, spawnX, spawnY, spawnZ);

            float exitX = (float) dungeonsSection.getDouble(dungeon + ".exitX", 0);
            float exitY = (float) dungeonsSection.getDouble(dungeon + ".exitY", 128);
            float exitZ = (float) dungeonsSection.getDouble(dungeon + ".exitZ", 0);
            String exitWorld = dungeonsSection.getString(dungeon + ".exitWorld", "world");

            int maxX = dungeonsSection.getInt(dungeon + ".maxX");
            int maxY = dungeonsSection.getInt(dungeon + ".maxY");
            int maxZ = dungeonsSection.getInt(dungeon + ".maxZ");

            Location dungeonExit = new Location(DungeonMod.getPlugin().getServer().getWorld(exitWorld), exitX, exitY, exitZ);

            if (type.equals("PersistentDungeon")) {
                dungeons.put(dungeon, new PersistentDungeon(dungeonSpawn, dungeonExit, dungeon, maxX, maxY, maxZ));
            } else if (type.equals("DecayDungeon")) {
                int variance = dungeonsSection.getInt(dungeon + ".breakTimeVarianceSeconds", 10);
                int avgTime = dungeonsSection.getInt(dungeon + ".breakAvgTimeSeconds", 10);

                dungeons.put(dungeon, new DecayDungeon(dungeonSpawn, dungeonExit, dungeon,
                        variance * 20, avgTime * 20, maxX, maxY, maxZ));
            }
        }
    }

    private static HashMap<String, Dungeon> dungeons = new HashMap<>();

    public static Dungeon getDungeon(String name) {
        name = name.replaceAll("^dungeon_", "");
        return dungeons.get(name);
    }
}