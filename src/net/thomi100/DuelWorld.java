package net.thomi100;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.google.common.base.Joiner;

public class DuelWorld implements Listener {

	private static DuelWorlds plugin;

	public DuelWorld(DuelWorlds main) {
		
		DuelWorld.plugin = main;
		DuelWorld.plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
	}

	public String sign0 = "§7[§9Duel§eWorld§7]";
	public String sign1 = " §f ";
	public String sign2_j = "§a§lWarteschlange";
	public String sign3_j = "§a§lbetreten";
	public String sign2_l = "§c§lWarteschlange";
	public String sign3_l = "§c§lverlassen";

	public static String getHelp() {
	    return ThomiAPI.prefixGeneral + "§aWarteschlange betreten§7: §e/dw join" +
                " \n" + ThomiAPI.prefixGeneral + "§aWarteschlange verlassen§7: §e/dw leave" +
                " \n" + ThomiAPI.prefixGeneral + "§aSpieler herausfordern§7: §e/dw duel <Spieler>" +
                " \n" + ThomiAPI.prefixGeneral + "§aAnfrage annehmen§7: §e/dw accept <Spieler>" +
                " \n" + ThomiAPI.prefixGeneral + "§aStats§7: §e/dw stats [Spieler]";
    }

	public static Location ending = null;
	public static Location npc = null;
	public static Location winholo = null;
	public static Location rankholo = null;
	
	public void setLocations() {
		if(plugin.getConfig().getString("DuelWorld.ending") != null) {
			ending = ThomiAPI.toLocation(plugin.getConfig().getString("DuelWorld.ending"));
		}
		if(plugin.getConfig().getString("DuelWorld.NPC") != null) {
			npc = ThomiAPI.toLocation(plugin.getConfig().getString("DuelWorld.NPC"));
		}
		if(plugin.getConfig().getString("DuelWorld.winholo") != null) {
			winholo = ThomiAPI.toLocation(plugin.getConfig().getString("DuelWorld.winholo"));
		}
		if(plugin.getConfig().getString("DuelWorld.rankholo") != null) {
			rankholo = ThomiAPI.toLocation(plugin.getConfig().getString("DuelWorld.rankholo"));
		}
	}
	
	public void command(CommandSender sender, Command cmd, String[] args) {
		if(sender instanceof Player) {
			Player p = ((Player) sender);
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("join")) {
					plugin.DuelArenas().joinQueue(p);
				} else if(args[0].equalsIgnoreCase("rank")) {
					DuelStats.sendRank(p);
				} else if(args[0].equalsIgnoreCase("lead")) {
					DuelStats.sendLead(p, DuelStats.getLeadRanks(), "Höchste Ränge", "/dw lead", 1);
				} else if(args[0].equalsIgnoreCase("leave")) {
					plugin.DuelArenas().leave(p);
				} else if(args[0].equalsIgnoreCase("queue")) {
					if(plugin.DuelArenas().queue.isEmpty()) {
						p.sendMessage(ThomiAPI.prefixGeneral + "§eDie Wartschlange ist leer.");
					} else {
						ArrayList<String> queue = new ArrayList<String>();
						for(Player pl : plugin.DuelArenas().queue) queue.add(pl.getName());
						p.sendMessage(ThomiAPI.prefixGeneral + "§aFolgende Spieler sind in der Warteschlange: \n§7§ §e" + Joiner.on("\n§7§ §e").join(queue));
					}
				} else if(args[0].equalsIgnoreCase("arenas")) {
					plugin.DuelArenas().showArenas(p);
				} else if(args[0].equalsIgnoreCase("setup") || args[0].equalsIgnoreCase("admin")) {
					if(p.hasPermission("DuelWorld.menu")) {
						plugin.DuelSetup().openMenu(p);
					} else {
						p.sendMessage(ThomiAPI.noPermMsg);
					}
				} else if(args[0].equalsIgnoreCase("stats")) {
					plugin.DuelStats().sendStats(p, p.getName());
				} else if(args[0].equalsIgnoreCase("history")) {
					plugin.DuelStats().openHistory(p, p.getUniqueId().toString());
				} else {
					p.sendMessage(getHelp());
				}
			} else if(args.length == 2) {
				if(args[0].equalsIgnoreCase("duel") || args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("battle")) {
					plugin.DuelArenas().askDuel(p, args[1]);
				} else if(args[0].equalsIgnoreCase("accept")) {
					plugin.DuelArenas().acceptDuel(p, args[1]);
				} else if(args[0].equalsIgnoreCase("stats")) {
					plugin.DuelStats().sendStats(p, args[1]);
				} else if(args[0].equalsIgnoreCase("history")) {
					String uuid = UUIDHandler.getUUID(args[1]);
					if(uuid != null) {
						plugin.DuelStats().openHistory(p, uuid);
					} else {
						p.sendMessage(ThomiAPI.prefixGeneral + "§cDer Spieler '§7" + args[1] + "§c' konnte nicht gefunden werden. Vertippt?");
					}
				} else if(args[0].equalsIgnoreCase("lead") && (args[1].equalsIgnoreCase("win") || args[1].equalsIgnoreCase("wins") || args[1].equalsIgnoreCase("siege") || args[1].equalsIgnoreCase("sieg"))) {
					DuelStats.sendLead(p, DuelStats.getLeadWins(), "Anzahl Siege", "/dw lead win", 1);
				} else if(args[0].equalsIgnoreCase("lead") && ThomiAPI.isInt(args[1])) {
					DuelStats.sendLead(p, DuelStats.getLeadRanks(), "Höchste Ränge", "/dw lead", Integer.parseInt(args[1]));
				} else {
					p.sendMessage(getHelp());
				}
			} else if(args.length == 3) {
				if(args[0].equalsIgnoreCase("lead") && (args[1].equalsIgnoreCase("win") || args[1].equalsIgnoreCase("wins") || args[1].equalsIgnoreCase("siege") || args[1].equalsIgnoreCase("sieg")) && ThomiAPI.isInt(args[2])) {
					DuelStats.sendLead(p, DuelStats.getLeadWins(), "Anzahl Siege", "/dw lead win", Integer.parseInt(args[2]));				
					}
			} else {
				p.sendMessage(getHelp());
			}
		}
	}

}
