package pw.amel.dungeonmod;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import pw.amel.dungeonmod.blockcopy.CopyBlock;

import java.util.logging.Level;


public class DecayDungeon extends Dungeon {
    public DecayDungeon(Location spawnLocation, Location exitLocation,
                        String name, boolean generateBedrockBox,
                        int avgTime, int variance,
                        int maxX, int maxY, int maxZ) {
        super(spawnLocation, exitLocation, name, maxX, maxY, maxZ);
        new DecayListener(this, avgTime, variance, getMaxX());
        DungeonMod.getPlugin().getLogger().log(Level.INFO, "Building dungeon " + name);
        if (dungeonWorld.getBlockAt(-50, 50, -50).getType() == Material.AIR) {
            if (generateBedrockBox) {
                DungeonMod.getPlugin().getLogger().log(Level.INFO, "Building bedrock box");
                // Start building a box around the dungeon template area.
                for (int x = -getMaxX() - 1; x <= -1; x++) {
                    for (int z = 0; z <= getMaxZ(); z++) {
                        new Location(dungeonWorld, x, 0, z).getBlock().setType(Material.BEDROCK);
                        new Location(dungeonWorld, x, getMaxY(), z).getBlock().setType(Material.BEDROCK);
                    }
                }

                for (int y = 0; y <= getMaxY(); y++) {
                    for (int x = -getMaxX() - 1; x <= -1; x++) {
                        new Location(dungeonWorld, x, y, 0).getBlock().setType(Material.BEDROCK);
                        new Location(dungeonWorld, x, y, getMaxZ()).getBlock().setType(Material.BEDROCK);
                    }
                    for (int z = 0; z <= getMaxZ(); z++) {
                        new Location(dungeonWorld, -1, y, z).getBlock().setType(Material.BEDROCK);
                        new Location(dungeonWorld, -getMaxX() - 1, y, z).getBlock().setType(Material.BEDROCK);
                    }
                }
                // Finish building the box.
            } else {
                // Something for the player to stand on.
                getSpawnLocation().add(-getMaxX() -1, -1, 0).getBlock().setType(Material.BEDROCK);
            }

            new Location(dungeonWorld, -50, 50, -50).getBlock().setType(Material.BEDROCK);
            // ^^^ Store the fact that the box has already been built before.
            DungeonMod.getPlugin().getLogger().log(Level.INFO, "Finished building bedrock box");
        }
        rebuild();
        DungeonMod.getPlugin().getLogger().log(Level.INFO, "Finished building dungeon " + name);
    }

    @Override
    public void teleportPlayerToTemplate(Player player) {
        Location templateSpawn = getSpawnLocation().clone();
        templateSpawn.setX(templateSpawn.getX() - getMaxX());
        player.teleport(templateSpawn);
    }

    /**
     * rebuild() reports back with a percentage complete every BLOCKS_PER_REPORT when copying over the
     * dungeon from the template.
     */
    private static final int BLOCKS_PER_REPORT = 100_000;

    public void rebuild() {
        long blocksToCopy = (long) getMaxX() * (long) getMaxY() * (long) getMaxZ();
        long blocksPerSlice = (long) getMaxZ() * (long) getMaxY();
        long blocksCopied = 0;
        long blocksCopiedAtLastReport = 0;

        DungeonMod.getPlugin().getLogger().log(Level.INFO, "Copying " + blocksToCopy + " blocks...");

        for (int x = 0; x <= getMaxX(); x++) {
            for (int y = 0; y <= getMaxY(); y++) {
                for (int z = 0; z <= getMaxZ(); z++) {
                    Location fromLoc = new Location(dungeonWorld, x - getMaxX() - 1, y, z);
                    Block fromBlock = fromLoc.getBlock();
                    Location to = new Location(dungeonWorld, x, y, z);
                    CopyBlock.copyBlock(fromBlock, to.getBlock().getState());
                }
            }

            if (blocksCopied - blocksCopiedAtLastReport > BLOCKS_PER_REPORT) {
                DungeonMod.getPlugin().getLogger().log(Level.INFO, ((float) blocksCopied / blocksToCopy) * 100 + "% done.");
                blocksCopiedAtLastReport = blocksCopied;
            }
            blocksCopied += blocksPerSlice;
        }

        DungeonMod.getPlugin().getLogger().log(Level.INFO, "Finished copying " + blocksToCopy + " blocks.");
    }
}
