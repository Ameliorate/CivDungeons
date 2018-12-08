package pw.amel.dungeonmod;

import org.bukkit.plugin.java.JavaPlugin;
import pw.amel.dungeonmod.command.*;
import pw.amel.dungeonmod.portal.BlockBreakPortal;
import pw.amel.dungeonmod.portal.BlockInteractPortal;
import pw.amel.dungeonmod.portal.EntityDamagePortal;
import pw.amel.dungeonmod.portal.PlayerMovePortal;

public class DungeonMod extends JavaPlugin {
    private static DungeonMod plugin;
    private static ConfigManager configManager;

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
        EntityDamagePortal entityDamagePortal = new EntityDamagePortal();
        getServer().getPluginManager().registerEvents(entityDamagePortal, this);

        configManager = new ConfigManager();

        configManager.addPortalConstructor("BlockInteract", blockInteractPortal);
        configManager.addPortalConstructor("BlockBreak", blockBreakPortal);
        configManager.addPortalConstructor("PlayerMove", playerMovePortal);
        configManager.addPortalConstructor("EntityDamage", entityDamagePortal);

        configManager.reload();
    }

    public static DungeonMod getPlugin() {
        return plugin;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }
}
