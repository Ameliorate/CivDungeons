package pw.amel.dungeonmod.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import pw.amel.dungeonmod.ConfigManager;
import pw.amel.dungeonmod.DecayDungeon;
import pw.amel.dungeonmod.Dungeon;

public class DungeonReload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }
        String dungeonName = args[0];
        Dungeon dungeon = ConfigManager.getDungeon(dungeonName);
        if (dungeon == null) {
            sender.sendMessage(ChatColor.RED + "Dungeon not found.");
            return true;
        }
        if (!(dungeon instanceof DecayDungeon)) {
            sender.sendMessage(ChatColor.RED + "That dungeon is not a DecayDungeon. This command is only useful for DecayDungeons.");
            return true;
        }
        sender.sendMessage(ChatColor.BLUE + "Rebuilding dungeon...");
        ((DecayDungeon) dungeon).rebuild();
        sender.sendMessage(ChatColor.BLUE + "Finished rebuilding dungeon.");
        return true;
    }
}
