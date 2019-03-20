package de.greenman1805.survival;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.greenman1805.survival.Leaderboard.LeaderboardType;
import net.milkbowl.vault.economy.Economy;


public class Main extends JavaPlugin {
	public static Economy econ = null;
	public static String prefix = "§f[§9Survival§f] §7";
	public static WorldGuardPlugin worldGuard;

	public static HashMap<String, Integer> combat = new HashMap<String, Integer>();
	public static List<UUID> pvpdisabled = new ArrayList<UUID>();
	public static HashMap<UUID, Long> pvpDelay = new HashMap<UUID, Long>();
	public static int seconds;
	public static List<String> worlds = new ArrayList<String>();
	
	public static Main plugin;

	@Override
	public void onEnable() {
		if (!setupEconomy()) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		PluginManager pluginManager = getServer().getPluginManager();
		worldGuard = (WorldGuardPlugin) pluginManager.getPlugin("WorldGuard");
		plugin = this;
		checkUserdataDirectory();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new CombatListener(this), this);
		new CombatAPI(this);
		registerCommands("pvp", new PvPCommand());
		getValues();
		new Leaderboard(new Location(Bukkit.getWorld("Survival"), 1505, 74,-1848), LeaderboardType.Kills, 60);
		new Leaderboard(new Location(Bukkit.getWorld("Survival"), 1505, 74,-1840), LeaderboardType.Bounty, 60);
	}
	
	@Override
	public void onDisable() {
		Leaderboard.deleteAll();
	}



	public void registerCommands(String cmd, CommandExecutor exe) {
		getCommand(cmd).setExecutor(exe);
	}

	private void checkUserdataDirectory() {
		File file1 = new File("plugins//Survival");
		File file2 = new File("plugins//Survival//config.yml");

		if (!file1.isDirectory()) {
			file1.mkdir();
		}

		if (!file2.exists()) {
			try {
				file2.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file2);
			List<String> list = new ArrayList<String>();
			list.add("Survival");
			list.add("Nether");
			cfg.set("Combat time in seconds", 10);
			cfg.set("enabledWorlds", list);
			try {
				cfg.save(file2);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void getValues() {
		File file = new File("plugins//Survival//config.yml");
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		seconds = cfg.getInt("Combat time in seconds");
		worlds = cfg.getStringList("enabledWorlds");
	}
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

}
