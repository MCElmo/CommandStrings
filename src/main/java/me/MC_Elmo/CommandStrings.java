package me.MC_Elmo;

import me.MC_Elmo.commands.CommandStringExecutor;
import me.MC_Elmo.utils.MainUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.logging.Logger;
@SuppressWarnings("unused")

public class CommandStrings extends JavaPlugin
{
    private CommandStrings plugin;
    private FileConfiguration config;
    private String prefix = "&e[&cCommand&aStrings&e]&r";
    private Logger logger;
    private CommandExecutor executor = new CommandStringExecutor(this, new MainUtils(this));
    private BukkitScheduler scheduler;

    public void onEnable()
    {
        plugin = this;
        config = super.getConfig();
        config.options().copyDefaults(true);
        saveConfig();
        logger = getLogger();
        logger.info(prefix  + " Plugin by MC_Elmo.");
        this.getCommand("commandstring").setExecutor(executor);
    }

    public void onDisable()
    {
        logger.info(prefix + " Plugin Disabled!");
    }




    public BukkitScheduler getScheduler()
    {
        return this.scheduler;
    }
    public String getPrefix()
    {
        return prefix;
    }

    public Logger getPlugLogger()
    {
        return logger;
    }

    public CommandStrings getPlugin()
    {
        return plugin;
    }










}
