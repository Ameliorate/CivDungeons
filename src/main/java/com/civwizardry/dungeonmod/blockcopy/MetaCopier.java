package com.civwizardry.dungeonmod.blockcopy;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class MetaCopier implements BlockCopier {
    @Override
    public void copy(Block template, BlockState dest) {
        dest.setData(template.getState().getData());
    }
}
