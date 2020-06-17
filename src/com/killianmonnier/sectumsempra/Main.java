package com.killianmonnier.sectumsempra;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin implements Listener {
	
	public final int field = 30;
	public final int sectumDamage = 5;
	
	@Override
	public void onEnable() {
		System.out.println("Plugin is loaded");
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		System.out.println("Plugin is shutting down");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			if (label.equalsIgnoreCase("wand")) {
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
		// Get the entity were the player is looking at.
		LivingEntity opponent = null;
		
		for (Entity e : getEntitys(player)) {
            if (getLookingAt(player, ((LivingEntity) e))) {
            	opponent = (LivingEntity) e;
            	break;
            }
        }
		
		if (opponent != null) {
			// Incantation.
			player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.ITALIC + "Sectumsempra !");
			
			// Magic spell animation on the player.
			Location playerLocation = player.getLocation();
			
			float a = 0;
			
			for (int i = 0; i < 10; i++) {
				a += Math.PI / 16;
				
				Location first = playerLocation.clone().add(Math.sin(a), Math.cos(a) + 1, Math.cos(a));
				Location second = playerLocation.clone().add(Math.sin(a + Math.PI), Math.cos(a) + 1, Math.cos(a + Math.PI));
				
				player.getWorld().spawnParticle(Particle.SQUID_INK, first, 0, 0, 0, 0, 0);
				player.getWorld().spawnParticle(Particle.SQUID_INK, second, 0, 0, 0, 0, 0);
			}
			
			// Sectum : lacerates.
			opponent.damage((double) sectumDamage);
			
			// deprecated methods (below) I need to use Attribute.GENERIC_MAX_HEALTH.
			// Sempra : forever.
			if (opponent.getMaxHealth() > sectumDamage) {
				opponent.setMaxHealth((double) opponent.getMaxHealth() - (double) sectumDamage);
			}
			
			// Effect on the opponent.
			PotionEffect wither = new PotionEffect(PotionEffectType.WITHER, 20*5, 1, true, true);
			PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 20*15, 1, true, true);
			PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 20*10, 1, true, true);
			opponent.addPotionEffect(wither);
			opponent.addPotionEffect(blindness);
			opponent.addPotionEffect(slow);
			
			// Project the opponent in the air.
			opponent.setVelocity(player.getLocation().getDirection().multiply(1).setY(0.5));
			
			// Magic spell animation on the opponent.
			Location opponentLocation = opponent.getLocation();
			opponent.getWorld().playEffect(opponentLocation, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		}
	}
	
	// Return a boolean telling if the player is looking at a specific entity.
	private boolean getLookingAt(Player player, LivingEntity livingEntity) {
	    Location eye = player.getEyeLocation();
	    Vector toEntity = livingEntity.getEyeLocation().toVector().subtract(eye.toVector());
	    double dot = toEntity.normalize().dot(eye.getDirection());
	    
	    return dot > 0.99D;
	}
	
	// Return entitys in a field around the player.
	private List<Entity> getEntitys(Player player) {
	    List<Entity> entitys = new ArrayList<Entity>();
	    for (Entity e : player.getNearbyEntities(field, field, field)) {
	        if (e instanceof LivingEntity){
	            if (getLookingAt(player, (LivingEntity) e)) {
	                entitys.add(e);
	            }
	        }
	    }
	    return entitys;
	}
	
	// Return a custom item named "The Elder Wand" when using the command /wand.
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
	
	// Return a boolean telling if an item as a specific type and name corresponding with the parameters.
	public boolean isItemWithName(ItemStack item, Material type, String name) {
		return item != null && item.getType() == type && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains(name);
	}
	
	// Method called when a player interact with something.
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		ItemStack item = event.getItem();
		
		if (isItemWithName(item, Material.STICK, "The Elder Wand") && action == Action.RIGHT_CLICK_AIR) {
			sectumsempra(player);
		}
	}
	
	// Method called when a player join the server.
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		this.getServer().broadcastMessage(ChatColor.DARK_GREEN + "Welcome and bienvenue ! You can use /wand to find a wand. Right-click with it dear Severus Snape !");
	}
	
}
