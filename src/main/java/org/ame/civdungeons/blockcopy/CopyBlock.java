package org.ame.civdungeons.blockcopy;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.ArrayList;

/**
 * Static class to dynamically copy blocks from a template.
 */
public class CopyBlock {
    private static ArrayList<BlockCopier> copiers = new ArrayList<>();

    /**
     * Add a blockcopier to be called while copying a block.
     */
    public static void addBlockCopier(BlockCopier copier) {
        copiers.add(copier);
    }

    /**
     * Add a blockcopier to be called while copying a block.
     * @param step What step to call the copier. Lower numbers are called first.
     *             By default, 1 copies block ids, 2 copies metadata, and 3 copies inventories and the such.
     *             All other copiers are unset, and using a too large id will add it to the end of the list.
     */
    public static void addBlockCopier(BlockCopier copier, int step) {
        if (step > copiers.size()) {
            copiers.add(copier);
        } else {
            copiers.add(step - 1, copier);
        }
    }

    public static void copyBlock(Block template, BlockState dest) {
        for (BlockCopier copier : copiers) {
            copier.copy(template, dest);
            dest.update(true);
            dest = dest.getBlock().getState();
            // Certain parts of a blockstate aren't updated if you don't do this.
            // For example, if `state.setType(Materials.CHEST)` is called,
            // `state.getType() == Materials.Chest` is true but `(Chest) state` throws an exception.
        }
    }
}
