package pw.amel.dungeonmod;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.logging.Level;

/**
 * A kind of dungeon where blocks placed inside never reset.
 */
public class PersistentDungeon extends Dungeon {
    public PersistentDungeon(Location spawnLocation, Location exitLocation, String name, boolean generateBedrockBox,
                             int maxX, int maxY, int maxZ) {
        super(spawnLocation, exitLocation, name, maxX, maxY, maxZ);
        DungeonMod.getPlugin().getLogger().log(Level.INFO, "Building dungeon " + name);
        if (dungeonWorld.getBlockAt(-50, 50, -50).getType() == Material.AIR) {
            if (generateBedrockBox) {
                // Start building a box around the dungeon template area.
                for (int x = 0; x <= getMaxX(); x++) {
                    for (int z = 0; z <= getMaxZ(); z++) {
                        new Location(dungeonWorld, x, 0, z).getBlock().setType(Material.BEDROCK);
                        new Location(dungeonWorld, x, getMaxY(), z).getBlock().setType(Material.BEDROCK);
                    }
                }

                for (int y = 0; y <= getMaxY(); y++) {
                    for (int x = 0; x <= getMaxX(); x++) {
                        new Location(dungeonWorld, x, y, 0).getBlock().setType(Material.BEDROCK);
                        new Location(dungeonWorld, x, y, getMaxZ()).getBlock().setType(Material.BEDROCK);
                    }
                    for (int z = 0; z <= maxZ; z++) {
                        new Location(dungeonWorld, 0, y, z).getBlock().setType(Material.BEDROCK);
                        new Location(dungeonWorld, getMaxX(), y, z).getBlock().setType(Material.BEDROCK);
                    }
                }
                // Finish building the box.
            } else {
                // Build something for the player to stand on.
                getSpawnLocation().add(0, -1, 0).getBlock().setType(Material.BEDROCK);
            }

            new Location(dungeonWorld, -50, 50, -50).getBlock().setType(Material.BEDROCK);
            // ^^^ Store the fact that the box has already been built before.
        }
        DungeonMod.getPlugin().getLogger().log(Level.INFO, "Finished building dungeon " + name);
    }
}
