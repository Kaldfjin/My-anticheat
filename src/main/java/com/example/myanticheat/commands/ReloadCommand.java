package com.example.myanticheat.commands;

import com.example.myanticheat.MyAntiCheat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final MyAntiCheat plugin;
    
    public ReloadCommand(MyAntiCheat plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("myanticheat.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPluginConfig();
            sender.sendMessage(ChatColor.GREEN + "MyAntiCheat configuration reloaded successfully!");
            return true;
        }
        
        sendHelp(sender);
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== MyAntiCheat Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/myanticheat reload" + ChatColor.WHITE + " - Reload the plugin configuration");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            if ("reload".startsWith(input)) {
                completions.add("reload");
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
} 