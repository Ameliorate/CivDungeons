package com.civwizardry.dungeonmod.blockcopy;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * Used for copying blocks and all their metadata, such as vanilla metadata, inventories, and citidel reinforcments.
 */
public interface BlockCopier {
    /**
     * Called in the middle of copying the block. The implementer should copy the data from template to dest.
     * @param template What the block should be, but in the wrong position.
     *                 No property of this should be changed while copying.
     * @param dest The destination of the block. Currently contains the data of what the block originally was,
     *             unless it was changed by previous steps.
     */
    void copy(Block template, BlockState dest);
}
