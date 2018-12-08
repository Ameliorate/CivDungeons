package pw.amel.dungeonmod.blockcopy;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.TileEntity;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import pw.amel.dungeonmod.DungeonMod;

import java.util.logging.Level;

/**
 * Static class to copy blocks from a template.
 */
public class CopyBlock {
    @SuppressWarnings("deprecation")
    public static void copyBlock(Block from, Block to) {
        to.setTypeIdAndData(from.getTypeId(), from.getData(), false);
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
}
