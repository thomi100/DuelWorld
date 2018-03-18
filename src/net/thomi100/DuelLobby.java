package net.thomi100;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class DuelLobby {

	// TODO: Bei queue wiederholende Nachricht dass in Warteschlange

	public static Plugin plugin;

	public static Hologram rankHolo = null;
	
	public static void refreshRankLead() {
		
		// Removing the old hologram
		if(rankHolo != null) {
			rankHolo.delete();
		}
		
		// Creating the new Hologram
		Location loc = DuelWorld.rankholo;
		if(loc != null) {
			ArrayList<String> list = DuelStats.getLeadRanks();
			rankHolo = HologramsAPI.createHologram(plugin, loc);
			rankHolo.appendTextLine("§9§lDuel§e§lWorld");
			rankHolo.appendTextLine(" §f ");
			rankHolo.appendTextLine("§e§lHöchste Ränge");
			rankHolo.appendTextLine(" §f ");
			for(int i = 0; i <= 9; i++) {
				if(list.size() > i) {
					String player = list.get(i).split(";")[0];
					String amount = list.get(i).split(";")[1];
					rankHolo.appendTextLine("§a§l" + (i + 1) + ": §r§e" + player + "§r§8: §c" + amount);
				}
			}
			rankHolo.appendTextLine(" §f ");
			rankHolo.appendTextLine("§7Aktualisiert: §r§a" + ThomiAPI.getTime("HH:mm:ss"));
		}
		
		// Making the task run further
		BukkitRunnable run = new BukkitRunnable() {
			@Override
			public void run() {
				refreshRankLead();
			}
		};
		if(loc != null && loc.getWorld().getPlayers().isEmpty()) {
			run.runTaskLater(plugin, 30*20);
		} else {
			run.runTaskLater(plugin, 2*60*20);
		}
		
	}
	public static Hologram winHolo = null;
	
	public static void refreshWinLead() {
		
		// Removing the old hologram
		if(winHolo != null) {
			winHolo.delete();
		}
		
		// Creating the new Hologram
		Location loc = DuelWorld.winholo;
		if(loc != null) {
			ArrayList<String> list = DuelStats.getLeadWins();
			winHolo = HologramsAPI.createHologram(plugin, loc);	
			winHolo.appendTextLine("§9§lDuel§e§lWorld");
			winHolo.appendTextLine(" §f ");
			winHolo.appendTextLine("§e§lMeiste Siege");
			winHolo.appendTextLine(" §f ");
			for(int i = 0; i <= 9; i++) {
				if(list.size() > i) {
					String player = list.get(i).split(";")[0];
					String amount = list.get(i).split(";")[1];
					winHolo.appendTextLine("§a§l" + (i + 1) + ": §r§e" + player + "§r§8: §c" + amount);
				}
			}
			winHolo.appendTextLine(" §f ");
			winHolo.appendTextLine("§7Aktualisiert: §r§a" + ThomiAPI.getTime("HH:mm:ss"));
		}
		
		// Making the task run further
		BukkitRunnable run = new BukkitRunnable() {
			@Override
			public void run() {
				refreshWinLead();
			}
		};
		if(loc != null && loc.getWorld().getPlayers().isEmpty()) {
			run.runTaskLater(plugin, 30*20);
		} else {
			run.runTaskLater(plugin, 2*60*20);
		}
		
	}

}
