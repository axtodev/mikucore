package com.mikucore.listeners;

import com.mikucore.MikuCore;
import com.mikucore.commands.StaffChatCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatListener implements Listener {
    private final MikuCore plugin;
    private final StaffChatCommand staffChatCommand;
    private final Pattern wordBoundary = Pattern.compile("\\b");
    private List<String> censoredWords;
    private String replacement;
    private boolean muteOnViolation;
    private boolean notifyStaff;
    private boolean enableChatFormatting;
    private String chatFormat;

    public ChatListener(MikuCore plugin) {
        this.plugin = plugin;
        this.staffChatCommand = new StaffChatCommand(plugin);
        reloadConfig();
    }

    public void reloadConfig() {
        FileConfiguration config = plugin.getConfig();
        this.censoredWords = config.getStringList("chat-filter.censored-words");
        this.replacement = config.getString("chat-filter.replacement", "***");
        this.muteOnViolation = config.getBoolean("chat-filter.mute-on-violation", true);
        this.notifyStaff = config.getBoolean("chat-filter.notify-staff", true);
        this.enableChatFormatting = config.getBoolean("chat.formatting.enabled", true);
        this.chatFormat = config.getString("chat.formatting.format", "&7[%player_displayname%&7] %message%");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Handle staff chat
        if (staffChatCommand.isInStaffChat(player)) {
            event.setCancelled(true);
            if (message.trim().isEmpty()) {
                player.sendMessage(ChatColor.RED + "Usage: /sc <message> or type a message while in staff chat mode");
                return;
            }
            staffChatCommand.sendStaffMessage(player, message);
            return;
        }

        // Check for empty messages
        if (message.trim().isEmpty()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot send empty messages!");
            return;
        }

        // Check for censored words and filter them
        FilterResult filterResult = filterMessage(player, message);
        
        if (filterResult.isViolation()) {
            handleViolation(player, message, filterResult.getFilteredMessage());
            if (muteOnViolation) {
                event.setCancelled(true);
                return;
            }
            event.setMessage(filterResult.getFilteredMessage());
        }

        // Apply chat formatting
        if (enableChatFormatting) {
            event.setFormat(applyChatFormat(player, filterResult.getFilteredMessage()));
        }
    }

    private FilterResult filterMessage(Player player, String message) {
        String filteredMessage = message;
        boolean hasViolation = false;
        
        for (String word : censoredWords) {
            if (message.toLowerCase().contains(word.toLowerCase())) {
                hasViolation = true;
                filteredMessage = filteredMessage.replaceAll("(?i)" + Pattern.quote(word), 
                    ChatColor.translateAlternateColorCodes('&', replacement));
            }
        }
        
        return new FilterResult(hasViolation, filteredMessage, message);
    }
    
    private void handleViolation(Player player, String originalMessage, String filteredMessage) {
        if (notifyStaff) {
            String staffMessage = String.format("&c[FILTER] &7%s: &f%s &8(&e!MUTA&8) &7-> &f%s", 
                player.getName(), originalMessage, filteredMessage);
            
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("mikucore.staff"))
                .forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', staffMessage)));
            
            plugin.getLogger().info(String.format("[FILTER] %s: %s (MUTED: %s)", 
                player.getName(), originalMessage, filteredMessage));
        }
        
        if (muteOnViolation) {
            player.sendMessage(ChatColor.RED + "Your message contained inappropriate language and was blocked.");
        }
    }
    
    private String applyChatFormat(Player player, String message) {
        return chatFormat
            .replace("%player_name%", player.getName())
            .replace("%player_displayname%", player.getDisplayName())
            .replace("%message%", message)
            .replace('&', ChatColor.COLOR_CHAR);
    }
    
    private static class FilterResult {
        private final boolean violation;
        private final String filteredMessage;
        private final String originalMessage;
        
        public FilterResult(boolean violation, String filteredMessage, String originalMessage) {
            this.violation = violation;
            this.filteredMessage = filteredMessage;
            this.originalMessage = originalMessage;
        }
        
        public boolean isViolation() {
            return violation;
        }
        
        public String getFilteredMessage() {
            return filteredMessage;
        }
        
        public String getOriginalMessage() {
            return originalMessage;
        }
    }
}
