package org.ame.civdungeons;

import com.sk89q.worldedit.data.DataException;
import org.ame.civdungeons.blockcopy.CopyBlock;
import org.ame.civdungeons.blockcopy.InventoryCopier;
import org.ame.civdungeons.blockcopy.MetaCopier;
import org.ame.civdungeons.blockcopy.TypeCopier;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class CivDungeons extends JavaPlugin {
    private static CivDungeons plugin;

    @Override
    public void onEnable() {
        plugin = this;

        File testDungeonFile = new File(getDataFolder().getAbsolutePath() + File.separator + "schematics" +
                File.separator + "test.schematic");
        if (!testDungeonFile.exists()) {
            InputStream testDungeon = getClassLoader().getResourceAsStream("test.schematic");
            Path testDungeonPath = testDungeonFile.toPath();
            try {
                Files.copy(testDungeon, testDungeonPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        saveDefaultConfig();
        Configuration config = getConfig();

        ConfigurationSection dungeons = config.getConfigurationSection("dungeons");

        CopyBlock.addBlockCopier(new TypeCopier(), 1);
        CopyBlock.addBlockCopier(new MetaCopier(), 2);
        CopyBlock.addBlockCopier(new InventoryCopier(), 3);

        for (String dungeon : dungeons.getKeys(false)) {
            String type = dungeons.getString(dungeon + ".type");
            if (!(type.equals("PersistentDungeon") || type.equals("DecayDungeon"))) {
                throw new ConfigurationException("config.dungeons.testdungeon.type must be PersistentDungeon or DecayDungeon");
            }

            float spawnX = (float) dungeons.getDouble(dungeon + ".spawnX");
            float spawnY = (float) dungeons.getDouble(dungeon + ".spawnY");
            float spawnZ = (float) dungeons.getDouble(dungeon + ".spawnZ");
            Location dungeonSpawn = new Location(null, spawnX, spawnY, spawnZ);

            float exitX = (float) dungeons.getDouble(dungeon + ".exitX");
            float exitY = (float) dungeons.getDouble(dungeon + ".exitY");
            float exitZ = (float) dungeons.getDouble(dungeon + ".exitZ");
            String exitWorld = dungeons.getString(dungeon + ".exitWorld");

            Location dungeonExit = new Location(getServer().getWorld(exitWorld), exitX, exitY, exitZ);
            String schematic = dungeons.getString(dungeon + ".schematic");
            File schematicFile = new File(getDataFolder().getAbsolutePath() + File.separator + "schematics" +
                    File.separator + schematic + ".schematic");

            try {
                if (type.equals("PersistentDungeon")) {
                    this.dungeons.put(dungeon, new PersistentDungeon(dungeonSpawn, dungeonExit, dungeon,
                            schematicFile));
                } else if (type.equals("DecayDungeon")) {
                    int variance = dungeons.getInt(dungeon + ".breakTimeVarianceSeconds");
                    int avgTime = dungeons.getInt(dungeon + ".breakAvgTimeSeconds");

                    this.dungeons.put(dungeon, new DecayDungeon(dungeonSpawn, dungeonExit, dungeon, schematicFile,
                            variance * 20, avgTime * 20));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (@SuppressWarnings("deprecation") DataException e) {
                e.printStackTrace();
            }
        }
    }

    public static CivDungeons getPlugin() {
        return plugin;
    }

    public HashMap<String, Dungeon> dungeons = new HashMap<>();
}
