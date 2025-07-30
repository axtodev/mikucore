package com.mikucore.commands;

import com.mikucore.MikuCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffChatCommand implements CommandExecutor {
    private final MikuCore plugin;
    private final Set<UUID> staffChatToggled = new HashSet<>();
    private final String PREFIX = ChatColor.translateAlternateColorCodes('&', "&8[&cStaff&8] ");

    public StaffChatCommand(MikuCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use staff chat!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("mikucore.staff")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use staff chat!");
            return true;
        }

        if (args.length == 0) {
            // Toggle staff chat mode
            if (staffChatToggled.contains(player.getUniqueId())) {
                staffChatToggled.remove(player.getUniqueId());
                player.sendMessage(PREFIX + ChatColor.GRAY + "Staff chat disabled. Your messages will now go to public chat.");
            } else {
                staffChatToggled.add(player.getUniqueId());
                player.sendMessage(PREFIX + ChatColor.GREEN + "Staff chat enabled. Your messages will now be sent to staff only.");
            }
            return true;
        }

        // Send message to staff chat
        sendStaffMessage(player, String.join(" ", args));
        return true;
    }

    public void sendStaffMessage(Player sender, String message) {
        String formattedMessage = String.format("%s%s%s: %s",
            PREFIX,
            ChatColor.WHITE,
            sender.getName(),
            message);

        // Send to all online staff members
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("mikucore.staff")) {
                player.sendMessage(formattedMessage);
            }
        }

        // Log to console
        Bukkit.getConsoleSender().sendMessage(formattedMessage);
    }

    public boolean isInStaffChat(Player player) {
        return staffChatToggled.contains(player.getUniqueId());
    }

    public void toggleStaffChat(Player player, boolean enabled) {
        if (enabled) {
            staffChatToggled.add(player.getUniqueId());
        } else {
            staffChatToggled.remove(player.getUniqueId());
        }
    }
}
