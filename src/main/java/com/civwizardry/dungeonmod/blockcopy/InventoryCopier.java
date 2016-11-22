package com.civwizardry.dungeonmod.blockcopy;

import org.bukkit.block.*;

public class InventoryCopier implements BlockCopier {
    @Override
    public void copy(Block template, BlockState dest) {
        if (template.getState() instanceof Chest) {
            Chest destChest = (Chest) dest;
            Chest templateChest = (Chest) template.getState();

            destChest.getBlockInventory().setContents(templateChest.getInventory().getContents());
        } else if (template.getState() instanceof Furnace) {
            Furnace destChest = (Furnace) dest;
            Furnace templateChest = (Furnace) template.getState();

            destChest.getInventory().setContents(templateChest.getInventory().getContents());
        } else if (template.getState() instanceof Dropper) {
            Dropper destChest = (Dropper) dest;
            Dropper templateChest = (Dropper) template.getState();

            destChest.getInventory().setContents(templateChest.getInventory().getContents());
        } else if (template.getState() instanceof Hopper) {
            Hopper destChest = (Hopper) dest;
            Hopper templateChest = (Hopper) template.getState();

            destChest.getInventory().setContents(templateChest.getInventory().getContents());
        } else if (template.getState() instanceof BrewingStand) {
            BrewingStand destChest = (BrewingStand) dest;
            BrewingStand templateChest = (BrewingStand) template.getState();

            destChest.getInventory().setContents(templateChest.getInventory().getContents());
        } else if (template.getState() instanceof Sign) {
            Sign destChest = (Sign) dest;
            Sign templateChest = (Sign) template.getState();

            int i = 0;
            for (String line : templateChest.getLines()) {
                destChest.setLine(i, line);
                i++;
            }
        } else if (template.getState() instanceof Banner) {
            Banner destChest = (Banner) dest;
            Banner templateChest = (Banner) template.getState();

            destChest.setPatterns(templateChest.getPatterns());
        }
    }
}
