package pw.amel.dungeonmod;

import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import pw.amel.dungeonmod.blockcopy.CopyBlock;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class DecayListener implements Listener {
    DecayListener(Dungeon dungeon, int avgTime, int variance, int blockOffset) {
        this.dungeon = dungeon;
        this.avgTime = avgTime;
        this.variance = variance;
        this.blockOffset = blockOffset;

    }

    private Dungeon dungeon;
    private int avgTime;
    private int variance;
    private int blockOffset;

    private HashMap<Location, Boolean> blockLockSilent = new HashMap<>();

    public void handleBlockBreak(Block broken, boolean silent) {
        if (broken.getLocation().getWorld() != dungeon.dungeonWorld) {
            return;
        } else if (blockLockSilent.get(broken.getLocation()) != null) {
            if (blockLockSilent.get(broken.getLocation()) && !silent)
                blockLockSilent.put(broken.getLocation(), false);
            return;
        } else if (broken.getLocation().getX() < 0) {
            return;
        }

        blockLockSilent.put(broken.getLocation(), silent);

        int semiDelay = new Random().nextInt(variance) - (variance / 2);
        int delay = semiDelay + avgTime;
        final Block dest = broken;

        DungeonMod.getPlugin().getServer().getScheduler().runTaskLater(DungeonMod.getPlugin(), () -> {
            Plugin citadel = DungeonMod.getPlugin().getServer().getPluginManager().getPlugin("Citadel");
            if (citadel != null) {
                ReinforcementManager manager = Citadel.getReinforcementManager();
                Reinforcement reinforcement = manager.getReinforcement(dest);
                if (reinforcement != null) {
                    manager.deleteReinforcement(reinforcement);
                }
            }

            Block template =
                    new Location(dungeon.dungeonWorld, broken.getX() - blockOffset - 1, broken.getY(), broken.getZ())
                            .getBlock();

            boolean loud = !blockLockSilent.get(broken.getLocation()) ||
                    dest.getLocation().getBlock().getType() == Material.AIR;
            if (template.getType() == Material.AIR && dest.getType() == Material.AIR)
                loud = false;

            CopyBlock.copyBlock(template, dest);
            if (loud)
                dest.getWorld().playEffect(dest.getLocation(), Effect.STEP_SOUND, template.getType(), 10);

            blockLockSilent.remove(dest.getLocation());

            DungeonMod.getPlugin().getServer().getScheduler().runTask(DungeonMod.getPlugin(), () -> {
                if (dest.getLocation().getBlock().getType() != template.getType()) {
                    handleBlockBreak(dest, silent);
                }
            });
        }, delay);
    }

    private void handleBreakSilent(Block broken) {
        handleBlockBreak(broken, false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        handleBlockBreak(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplosionBlockBreak(BlockExplodeEvent event) {
        event.blockList().forEach(this::handleBreakSilent);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplosionBlockBreak(EntityExplodeEvent event) {
        event.blockList().forEach(this::handleBreakSilent);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        handleBlockBreak(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        handleBlockBreak(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
        handleBlockBreak(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        handleBlockBreak(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        handleBlockBreak(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        handleBlockBreak(event.getSource().getLocation().getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory() == null) {
            return;
        } else if (event.getInventory().getName().equals("container.inventory")) {
            return;
        } else if (event.getInventory().getLocation() == null) {
            return;
        }

        handleBlockBreak(event.getInventory().getLocation().getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        DungeonMod.getPlugin().getServer().getScheduler().runTask(DungeonMod.getPlugin(),
                () -> handleBlockBreak(event.getClickedBlock(), true));
        // This is done a tick later because placing a block will trigger this event before running the BlockPlaceEvent,
        // causing it to be handled as a "break" rather than a "place".
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() == null) {
            return;
        } else if (event.getInventory().getName().equals("container.inventory")) {
            return;
        } else if (event.getInventory().getLocation() == null) {
            return;
        }

        handleBlockBreak(event.getInventory().getLocation().getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() == null) {
            return;
        } else if (event.getInventory().getName().equals("container.inventory")) {
            return;
        } else if (event.getInventory().getLocation() == null) {
            return;
        }

        handleBlockBreak(event.getInventory().getLocation().getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory() == null) {
            return;
        } else if (event.getInventory().getName().equals("container.inventory")) {
            return;
        } else if (event.getInventory().getLocation() == null) {
            return;
        }

        handleBlockBreak(event.getInventory().getLocation().getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRedstone(BlockRedstoneEvent event) {
        ArrayList<Block> blocks = new ArrayList<>();
        Block blk = event.getBlock();

        blocks.add(blk.getRelative(BlockFace.NORTH));
        blocks.add(blk.getRelative(BlockFace.EAST));
        blocks.add(blk.getRelative(BlockFace.SOUTH));
        blocks.add(blk.getRelative(BlockFace.WEST));
        blocks.add(blk.getRelative(BlockFace.DOWN));
        blocks.add(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH));
        blocks.add(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST));
        blocks.add(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH));
        blocks.add(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST));

        blocks.forEach((block) -> handleBlockBreak(block, block.getType() != Material.TNT));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.AIR)
            return;
        if (event.getBlock().getType() == Material.WATER || event.getBlock().getType() == Material.STATIONARY_WATER)
            return;
        if (event.getBlock().getType() == Material.LAVA || event.getBlock().getType() == Material.STATIONARY_LAVA)
            return;

        DungeonMod.getPlugin().getServer().getScheduler().runTask(DungeonMod.getPlugin(),
                () -> handleBlockBreak(event.getBlock(), true));
        // This is done a tick later because placing a block will trigger this event before running the BlockPlaceEvent,
        // causing it to be handled as a "break" rather than a "place".
    }

    public void handleBlockPlace(Block placed) {
        if (placed.getLocation().getWorld() != dungeon.dungeonWorld) {
            return;
        } else if (blockLockSilent.get(placed.getLocation()) != null) {
            return;
        } else if (placed.getLocation().getX() < 0) {
            return;
        }

        blockLockSilent.put(placed.getLocation(), false);

        int semiDelay = ThreadLocalRandom.current().nextInt(-variance, variance);
        int delay = semiDelay + avgTime;

        DungeonMod.getPlugin().getServer().getScheduler().runTaskLater(DungeonMod.getPlugin(), () -> {
            Plugin citadel = DungeonMod.getPlugin().getServer().getPluginManager().getPlugin("Citadel");
            if (citadel != null) {
                ReinforcementManager manager = Citadel.getReinforcementManager();
                Reinforcement reinforcement = manager.getReinforcement(placed);
                if (reinforcement != null)
                    manager.deleteReinforcement(reinforcement);
            }

            if (placed.getLocation().getBlock().getType() == Material.AIR) {
                return;
            }

            placed.breakNaturally(null); // break with an empty hand
            placed.getWorld().playEffect(placed.getLocation(), Effect.STEP_SOUND, placed.getType(), 10);

            blockLockSilent.remove(placed.getLocation());
        }, delay);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        handleBlockPlace(event.getBlock());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBucketPlace(PlayerBucketEmptyEvent event) {
        Location clicked = event.getBlockClicked().getLocation();
        BlockFace face = event.getBlockFace();
        Location block = clicked.add(face.getModX(), face.getModY(), face.getModZ());
        handleBlockPlace(block.getBlock());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityBlockChange(EntityChangeBlockEvent event) {
        handleBlockPlace(event.getBlock());
    }
}
