package com.killianmonnier.sectumsempra;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
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
	public final double range = 30.0;
	public final int sectumDamage = 5;
	public final int spellsNumber = 4;
	public final double hitbox = 1;
	
	public int actualSpell = 1;
	public String[] spellsName = new String[] {"Sectumsempra", "Meteorribilis Recanto", "Vulnera Sanentur", "Hominum revelio"};
	public boolean isThunder, isRaining;
	
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
	
	// Spell damaging a LivingEntity.
	public void sectumsempra(Player player) {
		Location playerLocation = player.getLocation();
		
		// Incantation.
		player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.ITALIC + spellsName[0] + " !");
		
		// Magic spell animation on the player.
		double a = 0;
		
		for (int i = 0; i < 10; i++) {
			a += Math.PI / 16;
			
			Location first = playerLocation.clone().add(Math.sin(a), Math.cos(a) + 1, Math.cos(a));
			Location second = playerLocation.clone().add(Math.sin(a + Math.PI), Math.cos(a) + 1, Math.cos(a + Math.PI));
			
			player.getWorld().spawnParticle(Particle.SQUID_INK, first, 0, 0, 0, 0, 0);
			player.getWorld().spawnParticle(Particle.SQUID_INK, second, 0, 0, 0, 0, 0);
		}
		
		// Magic spell trail.
		new BukkitRunnable() {
			Location playerEyeLocation = player.getEyeLocation();
			Location spellLocation = playerEyeLocation;
			Vector direction = playerEyeLocation.getDirection().normalize();
			DustOptions black = new DustOptions(Color.fromRGB(0), 1);
			DustOptions red = new DustOptions(Color.fromRGB(255, 0, 0), 1);
			LivingEntity opponent = null;
			
			double t = 0;
			double xtrav, ytrav, ztrav;
			double x, y, z;
			
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				t += 0.5;
				xtrav = direction.getX() * t;
				ytrav = direction.getY() * t;
				ztrav = direction.getZ() * t;
				spellLocation.add(xtrav, ytrav, ztrav);
				
				// The black trail.
				for (double i = 0; i <= 2 * Math.PI; i += Math.PI / 32) {
					x = Math.cos(t);
					y = Math.cos(t);
					z = Math.sin(t);
					spellLocation.add(x, y, z);
					
					player.getWorld().spawnParticle(Particle.REDSTONE, spellLocation, 0, black);
					spellLocation.subtract(x, y, z);
				}
				
				// The red trail.
				player.getWorld().spawnParticle(Particle.REDSTONE, spellLocation, 0, red);
				
				// Detect a supposed opponent.
				for (Entity entity : getEntitysLooked(player, range)) {
					if (isSimilary(entity.getLocation().getX(), spellLocation.getX()) && isSimilary(entity.getLocation().getY() + hitbox, spellLocation.getY()) && isSimilary(entity.getLocation().getZ(), spellLocation.getZ())) {
						opponent = (LivingEntity) entity;
						System.out.println("Target : " + opponent);
					}
				}
				
				// Curse on the possible opponent.
				if (opponent != null) {
					// Sectum : lacerates.
					opponent.damage((double) sectumDamage);
					
					// deprecated methods (below) I need to use Attribute.GENERIC_MAX_HEALTH.
					// Sempra : forever.
					if (opponent.getMaxHealth() > sectumDamage) {
						opponent.setMaxHealth((double) opponent.getMaxHealth() - (double) sectumDamage);
					}
						
					// Effect on the opponent.
					PotionEffect wither = new PotionEffect(PotionEffectType.WITHER, 20*10, 2, false, false);
					PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 20*30, 2, false, false);
					PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 20*25, 3, false, false);
					opponent.addPotionEffect(wither);
					opponent.addPotionEffect(blindness);
					opponent.addPotionEffect(slow);
					
					// Project the opponent in the air.
					opponent.setVelocity(player.getLocation().getDirection().multiply(1).setY(0.5));
					
					// Magic spell animation on the opponent.
					Location opponentLocation = opponent.getLocation();
					opponent.getWorld().playEffect(opponentLocation, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
					
					// Stopping the spell.
					t = range;
				}
				
				spellLocation.subtract(xtrav, ytrav, ztrav);
				 
				if (t >= range) {
					cancel();
				}
				
			}
		}.runTaskTimer(getPlugin(Main.class), 0, 0);
		
	}

	// Spell glowing a LivingEntity.
	public void hominumRevelio(Player player) {
		LivingEntity opponent = null;
		Location eyeLocation = player.getEyeLocation();


		for (Entity entity : getEntitys(player, range)) {
			opponent = (LivingEntity) entity;
			PotionEffect glowing = new PotionEffect(PotionEffectType.GLOWING, 20*10, 2, false, false);
			opponent.addPotionEffect(glowing);
		}

		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1f);
		player.spawnParticle(Particle.SNEEZE, eyeLocation.add(eyeLocation.getDirection()), 1);
		player.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + spellsName[3] + " !");

	}
	
	// Spell changing the weather.
	public void meteorribilisRecanto(Player player) {
		// Incantation.
		player.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + spellsName[1] + " !");
		
		// Animation.
		player.getWorld().strikeLightningEffect(player.getLocation());
		
		// Action.
		if (isThunder || isRaining) {
			player.performCommand("weather clear");
			isRaining = false;
			isThunder = false;
		} else {
			player.performCommand("weather thunder");
			isThunder = true;
		}
	}
	
	// Spell healing the LivingEntity the caster is looking at or healing the caster, Vulnera Sanentur : wounds healing.
	public void vulneraSanentur(Player player) {
		LivingEntity target = null;
		boolean asOneTarget = false;
		
		// Incantation.
		player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + spellsName[2] + " !");
		
		// Detect a supposed wounded the player is looking at.
		for (Entity entity : getEntitysLooked(player, 5.0)) {
			if (!asOneTarget) {
				target = (LivingEntity) entity;
				System.out.println("Target : " + entity);
				asOneTarget = true;
			}
		}
		
		// Healing the LivingEntity the caster is looking at.
		if (target != null) {
			// Animation.
			target.getWorld().playEffect(target.getEyeLocation(), Effect.DRAGON_BREATH, 1);
			
			// Action.
			PotionEffect heal = new PotionEffect(PotionEffectType.HEAL, 20*10, 5, false, false);
			PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 20*5, 5, false, false);
			target.addPotionEffect(heal);
			target.addPotionEffect(slow);
		}
		
		// Healing the caster.
		else {
			// Animation.
			player.getWorld().playEffect(player.getEyeLocation(), Effect.DRAGON_BREATH, 1);
			
			// Action.
			PotionEffect heal = new PotionEffect(PotionEffectType.HEAL, 20*10, 5, false, false);
			PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 20*5, 5, false, false);
			player.addPotionEffect(heal);
			player.addPotionEffect(slow);
		}
		
	}
	
	// Method telling if a player is looking in the direction of a LivingEntity.
	private boolean getLookingAt(Player player, LivingEntity livingEntity){
		Location eye = player.getEyeLocation();
		Vector toEntity = livingEntity.getEyeLocation().toVector().subtract(eye.toVector());
		double dot = toEntity.normalize().dot(eye.getDirection());
		
		return dot > 0.99D;
	}

	// Method returning a list of all the LivingEntity in a field delimited by a range.
	private List<Entity> getEntitys(Player player, double range){
		List<Entity> entitys = new ArrayList<Entity>();

		for (Entity e : player.getNearbyEntities(range, range, range)){
			if (e instanceof LivingEntity){
				entitys.add(e);
			}
		}

		return entitys;
	}
	
	// Method returning a list of all the LivingEntity in a field delimited by a range looked by the player.
	private List<Entity> getEntitysLooked(Player player, double range){
		List<Entity> entitys = getEntitys(player, range);
		
		for (Entity e : entitys){
			if (!getLookingAt(player, (LivingEntity) e)){
				entitys.remove(e);
			}
		}
		
		return entitys;
	}
	
	// Return a boolean telling if a variable 'a' is similary to a variable 'b' in function to the hitbox.
	private boolean isSimilary(double a, double b) {
		if ((a > 0 && b > 0) || (a < 0 && b < 0)) 
			if (Math.abs(a - b) < hitbox) return true;
		return false;
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
	private boolean isItemWithName(ItemStack item, Material type, String name) {
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
				case 3:
					vulneraSanentur(player);
					break;
				case 4:
					hominumRevelio(player);
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
