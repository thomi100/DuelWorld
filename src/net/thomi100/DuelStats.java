package net.thomi100;

import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DuelStats implements Listener {

	private static DuelWorlds plugin;

	public DuelStats(DuelWorlds main) {
		
		DuelStats.plugin = main;
		DuelStats.plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
	}
	
	public static void sendRank(Player p) {
		File file = new File(plugin.getDataFolder() + "/ranking.yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		String uuid = p.getUniqueId().toString();
		if(fc.getString(uuid + ".elo") != null) {
			int elo = fc.getInt(uuid + ".elo");
			int playedGames = fc.getStringList(uuid + ".matches").size();
			long lastRanked = fc.getLong(uuid + ".rankedAgo");
			DuelRanks rank = DuelRanks.getRank(elo);
			int toNext = DuelRanks.eloToNext(elo);
			p.sendMessage("§8§m--------§r§8[§9§lDein Ranking§r§8]§m--------");
			p.sendMessage("§eDein Rang§8: §l" + DuelRanks.getFormatted(rank));
			p.sendMessage("§eELO-Punkte§8: §9" + elo);
			p.sendMessage("§7Gespielte Spiele§8: §9" + playedGames);
			p.sendMessage("§7Zuletzt gespielt§8: §9" + new SimpleDateFormat("dd.MM.yyyy, HH:mm").format(new Date(lastRanked)));
			p.sendMessage("§7Rangaufstieg nach §9 ca. " + Integer.parseInt((toNext/DuelRanks.averageElo) + "") + "§7 Siegen.");
			p.sendMessage("§8§m--------------------------------");
		}
	}
	
	public static void sendLead(Player p, ArrayList<String> list, String name, String command, int page) {
		if(page <= 0) page = 1;
		
		List<String> good = new ArrayList<String>();
		int indexes = 0;
		if(page > 1) indexes = (page*9) - 9;
		if(page > 100) indexes = (100*9) - 9;
		for(int i = indexes; i <= indexes + 9; i++) {
			if(list.size() > i) {
				String g = list.get(i);
				good.add("§a§l" + (i + 1) + ": §r§e" + g.split(";")[0] + "§8: §c" + g.split(";")[1]);
			}
		}
		String json = "[\"\",{\"text\":\"------------\",\"color\":\"dark_gray\",\"strikethrough\":true},{\"text\":\"[\",\"color\":\"dark_gray\",\"strikethrough\":false},{\"text\":\"< \",\"color\":\"green\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + command + " " + (page - 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"<\",\"color\":\"green\"}]}}},{\"text\":\"" + page + "\",\"color\":\"yellow\",\"bold\":false},{\"text\":\" >\",\"color\":\"green\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + command + " " + (page + 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\">\",\"color\":\"green\"}]}}},{\"text\":\"]\",\"color\":\"dark_gray\",\"bold\":false},{\"text\":\"------------\",\"color\":\"dark_gray\",\"strikethrough\":true}]";
		
		p.sendMessage("\n§8§m--------§r§8[§c" + name + "§8]§8§m--------");
		for(String goo : good) p.sendMessage(goo);
		if(good.isEmpty()) p.sendMessage("§e§lkeine Einträge");
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(json)));
	}
	
	public static ArrayList<String> getLeadRanks() {
		ArrayList<String> lead = new ArrayList<String>();
		
		HashMap<String, Integer> players = new HashMap<String, Integer>();

		File file = new File(plugin.getDataFolder() + "/ranking.yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		
		for(String uuid : fc.getKeys(false)) {
			if(fc.getString(uuid + ".elo") != null) {
				players.put(UUIDHandler.getName(uuid), fc.getInt(uuid + ".elo"));
			}
		}
		
		while(!players.isEmpty()) {
			int max = 0;
			String pla = null;
			for(String pl : players.keySet()) {
				if(players.get(pl) >= max) {
					pla = pl;
					max = players.get(pl);
				}
			}
			if(pla != null) {
				players.remove(pla);
				lead.add(pla + ";" + max);
			}
		}
		
		return lead;
	}
	
	public static ArrayList<String> getLeadWins() {
		ArrayList<String> lead = new ArrayList<String>();
		
		HashMap<String, Integer> players = new HashMap<String, Integer>();

		String path  = plugin.getDataFolder() + "/players/";
        File folder = new File(path);

        String[] files = folder.list();
		if(files != null && files.length > 0) {
			for(String uuid : files) {
				File file = new File(plugin.getDataFolder() + "/players/" + uuid.replace(".yml", "") + ".yml");
				FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
				players.put(UUIDHandler.getName(uuid.replace(".yml", "")), fc.getInt("stats.wins"));
			}
		}
		
		while(!players.isEmpty()) {
			int max = 0;
			String pla = null;
			for(String pl : players.keySet()) {
				if(players.get(pl) >= max) {
					pla = pl;
					max = players.get(pl);
				}
			}
			if(pla != null) {
				players.remove(pla);
				lead.add(pla + ";" + max);
			}
		}
		
		return lead;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().getName().equals("§eDuellverlauf")) {
			e.setCancelled(true);
		}
	}
	
	public void cancelStreak(Player loser, Player winner, String con) {
		if(con != null) {
			int streak = Integer.parseInt(con);
			if(streak > 6) {
				for(Player pl : Bukkit.getOnlinePlayers()) {
					if(!ThomiAPI.isSilent(pl)) {
						pl.sendMessage(ThomiAPI.prefixGeneral + "§a" + winner.getName() + " §7hat die §b" + streak +  "er-Winstreak §7von " + loser.getName() + "§c beendet§7!");
						ThomiAPI.sendSound(pl, Sound.ENDERMAN_HIT, 15);
					}
				}
			}
		}
	}
	
	public void sendWinstreak(Player p, int streak) {
		if(((streak + "").endsWith("0") || (streak + "").endsWith("5")) && streak > 3) {
			for(Player pl : Bukkit.getOnlinePlayers()) {
				if(!ThomiAPI.isSilent(pl)) {
					pl.sendMessage(ThomiAPI.prefixGeneral + "§a" + p.getName() + " §7hat §b" + streak +  " §7Duelle hintereinander gewonnen!");
				}
			}
            CreditAPI.updateCoins(p, 3);
            p.sendMessage(ThomiAPI.prefixGeneral + "§aDu erhältst §63 " + CreditAPI.CURRENCY_NAME_PL + "§a.");
		}
	}
	
	public void openHistory(Player p, String uuid) {
		Inventory history = Bukkit.createInventory(null, 54, "§eDuellverlauf");
		
		File file = new File(plugin.getDataFolder() + "/players/" + uuid + ".yml");
		if(!file.exists()) try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		
		HashMap<String, ItemStack> kits = new HashMap<>();
		
		for(String kit : plugin.DuelSetup().getKits()) {
			File kitFile = new File(plugin.getDataFolder() + "/kits/" + kit);
			if(kitFile.exists()) {
				FileConfiguration kitFC = YamlConfiguration.loadConfiguration(kitFile);
				kits.put(kit.replace(".yml", ""), kitFC.getItemStack("icon"));
			}
		}
		List<String> duels = fc.getStringList("stats.duels");
		for(int i = duels.size() - 1; i >= 0; i--) {
			String[] s = duels.get(i).split(" ");
			String time = s[0];
			time = time.replaceAll("-", " - ");
			String winner = s[1];
			String loser = s[3];
			String kit = s[5].replace(",", "");
			String arena = s[7].replace(")", "");
			String hp = "§7§ounbekannt";
			if(s.length >= 10) {
				hp = "§e" + (Double.parseDouble(s[9]) / 2) + "§c§l❤";
			}
			int elo = -1;
			if(s.length >= 12) {
				elo = Integer.parseInt(s[11]);
			}
			boolean won = false;
			if(UUIDHandler.getUUID(winner) != null && UUIDHandler.getUUID(winner).equals(uuid)) won = true;
			
			ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 10);
			List<String> lore = new ArrayList<String>();
			if(kits.containsKey(kit)) {
				item = kits.get(kit);
			}
			ItemMeta itemMeta = item.getItemMeta();
			if(won) {
				itemMeta.setDisplayName("§a" + time);
				lore.add("      §a§lSieg");
				lore.add(" §f ");
				if(elo > -1) {
					lore.add("      §7§lRANKED");
					lore.add("§6ELO erhalten§7: " + elo);
					lore.add(" §f ");
				}
				lore.add("§6Gegner§7: §e" + loser);
			} else {
				itemMeta.setDisplayName("§c" + time);
				lore.add("      §c§lNiederlage");
				lore.add(" §f ");
				if(elo > -1) {
					lore.add("      §7§lRANKED");
					lore.add("§6ELO verloren§7: " + elo);
					lore.add(" §f ");
				}
				lore.add("§6Gegner§7: §e" + winner);
			}
			lore.add("§6Kit§7: §e" + kit.replace(".yml", ""));
			lore.add("§6Arena§7: §e" + arena.replace(".yml", ""));
			lore.add("§6Leben§7: " + hp);
			itemMeta.setLore(lore);
			item.setItemMeta(itemMeta);
			history.addItem(item);
		}
		
		p.openInventory(history);
	}
	
	public void win(Player winner, Player loser, String arena, String kit, double damage, boolean isRanked, int elo) {
		String winnerU = UUIDHandler.getUUID(winner.getName());
		String loserU = UUIDHandler.getUUID(loser.getName());
		String game = ThomiAPI.getTime("dd.MM.yyyy-HH:mm:ss") + " " + winner.getName() + " > " + loser.getName() + " (Kit: " + kit + ", Arena: " + arena + ") HP: " + winner.getHealth() + " ELO: " + elo;

		File arenaFile = new File(plugin.getDataFolder() + "/arenas/" + arena.replace(".yml", "") + ".yml");
		FileConfiguration arenaFC = YamlConfiguration.loadConfiguration(arenaFile);
		Integer arenaGames = arenaFC.getInt("stats.games") + 1;
		int arenaDamage = (int) damage;
		arenaDamage += arenaFC.getInt("stats.damage");
		ArrayList<String> arenaDuels = (ArrayList<String>) arenaFC.getStringList("stats.duels");
		arenaDuels.add(game);
		arenaFC.set("stats.games", arenaGames);
		arenaFC.set("stats.damage", arenaDamage);
		arenaFC.set("stats.duels", arenaDuels);
		ThomiAPI.saveFile(arenaFC, arenaFile);
		
		File kitFile = new File(plugin.getDataFolder() + "/kits/" + kit.replace(".yml", "") + ".yml");
		FileConfiguration kitFC = YamlConfiguration.loadConfiguration(kitFile);
		Integer kitGames = kitFC.getInt("stats.games") + 1;
		int kitDamage = (int) damage;
		kitDamage += kitFC.getInt("stats.damage");
		ArrayList<String> kitDuels = (ArrayList<String>) kitFC.getStringList("stats.duels");
		kitDuels.add(game);
		kitFC.set("stats.games", kitGames);
		kitFC.set("stats.damage", kitDamage);
		kitFC.set("stats.duels", kitDuels);
		ThomiAPI.saveFile(kitFC, kitFile);
		
		File winnerFile = new File(plugin.getDataFolder() + "/players/" + winnerU + ".yml");
		if(!winnerFile.exists()) try { winnerFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
		FileConfiguration winnerFC = YamlConfiguration.loadConfiguration(winnerFile);
		Integer wins = 1;
		if(winnerFC.getString("stats.wins") != null) wins += winnerFC.getInt("stats.wins");
		int winDamage = (int) damage;
		if(winnerFC.getString("stats.damage") != null) winDamage += winnerFC.getInt("stats.damage");
		ArrayList<String> winDuels = new ArrayList<String>();
		if(winnerFC.getStringList("stats.duels") != null) winDuels.addAll(winnerFC.getStringList("stats.duels"));
		winDuels.add(game);
		winnerFC.set("stats.wins", wins);
		winnerFC.set("stats.damage", winDamage);
		winnerFC.set("stats.duels", winDuels);
		int winStreak = 1;
		if(winnerFC.getString("stats.winstreak") != null) winStreak += winnerFC.getInt("stats.winstreak");
		winnerFC.set("stats.winstreak", winStreak);
		if(winnerFC.getString("stats.winstreak_top") == null || winnerFC.getInt("stats.winstreak_top") < winStreak) winnerFC.set("stats.winstreak_top", winStreak);
		sendWinstreak(winner, winStreak);
		ThomiAPI.saveFile(winnerFC, winnerFile);

		File loserFile = new File(plugin.getDataFolder() + "/players/" + loserU + ".yml");
		if(!loserFile.exists()) try { loserFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
		FileConfiguration loserFC = YamlConfiguration.loadConfiguration(loserFile);
		Integer losses = 1;
		if(loserFC.getString("stats.losses") != null) losses += loserFC.getInt("stats.losses");
		int losDamage = (int) damage;
		if(loserFC.getString("stats.damage") != null) losDamage += loserFC.getInt("stats.damage");
		ArrayList<String> losDuels = new ArrayList<String>();
		if(loserFC.getStringList("stats.duels") != null) losDuels.addAll(loserFC.getStringList("stats.duels"));
		losDuels.add(game);
		loserFC.set("stats.losses", losses);
		loserFC.set("stats.damage", losDamage);
		loserFC.set("stats.duels", losDuels);
		cancelStreak(loser, winner, loserFC.getString("stats.winstreak"));
		loserFC.set("stats.winstreak", 0);
		ThomiAPI.saveFile(loserFC, loserFile);
		
		File gameFile = new File(plugin.getDataFolder() + "/games.yml");
		if(!gameFile.exists()) try { gameFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
		FileConfiguration gameFC = YamlConfiguration.loadConfiguration(gameFile);
		List<String> games = gameFC.getStringList("list");
		String con = ThomiAPI.getTime("dd.MM.yyyy HH:mm:ss") + ": " + winner.getName() +  " vs. " + loser.getName() + " in arena " + arena + " with kit " + kit;
		games.add(con);
		gameFC.set("list", games);
		ThomiAPI.saveFile(gameFC, gameFile);
	}
	
	public void sendStats(Player p, String target) {
		String uuid = UUIDHandler.getUUID(target);
		File file = new File(plugin.getDataFolder() + "/players/" + uuid + ".yml");
		if(uuid != null) {
			if(file.exists()) {
				FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
				if(uuid.equalsIgnoreCase(p.getUniqueId().toString())) {
					p.sendMessage("\n§8§m--------§r§8[§cDeine Stats§8]§8§m--------");
				} else {
					p.sendMessage("\n§8§m--------§r§8[§cStats von " + target + "§8]§8§m--------");
				}
				String wins = fc.getString("stats.wins");
				if(wins == null) wins = "0";
				String losses = fc.getString("stats.losses");
				if(losses == null) losses = "0";
				String winstreak = fc.getString("stats.winstreak");
				if(winstreak == null) winstreak = "0";
				String winstreak_top = fc.getString("stats.winstreak_top");
				if(winstreak_top == null) winstreak_top = "0";
				String damage = fc.getString("stats.damage");
				if(damage == null) damage = "0";
				int elo = DuelRanks.getElo(uuid);
				String rank = DuelRanks.getFormatted(DuelRanks.getRank(elo));
				p.sendMessage("§eSiege§8: §c" + wins);
				p.sendMessage("§eNiederlagen§8: §c" + losses);
				p.sendMessage("§eDuelle total§8: §c" + fc.getStringList("stats.duels").size());
				p.sendMessage("§eDuellschaden§8: §c" + damage);
				p.sendMessage("§eRang§8: §c" + rank + " §7[§e" + elo + "§7]");
				p.sendMessage("§eSiege in Folge§8: §c" + winstreak);
				p.sendMessage("§eBeste Winstreak§8: §c" + winstreak_top);
				p.sendMessage("§8§m--------------------------");
			} else {
				p.sendMessage(ThomiAPI.prefixGeneral + "§e" + target + "§c hat noch nie §9Duel§eWorld§c gespielt.");
			}
		} else {
			p.sendMessage(ThomiAPI.prefixGeneral + "§cDer Spieler '§7" + target + "§c' konnte nicht gefunden werden. Vertippt?");
		}
	}

}
