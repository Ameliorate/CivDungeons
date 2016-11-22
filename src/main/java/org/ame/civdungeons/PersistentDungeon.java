package org.ame.civdungeons;

import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * A kind of dungeon where blocks placed inside never reset.
 */
public class PersistentDungeon extends Dungeon {
    @SuppressWarnings("deprecation")
    public PersistentDungeon(Location spawnLocation, Location exitLocation,
                             String name, File schematic, CivDungeons mainPlugin) throws IOException, DataException {
        super(spawnLocation, exitLocation, name,
                SchematicFormat.getFormat(schematic).load(schematic).getWidth(),   // Blame java, you can't even
                SchematicFormat.getFormat(schematic).load(schematic).getHeight(),  // have variables before calling
                SchematicFormat.getFormat(schematic).load(schematic).getLength(),  // super.
                mainPlugin);
        if (dungeonWorld.getBlockAt(-50, 50, -50).getType() == Material.AIR) {
            mainPlugin.getLogger().log(Level.INFO, "Building dungeon " + name);
            buildDungeon(schematic, new Location(dungeonWorld, 0, 0, 0));
            new Location(dungeonWorld, -50, 50, -50).getBlock().setType(Material.BEDROCK);
            mainPlugin.getLogger().log(Level.INFO, "Finished building dungeon " + name);
        }
    }
}
