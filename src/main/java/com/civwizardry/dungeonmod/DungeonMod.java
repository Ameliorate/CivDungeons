package com.civwizardry.dungeonmod;

import com.civwizardry.dungeonmod.blockcopy.CopyBlock;
import com.civwizardry.dungeonmod.blockcopy.InventoryCopier;
import com.civwizardry.dungeonmod.blockcopy.MetaCopier;
import com.civwizardry.dungeonmod.blockcopy.TypeCopier;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DungeonMod extends JavaPlugin {
    private static DungeonMod plugin;

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

    public static DungeonMod getPlugin() {
        return plugin;
    }


}
