package net.thomi100;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;

public class DuelArenas implements Listener {

	private static DuelWorlds plugin;

	public DuelArenas(DuelWorlds main) {
		
		DuelArenas.plugin = main;
		DuelArenas.plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
	}
	
	public Location getMid(Location l1, Location l2) {
		double x = (l1.getX()+l2.getX()) / 2;
		double y = (l1.getY()+l2.getY()) / 2;
		double z = (l1.getZ()+l2.getZ()) / 2;
		return new Location(l1.getWorld(), x, y, z);
	}
	
	public void partCircle(Location center) {
		int amount = 47;
		int radius = 1;
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        for(int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            center.getWorld().playEffect(new Location(world, x, center.getY(), z), Effect.TILE_DUST, 7);
            center.getWorld().playEffect(new Location(world, x, center.getY(), z), Effect.WATERDRIP, 10);
        }
    }
	
	public void giveKit(Player p, String kit, boolean first) {
		p.getInventory().clear();
		kit = kit.replace(".yml", "");
		File file = new File(plugin.getDataFolder() + "/kits/" + kit + ".yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		for(int i = 0; i <= 35; i++) if(fc.getItemStack("items." + i) != null) p.getInventory().setItem(i, fc.getItemStack("items." + i));
		if(fc.getItemStack("items.helmet") != null) {
			ItemStack item = fc.getItemStack("items.helmet");
			if(item.getType().equals(Material.LEATHER_HELMET)) {
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				if(meta.getColor() == ((LeatherArmorMeta) new ItemStack(Material.LEATHER_HELMET).getItemMeta()).getColor()) {
					if(first) {
						meta.setColor(Color.fromRGB(0, 255, 0));
					} else {
						meta.setColor(Color.fromRGB(0, 135, 255));
					}
					item.setItemMeta(meta);
				}
			}
			p.getInventory().setHelmet(item);
		}
		if(fc.getItemStack("items.chestplate") != null) {
			ItemStack item = fc.getItemStack("items.chestplate");
			if(item.getType().equals(Material.LEATHER_CHESTPLATE)) {
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				if(meta.getColor() == ((LeatherArmorMeta) new ItemStack(Material.LEATHER_HELMET).getItemMeta()).getColor()) {
					if(first) {
						meta.setColor(Color.fromRGB(0, 255, 0));
					} else {
						meta.setColor(Color.fromRGB(0, 135, 255));
					}
					item.setItemMeta(meta);
				}
			}
			p.getInventory().setChestplate(item);
		}
		if(fc.getItemStack("items.leggings") != null) {
			ItemStack item = fc.getItemStack("items.leggings");
			if(item.getType().equals(Material.LEATHER_LEGGINGS)) {
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				if(meta.getColor() == ((LeatherArmorMeta) new ItemStack(Material.LEATHER_HELMET).getItemMeta()).getColor()) {
					if(first) {
						meta.setColor(Color.fromRGB(0, 255, 0));
					} else {
						meta.setColor(Color.fromRGB(0, 135, 255));
					}
					item.setItemMeta(meta);
				}
			}
			p.getInventory().setLeggings(item);
		}
		if(fc.getItemStack("items.boots") != null) {
			ItemStack item = fc.getItemStack("items.boots");
			if(item.getType().equals(Material.LEATHER_BOOTS)) {
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				if(meta.getColor() == ((LeatherArmorMeta) new ItemStack(Material.LEATHER_HELMET).getItemMeta()).getColor()) {
					if(first) {
						meta.setColor(Color.fromRGB(0, 255, 0));
					} else {
						meta.setColor(Color.fromRGB(0, 135, 255));
					}
					item.setItemMeta(meta);
				}
			}
			p.getInventory().setBoots(item);
		}
		p.updateInventory();
	}
	
	@SuppressWarnings("deprecation")
	private void joinGame(Player p1, Player p2, String arena, String kit) {
		removeInvitations(p1);
		removeInvitations(p2);
		queue.remove(p1);
		queue.remove(p2);
		HashMap<Player, Player> ign = new HashMap<>();
		ign.put(p1, p2);
		ingames.put(arena, ign);
		ingameKits.put(arena, kit.replace(".yml", ""));
		
		int eloP2 = DuelRanks.get(p2.getUniqueId().toString());
		String rankP2 = DuelRanks.getFormatted(DuelRanks.getRank(eloP2));

		int eloP1 = DuelRanks.get(p1.getUniqueId().toString());
		String rankP1 = DuelRanks.getFormatted(DuelRanks.getRank(eloP1));
		
		File file = new File(plugin.getDataFolder() + "/arenas/" + arena + ".yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		String erbauer = fc.getString("arena.erbauer");
		Location spawn1 = ThomiAPI.toLocation(fc.getString("arena.spawn." + 1));
		Location spawn2 = ThomiAPI.toLocation(fc.getString("arena.spawn." + 2));
		p1.teleport(spawn1);
		p2.teleport(spawn2);

		p1.sendMessage(" §f ");
		p1.sendMessage(ThomiAPI.line);
		p1.sendMessage(ThomiAPI.prefixGeneral + "§aGegner§7: §e" + p2.getName());
		p1.sendMessage(ThomiAPI.prefixGeneral + "§aRang§7: §e" + rankP2 + " §7[§6" + eloP2 + "§7]");
		p1.sendMessage(ThomiAPI.prefixGeneral + "§aDein Rang§7: §e" + rankP1 + " §7[§6" + eloP1 + "§7]");
		p1.sendMessage(ThomiAPI.prefixGeneral + "§aArena§7: §e" + arena);
		if(erbauer != null) p1.sendMessage(ThomiAPI.prefixGeneral + "§aErbauer§7: §e" + erbauer);
		p1.sendMessage(ThomiAPI.prefixGeneral + "§aKit§7: §e" + kit);

		p2.sendMessage(" §f ");
		p2.sendMessage(ThomiAPI.line);
		p2.sendMessage(ThomiAPI.prefixGeneral + "§aGegner§7: §e" + p1.getName());
		p2.sendMessage(ThomiAPI.prefixGeneral + "§aRang§7: §e" + rankP1 + " §7[§6" + eloP1 + "§7]");
		p2.sendMessage(ThomiAPI.prefixGeneral + "§aDein Rang§7: §e" + rankP2 + " §7[§6" + eloP2 + "§7]");
		p2.sendMessage(ThomiAPI.prefixGeneral + "§aArena§7: §e" + arena);
		if(erbauer != null) p2.sendMessage(ThomiAPI.prefixGeneral + "§aErbauer§7: §e" + erbauer);
		p2.sendMessage(ThomiAPI.prefixGeneral + "§aKit§7: §e" + kit);
		
		File rankedFl = new File(Bukkit.getPluginManager().getPlugin("DuelWorld").getDataFolder() + "/ranking.yml");
		FileConfiguration rankedFc = YamlConfiguration.loadConfiguration(rankedFl);

		boolean playedToday1 = false;
		boolean playedToday2 = false;
		File matchFl = new File(Bukkit.getPluginManager().getPlugin("DuelWorld").getDataFolder() + "/games.yml");
		FileConfiguration matchFc = YamlConfiguration.loadConfiguration(matchFl);
		for(String s : matchFc.getStringList("list")) {
			if(s.startsWith(ThomiAPI.getTime("dd.MM.yyyy "))) {
				String[] x = s.split(" vs. ");
				if(x[0].endsWith(p1.getName()) || x[1].startsWith(p1.getName())) {
					playedToday1 = true;
				}
				if(x[0].endsWith(p2.getName()) || x[1].startsWith(p2.getName())) {
					playedToday2 = true;
				}
			}
		}
		if(DuelRanks.gamesToday(rankedFc, p1.getUniqueId().toString(), p2.getName()) < 3) {
			if(playedToday1 && playedToday2) {
				BountifulAPI.sendFullTitle(p1, 7, 30, 10, "", "§7§lRANKED");
				BountifulAPI.sendFullTitle(p2, 7, 30, 10, "", "§7§lRANKED");
				ingameRankeds.put(arena, true);
				int eloToNext1 = DuelRanks.eloToNext(eloP1);
				if(eloToNext1 > DuelRanks.averageElo && (Integer.parseInt((eloToNext1/DuelRanks.averageElo) + "") < 50)) p1.sendMessage(ThomiAPI.prefixGeneral + "§aNoch ca. §e" + Integer.parseInt((eloToNext1/DuelRanks.averageElo) + "") + " §aSiege zum Rangaufstieg.");
				int eloToNext2 = DuelRanks.eloToNext(eloP2);
				if(eloToNext2 > DuelRanks.averageElo && (Integer.parseInt((eloToNext2/DuelRanks.averageElo) + "") < 50)) p2.sendMessage(ThomiAPI.prefixGeneral + "§aNoch ca. §e" + Integer.parseInt((eloToNext2/DuelRanks.averageElo) + "") + " §aSiege zum Rangaufstieg.");
			} else {
				ingameRankeds.put(arena, false);
			}
		} else {
			ingameRankeds.put(arena, false);
		}
		
		ThomiAPI.clearPlayer(p1, true);
        ThomiAPI.clearPlayer(p2, true);
		
		giveKit(p1, kit, true);
		giveKit(p2, kit, false);

		DuelListener.starting.add(p1);
		DuelListener.starting.add(p2);
		
		Location spawnA = ThomiAPI.toLocation(fc.getString("arena.spawn.1"));
		Location spawnB = ThomiAPI.toLocation(fc.getString("arena.spawn.2"));
		Location mid = (fc.getString("arena.middle") != null) ? ThomiAPI.toLocation(fc.getString("arena.middle")) : getMid(spawnA, spawnB);
		mid.setY(mid.getY() + 1);
		
		arenaTime.put(arena, 120+4);
		arenaTask.put(arena, new BukkitRunnable() {
			@Override
			public void run() {
				if(arenaTime.containsKey(arena)) {
					int time = arenaTime.get(arena) - 1;
					arenaTime.replace(arena, time);
					Integer[] times = {90, 60, 30, 3, 2, 1};
					Integer[] infoTimes = {45, 10};
					if(time <= 30) {
						partCircle(mid);
					}
					if(time <= 0) {
						timeEnd(mid, p1, p2);
					}
					if(Arrays.asList(times).contains(time)) {
						p1.sendMessage(ThomiAPI.prefixGeneral + "§aDie Runde endet in §e" + time + "§a Sekunden.");
						p2.sendMessage(ThomiAPI.prefixGeneral + "§aDie Runde endet in §e" + time + "§a Sekunden.");
						if(time <= 30) {
							ThomiAPI.sendSound(p1, Sound.NOTE_BASS, 15);
							ThomiAPI.sendSound(p2, Sound.NOTE_BASS, 15);
						} else {
							ThomiAPI.sendSound(p1, Sound.NOTE_BASS);
							ThomiAPI.sendSound(p2, Sound.NOTE_BASS);
						}
					}
					if(Arrays.asList(infoTimes).contains(time)) {
						p1.sendMessage(ThomiAPI.prefixGeneral + "§aWer in §e" + time + "§a Sekunden der Mitte näher ist, gewinnt das Duell!");
						p2.sendMessage(ThomiAPI.prefixGeneral + "§aWer in §e" + time + "§a Sekunden der Mitte näher ist, gewinnt das Duell!");
						ThomiAPI.sendSound(p1, Sound.BAT_HURT, 15);
						ThomiAPI.sendSound(p2, Sound.BAT_HURT, 15);
					}
				} else {
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 0, 20L));
		
		ThomiAPI.doLater(10, new Runnable() { 
			@Override 
			public void run() {
				BountifulAPI.sendTitle(p1, 5, 10, 5, "§a§l3");
				BountifulAPI.sendTitle(p2, 5, 10, 5, "§a§l3");
				ThomiAPI.sendSound(p1, Sound.NOTE_BASS);
				ThomiAPI.sendSound(p2, Sound.NOTE_BASS);
			}
		});
		ThomiAPI.doLater(35, new Runnable() { 
			@Override 
			public void run() {
				BountifulAPI.sendTitle(p1, 5, 10, 5, "§e§l2");
				BountifulAPI.sendTitle(p2, 5, 10, 5, "§e§l2");
				ThomiAPI.sendSound(p1, Sound.ORB_PICKUP);
				ThomiAPI.sendSound(p2, Sound.ORB_PICKUP);
			}
		});
		ThomiAPI.doLater(60, new Runnable() { 
			@Override 
			public void run() {
				BountifulAPI.sendTitle(p1, 5, 10, 5, "§c§l1");
				BountifulAPI.sendTitle(p2, 5, 10, 5, "§c§l1");
				ThomiAPI.sendSound(p1, Sound.ORB_PICKUP, 15);
				ThomiAPI.sendSound(p2, Sound.ORB_PICKUP, 15);
			}
		});
		ThomiAPI.doLater(85, new Runnable() { 
			@Override 
			public void run() {
				BountifulAPI.sendFullTitle(p1, 5, 15, 15, "", "§7§lViel Glück!");
				BountifulAPI.sendFullTitle(p2, 5, 15, 15, "", "§7§lViel Glück!");
				ThomiAPI.sendSound(p1, Sound.LEVEL_UP, 15);
				ThomiAPI.sendSound(p2, Sound.LEVEL_UP, 15);
				DuelListener.starting.remove(p1);
				DuelListener.starting.remove(p2);
			}
		});
	}
	
	public void timeEnd(Location mid, Player p1, Player p2) {

		if(!p1.getLocation().getWorld().equals(mid.getWorld())) {
			plugin.DuelListener().ohDeath(p1);
			return;
		}
		if(!p2.getLocation().getWorld().equals(mid.getWorld())) {
			plugin.DuelListener().ohDeath(p2);
			return;
		}
		
		if(p1.getLocation().distance(mid) < p2.getLocation().distance(mid)) {
			plugin.DuelListener().ohDeath(p2);
		} else {
			plugin.DuelListener().ohDeath(p1);
		}
		
	}
	
	public void init() {
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				if(queue.size() >= 2) {
					String arena = freeArena();
					if(arena != null) {
						String kit = randKit();
						if(kit != null) {
							joinGame(queue.get(0), queue.get(1), arena.replace(".yml", ""), kit.replace(".yml", ""));
						}
					}
				}
			}
		}, 3*20, 3*20);
	}
	
	public ArrayList<Player> queue = new ArrayList<Player>();
	public HashMap<String, HashMap<Player, Player>> ingames = new HashMap<String, HashMap<Player, Player>>();
	public HashMap<String, String> ingameKits = new HashMap<String, String>();
	public HashMap<String, Boolean> ingameRankeds = new HashMap<String, Boolean>();
	public HashMap<String, Double> ingameDamage = new HashMap<String, Double>();
	public HashMap<String, BukkitTask> arenaTask = new HashMap<String, BukkitTask>();
	public HashMap<String, Integer> arenaTime = new HashMap<String, Integer>();
	
	private String randKit() {
		ArrayList<String> kits = new ArrayList<String>();
		ArrayList<String> cc = plugin.DuelSetup().getKits();
		for(String kit : cc) {
			File file = new File(plugin.getDataFolder() + "/kits/" + kit.replace(".yml", "") + ".yml");
			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
			if(fc.getString("finished") != null) {
				if(fc.getString("kit.enabled") == null || fc.getBoolean("kit.enabled")) {
					kits.add(kit.replace(".yml", ""));
				}
				
			}
		}
		if(!kits.isEmpty()) {
			return kits.get(getRandom(0, kits.size()));
		}
		return null;
	}
	
	private String freeArena() {
		ArrayList<String> freeArenas = new ArrayList<String>();
		ArrayList<String> cc = plugin.DuelSetup().getArenas();
		for(String s : cc) {
			if(!ingames.containsKey(s)) {
				File file = new File(plugin.getDataFolder() + "/arenas/" + s.replace(".yml", "") + ".yml");
				FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
				if(fc.getString("arena.spawn." + 1) != null && fc.getString("arena.spawn." + 2) != null) {
					if(fc.getString("arena.enabled") == null || fc.getBoolean("arena.enabled")) {
						freeArenas.add(s);
					}
				}
			}
		}
		if(!freeArenas.isEmpty()) {
			return freeArenas.get(getRandom(0, freeArenas.size()));
		}
		return null;
	}

	private static int getRandom(int min, int max) {
        return (int) (Math.random() * (max - min));
    }
	
	public String getGame(Player p) {
		for(String arena : ingames.keySet()) {
			if(ingames.get(arena).containsKey(p) || ingames.get(arena).containsValue(p)) return arena;
		}
		return null;
	}
	
	public boolean isIngame(Player p) {
		for(String arena : ingames.keySet()) {
			if(ingames.get(arena).containsKey(p) || ingames.get(arena).containsValue(p)) return true;
		}
		return false;
	}
	
	public void leave(Player p) {
		if(queue.contains(p)) {
			queue.remove(p);
			p.sendMessage(ThomiAPI.prefixGeneral + "§eDu hast die Wartschlange verlassen.");
		} else {
			p.sendMessage(ThomiAPI.prefixGeneral + "§cDu bist nicht in der Wartschlange.");
		}
	}
	
	public void joinQueue(Player p) {
		if(!isIngame(p)) {
			if(!queue.contains(p)) {
				queue.add(p);
				p.sendMessage(ThomiAPI.prefixGeneral + "§aDu bist der Warteschlange beigetreten.");
				p.sendMessage(ThomiAPI.prefixGeneral + "§aWarteschlange verlassen§7: §e/DuelWorld leave");
				ThomiAPI.sendSound(p, Sound.ORB_PICKUP, 15);
			} else {
				p.sendMessage(ThomiAPI.prefixGeneral + "§cDu bist bereits in der Warteschlange.");
				ThomiAPI.sendSound(p, Sound.NOTE_BASS);
			}
		} else {
			p.sendMessage(ThomiAPI.prefixGeneral + "§cDu bist bereits ingame!");
			ThomiAPI.sendSound(p, Sound.ORB_PICKUP);
		}
	}
	
	public void showArenas(Player p) {
		
		List<String> arenas = plugin.DuelSetup().getArenas();
		int size = 9;
		while(size <= arenas.size()) size += 9;
		size += 9;
		Inventory inv = Bukkit.createInventory(null, size, "§eDW-Arenas");
		for(String arena : plugin.DuelSetup().getArenas()) {
			arena = arena.replace(".yml", "");
			File file = new File(plugin.getDataFolder() + "/arenas/" + arena + ".yml");
			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
			if(ingames.containsKey(arena)) {
				ItemStack item = new ItemStack(Material.PAPER, 1);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName("§6§l" + arena);
				ArrayList<String> lore = new ArrayList<String>();
				ArrayList<Player> player1 = new ArrayList<Player>();
				ArrayList<Player> player2 = new ArrayList<Player>();
				player1.addAll(plugin.DuelArenas().ingames.get(arena).keySet());
				player2.addAll(plugin.DuelArenas().ingames.get(arena).values());
				Player a = player1.get(0);
				Player b = player2.get(0);
				lore.add("§eSpieler 1§7: §b" + a.getName());
				lore.add("§eSpieler 2§7: §b" + b.getName());
				lore.add(" §f ");
				lore.add("§eKit§7: §b" + ingameKits.get(arena));
				meta.setLore(lore);
				item.setItemMeta(meta);
				inv.addItem(item);
			} else if(fc.getString("arena.enabled") == null || fc.getBoolean("arena.enabled")) {
				ItemStack item = new ItemStack(Material.BARRIER, 1);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName("§c§l" + arena);
				ArrayList<String> lore = new ArrayList<String>();
				lore.add("§c§onicht ingame");
				meta.setLore(lore);
				item.setItemMeta(meta);
				inv.addItem(item);
			} else if(p.hasPermission("DuelWorld.setup.activateArenas")) {
				ItemStack item = new ItemStack(Material.INK_SACK, 1, (byte) 1);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName("§c§l" + arena);
				ArrayList<String> lore = new ArrayList<String>();
				lore.add("§c§odeaktiviert");
				meta.setLore(lore);
				item.setItemMeta(meta);
				inv.addItem(item);
			}
		}
		
		ItemStack refresh = new ItemStack(Material.INK_SACK, 1, (byte) 10);
		ItemMeta refreshmeta = refresh.getItemMeta();
		refreshmeta.setDisplayName("§a§laktualisieren");
		refresh.setItemMeta(refreshmeta);
		inv.setItem(size - 1, refresh);
		
		p.openInventory(inv);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(ThomiAPI.isItem(e.getCurrentItem())) {
			if(e.getInventory().getName().equals("§eDW-Arenas")) {
				e.setCancelled(true);
				if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("aktualisieren")) {
					showArenas((Player) e.getWhoClicked());
					ThomiAPI.sendSound((Player) e.getWhoClicked(), Sound.ORB_PICKUP, 15);
				}
			}
		}
	}
	
	public void removeInvitations(Player p) {
		if(invites.containsKey(p)) invites.remove(p);
		ArrayList<Player> values = new ArrayList<>(invites.values());
		if(values.contains(p)) {
			ArrayList<Player> keys = new ArrayList<>(invites.keySet());
			invites.remove(keys.get(values.indexOf(p)));
		}
	
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		removeInvitations(e.getPlayer());
	}
	
	private HashMap<Player, Player> invites = new HashMap<Player, Player>();
	public void askDuel(Player p, String target) {
		if(Bukkit.getPlayerExact(target) != null) {
			Player next = Bukkit.getPlayerExact(target);
			if(!next.getName().equalsIgnoreCase(p.getName())) {
                if(!isIngame(p)) {
                    if(!isIngame(next)) {
                        if(!(invites.containsKey(next) && invites.get(next) == p)) {
                            next.sendMessage(ThomiAPI.prefixGeneral + "§e" + p.getName() + "§a§l fordert dich zu einem Duell heraus!");
                            String con = "[\"\",{\"text\":\"%prefix% \",\"color\":\"gray\"},{\"text\":\"[Anfrage annehmen]\",\"color\":\"green\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/dw accept %player%\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Herausforderung annehmen\",\"color\":\"green\"}]}}}]";
                            con = con.replace("%prefix%", ThomiAPI.prefixGeneral).replace("%player%", p.getName());
                            IChatBaseComponent comp = ChatSerializer.a(con);
                            PacketPlayOutChat packet = new PacketPlayOutChat(comp);
                            ((CraftPlayer) next).getHandle().playerConnection.sendPacket(packet);
                            p.sendMessage(ThomiAPI.prefixGeneral + "§aDu hast §e" + next.getName() + "§a herausgefordert.");
                            invites.put(p, next);
                        } else {
                            p.sendMessage(ThomiAPI.prefixGeneral + "§cDu hast bereits eine Herausforderung von §e" + next.getName() + "§c.");
                            p.sendMessage(ThomiAPI.prefixGeneral + "§aAnnehmen mit §e/dw accept <" + next.getName() + ">");
                        }
                    } else {
                        p.sendMessage(ThomiAPI.prefixGeneral + "§e" + next.getName() + "§c ist bereits ingame.");
                    }
                } else {
                    p.sendMessage(ThomiAPI.prefixGeneral + "§cDu bist bereits ingame.");
                }
            } else {
			    p.sendMessage(ThomiAPI.prefixGeneral + "§cDu kannst dich nicht selbst herausfordern.");
            }
		} else {
			p.sendMessage(ThomiAPI.prefixGeneral + "§c'§7" + target + "§c' ist nicht online. Vertippt?");
		}
	}
	
	public void acceptDuel(Player p, String target) {
		if(Bukkit.getPlayerExact(target) != null) {
			Player next = Bukkit.getPlayerExact(target);
			if(!isIngame(p)) {
				if(!isIngame(next)) {
					if(invites.containsKey(next) && invites.get(next) == p) {
						String arena = freeArena();
						if(arena != null) {
							String kit = randKit();
							if(kit != null) {
								joinGame(next, p, arena.replace(".yml", ""), kit.replace(".yml", ""));
							} else {
								p.sendMessage(ThomiAPI.prefixGeneral + "§cDie Einrichtung der Kits ist noch nicht abgeschlossen. Bitte teile dies einem Teammitglied mit.");
							}
						} else {
							p.sendMessage(ThomiAPI.prefixGeneral + "§cEs konnte keine freie Arena gefunden werden. Bitte sp§ter erneut versuchen.");
						}
					} else {
						p.sendMessage(ThomiAPI.prefixGeneral + "§cDu hast keine Herausforderung von " + next.getName() + "§c.");
					}
				} else {
					p.sendMessage(ThomiAPI.prefixGeneral + "§e" + next.getName() + "§c ist bereits ingame.");
				}
			} else {
				p.sendMessage(ThomiAPI.prefixGeneral + "§cDu bist bereits ingame.");
			}
		} else {
			p.sendMessage(ThomiAPI.prefixGeneral + "§c'§7" + target + "§c' ist nicht online. Vertippt?");
		}
	}

}
