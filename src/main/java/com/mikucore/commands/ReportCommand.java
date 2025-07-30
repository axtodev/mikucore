package com.mikucore.commands;

import com.mikucore.MikuCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportCommand implements CommandExecutor {
    private final MikuCore plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final FileConfiguration config;

    public ReportCommand(MikuCore plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player reporter = (Player) sender;

        if (args.length < 2) {
            reporter.sendMessage(ChatColor.RED + "Usage: /report <player> <reason>");
            return true;
        }

        // Check cooldown
        if (hasCooldown(reporter)) {
            long cooldownTime = config.getLong("report.cooldown", 300);
            long timeLeft = getCooldown(reporter);
            String message = config.getString("messages.report-cooldown", "&cYou must wait %seconds% seconds before reporting again.")
                    .replace("%seconds%", String.valueOf(timeLeft));
            reporter.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        Player reported = Bukkit.getPlayer(args[0]);
        if (reported == null) {
            reporter.sendMessage(ChatColor.RED + "Player not found or is offline!");
            return true;
        }

        if (reporter.equals(reported)) {
            reporter.sendMessage(ChatColor.RED + "You cannot report yourself!");
            return true;
        }

        // Build the reason
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // Send the report to online staff
        sendReport(reporter, reported, reason);
        
        // Set cooldown
        setCooldown(reporter);
        
        // Send confirmation to reporter
        String confirmation = config.getString("messages.report-sent", "&aYour report has been sent to online staff members.");
        reporter.sendMessage(ChatColor.translateAlternateColorCodes('&', confirmation));
        
        return true;
    }

    private void sendReport(Player reporter, Player reported, String reason) {
        String reportFormat = config.getString("messages.report-format", 
            "&8[&cReport&8] &7%reporter% reported %reported%: &f%reason%");
        
        String message = reportFormat
            .replace("%reporter%", reporter.getName())
            .replace("%reported%", reported.getName())
            .replace("%reason%", reason);
            
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        // Send to all online staff members
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("mikucore.staff.notify")) {
                player.sendMessage(message);
                
                // Play a sound effect for staff
                try {
                    player.playSound(player.getLocation(), 
                        org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 
                        1.0f, 1.0f);
                } catch (Exception e) {
                    // Sound not available in this version
                }
            }
        }
        
        // Log to console
        plugin.getLogger().info(String.format("[REPORT] %s reported %s: %s", 
            reporter.getName(), reported.getName(), reason));
    }
    
    private boolean hasCooldown(Player player) {
        if (player.hasPermission("mikucore.staff")) {
            return false; // Staff bypass cooldown
        }
        
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return false;
        }
        
        long cooldownTime = config.getLong("report.cooldown", 300) * 1000; // Convert to milliseconds
        long timeElapsed = System.currentTimeMillis() - cooldowns.get(player.getUniqueId());
        
        return timeElapsed < cooldownTime;
    }
    
    private long getCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return 0;
        }
        
        long cooldownTime = config.getLong("report.cooldown", 300) * 1000; // Convert to milliseconds
        long timeElapsed = System.currentTimeMillis() - cooldowns.get(player.getUniqueId());
        long timeLeft = (cooldownTime - timeElapsed) / 1000; // Convert to seconds
        
        return Math.max(0, timeLeft);
    }
    
    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
