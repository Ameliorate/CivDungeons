package pw.amel.dungeonmod.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.amel.dungeonmod.ConfigManager;
import pw.amel.dungeonmod.Dungeon;
import pw.amel.dungeonmod.DungeonMod;

public class DungeonEnter implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args.length > 2)
            return false;
        if (args.length == 1 && !(sender instanceof Player))
            return false;
        // Normally there'd be something "You need to be a Player to use this command", but with this command
        // the person behind the keyboard could figure it out.
        if (args.length == 2 && !sender.hasPermission("dungeonmod.commandenter.other")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to send other players to dungeons.");
            return true;
        }

        String dungeonName = args[0];
        String playerName = args.length == 2 ? args[1] : null;
        Player player = args.length == 1 ? (Player) sender : DungeonMod.getPlugin().getServer().getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.`");
        }
        Dungeon dungeon = ConfigManager.getDungeon(dungeonName);
        if (dungeon == null) {
            sender.sendMessage(ChatColor.RED + "Dungeon not found.");
            return true;
        }

        dungeon.teleportPlayerToSpawn(player);
        return true;
    }
}
