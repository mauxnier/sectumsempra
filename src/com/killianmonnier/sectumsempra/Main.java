package com.killianmonnier.sectumsempra;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin implements Listener {
	
	@Override
	public void onEnable() {
		System.out.println("Plugin is loaded");
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		System.out.println("Plugin now shutdown");
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
		// Incantation
		player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.ITALIC + "Sectumsempra !");
		
		LivingEntity opponent = null;
		
		for (Entity e : getEntitys(player)) {
            if (getLookingAt(player, ((LivingEntity) e))) {
            	opponent = (LivingEntity) e;
            	break;
            }
        }
		
		if (opponent != null) {
			// Effect on opponent
			PotionEffect wither = new PotionEffect(PotionEffectType.WITHER, 20*10, 1, true, true);
			PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 20*20, 1, true, true);
			PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 20*15, 1, true, true);
			opponent.addPotionEffect(wither);
			opponent.addPotionEffect(blindness);
			opponent.addPotionEffect(slow);
			
			// Project opponent in the air
			opponent.setVelocity(player.getLocation().getDirection().multiply(1).setY(0.5));
		}
		
		// Magic spell animation
		Location location = player.getLocation();
		/*
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("flame", true,
				(float) location.getX(), (float) location.getY(), (float) location.getZ(),
				0, 0, 0, 1);
		*/
		
		//((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
	
	private boolean getLookingAt(Player player, LivingEntity livingEntity) {
	    Location eye = player.getEyeLocation();
	    Vector toEntity = livingEntity.getEyeLocation().toVector().subtract(eye.toVector());
	    double dot = toEntity.normalize().dot(eye.getDirection());
	    
	    return dot > 0.99D;
	}
	
	private List<Entity> getEntitys(Player player) {
	    List<Entity> entitys = new ArrayList<Entity>();
	    for (Entity e : player.getNearbyEntities(10, 10, 10)) {
	        if (e instanceof LivingEntity){
	            if (getLookingAt(player, (LivingEntity) e)) {
	                entitys.add(e);
	            }
	        }
	    }
	    return entitys;
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
	
	public boolean isItemWithName(ItemStack item, Material type, String name) {
		return item != null && item.getType() == type && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains(name);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = (Player) event.getPlayer();
		Action action = event.getAction();
		ItemStack item = event.getItem();
		
		if (isItemWithName(item, Material.STICK, "The Elder Wand") && action == Action.LEFT_CLICK_AIR  || action == Action.LEFT_CLICK_BLOCK) {
			sectumsempra(player);
		}
	}
	
}
