package de.greenman1805.survival;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import de.greenman1805.bountyextra.Bounty;
import de.greenman1805.statsextra.StatsAPI;

public class Leaderboard {
	enum LeaderboardType {
		Kills, Bounty
	}

	public static List<Leaderboard> boards = new ArrayList<Leaderboard>();
	public Location location;
	private Hologram holo;
	LeaderboardType type;
	int updateTime;

	public Leaderboard(Location location, LeaderboardType type, int updateTime) {
		this.location = location;
		this.type = type;
		this.updateTime = updateTime;
		if (type == LeaderboardType.Kills) {
			updateKillsBoard();
		} else if (type == LeaderboardType.Bounty) {
			updateBountyBoard();
		}
		boards.add(this);
	}

	public static void deleteAll() {
		for (Leaderboard b : boards) {
			b.delete();
		}
	}

	public void delete() {
		holo.delete();
	}

	public static void updateAll() {
		for (Leaderboard b : boards) {
			if (b.type == LeaderboardType.Kills) {
				b.updateKillsBoard();
			} else if (b.type == LeaderboardType.Bounty) {
				b.updateBountyBoard();
			}
		}
	}

	private void updateBountyBoard() {
		if (holo == null) {
			holo = HologramsAPI.createHologram(Main.plugin, location);
		}
		holo.clearLines();
		holo.appendTextLine("§6Kopfgeld Leaderboard:");

		final TreeMap<Double, String> tm = new TreeMap<Double, String>(Collections.reverseOrder());
		Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, new Runnable() {

			@Override
			public void run() {
				double add = 0.0;
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (add == 1.0) {
						break;
					}
					double bounty = Bounty.getBounty(p) + add;
					tm.put(bounty, p.getName());
					add += 0.0001;

				}

				Set<?> s = tm.entrySet();
				Iterator<?> it = s.iterator();
				int i = 0;
				while (i < 10 && it.hasNext()) {
					Entry<?, ?> entry = (Entry<?, ?>) it.next();
					final String value = (String) entry.getValue();
					int a = 16 - value.length();
					String b = "";
					while (a != 0) {
						b += " ";
						a--;
					}
					i++;
					final Player p = Bukkit.getPlayer(value);
					final int place = i;
					final String playerSpace = b;
					Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {

						@Override
						public void run() {
							holo.appendTextLine("§b" + place + ". §f" + value + "§f: " + playerSpace + " §6" + Bounty.getBounty(p));
						}

					});
				}
			}

		});

	}

	private void updateKillsBoard() {
		if (holo == null) {
			holo = HologramsAPI.createHologram(Main.plugin, location);
		}
		holo.clearLines();
		holo.appendTextLine("§aKills Leaderboard:");

		final TreeMap<Double, String> tm = new TreeMap<Double, String>(Collections.reverseOrder());

		Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, new Runnable() {

			@Override
			public void run() {
				double add = 0.0;
				for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
					if (add == 1.0) {
						break;
					}
					double kills = StatsAPI.getKills(p.getUniqueId());

					tm.put(new Double(kills), p.getUniqueId().toString());
					add += 0.0001;
				}

				Set<?> s = tm.entrySet();
				Iterator<?> it = s.iterator();
				int i = 0;
				while (i < 10 && it.hasNext()) {
					Entry<?, ?> entry = (Entry<?, ?>) it.next();
					String value = (String) entry.getValue();

					final OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(value));
					if (op.getName() != null) {
						int a = 16 - op.getName().length();
						String b = "";
						while (a != 0) {
							b += " ";
							a--;
						}
						i++;
						final int place = i;
						final String playerSpace = b;
						Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {

							@Override
							public void run() {
								holo.appendTextLine("§b" + place + ". §f" + op.getName() + "§f: " + playerSpace + " §a" + StatsAPI.getKills(op.getUniqueId()));
							}

						});
					}
				}
			}

		});

	}

}
