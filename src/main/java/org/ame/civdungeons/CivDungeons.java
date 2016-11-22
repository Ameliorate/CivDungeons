package org.ame.civdungeons;

import org.ame.civdungeons.blockcopy.CopyBlock;
import org.ame.civdungeons.blockcopy.InventoryCopier;
import org.ame.civdungeons.blockcopy.MetaCopier;
import org.ame.civdungeons.blockcopy.TypeCopier;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

        ConfigManager.reload();

        CopyBlock.addBlockCopier(new TypeCopier(), 1);
        CopyBlock.addBlockCopier(new MetaCopier(), 2);
        CopyBlock.addBlockCopier(new InventoryCopier(), 3);
    }

    public static CivDungeons getPlugin() {
        return plugin;
    }


}
