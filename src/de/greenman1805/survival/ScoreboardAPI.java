package de.greenman1805.survival;

import org.bukkit.entity.Player;

import com.coloredcarrot.api.sidebar.Sidebar;
import com.coloredcarrot.api.sidebar.SidebarString;

import de.greenman1805.bountyextra.Bounty;
import de.greenman1805.statsextra.StatsAPI;

public class ScoreboardAPI {

	public static void updateScoreboard(Player p) {
		Sidebar sidebar = new Sidebar(" §9Skyshard§f.de ", Main.plugin);
		SidebarString line1 = new SidebarString("§f» §aKills:");
		SidebarString line2 = new SidebarString("§f» §f" + StatsAPI.getKills(p.getUniqueId()));
		SidebarString line3 = new SidebarString("§f» §cDeaths:");
		SidebarString line4 = new SidebarString("§f» §f" + StatsAPI.getDeaths(p.getUniqueId()));
		SidebarString line5 = new SidebarString("§f» §6Kopfgeld:");
		SidebarString line6 = new SidebarString("§f» §f" + Bounty.getBounty(p));
		SidebarString line7 = new SidebarString("§f» §9Shards:");
		SidebarString line8 = new SidebarString("§f» §f" + Main.econ.getBalance(p));
		SidebarString line9 = new SidebarString("§f» §ePvP:");
		SidebarString line10 = new SidebarString("§f» §aan");
		SidebarString line11 = new SidebarString("§f» §caus");
		
		sidebar.addEmpty();
		sidebar.addEntry(line1);
		sidebar.addEntry(line2);
		sidebar.addEmpty();
		sidebar.addEntry(line3);
		sidebar.addEntry(line4);
		sidebar.addEmpty();
		sidebar.addEntry(line5);
		sidebar.addEntry(line6);
		sidebar.addEmpty();
		sidebar.addEntry(line7);
		sidebar.addEntry(line8);
		sidebar.addEmpty();
		sidebar.addEntry(line9);
		if (Main.pvpdisabled.contains(p.getUniqueId())) {
			sidebar.addEntry(line11);
		} else {
			sidebar.addEntry(line10);
		}
		sidebar.showTo(p);
	}

}
