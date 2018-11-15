package pw.amel.dungeonmod;

import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class DecayDungeon extends Dungeon {
    @SuppressWarnings("deprecation")
    public DecayDungeon(Location spawnLocation, Location exitLocation,
                        String name, File schematic,
                        int avgTime, int variance,
                        int maxX, int maxY, int maxZ) throws IOException {
        super(spawnLocation, exitLocation, name, maxX, maxY, maxZ);
        DungeonMod.getPlugin().getLogger().log(Level.INFO, "Building dungeon " + name);
        buildDungeon(schematic, new Location(dungeonWorld, 0, 0, 0));
        buildDungeon(schematic, new Location(dungeonWorld, 0 - getMaxX(), 0, 0));
        DungeonMod.getPlugin().getLogger().log(Level.INFO, "Finished building dungeon " + name);
        new DecayListener(this, avgTime, variance, getMaxX());
    }
}
