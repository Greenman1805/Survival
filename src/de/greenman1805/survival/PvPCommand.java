package de.greenman1805.survival;

import java.util.concurrent.TimeUnit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.greenman1805.bountyextra.Bounty;

public class PvPCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = null;
		if (sender instanceof Player) {
			p = (Player) sender;
		}
		if (p != null) {
			if ((cmd.getName().equalsIgnoreCase("pvp"))) {
				if (args.length == 0) {

					if (!Main.pvpDelay.containsKey(p.getUniqueId()) || Main.pvpDelay.get(p.getUniqueId()) < System.currentTimeMillis()) {

						if (Main.pvpdisabled.contains(p.getUniqueId())) {
							Main.pvpdisabled.remove(p.getUniqueId());
							p.sendMessage(Main.prefix + "§aPvP ist aktiv.");
							CombatAPI.updateNametag(p);
							ScoreboardAPI.updateScoreboard(p);
							Main.pvpDelay.remove(p.getUniqueId());
							Main.pvpDelay.put(p.getUniqueId(), System.currentTimeMillis() + 300000);
						} else {
							if (CombatAPI.isInCombat(p)) {
								p.sendMessage(Main.prefix + "§4Du bist gerade im Kampf!");
							} else if (Bounty.getBounty(p) > 0) {
								p.sendMessage(Main.prefix + "§4Du hast Kopfgeld und kannst nicht in den Passiv Modus gehen!");
								p.sendMessage(Main.prefix + "§aBezahle dein Kopfgeld am Spawn!");
							} else {
								Main.pvpdisabled.add(p.getUniqueId());
								p.sendMessage(Main.prefix + "§aDu bist jetzt im Passiv Modus.");
								CombatAPI.updateNametag(p);
								ScoreboardAPI.updateScoreboard(p);
								Main.pvpDelay.remove(p.getUniqueId());
								Main.pvpDelay.put(p.getUniqueId(), System.currentTimeMillis() + 300000);
							}
						}
					} else {
						long until = Main.pvpDelay.get(p.getUniqueId()) - System.currentTimeMillis();
						long minutes = TimeUnit.MILLISECONDS.toMinutes(until);
						long seconds = TimeUnit.MILLISECONDS.toSeconds(until - minutes * 60 * 1000);
						p.sendMessage(Main.prefix + "§4Du kannst den PvP Modus erst in " + minutes + " Minuten und " + seconds +  " Sekunden wieder ändern.");
					}

				}

				if (args.length == 1 && args[0].equalsIgnoreCase("paybounty")) {
					int bounty = Bounty.getBounty(p);
					if (bounty > 0) {
						int account_after = (int) (Main.econ.getBalance(p) - bounty);
						if (account_after >= 0) {
							Main.econ.withdrawPlayer(p, bounty);
							Bounty.setBounty(p, 0);
							ScoreboardAPI.updateScoreboard(p);
							CombatAPI.updateNametag(p);
							p.sendMessage(Main.prefix + "§aDu hast dein Kopfgeld bezahlt.");
						} else {
							p.sendMessage(Main.prefix + "§4Du hast nicht genug Shards.");
						}
					} else {
						p.sendMessage(Main.prefix + "§4Du hast kein Kopfgeld.");
					}
				}
			}

		}
		return false;
	}

}
