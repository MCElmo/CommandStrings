package me.MC_Elmo.commands;

import me.MC_Elmo.CommandStrings;
import me.MC_Elmo.utils.MainUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


public class CommandStringExecutor implements CommandExecutor
{
    private static CommandStrings plugin;
    private static MainUtils util;
    private FileConfiguration config;
    private String prefix;
    private String title;
    private LinkedHashMap<String,Long> commands = new LinkedHashMap<String, Long>();

    public CommandStringExecutor(CommandStrings plugin, MainUtils util)
    {
        this.plugin = plugin;
        this.util = util;
        this.prefix = plugin.getPrefix();
        config = this.plugin.getConfig();
        title = ChatColor.STRIKETHROUGH + "-----" + ChatColor.RESET + prefix + ChatColor.RESET + ChatColor.STRIKETHROUGH + "-----";
    }


    public static MainUtils getUtil()
    {
        return util;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if(!(sender.isOp()))
        {
            sender.sendMessage(prefix + ChatColor.RED +" You must be OP to use this command!");
            return false;
        }
        switch(args.length)
        {
            case 0:
            {
                sender.sendMessage(prefix + ChatColor.RED + "Not Enough Arguments! Try /cs help");
                return false;
            }
            case 1:
            {
                if (args[0].equalsIgnoreCase("help"))
                {
                    sender.sendMessage(title);
                    sender.sendMessage(ChatColor.GREEN + "/commandstring help : " + ChatColor.DARK_GREEN + "Display Plugin help.");
                    sender.sendMessage(ChatColor.GREEN + "/commandstring list : " + ChatColor.DARK_GREEN  + "List available CommandStrings");
                    sender.sendMessage(ChatColor.GREEN + "/commandstring list -noplayer: " + ChatColor.DARK_GREEN  + "List CommandStrings that dont use a player.");
                    sender.sendMessage(ChatColor.GREEN + "/commandstring player <player> <commandstring> : " + ChatColor.DARK_GREEN + "Execute a Command String with <player>.");
                    sender.sendMessage(ChatColor.GREEN + "/commandstring string <commandstring> : " + ChatColor.DARK_GREEN + "Execute a Command String.");
                    sender.sendMessage(ChatColor.GREEN + "/commandstring reload : " + ChatColor.DARK_GREEN + "Reload the Config.");
                    return true;
                }else if(args[0].equalsIgnoreCase("list"))
                {

                    ConfigurationSection stringSec = config.getConfigurationSection("Command Strings");
                    if(stringSec == null)
                    {
                        util.log("Error! Unable to get Configuration Section From config.yml");
                    }
                    Set<String> stringSet = stringSec.getKeys(false);
                    if(stringSet == null)
                    {
                        sender.sendMessage(prefix + "There are no available Command Strings!");
                        return false;
                    }
                    Iterator<String> stringIterator = stringSet.iterator();
                    sender.sendMessage(prefix + ChatColor.GREEN + "Available Command Strings : ");
                    while(stringIterator.hasNext())
                    {
                        sender.sendMessage(ChatColor.DARK_GREEN + " - " + stringIterator.next());
                        stringIterator.remove();
                    }
                    return true;
                }else if(args[0].equalsIgnoreCase("reload"))
                {
                    plugin.reloadConfig();
                    config = plugin.getConfig();
                    plugin.saveConfig();
                    sender.sendMessage(prefix + ChatColor.GREEN + " Successfully reloaded the config!");
                    return true;
                }else
                {
                    sender.sendMessage(prefix + ChatColor.RED + "Incorrect Arguments! Try /cs help");
                    return false;
                }
            }
            case 2:
            {
                if(args[0].equalsIgnoreCase("string"))
                {
                    if (!(config.contains("Command Strings." + args[1])))
                    {
                        sender.sendMessage(prefix + ChatColor.RED + "Invalid Command String! Do /cs list for All Available Command Strings.");
                        return false;
                    }
                    List<String> stringCommandStrings = config.getStringList("Command Strings." + args[1] + ".commands");
                    for (String line : stringCommandStrings)
                    {
                        if (line.contains("{PLAYERNAME}"))
                        {
                            sender.sendMessage(prefix + ChatColor.RED + "Invalid CommandString! Command String cannot use a player!");
                            sender.sendMessage(prefix + ChatColor.AQUA + "Try /cs list -noplayer ");
                            sender.sendMessage(prefix + ChatColor.AQUA + "For commands that don't use a player.");
                            return false;
                        }
                    }
                    Long delay = 0L;
                    List<String> commandStrings = config.getStringList("Command Strings." + args[1] + ".commands");
                    ArrayList<String> first = new ArrayList<String>();
                    if(config.getBoolean("Command Strings." + args[1] + ".delay.global_delay", false))
                    {
                        delay = util.parseTime(config.getString("Command Strings." + args[1] + ".delay.global_delayString"));
                        int count = 0;
                        for (String line : commandStrings)
                        {
                            line = line.trim();
                            if (line.length() > 1)
                            {
                                if (line.startsWith("/"))
                                {
                                    String command = line.substring(1);
                                    if (!(config.getBoolean("Command Strings." + args[1] + ".delay.firstExecuteDelay")) && count == 0)
                                    {
                                        commands.put(command,0L);
                                        first.add(command);
                                        count++;
                                        continue;
                                    } else
                                    {
                                        commands.put(command, delay * 20L);
                                        count++;
                                        continue;
                                    }
                                } else
                                {
                                    util.log("Command : " + line + "in " + args[1] + "is invalid!");
                                    continue;
                                }
                            } else
                            {
                                util.log("Command : \"" + line + "\" in " + args[1] + "is invalid!");
                                continue;
                            }

                        }
                    }
                    else
                    {
                        for (String line : commandStrings)
                        {
                            line = line.trim();
                            if (line.startsWith("/"))
                            {
                                String command = line.substring(1);
                                commands.put(command, delay * 20L);
                                delay = 0L;
                            } else if (line.startsWith("delay"))
                            {

                                line = line.replaceAll("delay", "");
                                line = line.trim();
                                delay = util.parseTime(line);
                                continue;
                            } else
                            {
                                util.log("Command : \"" + line + "\" in " + args[1] + "is invalid!");
                                continue;
                            }
                        }

                    }
                    Iterator<Map.Entry<String,Long>> it = commands.entrySet().iterator();
                    Long currentDelay = 0L;
                    while(it.hasNext())
                    {
                        final Map.Entry<String,Long> commandEntry = it.next();
                        if(first.size() > 0)
                        {
                            if (first.get(0).equals(commandEntry.getKey()))
                            {
                                new BukkitRunnable()
                                {
                                    public void run()
                                    {
                                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandEntry.getKey());
                                    }
                                }.runTaskLater(this.plugin, commandEntry.getValue());
                                currentDelay += commandEntry.getValue();
                            }
                        }
                        else
                        {
                            currentDelay += commandEntry.getValue();
                            new BukkitRunnable()
                            {
                                public void run()
                                {
                                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandEntry.getKey());
                                }
                            }.runTaskLater(this.plugin,currentDelay);
                        }
                        it.remove();
                    }
                    sender.sendMessage(prefix + ChatColor.GREEN + "Command String : " + ChatColor.DARK_GREEN +  args[1] + " successfully started!");
                    return true;

                }
                else if(args[0].equalsIgnoreCase("list"))
                {
                    if(args[1].equalsIgnoreCase("-noplayer"))
                    {
                        List<String> noPlayers = new ArrayList<String>();
                        ConfigurationSection stringSec = config.getConfigurationSection("Command Strings");
                        if (stringSec == null)
                        {
                            util.logSevere("Error! Unable to get Configuration Section From config.yml");
                            sender.sendMessage(prefix + ChatColor.RED  + "Config Error!");
                            return false;
                        }
                        Set<String> stringSet = stringSec.getKeys(false);
                        if (stringSet == null)
                        {
                            sender.sendMessage(prefix + "There are no available Command Strings!");
                            return false;
                        }
                        Iterator<String> stringIterator = stringSet.iterator();
                        while (stringIterator.hasNext())
                        {

                            String next = stringIterator.next();
                            for(String noPlayerString : config.getStringList("Command Strings." + next + ".commands"))
                            {
                                if(!(noPlayerString.contains("{PLAYERNAME}")))
                                {
                                    if(!(noPlayers.contains(next)))
                                    {
                                        noPlayers.add(next);
                                        continue;
                                    }
                                    else
                                        continue;
                                }else
                                {
                                    noPlayers.remove(next);
                                    break;
                                }
                            }
                            stringIterator.remove();
                        }
                        sender.sendMessage(prefix + ChatColor.GREEN + "Available Command Strings -noplayer : ");
                        Iterator<String> noPlayerIterator = noPlayers.iterator();
                        while(noPlayerIterator.hasNext())
                        {
                            sender.sendMessage(ChatColor.DARK_GREEN + " - " + noPlayerIterator.next());
                            noPlayerIterator.remove();
                        }
                        return true;
                    }else
                    {
                        sender.sendMessage(prefix + ChatColor.RED + "Incorrect Arguments! Try /cs help");
                        return false;
                    }
                }else
                {
                    sender.sendMessage(prefix + ChatColor.RED + "Incorrect Arguments! Try /cs help");
                    return false;
                }

            }
            case 3:
            {
                if(!(args[0].equalsIgnoreCase("player")))
                {
                    sender.sendMessage(prefix + ChatColor.RED + "Incorrect Arguments! Try /cs help");
                    return false;
                }
                    Player target = Bukkit.getPlayer(args[1]);
                    if(target == null)
                    {
                        sender.sendMessage(prefix + ChatColor.RED + "Could not find player : " + args[1] + "!");
                        return false;
                    }
                    if(!(config.contains("Command Strings." + args[2])))
                    {
                        sender.sendMessage(prefix + ChatColor.RED + "Invalid Command String! Do /cs list for All Available Command Strings.");
                        return false;
                    }

                        Long delay = 0L;
                        List<String> commandStrings = config.getStringList("Command Strings." + args[2] + ".commands");
                        ArrayList<String> first = new ArrayList<String>();
                        if(config.getBoolean("Command Strings." + args[2] + ".delay.global_delay",false))
                        {
                            delay = util.parseTime(config.getString("Command Strings." + args[2] + ".delay.global_delayString"));
                            int count = 0;
                            for (String line : commandStrings)
                            {
                                line = line.trim();
                                line = line.replace("{PLAYERNAME}", target.getName());

                                if (line.length() > 1)
                                {
                                    if (line.startsWith("/"))
                                    {
                                        String command = line.substring(1);
                                        if (!(config.getBoolean("Command Strings." + args[2] + ".delay.firstExecuteDelay")) && count == 0)
                                        {
                                            commands.put(command,0L);
                                            first.add(command);
                                            count++;
                                            continue;
                                        } else
                                        {
                                            commands.put(command, delay * 20L);
                                            count++;
                                            continue;
                                        }
                                    } else
                                    {
                                        util.log("Command : \"" + line + "\" in " + args[2] + "is invalid!");
                                        continue;
                                    }
                                } else
                                {
                                    util.log("Command : \"" + line + "\" in " + args[2] + "is invalid!");
                                    continue;
                                }

                            }
                        }
                        else
                        {
                            for (String line : commandStrings)
                            {
                                line = line.trim();
                                line = line.replace("{PLAYERNAME}", target.getName());

                                if (line.startsWith("/"))
                                {
                                    String command = line.substring(1);
                                      commands.put(command, delay * 20L);
                                    delay = 0L;
                                } else if (line.startsWith("delay"))
                                {

                                    line = line.replaceAll("delay", "");
                                    line = line.trim();
                                    delay = util.parseTime(line);
                                    continue;
                                } else
                                {
                                    util.log("Command : \"" + line + "\" in " + args[2] + "is invalid!");
                                    continue;
                                }
                            }

                        }
                    Iterator<Map.Entry<String,Long>> it = commands.entrySet().iterator();
                util.log(String.valueOf(commands.entrySet()));
                    Long currentDelay = 0L;
                while(it.hasNext())
                {
                    final Map.Entry<String, Long> commandEntry = it.next();
                    if(first.size() > 0)
                    {
                        if (first.get(0).equals(commandEntry.getKey()))
                        {
                            new BukkitRunnable()
                            {
                                public void run()
                                {
                                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandEntry.getKey());
                                }
                            }.runTaskLater(this.plugin, commandEntry.getValue());
                            currentDelay += commandEntry.getValue();
                        }
                    }
                    else
                    {
                        currentDelay += commandEntry.getValue();
                        util.log(commandEntry.getKey() + " : " + currentDelay);
                        if(currentDelay == 0L)
                        {
                            util.log(commandEntry.getKey() + " ran");
                            new BukkitRunnable()
                            {
                                public void run()
                                {
                                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandEntry.getKey());
                                    plugin.getLogger().info("Dispatched : " + commandEntry.getKey());
                                }
                            }.runTask(this.plugin);
                        }else
                        {
                            new BukkitRunnable()
                            {
                                public void run()
                                {
                                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandEntry.getKey());
                                }
                            }.runTaskLater(this.plugin, currentDelay);
                        }
                    }
                    it.remove();
                }
                    sender.sendMessage(prefix + ChatColor.GREEN + "Command String : " + ChatColor.DARK_GREEN +  args[2] + " successfully started!");
                return true;
             }
            default:
            {
                sender.sendMessage(prefix + ChatColor.RED + "Too many Arguments! Try /cs help");
                return false;
            }

        }
    }






}
