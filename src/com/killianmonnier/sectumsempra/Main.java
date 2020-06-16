package com.killianmonnier.sectumsempra;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	@Override
	public void onEnable() {
		
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			if (label.equalsIgnoreCase("sectumsempra") || label.equalsIgnoreCase("ss")) {
				if (player.hasPermission("sectumsempra.use")) {
					player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.ITALIC + "Sectumsempra !");
					sectumsempra(player);
					return true;
				}
				
				player.sendMessage(ChatColor.MAGIC + "" + ChatColor.BOLD + "This magical spell is prohibited by the Minister for Magic. Beware muggle !");
				return true;
				
			} else if (label.equalsIgnoreCase("wand")) {
				if (player.getInventory().firstEmpty() == -1) {
					// inventory is full
					Location loc = player.getLocation();
					World world = player.getWorld();
					
					world.dropItemNaturally(loc, getWand());
					player.sendMessage(ChatColor.GOLD + "You look inside the sorting hat and found something in it. You inadvertently drop it.");
					return true;
				}
				
				player.getInventory().addItem(getWand());
				player.sendMessage(ChatColor.GOLD + "You look inside the sorting hat and found something in it. You grab it and place it in your pocket.");
				return true;
			}
			
		} else {
			sender.sendMessage("Nothing's for you mate");
			return true;
		}
		
		return false;
	}
	
	public void sectumsempra(Player player) {
		
	}
	
	public ItemStack getWand() {
		
		ItemStack wand = new ItemStack(Material.STICK);
		ItemMeta meta = wand.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "The Elder Wand");
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(ChatColor.GOLD + "" + ChatColor.ITALIC + "The most powerful wand that has ever existed");
		
		meta.setLore(lore);
		wand.setItemMeta(meta);
		
		return wand;
	}
}
