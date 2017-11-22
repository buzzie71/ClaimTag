package com.gmail.buzziespy.ClaimTag;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClaimTag extends JavaPlugin implements Listener{
	
public static boolean runnerTag;
public static List<String> runnerList;
public static boolean debugMode;
public static boolean suppressGlobalAlerts;

	
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
		getLogger().info("Loading global alerts...");
		debugMode = getConfig().getBoolean("suppress-global-alerts");
	}
	
	@Override
	public void onDisable()
	{
		//save the config file
		getLogger().info("Saving config!");
		this.saveConfig();
	}
	
	@EventHandler
	public void onTag(PlayerInteractEntityEvent e)
	{
		if (debugMode)
		{
			getLogger().info("PlayerInteractEntityEvent: " + e.getPlayer().getName() + " interacted with " + e.getRightClicked().getName() + " using " + e.getHand().toString());
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
			 *  2: /ct suppress-alerts [true|false] - Mutes server-wide alerts for this plugin (for testing)
			 *  2: /ct setprize <runnername> - Sets tagging prize for a particular runner using what's selected in inventory.  
			 *  
			 *  /runners: Shows all runners (and whether player has tagged them) - for players
			 *  
			 */
			if (args.length == 0)
			{
				sender.sendMessage(ChatColor.AQUA + "https://github.com/buzzie71/MaskOfFutures/blob/master/README.md");
				sender.sendMessage(ChatColor.AQUA + "=====ClaimTag, v"+ getDescription().getVersion() +"=====");
				sender.sendMessage(ChatColor.AQUA + "Runner tag: " + runnerTag);
				sender.sendMessage(ChatColor.AQUA + "Debug mode: " + debugMode);
				sender.sendMessage(ChatColor.AQUA + "Suppress broadcasts: " + suppressGlobalAlerts);
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
				else if (args[0].equalsIgnoreCase("setprize"))
				{
					sender.sendMessage(ChatColor.RED + "/ct setprize <playername>");
				}
				else if (args[0].equalsIgnoreCase("save"))
				{
					saveConfig();
					sender.sendMessage(ChatColor.GREEN + "ClaimTag Config changes saved to file!");
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "/ct [runnerlist|addrunner|delrunner|debug-mode|runner-tag|suppress-alerts|setprize|getprize|save]");
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
				}
				else if (args[0].equalsIgnoreCase("delrunner"))
				{
					//int index = Integer.parseInt(args[1]);
					//sender.sendMessage(ChatColor.RED + removePlayerFromRunnersList(index-1) + " has been removed from the runners list.");
					//try removing by name instead:
					
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
							debugMode = true;
							sender.sendMessage(ChatColor.GREEN + "Debug mode set to true. Debug messages will appear in console.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "/ct debug-mode <true|false>");
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
							debugMode = true;
							sender.sendMessage(ChatColor.GREEN + "Runner tag set to true. Players will not be able to tag runners for prizes.");
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
							suppressGlobalAlerts = true;
							sender.sendMessage(ChatColor.GREEN + "Suppress broadcasts set to false. Server-wide broadcasts from this plugin will display to all online players.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "/ct suppress-broadcasts <true|false>");
					}
				}
				else if (args[0].equalsIgnoreCase("setprize"))
				{
					if (sender instanceof Player)
					{
						Player p = (Player)sender;
						String runnerName = args[1];
						//DEBUG:
						getLogger().info("Attempting to access runners."+runnerName);
						if (getConfig().isItemStack("runners."+runnerName))
						{
							ItemStack i = p.getEquipment().getItemInMainHand();
							getConfig().set("runners."+runnerName, i);
							p.sendMessage(ChatColor.GREEN + "Prize for tagging " + runnerName + " has been set.");
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
							if (givePrizeToPlayer(p, runnerName))
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
				else
				{
					sender.sendMessage(ChatColor.RED + "/ct [runnerlist|addrunner|delrunner|debug-mode|runner-tag|suppress-alerts|setprize|getprize|save]");
				}
			}
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("runners")) //public command
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
	public static boolean hasTagged(String runnerName, Player p)
	{
		//FILL THIS IN!
		return true;
	}
	
	public void addPlayerToRunnersList(String s)
	{
		//runnerList.add(s);
		//getConfig().set("runners", runnerList);
		
		getConfig().createSection("runners." + s);
		ItemStack empty = new ItemStack(Material.AIR);
		getConfig().set("runners." + s, empty);
		//saveConfig();
	}
	
	public String removePlayerFromRunnersList(int i)
	{
		String runnerName = runnerList.remove(i);
		getConfig().set("runners."+runnerName, null);
		return runnerName;
		//saveConfig();
	}
	
	public String removePlayerFromRunnersList(String runnerName)
	{
		if (getConfig().isItemStack("runners."+runnerName))
		{	
			getConfig().set("runners."+runnerName, null);
		}
		return runnerName;
		//saveConfig();
	}
	
	public void getRunnerList()
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
	
	public boolean givePrizeToPlayer(Player p, String runnerName)
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