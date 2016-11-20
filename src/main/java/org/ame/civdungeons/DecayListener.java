package org.ame.civdungeons;

import org.ame.civdungeons.blockcopy.CopyBlock;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class DecayListener implements Listener {
    DecayListener(Main mainPlugin, Dungeon dungeon, int avgTime, int variance, int blockOffset) {
        this.mainPlugin = mainPlugin;
        this.dungeon = dungeon;
        this.avgTime = avgTime;
        this.variance = variance;
        this.blockOffset = blockOffset;

        mainPlugin.getServer().getPluginManager().registerEvents(this, mainPlugin);
    }

    private Main mainPlugin;
    private Dungeon dungeon;
    private int avgTime;
    private int variance;
    private int blockOffset;

    private HashSet<Location> blockLock = new HashSet<>();

    private void handleBlockBreak(Block broken) {
        if (broken.getLocation().getWorld() != dungeon.dungeonWorld) {
            return;
        } else if (blockLock.contains(broken.getLocation())) {
            return;
        }

        blockLock.add(broken.getLocation());

        int semiDelay = new Random().nextInt(variance) - (variance / 2);
        int delay = semiDelay + avgTime;
        final BlockState dest = broken.getState();  // Array to get around lambda issues.

        mainPlugin.getServer().getScheduler().runTaskLater(mainPlugin, () -> {
            Block template =
                    new Location(dungeon.dungeonWorld, broken.getX() - blockOffset, broken.getY(), broken.getZ())
                    .getBlock();

            CopyBlock.copyBlock(template, dest);
            dest.getWorld().playEffect(dest.getLocation(), Effect.STEP_SOUND, dest.getType(), 10);

            blockLock.remove(dest.getLocation());
        }, delay);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        handleBlockBreak(event.getBlock());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onExplosionBlockBreak(BlockExplodeEvent event) {
        event.blockList().forEach(this::handleBlockBreak);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onExplosionBlockBreak(EntityExplodeEvent event) {
        event.blockList().forEach(this::handleBlockBreak);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockBurn(BlockBurnEvent event) {
        handleBlockBreak(event.getBlock());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockFade(BlockFadeEvent event) {
        handleBlockBreak(event.getBlock());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
        handleBlockBreak(event.getBlock());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onLeavesDecay(LeavesDecayEvent event) {
        handleBlockBreak(event.getBlock());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockDispense(BlockDispenseEvent event) {
        handleBlockBreak(event.getBlock());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        handleBlockBreak(event.getSource().getLocation().getBlock());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory() == null) {
            return;
        } else if (event.getInventory().getName().equals("container.inventory")) {
            return;
        }

        handleBlockBreak(event.getInventory().getLocation().getBlock());
    }

    private void handleBlockPlace(Block placed) {
        if (placed.getLocation().getWorld() != dungeon.dungeonWorld) {
            return;
        } else if (blockLock.contains(placed.getLocation())) {
            return;
        }

        blockLock.add(placed.getLocation());

        int semiDelay = ThreadLocalRandom.current().nextInt(-variance, variance);
        int delay = semiDelay + avgTime;

        BlockState placedCopy = placed.getState();
        Collection<ItemStack> drops = placed.getDrops();

        mainPlugin.getServer().getScheduler().runTaskLater(mainPlugin, () -> {
            if (placedCopy.getBlock().getType() == Material.AIR) {
                return;
            }

            placedCopy.getWorld().playEffect(placed.getLocation(), Effect.STEP_SOUND, placedCopy.getType(), 10);
            placedCopy.setType(Material.AIR);
            placedCopy.update(true);

            for (ItemStack drop : drops) {
                placedCopy.getWorld().dropItemNaturally(placedCopy.getLocation(), drop);
            }

            blockLock.remove(placedCopy.getLocation());
        }, delay);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        handleBlockPlace(event.getBlock());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBucketPlace(PlayerBucketEmptyEvent event) {
        Location clicked = event.getBlockClicked().getLocation();
        BlockFace face = event.getBlockFace();
        Location block = clicked.add(face.getModX(), face.getModY(), face.getModZ());
        handleBlockPlace(block.getBlock());
    }
}
