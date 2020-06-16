package com.killianmonnier.sectumsempra;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	@Override
	public void onEnable() {
		
	}
	
	@Override
	public void onDisable() {
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("hello")) {
			if (sender instanceof Player) {
				// player
				Player player = (Player) sender;
				if (player.hasPermission("hello.use")) {
					player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.ITALIC + "I'm Harry, Harry Potter.");
					return true;
				} else {
					player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You do not have permission!");
				}
				return true;
				
			} else {
				// console
				sender.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "I don't give a DAMN what your father thinks, Malfoy.");
				return true;
			}
		}
		
		return false;
	}
}
