package pw.amel.dungeonmod;

import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import pw.amel.dungeonmod.blockcopy.CopyThing;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

class DecayListener implements Listener {
    DecayListener(DecayDungeon dungeon, int avgTime, int variance, int blockOffset) {
        this.dungeon = dungeon;
        this.avgTime = avgTime;
        this.variance = variance;
        this.blockOffset = blockOffset;

    }

    private DecayDungeon dungeon;
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

            CopyThing.copyBlock(template, dest);
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

            placed.getWorld().playEffect(placed.getLocation(), Effect.STEP_SOUND, placed.getType(), 10);
            placed.breakNaturally(null); // break with an empty hand

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

    private HashSet<UUID> entityLock = new HashSet<>();

    public void entityPlace(Entity placed) {
        UUID placedUUID = placed.getUniqueId();

        if (placed.getLocation().getWorld() != dungeon.dungeonWorld)
            return;
        if (placed.getLocation().getX() < 0)
            return;
        if (entityLock.contains(placedUUID)) {
            return;
        }
        entityLock.add(placedUUID);

        boolean kill = placed instanceof Damageable && !(placed instanceof Wither) && !(placed instanceof ArmorStand);
        // If the wither is killed before its invuln period is over, it won't be killed.
        // We also don't want to no-effort drop the nether star.
        // Not that most this matters, since this is a civserver and withers are usually disabled.

        // Armor stands are damagable but can't be killed.

        ItemStack drop = null;
        if (placed instanceof Boat) {
            drop = new ItemStack(Material.BOAT, 1);
        } else if (placed instanceof ArmorStand) {
            drop = new ItemStack(Material.ARMOR_STAND, 1);
        } else if (placed instanceof Painting) {
            drop = new ItemStack(Material.PAINTING, 1);
        }
        final ItemStack finalDrop = drop; // has to be final to use in a lambda.

        int semiDelay = ThreadLocalRandom.current().nextInt(-variance, variance);
        int delay = semiDelay + avgTime;

        DungeonMod.getPlugin().getServer().getScheduler().runTaskLater(DungeonMod.getPlugin(), () -> {
            Entity placedNow = getEntityWithUUID(placedUUID);
            if (placedNow == null)
                return;

            if (kill) {
                ((Damageable) placedNow).damage(Integer.MAX_VALUE);
            } else {
                placedNow.remove();
            }
            if (finalDrop != null)
                dungeon.getDungeonWorld().dropItemNaturally(placedNow.getLocation(), finalDrop);
            entityLock.remove(placedUUID);
        }, delay);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntitySpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM)
            return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT)
            return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER)
            return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL)
            return;

        entityPlace(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntitySpawn(EntitySpawnEvent event) {
        if ( event.getEntityType() != EntityType.ENDER_CRYSTAL &&
                event.getEntityType() != EntityType.ARMOR_STAND)
            return;

        entityPlace(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onVehicleCreate(VehicleCreateEvent event) {
        if (!CopyThing.isCopyingEntity())
            entityPlace(event.getVehicle());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onHangingPlace(HangingPlaceEvent event) {
        entityPlace(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEndCrystal(PlayerInteractEvent event) {
        // Hack because there's just plain no event from placing a end crystal.
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getMaterial() == Material.END_CRYSTAL) {
                DungeonMod.getPlugin().getServer().getScheduler().runTask(DungeonMod.getPlugin(), () -> {
                    List<Entity> entities = event.getPlayer().getNearbyEntities(4, 4, 4);
                    for (Entity entity : entities) {
                        if (entity.getType() == EntityType.ENDER_CRYSTAL) {
                            EnderCrystal crystal = (EnderCrystal) entity;
                            Block belowCrystal = crystal.getLocation().getBlock().getRelative(BlockFace.DOWN);

                            if (event.getClickedBlock().equals(belowCrystal)) {
                                entityPlace(crystal);
                                break;
                            }
                        }
                    }
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onSlimeSplit(SlimeSplitEvent event) {
        entityPlace(event.getEntity());
    }

    /**
     * Prevents a recursive infinite loop when a vehicle has passengers.
     */
    private HashSet<UUID> vehicularLock = new HashSet<>();

    private Entity getEntityWithUUID(UUID uuid) {
        List<Entity> results = dungeon.getDungeonWorld().getEntities().stream()
                .filter((e) -> e.getUniqueId().equals(uuid))
                .collect(Collectors.toList());
        assert results.size() == 1 || results.size() == 0;
        if (results.isEmpty())
            return null;
        else
            return results.get(0);
    }

    public void entityBreak(Entity broken) {
        UUID brokenUUID = broken.getUniqueId();

        if (broken.getLocation().getWorld() != dungeon.dungeonWorld)
            return;
        if (broken.getLocation().getX() < 0)
            return;
        if (entityLock.contains(brokenUUID))
            return;

        if (!vehicularLock.contains(brokenUUID)) {
            vehicularLock.add(brokenUUID);
            if (broken.getVehicle() != null)
                entityBreak(broken.getVehicle());
            if (!broken.getPassengers().isEmpty())
                broken.getPassengers().forEach(this::entityBreak);
            vehicularLock.remove(brokenUUID);
        }

        if (!dungeon.clonesToTemplates.containsKey(brokenUUID))
            return;

        entityLock.add(brokenUUID);

        int semiDelay = ThreadLocalRandom.current().nextInt(-variance, variance);
        int delay = semiDelay + avgTime;

        DungeonMod.getPlugin().getServer().getScheduler().runTaskLater(DungeonMod.getPlugin(), () -> {
            Entity template = getEntityWithUUID(dungeon.clonesToTemplates.get(brokenUUID));
            if (template == null) {
                entityLock.remove(brokenUUID);
                dungeon.clonesToTemplates.remove(brokenUUID);
                return;
            }

            Entity newClone = CopyThing.copyEntity(template, dungeon.templateLocationToMainDungeon(template.getLocation()));
            if (newClone == null) {
                entityLock.remove(brokenUUID);
                dungeon.clonesToTemplates.remove(brokenUUID);
                return;
            }

            Entity brokenNow = getEntityWithUUID(brokenUUID);
            if (brokenNow != null)
                brokenNow.remove();

            entityLock.remove(brokenUUID);
            dungeon.clonesToTemplates.remove(brokenUUID);
            dungeon.clonesToTemplates.put(newClone.getUniqueId(), template.getUniqueId());
        }, delay);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onCreeperPower(CreeperPowerEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityCombust(EntityCombustEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityDamage(EntityDamageEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityDeath(EntityDeathEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityExplode(EntityExplodeEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityPickupItem(EntityPickupItemEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityRegainHealth(EntityRegainHealthEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityResurrect(EntityResurrectEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityTame(EntityTameEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityTeleport(EntityTeleportEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityUnleash(EntityUnleashEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPigZap(PigZapEvent event) {
        entityBreak(event.getEntity());
        entityPlace(event.getPigZombie());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onSheepDyeWool(SheepDyeWoolEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onSheepRegrowWool(SheepRegrowWoolEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onVillagerReplenishTrade(VillagerReplenishTradeEvent event) {
        entityBreak(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        entityBreak(event.getRightClicked());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onVehicleDamage(VehicleDamageEvent event) {
        entityBreak(event.getVehicle());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onVehicleDestroy(VehicleDestroyEvent event) {
        entityBreak(event.getVehicle());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onVehicleEnter(VehicleEnterEvent event) {
        entityBreak(event.getVehicle());
        entityBreak(event.getEntered());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onVehicleExit(VehicleExitEvent event) {
        entityBreak(event.getVehicle());
        entityBreak(event.getExited());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onHangingBreak(HangingBreakEvent event) {
        entityBreak(event.getEntity());
    }
}
