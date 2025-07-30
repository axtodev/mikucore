package com.mikucore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnnounceCommand implements CommandExecutor {
    private final String PREFIX = ChatColor.translateAlternateColorCodes('&', "&8[&6Announcement&8] ");
    private final String LINE = ChatColor.translateAlternateColorCodes('&', "&7&m----------------------------------------------------");
    private final String PERMISSION = "mikucore.announce";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /announce <message>");
            return true;
        }

        String message = String.join(" ", args);
        broadcastAnnouncement(message, sender);
        return true;
    }

    private void broadcastAnnouncement(String message, CommandSender sender) {
        // Format the announcement
        String formattedMessage = String.format("%s%s%s",
            PREFIX,
            ChatColor.YELLOW,
            message);

        // Send the announcement to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(LINE);
            player.sendMessage(formattedMessage);
            player.sendMessage(LINE);
            
            // Play a sound effect (if the player has sounds enabled)
            try {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            } catch (NoSuchFieldError e) {
                // Sound not available in this version
            }
        }

        // Log to console
        String logMessage = String.format("[ANNOUNCEMENT] %s: %s", 
            sender.getName(), 
            message);
        Bukkit.getConsoleSender().sendMessage(logMessage);
    }
}
