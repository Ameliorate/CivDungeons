package com.civwizardry.dungeonmod.blockcopy;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class TypeCopier implements BlockCopier {
    @Override
    public void copy(Block template, BlockState dest) {
        dest.setType(template.getType());
    }
}
