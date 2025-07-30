package com.mikucore.commands;

import com.mikucore.MikuCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlaytimeCommand implements CommandExecutor {
    private final MikuCore plugin;

    public PlaytimeCommand(MikuCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can check their own playtime!");
                return true;
            }
            
            Player player = (Player) sender;
            showPlaytime(sender, player.getUniqueId(), player.getName());
            return true;
        }

        // Check for other player's playtime
        if (!sender.hasPermission("mikucore.playtime.others")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to check others' playtime!");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target != null) {
            showPlaytime(sender, target.getUniqueId(), target.getName());
        } else {
            // Check offline players
            @SuppressWarnings("deprecation")
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (offlinePlayer.hasPlayedBefore()) {
                showPlaytime(sender, offlinePlayer.getUniqueId(), offlinePlayer.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found!");
            }
        }
        
        return true;
    }

    private void showPlaytime(CommandSender sender, UUID playerId, String playerName) {
        long seconds = plugin.getPlaytime().getOrDefault(playerId, 0L);
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        String timeString = String.format(
            "%d days, %d hours, %d minutes",
            days, hours % 24, minutes % 60
        );

        sender.sendMessage(ChatColor.GOLD + "Playtime for " + playerName + ": " + 
            ChatColor.WHITE + timeString);
            
        // Check if player meets staff requirements
        if (sender.hasPermission("mikucore.staff")) {
            long requiredMinutes = plugin.getConfig().getLong("playtime.required-for-staff", 10080);
            if (minutes < requiredMinutes) {
                long remaining = requiredMinutes - minutes;
                long remainingHours = remaining / 60;
                long remainingMinutes = remaining % 60;
                
                sender.sendMessage(ChatColor.YELLOW + "This player needs " + 
                    remainingHours + " hours and " + remainingMinutes + 
                    " more minutes of playtime to be eligible for staff.");
            } else {
                sender.sendMessage(ChatColor.GREEN + "This player meets the minimum playtime requirement for staff!");
            }
        }
    }
}
