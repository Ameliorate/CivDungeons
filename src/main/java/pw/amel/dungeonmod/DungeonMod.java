package pw.amel.dungeonmod;

import org.bukkit.plugin.java.JavaPlugin;
import pw.amel.dungeonmod.command.*;
import pw.amel.dungeonmod.portal.BlockBreakPortal;
import pw.amel.dungeonmod.portal.BlockInteractPortal;
import pw.amel.dungeonmod.portal.PlayerMovePortal;

public class DungeonMod extends JavaPlugin {
    private static DungeonMod plugin;

    @Override
    public void onEnable() {
        plugin = this;

        getCommand("dungeonreload").setExecutor(new DungeonReload());
        getCommand("dungeonedit").setExecutor(new DungeonEdit());
        getCommand("dungeonenter").setExecutor(new DungeonEnter());
        getCommand("dungeonexit").setExecutor(new DungeonExit());
        getCommand("dungeonconfigreload").setExecutor(new ConfigReload());

        getServer().getPluginManager().registerEvents(new ConstructionHelmetTeleport(), this);
        BlockInteractPortal blockInteractPortal = new BlockInteractPortal();
        getServer().getPluginManager().registerEvents(blockInteractPortal, this);
        BlockBreakPortal blockBreakPortal = new BlockBreakPortal();
        getServer().getPluginManager().registerEvents(blockBreakPortal, this);
        PlayerMovePortal playerMovePortal = new PlayerMovePortal();
        getServer().getPluginManager().registerEvents(playerMovePortal, this);

        ConfigManager.addPortalConstructor("BlockInteract", blockInteractPortal);
        ConfigManager.addPortalConstructor("BlockBreak", blockBreakPortal);
        ConfigManager.addPortalConstructor("PlayerMove", playerMovePortal);

        ConfigManager.reload();
    }

    public static DungeonMod getPlugin() {
        return plugin;
    }
}
