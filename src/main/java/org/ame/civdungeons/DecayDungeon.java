package org.ame.civdungeons;

import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class DecayDungeon extends Dungeon {
    @SuppressWarnings("deprecation")
    public DecayDungeon(Location spawnLocation, Location exitLocation,
                        String name, File schematic, CivDungeons mainPlugin,
                        int avgTime, int variance) throws IOException, DataException {
        super(spawnLocation, exitLocation, name,
                SchematicFormat.getFormat(schematic).load(schematic).getWidth(),   // Blame java, you can't even
                SchematicFormat.getFormat(schematic).load(schematic).getHeight(),  // have variables before calling
                SchematicFormat.getFormat(schematic).load(schematic).getLength(),  // super.
                mainPlugin);
        mainPlugin.getLogger().log(Level.INFO, "Building dungeon " + name);
        buildDungeon(schematic, new Location(dungeonWorld, 0, 0, 0));
        buildDungeon(schematic, new Location(dungeonWorld, 0 - getMaxX(), 0, 0));
        mainPlugin.getLogger().log(Level.INFO, "Finished building dungeon " + name);
        new DecayListener(mainPlugin, this, avgTime, variance, getMaxX());
    }
}
