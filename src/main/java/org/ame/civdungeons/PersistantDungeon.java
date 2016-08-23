package org.ame.civdungeons;

import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;

/**
 * A kind of dungeon where blocks placed inside never reset.
 */
public class PersistantDungeon extends Dungeon {
    @SuppressWarnings("deprecation")
    public PersistantDungeon(Location spawnLocation, Location exitLocation,
                             String name, File schematic, Main mainPlugin) throws IOException, DataException {
        super(spawnLocation, exitLocation, name,
                SchematicFormat.getFormat(schematic).load(schematic).getWidth(),   // Blame java, you can't even
                SchematicFormat.getFormat(schematic).load(schematic).getHeight(),  // have variables before calling
                SchematicFormat.getFormat(schematic).load(schematic).getLength(),  // super.
                mainPlugin);
        if (dungeonWorld.getBlockAt(-50, 50, -50).getType() == Material.AIR) {
            buildDungeon(schematic);
            new Location(dungeonWorld, -50, 50, -50).getBlock().setType(Material.BEDROCK);
        }
    }
}
