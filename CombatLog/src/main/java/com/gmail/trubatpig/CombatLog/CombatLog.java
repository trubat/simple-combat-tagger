package com.gmail.trubatpig.CombatLog;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class CombatLog extends JavaPlugin implements Listener
{
	static private List<CombatTag> playersInCombat = new ArrayList<CombatTag>();
	static private List<CommandTag> commandTags = new ArrayList<CommandTag>();
	
	private int tagDuration = 200;
	private int commandDisableDuration = 200;
	private boolean enableWhitelist = true;
	private List<String> whitelistedCommands = new ArrayList<String>();
	private boolean enableBlacklist = false;
	private List<String> blacklist = new ArrayList<String>(); 
	private String combatTaggedMessage = "&cYou are now in combat. Don't log out!";
	private String combatExitMessage = "&7You are no longer in combat";
	private String commandTagExitMessage = "&7You can now perform commands";
	private String combatLogBroadcast = "&4PLAYER &chas logged out during combat while fighting &4TARGET";
	private String commandFailMessage = "&cYou can't use that command in combat";
	@Override
	public void onEnable()
	{	
		saveDefaultConfig();
		loadFromConfig();
		getServer().getPluginManager().registerEvents(this, this);
		new BukkitRunnable()
		{
			public void run()
			{
				for(int i = 0;playersInCombat != null && i < playersInCombat.size();i++)
				{
					if(playersInCombat.get(i).getTickDuration() - 20 > 0)
					{
						playersInCombat.get(i).subtractTickDuration(20);
					}
					else
					{
						playersInCombat.get(i).getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', combatExitMessage));
						playersInCombat.remove(i);
						i--;
					}
				}
				for(int i = 0;commandTags != null && i < commandTags.size();i++)
				{
					if(commandTags.get(i).getTickDuration() - 20 > 0)
					{
						commandTags.get(i).subtractTickDuration(20);
					}
					else
					{
						if(commandDisableDuration != tagDuration) commandTags.get(i).getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', commandTagExitMessage));
						commandTags.remove(i);
						i--;
					}
				}
			}
		}.runTaskTimer(this, 10, 20);
	}
	public void loadFromConfig()
	{
		//FileConfiguration config = this.getConfig();
		reloadConfig();
		tagDuration = getConfig().getInt("tagDuration") * 20;
		commandDisableDuration = getConfig().getInt("commandDisableDuration") * 20;
		enableWhitelist = getConfig().getBoolean("enableWhitelist");
		whitelistedCommands = (List<String>) getConfig().getList("whitelist");
		enableBlacklist = getConfig().getBoolean("enableBlacklist");
		blacklist = (List<String>) getConfig().getList("blacklist");
		combatTaggedMessage = getConfig().getString("combatTaggedMessage");
		combatExitMessage = getConfig().getString("combatExitMessage");
		commandTagExitMessage = getConfig().getString("commandTagExitMessage");
		combatLogBroadcast = getConfig().getString("combatLogBroadcast");
		commandFailMessage = getConfig().getString("commandFailMessage");
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,String label, String[]args)
	{
		if(cmd.getName().equalsIgnoreCase("sctreload") && sender.hasPermission("combattag.reload"))
		{
			loadFromConfig();
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cSimple Combat Tag config reloaded"));
			return true;
		}
		return false;
	}
	@EventHandler
	public void onPlayerCombat(final EntityDamageByEntityEvent event)
	{
		new BukkitRunnable()
		{
			public void run()
			{
				if(!event.isCancelled()) generateTag(event);
			}
		}.runTaskLater(this, 1);
	}
	private void generateTag(EntityDamageByEntityEvent event)
	{
		if((event.getDamager() instanceof Player || event.getDamager() instanceof Projectile) && event.getEntity() instanceof Player
				&& !event.isCancelled())
		{
			Player damager = null;
			Player damageReceiver = (Player) event.getEntity();
			if(event.getDamager() instanceof Projectile)
			{
				Projectile proj = (Projectile) event.getDamager();
				if(proj.getShooter() instanceof Player)
				{
					damager = (Player) proj.getShooter();
				}
				else
				{
					return;
				}
			}
			else
			{
				damager = (Player) event.getDamager();
			}
			
			boolean damagerTest = false;
			boolean damageReceiverTest = false;
			for(int i = 0;i < playersInCombat.size();i++)
			{
				if(playersInCombat.get(i).getPlayer().equals(damager))
				{
					playersInCombat.get(i).setTickDuration(tagDuration);
					playersInCombat.get(i).setTargetPlayer(damageReceiver);
					damagerTest = true;
				}
				else if(playersInCombat.get(i).getPlayer().equals(damageReceiver))
				{
					playersInCombat.get(i).setTickDuration(tagDuration);
					playersInCombat.get(i).setTargetPlayer(damager);
					damageReceiverTest = true;
				}
			}
			if(damager.equals(damageReceiver) && !damager.hasPermission("combattag.bypass"))
			{
				damager.sendMessage(ChatColor.translateAlternateColorCodes('&', combatTaggedMessage));
				playersInCombat.add(new CombatTag(damager, damageReceiver, tagDuration));
				commandTags.add(new CommandTag(damager, commandDisableDuration));
			}
			else
			{
				if(!damagerTest && !damager.hasPermission("combattag.bypass"))
				{
					damager.sendMessage(ChatColor.translateAlternateColorCodes('&', combatTaggedMessage));
					playersInCombat.add(new CombatTag(damager, damageReceiver, tagDuration));
					commandTags.add(new CommandTag(damager, commandDisableDuration));
				}
				if(!damageReceiverTest && !damageReceiver.hasPermission("combattag.bypass"))
				{
					damageReceiver.sendMessage(ChatColor.translateAlternateColorCodes('&', combatTaggedMessage));
					playersInCombat.add(new CombatTag(damageReceiver, damager, tagDuration));
					commandTags.add(new CommandTag(damageReceiver, commandDisableDuration));
				}
			}
		}
	}
	@EventHandler
	public void onPlayerLog(PlayerQuitEvent event)
	{
		for(CombatTag tag : playersInCombat)
		{
			if(tag.getPlayer().equals(event.getPlayer()))
			{
				getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastReplace(combatLogBroadcast, tag.getPlayer(), tag.getTargetPlayer())));
				tag.getPlayer().setHealth(0);
				playersInCombat.remove(tag);
				return;
			}
		}
	}
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event)
	{
		if(!isCommandOkay(event.getMessage()) && !event.getPlayer().hasPermission("combattag.commandbypass"))
		{
			for(CommandTag t : commandTags)
			{
				if(t.getPlayer().equals(event.getPlayer()))
				{
					event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', commandFailMessage));
					event.setCancelled(true);
				}
			}
		}
	}
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		for(CombatTag tag : playersInCombat)
		{
			if(tag.getPlayer().equals(event.getEntity()))
			{
				playersInCombat.remove(tag);
				return;
			}
		}
	}
	private String broadcastReplace(String broadcast, Player player, Player target)
	{
		broadcast = broadcast.replaceAll("PLAYER", player.getName());
		broadcast = broadcast.replaceAll("TARGET", target.getName());
		return broadcast;
	}
	private boolean isCommandOkay(String message)
	{
		boolean test = false;
		if(enableWhitelist)
		{
			for(String s : whitelistedCommands)
			{
				if(message.contains(s))
				{
					test = true;
					break;
				}
			}
		}
		else if(enableBlacklist)
		{
			for(String s : blacklist)
			{
				if(message.contains(s))
				{
					test = false;
					break;
				}
			}
		}
		return test;
	}
}
