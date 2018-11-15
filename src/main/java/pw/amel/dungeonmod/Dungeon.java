package pw.amel.dungeonmod;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public abstract class Dungeon {
    /**
     * Creates a new dungeon, loading it from disc.
     */
    public Dungeon(Location spawnLocation, Location exitLocation, String name, int maxX, int maxY, int maxZ) {
        this.spawnLocation = spawnLocation;
        this.exitLocation = exitLocation;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;

        WorldCreator worldCreator = new WorldCreator("dungeon_" + name);
        worldCreator.generator(new VoidGenerator());
        dungeonWorld = DungeonMod.getPlugin().getServer().createWorld(worldCreator);

        if (spawnLocation.getWorld() == null) {
            spawnLocation.setWorld(dungeonWorld);
        }

        DungeonMod.getPlugin().getServer().getPluginManager().registerEvents(new DungeonWorldBorder(this), DungeonMod.getPlugin());
    }

    protected World dungeonWorld;

    private Location spawnLocation;
    private Location exitLocation;
    private int maxX;
    private int maxY;
    private int maxZ;

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public void teleportPlayerToSpawn(Player player) {
        player.teleport(spawnLocation);
    }

    public void teleportPlayerToExit(Player player) {
        player.teleport(exitLocation);
    }

    /**
     * Builds the dungeon from a schematic, deleting the old dungeon if needed.
     */
    @SuppressWarnings("deprecation")
    protected void buildDungeon(File schematic, Location location) throws IOException, DataException {
        SchematicFormat schematicFormat = SchematicFormat.getFormat(schematic);
        CuboidClipboard paste = schematicFormat.load(schematic);

        EditSession session = new EditSession(BukkitUtil.getLocalWorld(dungeonWorld), -1);
        try {
            paste.paste(session, new Vector(location.getX(), location.getY(), location.getZ()), false);
        } catch (MaxChangedBlocksException e) {
            throw new AssertionError("MaxChangedBlocks is supposed to be infinity", e);
        }
    }
}
