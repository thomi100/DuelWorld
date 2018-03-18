package net.thomi100;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum DuelRanks {

	STONE, BRONZE, SILVER, GOLD, DIAMOND, PLATIN, GRANDMASTER;
	
	public static int averageElo = 9;
	
	public static int gamesToday(FileConfiguration fc, String winnerUUID, String looserNAME) {
		int playedToday = 0;
		List<String> matchesWinner = fc.getStringList(winnerUUID + ".matches");
		String today = ThomiAPI.getTime("dd-MM-yyyy");
		for(String match : matchesWinner) {
			String p1 = match.split(";")[0];
			String p2 = match.split(";")[1];
			String day = match.split(";")[3];
			if(p1.equals(looserNAME) || p2.equals(looserNAME)) {
				if(day.equals(today)) {
					playedToday++;
				}
			}
		}
		return playedToday;
	}
	
	public static void checkOld() {
		File file = new File(Bukkit.getPluginManager().getPlugin("DuelWorld").getDataFolder() + "/ranking.yml");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		Set<String> keys = fc.getKeys(false);
		long time = System.currentTimeMillis();
		int minus = 7;
		long days = 3*24*60*60*1000;
		boolean save = false;
		for(String uuid : keys) {
			if(fc.getString(uuid + ".rankedAgo") != null) {
				long rankedAgo = fc.getLong(uuid + ".rankedAgo");
				if((time - rankedAgo) >= days) {
					int elo = fc.getInt(uuid + ".elo");
					fc.set(uuid + ".rankedAgo", time);
					save = true;
					if(elo >= 1000 + minus) {
						fc.set(uuid + ".elo", (elo - minus));
						System.out.println("[DuelWorld] Took " + minus + " ELO from " + uuid  + " (" + UUIDHandler.getName(uuid) + ").");
					}
				}
			}
		}
		if(save) ThomiAPI.saveFile(fc, file);
	}
	
	public static int eloToNext(int elo) {
		if(elo < 1030) return 1030-elo;
		if(elo < 1150) return 1150-elo;
		if(elo < 1400) return 1400-elo;
		if(elo < 1900) return 1900-elo;
		if(elo < 2500) return 2500-elo;
		if(elo < 3000) return 3000-elo;
		return -1;
	}
	
	public static DuelRanks nextRank(DuelRanks rank) {
		if(rank == GRANDMASTER) return GRANDMASTER;
		if(rank == PLATIN) return GRANDMASTER;
		if(rank == DIAMOND) return PLATIN;
		if(rank == GOLD) return DIAMOND;
		if(rank == SILVER) return GOLD;
		if(rank == BRONZE) return SILVER;
		return BRONZE;
	}
	
	public static int getElo(String uuid) {
		File file = new File(Bukkit.getPluginManager().getPlugin("DuelWorld").getDataFolder() + "/ranking.yml");
		if(!file.exists()) return 1000;
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		
		if(fc.getString(uuid + ".elo") == null) return 1000;
		return fc.getInt(uuid + ".elo");
	}
	
	public static DuelRanks getRank(int elo) {
		if(elo > 3000) return GRANDMASTER;
		if(elo > 2500) return PLATIN;
		if(elo > 1900) return DIAMOND;
		if(elo > 1400) return GOLD;
		if(elo > 1150) return SILVER;
		if(elo > 1030) return BRONZE;
		return STONE;
	}
	
	public static String getFormatted(DuelRanks rank) {
		if(rank == GRANDMASTER) return "§3Grandmaster";
		if(rank == PLATIN) return "§9Platin";
		if(rank == DIAMOND) return "§bDiamant";
		if(rank == GOLD) return "§eGold";
		if(rank == SILVER) return "§fSilber";
		if(rank == BRONZE) return "§6Bronze";
		return "§7Stone";
	}
	
	private static int diff(int i1, int i2) {
		if(i1 == i2) return 0;
		if(i1 > i2) return i1-i2;
		return i2-i1;
	}
	
	public static int add(Player winner, Player looser, double distance, double winHealth) {
		int eloWinner = get(winner.getUniqueId().toString());
		int eloLooser = get(looser.getUniqueId().toString());
		
		int ELO = 5;
		
		int difference = diff(eloWinner, eloLooser);
		int difference2 = difference;
		while(difference2 >= 15) {
			ELO += 4;
			difference2 -= 15;
		}

		if(eloWinner > eloLooser) {
			ELO = 5;
		}
		
		if(difference < 20) ELO = 10;
		
		if(ELO > 40) ELO = 40;

		ELO += ThomiAPI.getRandom(0, 3);

		if(distance >= 0 && distance < 2) {
			ELO = ELO/3;
		} else if(winHealth > 9) {
			ELO = ELO/4;
		}

		int finalEloWinner = eloWinner + ELO;
		int finalEloLooser = eloLooser - ELO;

		
		String today = ThomiAPI.getTime("dd-MM-yyyy");
		String con = winner.getName() + ";" + looser.getName() + ";" + ELO + ";" + today;

		File file = new File(Bukkit.getPluginManager().getPlugin("DuelWorld").getDataFolder() + "/ranking.yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);

		BountifulAPI.sendActionBar(winner, "§e+ " + ELO + " ELO §7[§6" + finalEloWinner + "§7]");
		BountifulAPI.sendActionBar(looser, "§e- " + ELO + " ELO §7[§6" + finalEloLooser + "§7]");

		checkForRankChange(winner, eloWinner, finalEloWinner);
		checkForRankChange(looser, eloLooser, finalEloLooser);

		List<String> matchesWinner = fc.getStringList(winner.getUniqueId().toString() + ".matches");
		List<String> matchesLooser = fc.getStringList(looser.getUniqueId().toString() + ".matches");

		matchesWinner.add(con);
		matchesLooser.add(con);
		
		fc.set(winner.getUniqueId().toString() + ".elo", finalEloWinner);
		fc.set(looser.getUniqueId().toString() + ".elo", finalEloLooser);
		fc.set(winner.getUniqueId().toString() + ".rankedAgo", System.currentTimeMillis());
		fc.set(looser.getUniqueId().toString() + ".rankedAgo", System.currentTimeMillis());
		fc.set(looser.getUniqueId().toString() + ".matches", matchesLooser);
		fc.set(winner.getUniqueId().toString() + ".matches", matchesWinner);
		
		ThomiAPI.saveFile(fc, file);
		return ELO;
	}
	
	public static void checkForRankChange(Player p, int before, int after) {
		DuelRanks rankBefore = getRank(before);
		DuelRanks rankAfter = getRank(after);
		
		if(rankBefore != rankAfter) {
			if(before > after) {
				sendDemotion(p, rankAfter);
			} else {
				sendPromotion(p, rankAfter);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void sendPromotion(Player p, DuelRanks rank) {
		String tab = "    ";
		String rang = getFormatted(rank);
		String cc = rang.substring(0, 2);
		
		p.sendMessage(ThomiAPI.line);
		p.sendMessage(" §f ");
		p.sendMessage(tab + tab + "§a§lNEUER DUELL-RANG!");
		p.sendMessage(tab + tab + "§7Du bist nun " + cc + "§l" + rang + "§r§7!");
		p.sendMessage(" §f ");
		p.sendMessage(ThomiAPI.line);
		
		BountifulAPI.sendFullTitle(p, 7, 40, 12, "§7Neuer Rang:", cc + rang);
		ThomiAPI.sendSound(p, Sound.LEVEL_UP);
		ThomiAPI.sendSound(p, Sound.LEVEL_UP, 15);
		// ThomiAPI.spawnRandomFirework(p.getLocation());
	}
	@SuppressWarnings("deprecation")
	public static void sendDemotion(Player p, DuelRanks rank) {
		String tab = "    ";
		String rang = getFormatted(rank);
		String cc = rang.substring(0, 2);
		
		p.sendMessage(ThomiAPI.line);
		p.sendMessage(" §f ");
		p.sendMessage(tab + tab + "§7§lNeuer Duell-Rang!");
		p.sendMessage(tab + tab + "§7Du bist nun wieder " + cc + rang + "§r§7.");
		p.sendMessage(" §f ");
		p.sendMessage(ThomiAPI.line);
		
		BountifulAPI.sendFullTitle(p, 7, 30, 12, "§7Neuer Rang:", cc + rang);
		ThomiAPI.sendSound(p, Sound.BAT_DEATH, 15);
		ThomiAPI.sendSound(p, Sound.SPIDER_IDLE, 15);
	}
	
	public static int get(String uuid) {
		File file = new File(Bukkit.getPluginManager().getPlugin("DuelWorld").getDataFolder() + "/ranking.yml");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		if(fc.getString(uuid + ".elo") != null) return fc.getInt(uuid + ".elo");
		return 1000;
	}
	
	public static void create(Player p) {
		String uuid = p.getUniqueId().toString();
		File file = new File(Bukkit.getPluginManager().getPlugin("DuelWorld").getDataFolder() + "/ranking.yml");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		if(fc.getString(uuid + ".elo") == null) {
			fc.set(uuid + ".elo", 1000);
			fc.set(uuid + ".matches", new ArrayList<String>());
			ThomiAPI.saveFile(fc, file);
		}
	}
	
}
