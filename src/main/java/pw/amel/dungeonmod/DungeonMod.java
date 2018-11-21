package pw.amel.dungeonmod;

import org.bukkit.Location;
import pw.amel.dungeonmod.blockcopy.CopyBlock;
import pw.amel.dungeonmod.blockcopy.InventoryCopier;
import pw.amel.dungeonmod.blockcopy.MetaCopier;
import pw.amel.dungeonmod.blockcopy.TypeCopier;
import org.bukkit.plugin.java.JavaPlugin;
import pw.amel.dungeonmod.command.*;
import pw.amel.dungeonmod.portal.BlockBreakPortal;
import pw.amel.dungeonmod.portal.BlockInteractPortal;

public class DungeonMod extends JavaPlugin {
    private static DungeonMod plugin;

    @Override
    public void onEnable() {
        plugin = this;

        CopyBlock.addBlockCopier(new TypeCopier(), 1);
        CopyBlock.addBlockCopier(new MetaCopier(), 2);
        CopyBlock.addBlockCopier(new InventoryCopier(), 3);

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

        ConfigManager.addPortalConstructor("BlockInteract", blockInteractPortal);
        ConfigManager.addPortalConstructor("BlockBreak", blockBreakPortal);

        ConfigManager.reload();
    }

    public static DungeonMod getPlugin() {
        return plugin;
    }

    /**
     * Checks if a location is inside the two other locations
     * @param target The target location that is expected to be inside the cuboid formed by the other two points.
     * @param point1 One of the points that forms the cuboid that target is expected to be inside.
     * @param point2 The other of two points that forms the cuboid that the target may be inside.
     * @return If target was inside the cuboid that is formed by point1 and point2.
     */
    public static boolean isInArea(Location target, Location point1, Location point2){
        Location t = target;
        Location p1 = point1;
        Location p2 = point2;
        // Don't try to mind the source code too much. I got it from the internet and it should work.
        return p1.getWorld().getName().equals(p2.getWorld().getName()) &&
                t.getWorld().getName().equals(p1.getWorld().getName()) &&
                (t.getBlockX() >= p1.getBlockX() &&
                        t.getBlockX() <= p2.getBlockX() ||
                        t.getBlockX() <= p1.getBlockX() &&
                                t.getBlockX() >= p2.getBlockX()) &&
                (t.getBlockZ() >= p1.getBlockZ() &&
                        t.getBlockZ() <= p2.getBlockZ() ||
                        t.getBlockZ() <= p1.getBlockZ() &&
                                t.getBlockZ() >= p2.getBlockZ()) &&
                (t.getBlockY() >= p1.getBlockY() &&
                        t.getBlockY() <= p2.getBlockY() ||
                        t.getBlockY() <= p1.getBlockY() &&
                                t.getBlockY() >= p2.getBlockY());
    }
}
