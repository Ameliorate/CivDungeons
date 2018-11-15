package pw.amel.dungeonmod;

import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * A kind of dungeon where blocks placed inside never reset.
 */
public class PersistentDungeon extends Dungeon {
    public PersistentDungeon(Location spawnLocation, Location exitLocation,
                             String name, File schematic,
                             int maxX, int maxY, int maxZ) throws IOException {
        super(spawnLocation, exitLocation, name, maxX, maxY, maxZ);
        if (dungeonWorld.getBlockAt(-50, 50, -50).getType() == Material.AIR) {
            DungeonMod.getPlugin().getLogger().log(Level.INFO, "Building dungeon " + name);
            buildDungeon(schematic, new Location(dungeonWorld, 0, 0, 0));
            new Location(dungeonWorld, -50, 50, -50).getBlock().setType(Material.BEDROCK);
            DungeonMod.getPlugin().getLogger().log(Level.INFO, "Finished building dungeon " + name);
        }
    }
}
