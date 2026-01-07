package net.copokbl.takingTurnz;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TakeTurnzCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("takingturnz.use")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        int turnTime;
        try {
            turnTime = Integer.parseInt(args[0]);
            if (turnTime <= 0) {
                sender.sendMessage("Turn time must be a positive integer.");
                return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid turn time format. Please enter a valid integer.");
            return false;
        }

        List<Player> people = new ArrayList<>();
        if (args.length > 1 && args[1].equalsIgnoreCase("random")) {
            people.addAll(Bukkit.getOnlinePlayers());
            Collections.shuffle(people);
        } else {
            for (String arg : Arrays.stream(args).skip(1).toList()) {
                Player p = Bukkit.getPlayer(arg);
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "Player " + arg + " not found.");
                    return false;
                }
                people.add(p);
            }
        }

        Game game = new Game(turnTime, people);
        game.start();
        return false;
    }
}
