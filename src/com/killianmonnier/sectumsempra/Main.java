package com.killianmonnier.sectumsempra;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin implements Listener {
	
	// Manage spells.
	public final int range = 30;
	public final int sectumDamage = 5;
	public final int spellsNumber = 2;
	
	public int actualSpell = 1;
	public String[] spellsName = new String[] {"Sectumsempra", "Meteorribilis Recanto"};
	public boolean isThunder = true; // for fixing a bug
	public boolean isRaining;
	
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
		// Incantation.
		player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.ITALIC + spellsName[0] + " !");
		
		new BukkitRunnable() {
			Location playerLocation = player.getEyeLocation();
			Location spellLocation = playerLocation;
			Vector direction = playerLocation.getDirection().normalize();
			DustOptions black = new DustOptions(Color.fromRGB(0), 1);
			DustOptions red = new DustOptions(Color.fromRGB(255, 0, 0), 1);
			DustOptions green = new DustOptions(Color.fromRGB(0, 255, 0), 1);
			
			double t = 0;
			double xtrav, ytrav, ztrav;
			double x, y, z;
			
			@Override
			public void run() {
				t += 0.5;
				xtrav = direction.getX() * t;
				ytrav = direction.getY() * t;
				ztrav = direction.getZ() * t;
				spellLocation.add(xtrav, ytrav, ztrav);
				
				for (double i = 0; i <= 2 * Math.PI; i += Math.PI / 32) {
					x = Math.cos(t);
					y = Math.cos(t);
					z = Math.sin(t);
					spellLocation.add(x, y, z);
 
					player.getWorld().spawnParticle(Particle.REDSTONE, spellLocation, 0, black);
					//player.getWorld().spawnParticle(Particle.REDSTONE, spellLocation.clone().add(-0.2, -0.2, -0.2), 0, red);
					//player.getWorld().spawnParticle(Particle.REDSTONE, spellLocation.clone().add(0.2, 0.2, 0.2), 0, green);
					spellLocation.subtract(x, y, z);
				}
				
				spellLocation.subtract(xtrav, ytrav, ztrav);
 
				if (t > range) {
					cancel();
				}
			}
		}.runTaskTimer(getPlugin(Main.class), 0, 0);
	}
		/*
		// Magic spell animation on the player.
		double a = 0;
		
		for (int i = 0; i < 10; i++) {
			a += Math.PI / 16;
			
			Location first = playerLocation.clone().add(Math.sin(a), Math.cos(a) + 1, Math.cos(a));
			Location second = playerLocation.clone().add(Math.sin(a + Math.PI), Math.cos(a) + 1, Math.cos(a + Math.PI));
			
			player.getWorld().spawnParticle(Particle.SQUID_INK, first, 0, 0, 0, 0, 0);
			player.getWorld().spawnParticle(Particle.SQUID_INK, second, 0, 0, 0, 0, 0);
		}
		
		// Magic spell trail
		double t = 0;
		double x, y, z;
		
		for (int i = 0; i < 20; i++) {
			t += Math.PI / 16;
			x = Math.cos(t) + i;
			y = Math.sin(t) + 1;
			z = Math.sin(t) + i;
			
			Location spellLocation = playerLocation.clone().add(x, y, z);
			
			//player.getWorld().spawnParticle(Particle.SQUID_INK, spellLocation, 0, 0, 0, 0, 0);
			DustOptions dustOptions = new DustOptions(Color.fromRGB(0), 1);
			player.getWorld().spawnParticle(Particle.REDSTONE, spellLocation, 0, dustOptions);
		}
		
		if (opponent != null) {
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
		}*/
	
	public void meteorribilisRecanto(Player player) {
		// Incantation.
		player.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + spellsName[1] + " !");
		
		// Animation.
		player.getWorld().strikeLightningEffect(player.getLocation());
		
		// Action.
		if (isThunder || isRaining) {
			player.performCommand("weather clear");
		} else {
			player.performCommand("weather thunder");
		}
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
		
		if (isItemWithName(item, Material.STICK, "The Elder Wand") && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
			switch(actualSpell) {
				case 1:
					sectumsempra(player);
					break;
				case 2:
					meteorribilisRecanto(player);
					break;
				default:
					System.err.println("Error on the variable actualSpell");
			}
		}
		
		if (isItemWithName(item, Material.STICK, "The Elder Wand") && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
			if (actualSpell < spellsNumber)
				actualSpell++;
			else
				actualSpell = 1;
			player.sendMessage(ChatColor.GOLD + "" + "You change your spell to " + spellsName[actualSpell - 1]);
			
		}
	}
	
	// Method called when a player join the server.
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		this.getServer().broadcastMessage(ChatColor.DARK_GREEN + "Welcome Wizard ! You can use /wand to find a wand. Left-click to cast a spell and right-click to change it !");
	}
	
	// Method called when the weather change to thunder.
	@EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
		isThunder = event.toThunderState();
	}
	
	// Method called when the weather change to raining.
	@EventHandler
    public void onThunderChange(WeatherChangeEvent event) {
		isRaining = event.toWeatherState();
	}
}
