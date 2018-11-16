package pw.amel.dungeonmod;

import pw.amel.dungeonmod.blockcopy.CopyBlock;
import pw.amel.dungeonmod.blockcopy.InventoryCopier;
import pw.amel.dungeonmod.blockcopy.MetaCopier;
import pw.amel.dungeonmod.blockcopy.TypeCopier;
import org.bukkit.plugin.java.JavaPlugin;
import pw.amel.dungeonmod.command.ConstructionHelmetTeleport;
import pw.amel.dungeonmod.command.DungeonEdit;

public class DungeonMod extends JavaPlugin {
    private static DungeonMod plugin;

    @Override
    public void onEnable() {
        plugin = this;

        CopyBlock.addBlockCopier(new TypeCopier(), 1);
        CopyBlock.addBlockCopier(new MetaCopier(), 2);
        CopyBlock.addBlockCopier(new InventoryCopier(), 3);

        getCommand("dungeonedit").setExecutor(new DungeonEdit());
        getServer().getPluginManager().registerEvents(new ConstructionHelmetTeleport(), this);

        ConfigManager.reload();
    }

    public static DungeonMod getPlugin() {
        return plugin;
    }
}
