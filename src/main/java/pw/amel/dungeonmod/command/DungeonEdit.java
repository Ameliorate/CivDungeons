package pw.amel.dungeonmod.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pw.amel.dungeonmod.*;

import java.util.ArrayList;

public class DungeonEdit implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1)
            return false;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        Player senderPlayer = (Player) sender;
        if (senderPlayer.getInventory().getHelmet() != null) {
            sender.sendMessage(ChatColor.RED + "Please remove your helmet before editing any dungeons.");
            sender.sendMessage(ChatColor.RED + "Editing dungeons requires you wear a magically-summoned hard hat");
            return true;
        }

        String dungeonName = args[0];
        Dungeon dungeon = DungeonMod.getConfigManager().getDungeon(dungeonName);
        if (dungeon == null) {
            sender.sendMessage(ChatColor.RED + "Please enter a valid dungeon.");
            return true;
        }

        ItemStack constructionHelmet = new ItemStack(Material.GOLD_HELMET);
        ItemMeta meta = constructionHelmet.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Construction Helmet");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("This helmet prevents you from being teleported out of the dungeon.");
        lore.add("Remove it to exit the dungeon.");
        meta.setLore(lore);
        constructionHelmet.setItemMeta(meta);
        senderPlayer.getInventory().setHelmet(constructionHelmet);

        dungeon.teleportPlayerToTemplate(senderPlayer);

        sender.sendMessage(ChatColor.BLUE + "[DungeonMod] " + ChatColor.RESET + "You may remove your helmet to exit the dungeon at any time.");
        return true;
    }
}
