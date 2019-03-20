package de.greenman1805.survival;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.nametagedit.plugin.NametagEdit;

import de.greenman1805.bountyextra.Bounty;
import de.greenman1805.ranks.Rank;

public class CombatAPI {
	public static Main m;
	public static YamlConfiguration cfg;
	public static List<Player> toRemove = new ArrayList<Player>();

	public CombatAPI(Main plugin) {
		CombatAPI.m = plugin;
		tick();
	}

	public static Boolean isEnabledWorld(String name) {
		Boolean contains = false;
		if (Main.worlds.contains(name)) {
			contains = true;
		}
		return contains;
	}

	public static Boolean isInCombat(Player p) {
		if (Main.combat.containsKey(p.getName())) {
			return true;
		} else {
			return false;
		}
	}

	public static void tick() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(m, new Runnable() {
			@Override
			public void run() {
				countDownCombat();
			}
		}, 20, 20);
	}
	
	public static boolean processDamageEvent(Player damager, Player victim) {
		String world = damager.getWorld().getName();
		if (!Main.pvpdisabled.contains(damager.getUniqueId())) {
			if (!Main.pvpdisabled.contains(victim.getUniqueId())) {
				if (CombatAPI.isEnabledWorld(world)) {
					CombatAPI.setInCombat(damager, victim, Main.seconds);
				}
			} else {
				damager.sendMessage(Main.prefix + "§4Dieser Spieler ist im Passiv Modus.");
				return true;
			}
		} else {
			damager.sendMessage(Main.prefix + "§4Du bist im Passiv Modus.");
			damager.sendMessage(Main.prefix + "§a/pvp §fum PvP zu aktivieren.");
			return true;
		}
		return false;
	}

	private static void countDownCombat() {
		HashMap<String, Integer> ncombat = new HashMap<String, Integer>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (isInCombat(p)) {
				int seconds = Main.combat.get(p.getName()) - 1;
				if (seconds <= 0) {
					p.sendMessage(Main.prefix + "§aDu bist nicht mehr im Kampf.");
				} else {
					if (toRemove.contains(p)) {
						p.sendMessage(Main.prefix + "§aDu bist nicht mehr im Kampf.");
						toRemove.remove(p);
					} else {
						ncombat.put(p.getName(), seconds);
					}
				}
			}
		}
		Main.combat = ncombat;
	}

	public static void updateNametag(final Player p) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {

			@Override
			public void run() {
				String prefix = Rank.getPlayerPrefixColor(p);
				if (Main.pvpdisabled.contains(p.getUniqueId())) {
					prefix = "§fPassiv §7| " + prefix;
				} else {
					prefix = "§6 " + Bounty.getBounty(p) + " §7| " + prefix;
				}
				NametagEdit.getApi().setPrefix(p, prefix);
			}

		}, 5);
	}

	public static void setInCombat(Player p, Player d, int seconds) {
		if (!isInCombat(d)) {
			Main.combat.put(d.getName(), seconds);
			d.sendMessage(Main.prefix + "§fDu hast " + p.getDisplayName() + "§f angegriffen. Nicht ausloggen!");
		} else {
			Main.combat.replace(d.getName(), seconds);
		}
		if (!isInCombat(p)) {
			Main.combat.put(p.getName(), seconds);
			p.sendMessage(Main.prefix + d.getDisplayName() + "§f hat dich angegriffen. Nicht ausloggen!");
		} else {
			Main.combat.replace(p.getName(), seconds);
		}
	}

}
