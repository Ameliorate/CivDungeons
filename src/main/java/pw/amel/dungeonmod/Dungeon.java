package pw.amel.dungeonmod;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

public abstract class Dungeon {
    /**
     * Creates a new dungeon, loading it from disc.
     */
    public Dungeon(Location spawnLocation, Location exitLocation, String name, World.Environment environment, int maxX, int maxY, int maxZ) {
        this.spawnLocation = spawnLocation;
        this.exitLocation = exitLocation;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.environment = environment;

        WorldCreator worldCreator = new WorldCreator("dungeon_" + name);
        worldCreator.generator(new VoidGenerator());
        worldCreator.generateStructures(false);
        worldCreator.environment(environment);
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
    private World.Environment environment;

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public World getDungeonWorld() {
        return dungeonWorld;
    }

    public Location getExitLocation() {
        return exitLocation.clone();
    }

    public Location getSpawnLocation() {
        return spawnLocation.clone();
    }

    public void teleportPlayerToSpawn(Player player) {
        player.teleport(getSpawnLocation());
    }

    public void teleportPlayerToExit(Player player) {
        player.teleport(getExitLocation());
    }

    public void teleportPlayerToTemplate(Player player) {
        teleportPlayerToSpawn(player);
    }

    public World.Environment getEnvironment() {
        return environment;
    }
}
