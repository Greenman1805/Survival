package de.greenman1805.survival;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.greenman1805.bountyextra.Bounty;
import de.greenman1805.shards.MoneyChangeEvent;

public class CombatListener implements Listener {
	public Main m;

	public CombatListener(Main plugin) {
		this.m = plugin;
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		String world = e.getFrom().getWorld().getName();

		if (!p.hasPermission("survival.exempt")) {
			if (CombatAPI.isEnabledWorld(world)) {
				if (CombatAPI.isInCombat(p)) {
					if (!e.getCause().equals(TeleportCause.ENDER_PEARL)) {
						e.setCancelled(true);
						p.sendMessage(Main.prefix + "§4Du kannst dich im Kampf nicht teleportieren!");
					}
				}
			}
		}
	}

	@EventHandler
	public void onMoneyChange(MoneyChangeEvent e) {
		OfflinePlayer op = Bukkit.getOfflinePlayer(e.getUniqueId());
		if (op != null && op.isOnline()) {
			Player p = (Player) op;
			ScoreboardAPI.updateScoreboard(p);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (CombatAPI.isInCombat(p) && !p.hasPermission("survival.exempt")) {
			Location l = e.getTo();
			for (ProtectedRegion r : Main.worldGuard.getRegionManager(l.getWorld()).getApplicableRegions(l)) {
				if (r.getId().equalsIgnoreCase("pvp")) {
					e.setCancelled(true);
					p.sendMessage(Main.prefix + "§4Du bist im Kampf und kannst nicht zurück in den PvP Schutzbereich!");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerHitDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (e.getDamage() > 0 && !e.isCancelled()) {
				Player victim = (Player) e.getEntity();
				Player damager = (Player) e.getDamager();
				if (CombatAPI.processDamageEvent(damager, victim)) {
					e.setCancelled(true);
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerPotionDamage(PotionSplashEvent e) {
		for (Entity entity : e.getAffectedEntities()) {
			if (entity instanceof Player && e.getEntity().getShooter() instanceof Player) {
				if (!e.isCancelled()) {
					Player damager = (Player) e.getEntity().getShooter();
					Player victim = (Player) e.getHitEntity();
					if (CombatAPI.processDamageEvent(damager, victim)) {
						e.setCancelled(true);
					}
				}
			}
		}

	}

	@EventHandler
	public void playerEntityDurationDamage(EntityCombustByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getCombuster() instanceof Arrow) {
			if (!e.isCancelled()) {
				Arrow arrow = (Arrow) e.getCombuster();
				if (arrow.getShooter() instanceof Player) {
					Player damager = (Player) arrow.getShooter();
					Player victim = (Player) e.getEntity();
					if (CombatAPI.processDamageEvent(damager, victim)) {
						e.setDuration(0);
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerArrowDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Arrow) {
			if (e.getDamage() > 0 && !e.isCancelled()) {
				Arrow arrow = (Arrow) e.getDamager();
				if (arrow.getShooter() instanceof Player) {
					Player damager = (Player) arrow.getShooter();
					Player victim = (Player) e.getEntity();
					if (CombatAPI.processDamageEvent(damager, victim)) {
						e.setCancelled(true);
					}
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKill(PlayerDeathEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = e.getEntity();
			if (p.getKiller() instanceof Player) {
				Player k = p.getKiller();
				if (p != k) {
					if (CombatAPI.isInCombat(p)) {
						CombatAPI.toRemove.add(k);
						CombatAPI.toRemove.add(p);
					}
					ScoreboardAPI.updateScoreboard(p);
					ScoreboardAPI.updateScoreboard(k);
					CombatAPI.updateNametag(p);
					CombatAPI.updateNametag(k);
					Leaderboard.updateAll();
				}
			}
		}

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		if (Bounty.getBounty(p) > 0 && Main.pvpdisabled.contains(p.getUniqueId())) {
			Main.pvpdisabled.remove(p.getUniqueId());
		}
		ScoreboardAPI.updateScoreboard(p);
		Leaderboard.updateAll();
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {

			@Override
			public void run() {
				if (Main.pvpdisabled.contains(p.getUniqueId())) {
					p.sendMessage(Main.prefix + "§aDu bist im Passiv Modus.");
					p.sendMessage(Main.prefix + "§a/pvp §fum zu wechseln.");
				} else {
					p.sendMessage(Main.prefix + "§aPvP ist für dich aktiv.");
					p.sendMessage(Main.prefix + "§a/pvp §fum in den Passiv Modus zu wechseln.");
				}
				CombatAPI.updateNametag(p);
			}

		}, 20 * 3);
	}
	

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		Leaderboard.updateAll();
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		if (CombatAPI.isInCombat(p)) {
			for (ItemStack i : p.getInventory().getContents()) {
				if (i != null) {
					p.getWorld().dropItemNaturally(p.getLocation(), i);
					p.getInventory().remove(i);
				}
			}
			p.setHealth(0.0);
			Bukkit.getServer().broadcastMessage(Main.prefix + p.getDisplayName() + " §chat sich im Kampf ausgeloggt.");
			Main.combat.remove(p.getName());
		}
	}

}
