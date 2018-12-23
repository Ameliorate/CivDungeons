package pw.amel.dungeonmod.blockcopy;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.TileEntity;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Painting;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import pw.amel.dungeonmod.DungeonMod;

import java.util.logging.Level;

/**
 * Static class to copy blocks from a template.
 */
public class CopyThing {
    @SuppressWarnings("deprecation")
    public static void copyBlock(Block from, Block to) {
        to.setTypeIdAndData(from.getTypeId(), from.getData(), true);
        CraftWorld cw = (CraftWorld) from.getWorld();
        TileEntity teFrom = cw.getTileEntityAt(from.getX(), from.getY(), from.getZ());
        TileEntity teTo = cw.getTileEntityAt(to.getX(), to.getY(), to.getZ());
        if (teFrom == null)
            return;
        if (teTo == null)
            throw new AssertionError("Expected teFrom!=null&&toTo!=null implicitly.");
        NBTTagCompound fromNBT = teFrom.d();
        NBTTagCompound toNBT = (NBTTagCompound) fromNBT.clone();
        toNBT.setInt("x", to.getX());
        toNBT.setInt("y", to.getY());
        toNBT.setInt("z", to.getZ());
        teTo.load(toNBT);
        teTo.update();

        from = from.getLocation().getBlock();
        to = to.getLocation().getBlock();
        BlockState fromState = from.getState();
        BlockState toState = to.getState();

        if (fromState instanceof InventoryHolder) {
            InventoryHolder fromHolder = (InventoryHolder) fromState;
            if (fromHolder.getInventory() instanceof DoubleChestInventory) {
                DungeonMod.getPlugin().getLogger().log(Level.WARNING, "Double Chests don't work with dungeon mod. " +
                        "Chest @ " + fromState.getLocation());
            } else {
                ((InventoryHolder) toState).getInventory().setContents(((InventoryHolder) fromState).getInventory().getContents());
            }
        }
    }

    private static boolean isCopyingEntity = false;

    public static boolean isCopyingEntity() {
        return isCopyingEntity;
    }

    /**
     * Spawns an exact copy of the given entity at the given location
     *
     * If the entity has any other entities riding on top of it, all of those entities will be copied too.
     * @param from The old entity to be used as a template. This entity will not be killed or altered in any way.
     * @param to Where the new entity will be spawned
     * @return The newly spawned entity
     */
    public static Entity copyEntity(Entity from, Location to) {
        if (from instanceof Painting) {
            DungeonMod.getPlugin().getLogger().log(Level.WARNING, "Tried to copy painting to " + to +
                    " from " + from.getLocation() + ". DungeonMod does not support paintings.");
            return null;
        }

        isCopyingEntity = true;
        CraftWorld cw = (CraftWorld) from.getLocation().getWorld();
        CraftEntity en = (CraftEntity) from;
        NBTTagCompound nbt = en.getHandle().save(new NBTTagCompound());

        CraftEntity spawnedEn = (CraftEntity) cw.spawn(to, from.getClass());
        NBTTagCompound nbtSpawned = spawnedEn.getHandle().save(new NBTTagCompound());
        long uuidH = nbtSpawned.getLong("UUIDMost"); // We save the UUID here because copying over the template
        long uuidL = nbtSpawned.getLong("UUIDLeast"); // entity will also cause weird duplicate UUID issues in the rest of the code.
        spawnedEn.getHandle().f(nbt); // load from nbt
        spawnedEn.teleport(to);

        nbtSpawned = spawnedEn.getHandle().save(new NBTTagCompound());
        nbtSpawned.setLong("UUIDMost", uuidH);
        nbtSpawned.setLong("UUIDLeast", uuidL);
        spawnedEn.getHandle().f(nbtSpawned);

        if (!from.getPassengers().isEmpty()) {
            for (Entity rider : from.getPassengers()) {
                Entity newRider = copyEntity(rider, to);
                assert newRider != null;
                spawnedEn.addPassenger(newRider);
            }
        }

        isCopyingEntity = false;
        return spawnedEn;
    }
}
