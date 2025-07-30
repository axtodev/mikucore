package com.mikucore.commands;

import com.mikucore.MikuCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MessageCommand implements CommandExecutor {
    private final MikuCore plugin;

    public MessageCommand(MikuCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use private messages!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
            return true;
        }

        Player from = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (target == from) {
            sender.sendMessage(ChatColor.RED + "You cannot message yourself!");
            return true;
        }

        // Build the message
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();

        if (message.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "You cannot send an empty message!");
            return true;
        }

        // Format and send messages
        String format = plugin.getConfig().getString("messages.message-format", "&7[&e%from% &7-> &e%to%&7] &f%message%");
        
        // To sender
        String toSender = format
            .replace("%from%", "You")
            .replace("%to%", target.getName())
            .replace("%message%", message);
        
        // To receiver
        String toReceiver = format
            .replace("%from%", from.getName())
            .replace("%to%", "You")
            .replace("%message%", message);
        
        from.sendMessage(ChatColor.translateAlternateColorCodes('&', toSender));
        target.sendMessage(ChatColor.translateAlternateColorCodes('&', toReceiver));
        
        // Update last messenger for /r command
        plugin.getLastMessenger().put(target.getUniqueId(), from.getUniqueId());
        
        // Notify staff with social spy permission
        String spyFormat = plugin.getConfig().getString("messages.socialspy-format", "&7[&cSPY&7] [&e%from% &7-> &e%to%&7] &f%message%");
        String spyMessage = spyFormat
            .replace("%from%", from.getName())
            .replace("%to%", target.getName())
            .replace("%message%", message);
        
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("mikucore.socialspy") && staff != from && staff != target) {
                staff.sendMessage(ChatColor.translateAlternateColorCodes('&', spyMessage));
            }
        }
        
        return true;
    }
}
