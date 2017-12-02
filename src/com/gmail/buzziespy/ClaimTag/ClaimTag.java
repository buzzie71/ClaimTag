package com.gmail.buzziespy.ClaimTag;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClaimTag extends JavaPlugin implements Listener{
	
public static boolean runnerTag;
public static List<String> runnerList;
public static boolean debugMode;
public static boolean verboseDebug;
public static boolean suppressGlobalAlerts;
public static boolean taggingBroadcasts;

	
	@Override
	public void onEnable()
	{
		//enable the listener
		getServer().getPluginManager().registerEvents(this, this);
		//load up the config file
		getConfig();
		getLogger().info("Loading runner tagging status...");
		runnerTag = getConfig().getBoolean("runner-tag-on");
		getLogger().info("Loading runner list...");
		getRunnerList();
		//runnerList = getConfig().getStringList("runners");
		getLogger().info("Loading debug status...");
		debugMode = getConfig().getBoolean("debug-mode");
		getLogger().info("Loading debug verbosity...");
		verboseDebug = getConfig().getBoolean("verbose-debug");
		getLogger().info("Loading global alerts...");
		suppressGlobalAlerts = getConfig().getBoolean("suppress-global-alerts");
		getLogger().info("Loading tagging announcements status...");
		taggingBroadcasts = getConfig().getBoolean("tagging-announcements");
	}
	
	@Override
	public void onDisable()
	{
		//transfer config booleans to config file
		getConfig().set("runner-tag", runnerTag);
		getConfig().set("debug-mode", debugMode);
		getConfig().set("verbose-debug", verboseDebug);
		getConfig().set("suppress-broadcasts", suppressGlobalAlerts);
		getConfig().set("tagging-announcements", taggingBroadcasts);
		//save the config file
		getLogger().info("Saving config!");
		this.saveConfig();
	}
	
	//on P, this event is(?) the same one that is cancelled by no-PvP on WG
	@EventHandler(priority=EventPriority.LOW)
	public void onTag(EntityDamageByEntityEvent e)
	{
		if (runnerTag)
		{
			if (e.getDamager() instanceof Player && e.getEntity() instanceof Player)
			{
				Player r = (Player)e.getEntity();
				Player p = (Player)e.getDamager();
				if (getConfig().isItemStack("runners."+r.getName()))
				{
					if (debugMode && verboseDebug)
					{
						getLogger().info("[CT-1] " + p.getName() + " has punched " + r.getName());
					}
					
					if (hasTagged(r.getName(), p))
					{
						//do nothing if player has already tagged the runner
						if (debugMode)
						{
							getLogger().info("[CT-2] " + p.getName() + " has already tagged " + r.getName() + ".");
						}
						p.sendMessage(ChatColor.RED + "You have already tagged " + r.getName() + ".  Seek out another runner!");
						//EXPERIMENTAL
						p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
					}
					else
					{
						if (givePrizeToPlayer(r.getName(), p)) //if true, they got the prize
						{
							if (debugMode)
							{
								getLogger().info("[CT-2] " + p.getName() + " is receiving the prize for tagging " + r.getName() + ".");
							}
							if (taggingBroadcasts) //personal alert as alternate method to avoid cluttering chat feed when tagging broadcasts are on
							{
								getServer().broadcastMessage(ChatColor.GREEN + "[CT] " + p.getName() + " has tagged " + r.getName() + "!");
							}
							else
							{
								p.sendMessage(ChatColor.GREEN + "You have received the prize for tagging " + r.getName() + "!");
							}
							r.sendMessage(ChatColor.AQUA + p.getName() + " has tagged you and received a prize!"); 
							//EXPERIMENTAL
							p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
							
							//set player as having tagged the runner
							String taggerUUID = p.getUniqueId().toString();
							if (getConfig().isList(r.getName())) //if the runner already has a list
							{
								if (debugMode)
								{
									getLogger().info("[CT-1] " + p.getName() + " is being added to the list of players for " + r.getName() + ".");
								}
								List<String> taggedAlready = getConfig().getStringList(r.getName());
								taggedAlready.add(taggerUUID);
								getConfig().set(r.getName(), taggedAlready);
								setTaggedAfterCheck(r.getName(),p);
							}
							else //if there is no list yet to put the player in
							{
								if (debugMode)
								{
									getLogger().info("[CT-1] " + p.getName() + " is first in a new list of players for " + r.getName() + ".");
								}
								List<String> taggedAlready = new LinkedList<String>();
								taggedAlready.add(taggerUUID);
								getConfig().set(r.getName(), taggedAlready);
								setTaggedAfterCheck(r.getName(),p);
							}
						}
						else //the player didn't get the prize because their inventory was full
						{
							if (debugMode)
							{
								getLogger().info("[CT-2] " + p.getName() + "'s full inventory prevented them from receiving a prize for tagging " + r.getName() + ".");
							}
							p.sendMessage(ChatColor.RED + "Your inventory is full.  Tag " + r.getName() + " again after you make some space.");
							//EXPERIMENTAL
							p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
						}
					}
				}
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("ct")) //admin-level
		{
			/*
			 *  0: /ct - shows plugin configuration 
			 *  1: /ct runnerlist - shows list of runners (redundant?)
			 *  2: /ct addrunner <playername> - adds player to list of runners
			 *  2: /ct debug-mode [true|false] - toggles debug information in console
			 *  2: /ct runner-tag [true|false] - toggles tagging of runners (true = event is on)
			 *  2: /ct suppress-broadcasts [true|false] - Mutes server-wide alerts for this plugin (for testing)
			 *  2: /ct setprize <runnername> - Sets tagging prize for a particular runner using what's selected in inventory.  
			 *  2: /ct viewtagged <playername> - See who the specified player has tagged or not tagged so far
			 *  2: /ct clearmeta <playername>
			 *  /runners: Shows all runners (and whether player has tagged them) - for players
			 *  
			 */
			if (args.length == 0)
			{
				sender.sendMessage(ChatColor.AQUA + "https://github.com/buzzie71/ClaimTag/blob/master/README.md");
				sender.sendMessage(ChatColor.AQUA + "=====ClaimTag, v"+ getDescription().getVersion() +"=====");
				sender.sendMessage(ChatColor.AQUA + "Runner tag: " + runnerTag);
				sender.sendMessage(ChatColor.AQUA + "Debug mode: " + debugMode);
				sender.sendMessage(ChatColor.AQUA + "Verbose debug: " + verboseDebug);
				sender.sendMessage(ChatColor.AQUA + "Suppress broadcasts: " + suppressGlobalAlerts);
				sender.sendMessage(ChatColor.AQUA + "Tagging announcements: " + taggingBroadcasts);
				sender.sendMessage(ChatColor.RED + "WARNING: Toggling runner-tag with suppress-broadcasts set to false will alert the server!");
				sender.sendMessage(ChatColor.RED + "WARNING: Adding or removing runners with suppress-broadcasts false and runner-tag true will alert the server!");
			}
			else if (args.length == 1)
			{
				if (args[0].equalsIgnoreCase("runnerlist"))
				{
					sender.sendMessage(ChatColor.AQUA + "Runners in play: " + plainListRunners());
				}
				else if (args[0].equalsIgnoreCase("addrunner"))
				{
					sender.sendMessage(ChatColor.RED + "/ct addrunner <playername>");
				}
				else if (args[0].equalsIgnoreCase("delrunner"))
				{
					sender.sendMessage(ChatColor.RED + "/ct delrunner <playername>");
				}
				else if (args[0].equalsIgnoreCase("debug-mode"))
				{
					sender.sendMessage(ChatColor.AQUA + "Debug mode: " + debugMode);
					sender.sendMessage(ChatColor.RED + "/ct debug-mode <true|false>");
				}
				else if (args[0].equalsIgnoreCase("verbose-debug"))
				{
					sender.sendMessage(ChatColor.AQUA + "Verbose debug: " + verboseDebug);
					sender.sendMessage(ChatColor.RED + "/ct verbose-debug <true|false>");
				}
				else if (args[0].equalsIgnoreCase("runner-tag"))
				{
					sender.sendMessage(ChatColor.AQUA + "Runner tag: " + runnerTag);
					sender.sendMessage(ChatColor.RED + "/ct runner-tag <true|false>");
				}
				else if (args[0].equalsIgnoreCase("suppress-broadcasts"))
				{
					sender.sendMessage(ChatColor.AQUA + "Suppress broadcasts: " + suppressGlobalAlerts);
					sender.sendMessage(ChatColor.RED + "/ct suppress-broadcasts <true|false>");
				}
				else if (args[0].equalsIgnoreCase("tagging-announcements"))
				{
					sender.sendMessage(ChatColor.AQUA + "Tagging announcements: " + taggingBroadcasts);
					sender.sendMessage(ChatColor.RED + "/ct tagging-announcements <true|false>");
				}
				else if (args[0].equalsIgnoreCase("setprize"))
				{
					sender.sendMessage(ChatColor.RED + "/ct setprize <playername>");
				}
				else if (args[0].equalsIgnoreCase("save"))
				{
					//runners and players data are all manipulated where tagging is handled
					//just need to save the config settings
					getConfig().set("runner-tag", runnerTag);
					getConfig().set("debug-mode", debugMode);
					getConfig().set("verbose-debug", verboseDebug);
					getConfig().set("suppress-broadcasts", suppressGlobalAlerts);
					getConfig().set("tagging-announcements", taggingBroadcasts);
					saveConfig();
					sender.sendMessage(ChatColor.GREEN + "ClaimTag Config changes saved to file!");
				}
				else if (args[0].equalsIgnoreCase("reload"))
				{
					reloadConfig();
					getRunnerList();
					runnerTag = getConfig().getBoolean("runner-tag-on");
					debugMode = getConfig().getBoolean("debug-mode");
					verboseDebug = getConfig().getBoolean("verbose-debug");
					suppressGlobalAlerts = getConfig().getBoolean("suppress-global-alerts");
					taggingBroadcasts = getConfig().getBoolean("tagging-announcements");
					sender.sendMessage(ChatColor.GREEN + "ClaimTag config has been reloaded from file!");
				}
				else if (args[0].equalsIgnoreCase("viewtagged"))
				{
					sender.sendMessage(ChatColor.RED + "/ct viewtagged <playername>");
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "/ct [runnerlist|addrunner|delrunner|debug-mode|verbose-debug|runner-tag|suppress-broadcasts|tagging-announcements|setprize|getprize|save|reload|viewtagged]");
				}
			}
			else if (args.length == 2)
			{
				if (args[0].equalsIgnoreCase("addrunner"))
				{
					String playername = args[1];
					//meh just add the string to the list
					addPlayerToRunnersList(playername);
					sender.sendMessage(ChatColor.GREEN + playername + " has been added to the runners list.");
					//Leave this announcing to other methods so staff can assign a proper prize to the player first before word goes out
					//if (runnerTag && !suppressGlobalAlerts)
					//{
					//	getServer().broadcastMessage(ChatColor.GREEN + "[CT] " + args[1] + " is now a runner in the runner tagging event!");
					//}
				}
				else if (args[0].equalsIgnoreCase("delrunner"))
				{
					//int index = Integer.parseInt(args[1]);
					//sender.sendMessage(ChatColor.RED + removePlayerFromRunnersList(index-1) + " has been removed from the runners list.");
					//try removing by name instead:
					if (removePlayerFromRunnersList(args[1]))
					{
						sender.sendMessage(ChatColor.RED + args[1] + " has been removed from the runners list.");
						//similarly for this one; this can be announced by other means.
						//if (runnerTag && !suppressGlobalAlerts)
						//{
						//	getServer().broadcastMessage(ChatColor.RED + "[CT] " + args[1] + " is no longer a runner in the runner tagging event!");
						//}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "Cannot find " + args[1] + " in the runners list.");
					}
				}
				else if (args[0].equalsIgnoreCase("debug-mode"))
				{
					if (args[1].equalsIgnoreCase("true"))
					{
						if (debugMode)
						{
							sender.sendMessage(ChatColor.RED + "Debug mode is already set to true.");
						}
						else
						{
							debugMode = true;
							sender.sendMessage(ChatColor.GREEN + "Debug mode set to true. Debug messages will appear in console.");
						}
					}
					else if (args[1].equalsIgnoreCase("false"))
					{
						if (!debugMode)
						{
							sender.sendMessage(ChatColor.RED + "Debug mode is already set to false.");
						}
						else
						{
							debugMode = false;
							sender.sendMessage(ChatColor.RED + "Debug mode set to false. Debug messages will not appear in console.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "/ct debug-mode <true|false>");
					}
				}
				else if (args[0].equalsIgnoreCase("verbose-debug"))
				{
					if (args[1].equalsIgnoreCase("true"))
					{
						if (verboseDebug)
						{
							sender.sendMessage(ChatColor.RED + "Verbose debug is already set to true.");
						}
						else
						{
							verboseDebug = true;
							sender.sendMessage(ChatColor.GREEN + "Verbose debug set to true. Debug messages will be more numerous.");
						}
					}
					else if (args[1].equalsIgnoreCase("false"))
					{
						if (!debugMode)
						{
							sender.sendMessage(ChatColor.RED + "Debug mode is already set to false.");
						}
						else
						{
							debugMode = false;
							sender.sendMessage(ChatColor.RED + "Verbose debug set to false. Debug messages will be reduced.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "/ct verbose-debug <true|false>");
					}
				}
				else if (args[0].equalsIgnoreCase("runner-tag"))
				{
					if (args[1].equalsIgnoreCase("true"))
					{
						if (runnerTag)
						{
							sender.sendMessage(ChatColor.RED + "Runner tag is already set to true.");
						}
						else
						{
							runnerTag = true;
							sender.sendMessage(ChatColor.GREEN + "Runner tag set to true. Players can now tag runners for prizes.");
							if (!suppressGlobalAlerts)
							{
								announceEventStart();
							}
						}
					}
					else if (args[1].equalsIgnoreCase("false"))
					{
						if (!runnerTag)
						{
							sender.sendMessage(ChatColor.RED + "Runner tag is already set to false.");
						}
						else
						{
							runnerTag = false;
							sender.sendMessage(ChatColor.RED + "Runner tag set to false. Players will not be able to tag runners for prizes.");
							if (!suppressGlobalAlerts)
							{
								announceEventEnd();
							}
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "/ct runner-tag <true|false>");
					}
				}
				else if (args[0].equalsIgnoreCase("suppress-broadcasts"))
				{
					if (args[1].equalsIgnoreCase("true"))
					{
						if (suppressGlobalAlerts)
						{
							sender.sendMessage(ChatColor.RED + "Suppress broadcasts is already set to true.");
						}
						else
						{
							suppressGlobalAlerts = true;
							sender.sendMessage(ChatColor.GREEN + "Suppress broadcasts set to true. Server-wide broadcasts from this plugin will not display.");
						}
					}
					else if (args[1].equalsIgnoreCase("false"))
					{
						if (!suppressGlobalAlerts)
						{
							sender.sendMessage(ChatColor.RED + "Suppress broadcasts is already set to false.");
						}
						else
						{
							suppressGlobalAlerts = false;
							sender.sendMessage(ChatColor.RED + "Suppress broadcasts set to false. Server-wide broadcasts from this plugin will display to all online players.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "/ct suppress-broadcasts <true|false>");
					}
				}
				else if (args[0].equalsIgnoreCase("tagging-announcements"))
				{
					if (args[1].equalsIgnoreCase("true"))
					{
						if (taggingBroadcasts)
						{
							sender.sendMessage(ChatColor.RED + "Tagging announcements is already set to true.");
						}
						else
						{
							taggingBroadcasts = true;
							sender.sendMessage(ChatColor.GREEN + "Tagging announcements set to true. The plugin will announce player tagging of runners to the server.");
						}
					}
					else if (args[1].equalsIgnoreCase("false"))
					{
						if (!taggingBroadcasts)
						{
							sender.sendMessage(ChatColor.RED + "Tagging announcements is already set to false.");
						}
						else
						{
							taggingBroadcasts = false;
							sender.sendMessage(ChatColor.RED + "Tagging announcements set to false. The plugin will not announce player tagging of runners to the server.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "/ct tagging-announcements <true|false>");
					}
				}
				else if (args[0].equalsIgnoreCase("setprize"))
				{
					if (sender instanceof Player)
					{
						Player p = (Player)sender;
						String runnerName = args[1];
						//DEBUG:
						if (debugMode && verboseDebug)
						{
							getLogger().info("[CT-1] " + "Attempting to access runners."+runnerName);
						}
						if (getConfig().isItemStack("runners."+runnerName))
						{
							ItemStack i = p.getEquipment().getItemInMainHand();
							getConfig().set("runners."+runnerName, i);
							p.sendMessage(ChatColor.GREEN + "Prize for tagging " + runnerName + " has been set.");
							if (debugMode)
							{
								getLogger().info("[CT-2] " + "Runner "+runnerName+" has been assigned the prize of " + i.getType().toString());
							}
						}
						else
						{
							p.sendMessage(ChatColor.RED + runnerName + " is not on the runners list!");
						}
						
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "You must be in-game to run this command.");
					}
				}
				else if (args[0].equalsIgnoreCase("getprize"))
				{
					String runnerName = args[1];
					if (getConfig().isItemStack("runners."+runnerName))
					{
						if (sender instanceof Player)
						{
							Player p = (Player)sender;
							if (givePrizeToPlayer(runnerName, p))
							{
								p.sendMessage(ChatColor.GREEN + "You have received the prize for tagging " + runnerName + ".");
							}
							else
							{
								p.sendMessage(ChatColor.RED + "Your inventory is full. Clear some space and try again.");
							}
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "You must be in-game to run this command.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + runnerName + " is not on the runners list!");
					}
				}
				else if (args[0].equalsIgnoreCase("viewtagged"))
				{
					String playerName = args[1];
					Player p = getServer().getPlayer(playerName);
					if (p != null) //if the player is online
					{
						sender.sendMessage(ChatColor.AQUA + "Player " + playerName + " has seen these runners:" + listRunners(p));
					}
					else //if player is not online
					{
						//deprecated getOfflinePlayer(String name) - may need to replace?
						OfflinePlayer offline = getServer().getOfflinePlayer(playerName); 
						if (offline.hasPlayedBefore())
						{
							String offlinePlayerUUID = offline.getUniqueId().toString();
							
							//assemble the list for the offline player
							String playerRunnerList = "";
							for (String runner: runnerList)
							{
								for (String UID: getConfig().getStringList(runner))
								{
									if (UID.equals(offlinePlayerUUID)) //if UUID found in list, add it and search the next list
									{
										playerRunnerList = playerRunnerList + "\n" + ChatColor.GREEN + runner + ChatColor.RESET;
										break;
									}
								}
								playerRunnerList = playerRunnerList + "\n" + ChatColor.RED + runner + ChatColor.RESET;
							}
							sender.sendMessage(ChatColor.AQUA + "Player " + playerName + " has seen these runners:" + playerRunnerList);
							if (debugMode)
							{
								getLogger().info("[CT-2] Player " + playerName + " has seen these runners:" + playerRunnerList);
							}
						}
						else //if player specified has not been seen before
						{
							sender.sendMessage(ChatColor.RED + offline.getName() + " has never played on the server before!");
						}
						
					}
				}
				else if (args[0].equalsIgnoreCase("clearmeta"))
				{ //clear a player's metadata in case of desync due to a runner they tagged being removed and added again
					String playerName = args[1];
					Player p = getServer().getPlayer(playerName);
					if (p != null) //if player is online
					{
						int metcount = 0;
						for (String s: runnerList)
						{
							if (p.hasMetadata("ClaimTag."+s))
							{
								if (debugMode && verboseDebug)
								{
									getLogger().info("[CT-1] Removed metadata ClaimTag." + s + " from player " + p.getName());
								}
								metcount += 1;
								p.removeMetadata("ClaimTag."+s, this);
							}
						}
						if (debugMode)
						{
							getLogger().info("[CT-2] Cleared " + metcount + " metadata on player " + p.getName());
						}
					}
				}
				
				else
				{
					sender.sendMessage(ChatColor.RED + "/ct [runnerlist|addrunner|delrunner|debug-mode|verbose-debug|runner-tag|suppress-broadcasts|tagging-announcements|setprize|getprize|save|reload|viewtagged]");
				}
			}
			
			return true;
		}
		
		else if (cmd.getName().equalsIgnoreCase("runners")) //public command
		{
			if (sender instanceof Player)
			{
				Player p = (Player)sender;
				if (runnerTag) //if runner tagging is on
				{
					p.sendMessage(ChatColor.AQUA + "Runners in play:" + listRunners(p));
				}
				else
				{
					p.sendMessage(ChatColor.RED + "The event has ended.  Runners cannot be tagged.");
				}
			}
			else if (sender instanceof ConsoleCommandSender)
			{
				if (runnerTag)
				{
					sender.sendMessage(ChatColor.AQUA + "Runners in play:" + plainListRunners());
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "The event has ended.  Runners cannot be tagged.");
				}
			}
			return true;
		}
		//UNUSED: use /ct runner-tag [true|false] instead
		/*else if (cmd.getName().equalsIgnoreCase("toggle-runner-tag"))
		{
			if (runnerTag) //if logging, turn it off
			{
				getConfig().set("runner-tag-on", false);
				saveConfig();
				sender.sendMessage(ChatColor.GREEN + "Runner tagging has been enabled.");
				runnerTag = true;
			}
			else //if not logging, turn it on
			{
				getConfig().set("runner-tag-on", true);
				saveConfig();
				sender.sendMessage(ChatColor.RED + "Runner tagging has been disabled.");
				runnerTag = false;
			}
			return true;
		}*/
		return false;
	}
	
	public String listRunners(Player p)
	{
		String list = "";
		for (String s: runnerList)
		{
			//check list for if player has tagged them
			String color;
			if (!hasTagged(s,p))
			{
				color = ChatColor.RED + "";
			}
			else //if player has tagged them
			{
				color = ChatColor.GREEN + "";
			}
			list = list + "\n" + color + s + ChatColor.RESET;
		}
		return list;
	}
	
	public String plainListRunners()
	{
		String list = "";
		for (String s: runnerList)
		{
			list = list + "\n" + s ;
		}
		return list;
	}
	
	//Check for metadata on the player first: ClaimTag.<runnerName>
	//This is only valid for players who are currently online
	public boolean hasTagged(String runnerName, Player p)
	{
		//Check for metadata first
		if (p.hasMetadata("ClaimTag."+runnerName)) //this metadata will only contain "true" or "false".
		{
			if (p.getMetadata("ClaimTag."+runnerName).get(0).asString().equalsIgnoreCase("true"))
			{
				return true;
			}
			else if (p.getMetadata("ClaimTag."+runnerName).get(0).asString().equalsIgnoreCase("false"))
			{
				return false;
			}
			else //this should never happen, but put out a warning just in case...
			{
				if (debugMode && verboseDebug)
				{
					getLogger().info("[CT-1]: WARNING: ClaimTag."+runnerName+" metadata for player " + p.getName()+ " is not true or false! Attempting config check.");
				}
			}
		}
		
		//if no information in metadata, check the config file, then set the metadata once it is known
		List<String> taggedAlready = getConfig().getStringList(runnerName);
		String taggerUUID = p.getUniqueId().toString();
		for (String s: taggedAlready)
		{
			if (s.equals(taggerUUID))
			{
				setTaggedAfterCheck(runnerName, p);
				return true;
			}
		}
		setNotTaggedAfterCheck(runnerName, p);
		return false;
	}
	
	public void setTaggedAfterCheck(String runnerName, Player p)
	{
		if (debugMode && verboseDebug)
		{
			getLogger().info("[CT-1] " + p.getName() + " will be set with metadata for having tagged " + runnerName + ".");
		}
		if (p.hasMetadata("ClaimTag."+runnerName))
		{
			p.removeMetadata("ClaimTag."+runnerName, this);
		}
		p.setMetadata("ClaimTag."+runnerName, new FixedMetadataValue(this, "true"));
	}
	
	public void setNotTaggedAfterCheck(String runnerName, Player p)
	{
		if (debugMode && verboseDebug)
		{
			getLogger().info("[CT-1] " + p.getName() + " will be set with metadata for not tagging " + runnerName + ".");
		}
		if (p.hasMetadata("ClaimTag."+runnerName))
		{
			p.removeMetadata("ClaimTag."+runnerName, this);
		}
		p.setMetadata("ClaimTag."+runnerName, new FixedMetadataValue(this, "false"));
	}
	
	public void addPlayerToRunnersList(String s)
	{
		if (debugMode && verboseDebug)
		{
			getLogger().info("[CT-1] " + s + " is being added to runnerList.");
		}
		runnerList.add(s);
		//getConfig().set("runners", runnerList);
		
		if (debugMode && verboseDebug)
		{
			getLogger().info("[CT-1] " + s + " is being added to the config's runners list with default prize of coal.");
		}
		getConfig().createSection("runners." + s);
		ItemStack empty = new ItemStack(Material.COAL);
		getConfig().set("runners." + s, empty);
		//saveConfig();
	}
	
	//unused: remove by name instead
	/*public String removePlayerFromRunnersList(int i)
	{
		String runnerName = runnerList.remove(i);
		getConfig().set("runners."+runnerName, null);
		return runnerName;
		//saveConfig();
	}*/
	
	public boolean removePlayerFromRunnersList(String runnerName)
	{
		if (getConfig().isItemStack("runners."+runnerName))
		{	
			if (debugMode && verboseDebug)
			{
				getLogger().info("[CT-1] " + runnerName + " is being removed from the config runners list.");
			}
			getConfig().set("runners."+runnerName, null);
			
			if (debugMode && verboseDebug)
			{
				getLogger().info("[CT-1] " + runnerName + " is being removed from runnerList.");
			}
			for (String s: runnerList)
			{
				if (s.equals(runnerName))
				{
					runnerList.remove(s);
				}
			}
			//remove the runner's associated list of UUIDs if it exists
			if (getConfig().isList(runnerName))
			{
				getConfig().set(runnerName, null);
			}
			
			return true;
		}
		return false;
		//saveConfig();
	}
	
	public void getRunnerList()
	{
		if (getConfig().isConfigurationSection("runners"))
		{
			AbstractSet<String> runners = (AbstractSet<String>) getConfig().getConfigurationSection("runners").getKeys(false);
			Iterator<String> it = runners.iterator();
			List<String> runnerlistproto = new LinkedList<String>();
			while (it.hasNext())
			{
				runnerlistproto.add(it.next());
			}
			runnerList = runnerlistproto;
		}
		else //if runners is empty
		{
			runnerList = new LinkedList<String>();
		}
	}
	
	public String lineListRunners()
	{
		String lineList = "";
		if (runnerList.size() > 0)
		{
			for (String s: runnerList)
			{
				lineList = lineList + s + ", ";
			}
			return lineList.substring(0, lineList.length()-2); //trim the last comma 
		}
		return "";
	}
	
	public void announceEventStart()
	{
		getServer().broadcastMessage(ChatColor.GREEN + "[CT] The runner tagging has started!  Tag these runners to receive a prize for each runner: " + lineListRunners());
	}
	
	public void announceEventEnd()
	{
		getServer().broadcastMessage(ChatColor.GREEN + "[CT] The runner tagging has ended!  Thank you for participating!");
	}
	
	public boolean givePrizeToPlayer(String runnerName, Player p)
	{
		int emptyslot = p.getInventory().firstEmpty();
		if (emptyslot != -1) //ie. there is an empty slot in the player's inventory
		{
			p.getInventory().setItem(emptyslot, getConfig().getItemStack("runners."+runnerName));
			return true;
		}
		else //if inventory is full
		{
			return false;
		}
	}
}