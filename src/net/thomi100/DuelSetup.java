package net.thomi100;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DuelSetup implements Listener {

	private static DuelWorlds plugin;

	public DuelSetup(DuelWorlds main) {
		
		DuelSetup.plugin = main;
		DuelSetup.plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
	}
	
	private void setIcon(Player p, String kit) {
		ItemStack item = p.getItemInHand();
		File file = new File(plugin.getDataFolder() + "/kits/" + kit + ".yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		if(item != null && item.getAmount() > 0 && !item.getType().equals(Material.AIR)) {
			item = new ItemStack(item.getType(), 1);
			kit = kit.replace(".yml", "");
			fc.set("icon", p.getInventory().getItemInHand());
			ThomiAPI.saveFile(fc, file);
			p.sendMessage(ThomiAPI.prefixGeneral + "§aIcon des Kits §e" + kit + "§a gesetzt!");
		} else {
			p.sendMessage(ThomiAPI.prefixGeneral + "§cDas Item in der Hand ist ung§ltig.");
		}
		boolean enabled = true;
		if(fc.getString("kit.enabled") != null && !fc.getBoolean("kit.enabled")) enabled = false;
		openKitEdit(p, kit, enabled);
	}
	
	private void setItems(Player p, String kit) {
		kit = kit.replace(".yml", "");
		File file = new File(plugin.getDataFolder() + "/kits/" + kit + ".yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		fc.set("finished", "yes");
		
		for(int i = 0; i <= 35; i++) {
			if(p.getInventory().getItem(i) != null && !p.getInventory().getItem(i).getType().equals(Material.AIR)) {
				fc.set("items." + i, p.getInventory().getItem(i));
			} else {
				fc.set("items." + i, null);
			}
		}
		if(p.getInventory().getHelmet() != null) {
			fc.set("items.helmet", p.getInventory().getHelmet());
		} else {
			fc.set("items.helmet", null);
		}
		if(p.getInventory().getChestplate() != null) {
			fc.set("items.chestplate", p.getInventory().getChestplate());
		} else {
			fc.set("items.chestplate", null);
		}
		if(p.getInventory().getLeggings() != null) {
			fc.set("items.leggings", p.getInventory().getLeggings());
		} else {
			fc.set("items.leggings", null);
		}
		if(p.getInventory().getBoots() != null) {
			fc.set("items.boots", p.getInventory().getBoots());
		} else {
			fc.set("items.boots", null);
		}
		ThomiAPI.saveFile(fc, file);
		p.sendMessage(ThomiAPI.prefixGeneral + "§aItems in Kit §e" + kit + "§a gesetzt!");
		editKitSel(p);
	}
	
	public void toggleKit(Player p, String kit) {
		File file = new File(plugin.getDataFolder() + "/kits/" + kit.replace(".yml", "") + ".yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		boolean enabled = true;
		if(fc.getString("kit.enabled") != null && !fc.getBoolean("kit.enabled")) enabled = false;
		fc.set("kit.enabled", !enabled);
		ThomiAPI.saveFile(file, fc);
		if(enabled) p.sendMessage(ThomiAPI.prefixGeneral + "§cDas Kit §e" + kit + "§c wurde deaktiviert.");
		if(!enabled) p.sendMessage(ThomiAPI.prefixGeneral + "§aDas Kit §e" + kit + "§a wurde aktiviert.");
		ThomiAPI.sendSound(p, Sound.ORB_PICKUP);
		openKitEdit(p, kit, !enabled);
	}
	
	public void setMid(Player p, String arena) {
		File file = new File(plugin.getDataFolder() + "/arenas/" + arena.replace(".yml", "") + ".yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		fc.set("arena.middle", ThomiAPI.toString(p.getLocation(), true));
		ThomiAPI.saveFile(file, fc);
		p.sendMessage(ThomiAPI.prefixGeneral + "§aDie Mitte der Arena §e" + arena + "§a wurde gesetzt.");
		ThomiAPI.sendSound(p, Sound.ORB_PICKUP);
	}
	
	public void toggleArena(Player p, String arena) {
		File file = new File(plugin.getDataFolder() + "/arenas/" + arena.replace(".yml", "") + ".yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		boolean enabled = true;
		if(fc.getString("arena.enabled") != null && !fc.getBoolean("arena.enabled")) enabled = false;
		fc.set("arena.enabled", !enabled);
		ThomiAPI.saveFile(file, fc);
		if(enabled) p.sendMessage(ThomiAPI.prefixGeneral + "§cDie Arena §e" + arena + "§c wurde deaktiviert.");
		if(!enabled) p.sendMessage(ThomiAPI.prefixGeneral + "§aDie Arena §e" + arena + "§a wurde aktiviert.");
		ThomiAPI.sendSound(p, Sound.ORB_PICKUP);
		openArenaEdit(p, arena, !enabled);
	}
	
	public void tpToArena(Player p, String map) {
		File file = new File(plugin.getDataFolder() + "/arenas/" + map.replace(".yml", "") + ".yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		String loc = fc.getString("arena.spawn.1");
		if(loc == null) loc = fc.getString("arena.spawn.2");
		if(fc.getString("arena.middle") != null) loc = fc.getString("arena.middle");
		ThomiAPI.tp(p, loc);
	}
	
	private void setSpawn(Player p, String map, Integer nr) {
		File file = new File(plugin.getDataFolder() + "/arenas/" + map.replace(".yml", "") + ".yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		String loc = ThomiAPI.toString(p.getLocation(), true);
		fc.set("arena.spawn." + nr, loc);
		ThomiAPI.saveFile(fc, file);
		p.sendMessage(ThomiAPI.prefixGeneral + "§aSpawn §e" + nr + "§a gesetzt!");
		boolean enabled = true;
		if(fc.getString("arena.enabled") != null && !fc.getBoolean("arena.enabled")) enabled = false;
		openArenaEdit(p, map, enabled);
	}
	
	private void setWinHolo(Player p) {
		Location loc = p.getLocation();
		loc.setY(loc.getY() + 1);
		DuelWorld.winholo = loc;
		plugin.getConfig().set("DuelWorld.winholo", ThomiAPI.toString(loc, true));
		plugin.saveConfig();
		p.sendMessage(ThomiAPI.prefixGeneral + "§eWin-Hologram gesetzt!");
		p.sendMessage(ThomiAPI.prefixGeneral + "§eEs wird in den nächsten Minuten aktualisiert.");
	}
	
	private void setRankHolo(Player p) {
		Location loc = p.getLocation();
		loc.setY(loc.getY() + 1);
		DuelWorld.rankholo = loc;
		plugin.getConfig().set("DuelWorld.rankholo", ThomiAPI.toString(loc, true));
		plugin.saveConfig();
		p.sendMessage(ThomiAPI.prefixGeneral + "§eRank-Hologram gesetzt!");
		p.sendMessage(ThomiAPI.prefixGeneral + "§eEs wird in den nächsten Minuten aktualisiert.");
	}
	
	private void setEnding(Player p) {
		DuelWorld.ending = p.getLocation();
		plugin.getConfig().set("DuelWorld.ending", ThomiAPI.toString(p.getLocation(), true));
		plugin.saveConfig();
		p.sendMessage(ThomiAPI.prefixGeneral + "§aEndpunkt gesetzt!");
	}
	
	private void setNPC(Player p) {
		Location loc = p.getLocation();
		loc.setPitch(0);

		
		DuelWorld.npc = loc;
		plugin.getConfig().set("DuelWorld.NPC", ThomiAPI.toString(loc, true));
		plugin.saveConfig();
		p.sendMessage(ThomiAPI.prefixGeneral + "§aNPC für die Warteschlange gesetzt!");
		
		setNPC();
	}
	
	public static void setNPC() {
		for(Entity ent : Bukkit.getWorld("world").getEntities()) {
			if(ent.getType() == EntityType.ARMOR_STAND && ent instanceof CraftLivingEntity && ((CraftLivingEntity) ent).getEquipment().getItemInHand().getType().equals(Material.IRON_SWORD) && ((CraftLivingEntity) ent).getEquipment().getHelmet().getType().equals(Material.SKULL_ITEM)) {
				System.out.println("[DuelWorld] Removed ArmorStand at " + ThomiAPI.toString(ent.getLocation(), true));
				ent.remove();
			}
		}
		
		Location loc = DuelWorld.npc;
		if(loc == null) return;

		World wld = Bukkit.getWorld("world");
		Entity as = wld.spawnEntity(loc, EntityType.ARMOR_STAND);
		EntityEquipment ee = ((CraftLivingEntity) as).getEquipment();

		ItemStack skull_head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
		SkullMeta skull_headmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		skull_headmeta.setOwner("thomi100");
		skull_head.setItemMeta(skull_headmeta);
		ee.setHelmet(skull_head);
		
		ItemStack le_chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		LeatherArmorMeta le_chestplate_m = (LeatherArmorMeta)le_chestplate.getItemMeta();
		le_chestplate_m.setColor(org.bukkit.Color.NAVY);
		le_chestplate.setItemMeta(le_chestplate_m);
		ee.setChestplate(le_chestplate);
		
		ItemStack le_leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		LeatherArmorMeta le_leggings_m = (LeatherArmorMeta)le_leggings.getItemMeta();
		le_leggings_m.setColor(org.bukkit.Color.NAVY);
		le_leggings.setItemMeta(le_leggings_m);
		ee.setLeggings(le_leggings);
		
		ItemStack le_boots = new ItemStack(Material.LEATHER_BOOTS, 1);
		LeatherArmorMeta le_boots_m = (LeatherArmorMeta)le_boots.getItemMeta();
		le_boots_m.setColor(org.bukkit.Color.NAVY);
		le_boots.setItemMeta(le_boots_m);
		ee.setBoots(le_boots);
		
		ee.setItemInHand(new ItemStack(Material.IRON_SWORD, 1));
		
		as.setCustomName("§e§lWarteschlange");
		as.setCustomNameVisible(true);
		ArmorStand stand = ((ArmorStand) as);
		stand.setBasePlate(false);
		stand.setCanPickupItems(false);
		stand.setArms(true);
	}

	private void editArenaSel(Player p) {
		Inventory inv = Bukkit.createInventory(null, 18, "§eDuelWorld-Arenas");
        
        for(String map : getArenas()) {
        	ItemStack item = new ItemStack(Material.PAPER, 1);
        	ItemMeta itemmeta = item.getItemMeta();
        	itemmeta.setDisplayName("§e" + map);
        	ArrayList<String> lore = new ArrayList<String>();
    		File file = new File(plugin.getDataFolder() + "/arenas/" + map.replace(".yml", "") + ".yml");
    		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
    		lore.add("§6Ersteller§7: §e" + fc.getString("arena.creator"));
    		lore.add("§6Erbauer§7: §e" + fc.getString("arena.erbauer"));
    		lore.add("§6Erstellt am§7: §e" + fc.getString("arena.created"));
    		lore.add(" §f ");
    		lore.add("§6Games§7: §e" + fc.getString("stats.games"));
    		lore.add("§6Schaden§7: §e" + fc.getString("stats.damage"));
    		lore.add(" §f ");
    		if(fc.getString("arena.enabled") != null && !fc.getBoolean("arena.enabled")) {
        		lore.add("§c§ldeaktiviert");
    		} else {
        		lore.add("§a§laktiviert");
    		}
    		lore.add(" §f ");
    		if(fc.getString("arena.spawn." + 1) != null) lore.add("§6Welt§7: §e" + fc.getString("arena.spawn." + 1).split(";")[0]);
    		if(fc.getString("arena.spawn." + 1) == null) lore.add("§cSpawn 1 nicht definiert");
    		if(fc.getString("arena.spawn." + 2) == null) lore.add("§cSpawn 2 nicht definiert");
        	itemmeta.setLore(lore);
        	item.setItemMeta(itemmeta);
        	inv.addItem(item);
        }

		inv.setItem(17, ThomiAPI.back());
		
		p.openInventory(inv);
	}
	
	private void editKitSel(Player p) {
		Inventory inv = Bukkit.createInventory(null, 18, "§eDuelWorld-Kits");
        
        for(String kit : getKits()) {
    		File file = new File(plugin.getDataFolder() + "/kits/" + kit.replace(".yml", "") + ".yml");
    		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
    		
        	ItemStack item = new ItemStack(Material.IRON_HELMET, 1);
        	if(fc.getItemStack("icon") != null) item.setType(fc.getItemStack("icon").getType());
        	ItemMeta itemmeta = item.getItemMeta();
        	itemmeta.setDisplayName("§e" + kit);
        	ArrayList<String> lore = new ArrayList<String>();
    		lore.add("§6Ersteller§7: §e" + fc.getString("kit.creator"));
    		lore.add("§6Erstellt am§7: §e" + fc.getString("kit.created"));
    		lore.add(" §f ");
    		lore.add("§6Games§7: §e" + fc.getString("stats.games"));
    		lore.add("§6Schaden§7: §e" + fc.getString("stats.damage"));
    		if(fc.getString("finished") == null) {
        		lore.add(" §f ");
    			lore.add("§cItems nicht definiert");
    		}
        	itemmeta.setLore(lore);
        	item.setItemMeta(itemmeta);
        	inv.addItem(item);
        }
        
        inv.setItem(17, ThomiAPI.back());
		
		p.openInventory(inv);
	}
	
	public ArrayList<String> getKits() {
		ArrayList<String> kits = new ArrayList<String>();

		String path  = plugin.getDataFolder() + "/kits/";
        File folder = new File(path);

        String[] files = folder.list();
		if(files != null && files.length > 0) {
			for(String kit : files) {
				if(kit != null && kit.length() > 0) kits.add(kit);
			}
		}
		return kits;
	}
	
	public ArrayList<String> getArenas() {
		ArrayList<String> arenas = new ArrayList<String>();

		String path  = plugin.getDataFolder() + "/arenas/";
        File folder = new File(path);

        String[] files = folder.list();
		if(files != null && files.length > 0) {
			for(String arena : files) {
				arenas.add(arena);
			}
		}
		return arenas;
	}

	private HashMap<Player, String> newsErbauer = new HashMap<Player, String>();
	
	private ArrayList<Player> news = new ArrayList<Player>();
	private void newArena(Player p) {
		p.closeInventory();
		news.add(p);
		p.sendMessage(ThomiAPI.prefixGeneral + "§aWie soll die Map heissen?");
		p.sendMessage(ThomiAPI.prefixGeneral + "§eAbbrechen mit '§ccancel§e'.");
	}
	
	private ArrayList<Player> newsKit = new ArrayList<Player>();
	private void newKit(Player p) {
		p.closeInventory();
		newsKit.add(p);
		p.sendMessage(ThomiAPI.prefixGeneral + "§aWie soll das Kit heissen?");
		p.sendMessage(ThomiAPI.prefixGeneral + "§eAbbrechen mit '§ccancel§e'.");
	}
	
	public void openMenu(Player p) {
		Inventory inv = Bukkit.createInventory(null, 18, "§eDuelWorld-Setup");

		ItemStack newArena = new ItemStack(Material.PAPER, 1);
		ItemMeta newArenameta = newArena.getItemMeta();
		newArenameta.setDisplayName("§a§lNeue Arena");
		newArena.setItemMeta(newArenameta);
		inv.setItem(1, newArena);
		
		ItemStack editArena = new ItemStack(Material.IRON_PICKAXE, 1);
		ItemMeta editArenameta = editArena.getItemMeta();
		editArenameta.setDisplayName("§eArena bearbeiten");
		editArena.setItemMeta(editArenameta);
		inv.setItem(2, editArena);
		
		ItemStack newKits = new ItemStack(Material.PAPER, 1);
		ItemMeta newKitsmeta = newKits.getItemMeta();
		newKitsmeta.setDisplayName("§a§lNeues Kit");
		newKits.setItemMeta(newKitsmeta);
		inv.setItem(10, newKits);
		
		ItemStack editKits = new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1);
		ItemMeta editKitsmeta = editKits.getItemMeta();
		editKitsmeta.setDisplayName("§eKits bearbeiten");
		editKits.setItemMeta(editKitsmeta);
		inv.setItem(11, editKits);
		
		ItemStack ending = new ItemStack(Material.ENDER_PEARL, 1);
		ItemMeta endingmeta = ending.getItemMeta();
		endingmeta.setDisplayName("§9Endpunkt setzen");
		ending.setItemMeta(endingmeta);
		inv.setItem(7, ending);
		
		ItemStack toEnding = new ItemStack(Material.EYE_OF_ENDER, 1);
		ItemMeta toEndingmeta = toEnding.getItemMeta();
		toEndingmeta.setDisplayName("§9zum Endpunkt");
		toEnding.setItemMeta(toEndingmeta);
		inv.setItem(8, toEnding);
		
		ItemStack setNPC = new ItemStack(Material.ARMOR_STAND, 1);
		ItemMeta setNPCmeta = setNPC.getItemMeta();
		setNPCmeta.setDisplayName("§eNPC setzen");
		setNPC.setItemMeta(setNPCmeta);
		inv.setItem(16, setNPC);
		
		ItemStack zumNPC = new ItemStack(Material.EYE_OF_ENDER, 1);
		ItemMeta zumNPCmeta = zumNPC.getItemMeta();
		zumNPCmeta.setDisplayName("§ezum NPC");
		zumNPC.setItemMeta(zumNPCmeta);
		inv.setItem(17, zumNPC);
		
		ItemStack setWinHolo = new ItemStack(Material.GOLD_INGOT, 1);
		ItemMeta setWinHolometa = setWinHolo.getItemMeta();
		setWinHolometa.setDisplayName("§eWin-Lead setzen");
		setWinHolo.setItemMeta(setWinHolometa);
		inv.setItem(4, setWinHolo);
		
		ItemStack zumWinHolo = new ItemStack(Material.EYE_OF_ENDER, 1);
		ItemMeta zumWinHolometa = zumWinHolo.getItemMeta();
		zumWinHolometa.setDisplayName("§ezum Win-Lead");
		zumWinHolo.setItemMeta(zumWinHolometa);
		inv.setItem(5, zumWinHolo);
		
		ItemStack setRankHolo = new ItemStack(Material.GOLD_INGOT, 1);
		ItemMeta setRankHolometa = setRankHolo.getItemMeta();
		setRankHolometa.setDisplayName("§eRank-Lead setzen");
		setRankHolo.setItemMeta(setRankHolometa);
		inv.setItem(13, setRankHolo);
		
		ItemStack zumRankHolo = new ItemStack(Material.EYE_OF_ENDER, 1);
		ItemMeta zumRankHolometa = zumRankHolo.getItemMeta();
		zumRankHolometa.setDisplayName("§ezum Rank-Lead");
		zumRankHolo.setItemMeta(zumRankHolometa);
		inv.setItem(14, zumRankHolo);
		
		ItemStack ph1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 7);
		ItemMeta ph1m = ph1.getItemMeta();
		ph1m.setDisplayName(" §f ");
		ph1.setItemMeta(ph1m);
		 
		for(int i = 0; i < inv.getSize(); i++) {
			if(inv.getItem(i) == null) inv.setItem(i, ph1);
		}
		
		p.openInventory(inv);
	}
	
	@EventHandler
	private void onChat(AsyncPlayerChatEvent e) {
		if(newsErbauer.containsKey(e.getPlayer())) {
			e.setCancelled(true);
			String arena = newsErbauer.get(e.getPlayer());
			newsErbauer.remove(e.getPlayer());
			if(!e.getMessage().toLowerCase().contains("cancel")) {
				String erbauer = e.getMessage();
				File file = new File(plugin.getDataFolder() + "/arenas/" + arena + ".yml");
				try {
					file.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
				fc.set("arena.erbauer", erbauer);
				ThomiAPI.saveFile(fc, file);
				e.getPlayer().sendMessage(ThomiAPI.prefixGeneral + "§aErbauer der Arena '§e" + arena + "§a' gesetzt!");
				openArenaEdit(e.getPlayer(), arena);
			} else {
				e.getPlayer().sendMessage(ThomiAPI.prefixGeneral + "§cSetzen des Erbauers abgebrochen.");
				openArenaEdit(e.getPlayer(), arena);
			}
		}
		if(news.contains(e.getPlayer())) {
			e.setCancelled(true);
			news.remove(e.getPlayer());
			if(!e.getMessage().toLowerCase().contains("cancel")) {
				if(!getArenas().contains(e.getMessage())) {
					String name = e.getMessage();
					File file = new File(plugin.getDataFolder() + "/arenas/" + name + ".yml");
					if(!file.exists()) {
						try {
							file.createNewFile();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
						fc.set("arena.enabled", false);
						fc.set("arena.creator", e.getPlayer().getName());
						fc.set("arena.created", ThomiAPI.getTime("dd.MM.yyyy"));
						fc.set("stats.games", 0);
						fc.set("stats.damage", 0);
						fc.set("stats.duels", new ArrayList<String>());
						ThomiAPI.saveFile(fc, file);
						e.getPlayer().sendMessage(ThomiAPI.prefixGeneral + "§aNeue Arena '§e" + name + "§a' erstellt!");
						openArenaEdit(e.getPlayer(), name, false);
					} else {
						e.getPlayer().sendMessage(ThomiAPI.prefixGeneral + "§cDie Datei existiert bereits.");
					}
				} else {
					e.getPlayer().sendMessage(ThomiAPI.prefixGeneral + "§cDie Arena gibt es bereits.");
				}
			} else {
				e.getPlayer().sendMessage(ThomiAPI.prefixGeneral + "§cEinrichten abgebrochen.");
				openMenu(e.getPlayer());
			}
		}
		if(newsKit.contains(e.getPlayer())) {
			e.setCancelled(true);
			newsKit.remove(e.getPlayer());
			if(!e.getMessage().toLowerCase().contains("cancel")) {
				if(!getKits().contains(e.getMessage())) {
					String name = e.getMessage();
					File file = new File(plugin.getDataFolder() + "/kits/" + name + ".yml");
					if(!file.exists()) {
						try {
							file.createNewFile();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
						fc.set("kit.enabled", false);
						fc.set("kit.creator", e.getPlayer().getName());
						fc.set("kit.created", ThomiAPI.getTime("dd.MM.yyyy"));
						fc.set("stats.games", 0);
						fc.set("stats.damage", 0);
						fc.set("stats.duels", new ArrayList<String>());
						ThomiAPI.saveFile(fc, file);
						e.getPlayer().sendMessage(ThomiAPI.prefixGeneral + "§aNeues Kit '§e" + name + "§a' erstellt!");
						openKitEdit(e.getPlayer(), name, false);
					} else {
						e.getPlayer().sendMessage(ThomiAPI.prefixGeneral + "§cDie Datei existiert bereits.");
					}
				} else {
					e.getPlayer().sendMessage(ThomiAPI.prefixGeneral + "§cDas Kit gibt es bereits.");
				}
			} else {
				e.getPlayer().sendMessage(ThomiAPI.prefixGeneral + "§cEinrichten abgebrochen.");
				openMenu(e.getPlayer());
			}
		
		}
	}
	
	private void openKitEdit(Player p, String kit, boolean enabled) {
		Inventory inv = Bukkit.createInventory(null, 9, "§eDW-Kit " + kit.replace("§e", "").replace(".yml", ""));

		ItemStack items = new ItemStack(Material.STICK, 1);
		ItemMeta itemsmeta = items.getItemMeta();
		itemsmeta.setDisplayName("§aItems setzen");
		items.setItemMeta(itemsmeta);
		inv.setItem(1, items);
		
		ItemStack icon = new ItemStack(Material.PAINTING, 1);
		ItemMeta iconmeta = icon.getItemMeta();
		iconmeta.setDisplayName("§aIcon setzen");
		icon.setItemMeta(iconmeta);
		inv.setItem(3, icon);

		ItemStack enable = new ItemStack(Material.INK_SACK, 1, (byte) 1);
		ItemMeta enablemeta = enable.getItemMeta();
		enablemeta.setDisplayName("§c§lDeaktiviert");
		List<String> enablelore = new ArrayList<String>();
		enablelore.add("§a§l§oaktivieren");
		enablemeta.setLore(enablelore);
		enable.setItemMeta(enablemeta);
		ItemStack disable = new ItemStack(Material.INK_SACK, 1, (byte) 10);
		ItemMeta disablemeta = disable.getItemMeta();
		disablemeta.setDisplayName("§a§lAktiviert");
		List<String> disablelore = new ArrayList<String>();
		disablelore.add("§c§l§odeaktivieren");
		disablemeta.setLore(disablelore);
		disable.setItemMeta(disablemeta);

		if(enabled) inv.setItem(5, disable);
		if(!enabled) inv.setItem(5, enable);
		
		ItemStack ph1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 7);
		ItemMeta ph1m = ph1.getItemMeta();
		ph1m.setDisplayName(" §f ");
		ph1.setItemMeta(ph1m);
		
		inv.setItem(7, ThomiAPI.back());
		 
		for(int i = 0; i < 9; i++) {
			if(inv.getItem(i) == null) inv.setItem(i, ph1);
		}
		
		p.openInventory(inv);
	}

	private void openArenaEdit(Player p, String arena) {
		boolean enabled = true;
		File file = new File(plugin.getDataFolder() + "/arenas/" + arena + ".yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		if(fc.getString("arena.enabled") == null && !fc.getBoolean("arena.enabled")) enabled = false;
		openArenaEdit(p, arena, enabled);
	}
	
	private void openArenaEdit(Player p, String arena, boolean enabled) {
		Inventory inv = Bukkit.createInventory(null, 9, "§eDW-Arena " + arena.replace("§e", "").replace(".yml", ""));

		ItemStack spawn1 = new ItemStack(Material.REDSTONE, 1);
		ItemMeta spawn1meta = spawn1.getItemMeta();
		spawn1meta.setDisplayName("§9Spawn 1 setzen");
		spawn1.setItemMeta(spawn1meta);
		inv.setItem(1, spawn1);
		
		ItemStack spawn2 = new ItemStack(Material.REDSTONE, 1);
		ItemMeta spawn2meta = spawn2.getItemMeta();
		spawn2meta.setDisplayName("§9Spawn 2 setzen");
		spawn2.setItemMeta(spawn2meta);
		inv.setItem(2, spawn2);

		ItemStack erbauer = new ItemStack(Material.IRON_PICKAXE, 1);
		ItemMeta erbauermeta = erbauer.getItemMeta();
		erbauermeta.setDisplayName("§e§lErbauer");
		erbauer.setItemMeta(erbauermeta);
		inv.setItem(4, erbauer);

		ItemStack mitte = new ItemStack(Material.EYE_OF_ENDER, 1);
		ItemMeta mittemeta = mitte.getItemMeta();
		mittemeta.setDisplayName("§b§lMitte setzen");
		mitte.setItemMeta(mittemeta);
		inv.setItem(4, mitte);
		
		ItemStack teleport = new ItemStack(Material.EMPTY_MAP, 1);
		ItemMeta teleportmeta = teleport.getItemMeta();
		teleportmeta.setDisplayName("§9§lTeleport");
		teleport.setItemMeta(teleportmeta);
		inv.setItem(0, teleport);

		ItemStack enable = new ItemStack(Material.INK_SACK, 1, (byte) 1);
		ItemMeta enablemeta = enable.getItemMeta();
		enablemeta.setDisplayName("§c§lDeaktiviert");
		List<String> enablelore = new ArrayList<String>();
		enablelore.add("§a§l§oaktivieren");
		enablemeta.setLore(enablelore);
		enable.setItemMeta(enablemeta);
		ItemStack disable = new ItemStack(Material.INK_SACK, 1, (byte) 10);
		ItemMeta disablemeta = disable.getItemMeta();
		disablemeta.setDisplayName("§a§lAktiviert");
		List<String> disablelore = new ArrayList<String>();
		disablelore.add("§c§l§odeaktivieren");
		disablemeta.setLore(disablelore);
		disable.setItemMeta(disablemeta);

		if(enabled) inv.setItem(7, disable);
		if(!enabled) inv.setItem(7, enable);
		
		ItemStack ph1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 7);
		ItemMeta ph1m = ph1.getItemMeta();
		ph1m.setDisplayName(" §f ");
		ph1.setItemMeta(ph1m);
		
		inv.setItem(8, ThomiAPI.back());
		
		for(int i = 0; i < 9; i++) {
			if(inv.getItem(i) == null) inv.setItem(i, ph1);
		}
		
		p.openInventory(inv);
	}
	
	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if(e.getCurrentItem() != null && e.getClickedInventory() != null && !e.getCurrentItem().getType().equals(Material.AIR) && e.getCurrentItem().hasItemMeta()) {
			if(e.getInventory().getName().equals("§eDuelWorld-Setup")) {
				e.setCancelled(true);
				if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("neue arena")) {
					newArena((Player) e.getWhoClicked());
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("arena bearbeiten")) {
					editArenaSel((Player) e.getWhoClicked());
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("neues kit")) {
					newKit((Player) e.getWhoClicked());
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("kits bearbeiten")) {
					editKitSel((Player) e.getWhoClicked());
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("endpunkt setzen")) {
					e.getWhoClicked().closeInventory();
					setEnding((Player) e.getWhoClicked());
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("win-lead setzen")) {
					e.getWhoClicked().closeInventory();
					setWinHolo((Player) e.getWhoClicked());
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("rank-lead setzen")) {
					e.getWhoClicked().closeInventory();
					setRankHolo((Player) e.getWhoClicked());
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("zum endpunkt")) {
					if(DuelWorld.ending == null) {
						e.getWhoClicked().sendMessage(ThomiAPI.prefixGeneral + "§cDer Endpunkt wurde noch nicht gesetzt.");
					} else {
						e.getWhoClicked().teleport(DuelWorld.ending);
						ThomiAPI.sendSound((Player) e.getWhoClicked(), Sound.ENDERMAN_TELEPORT, 15);
					}
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("zum npc")) {
					if(DuelWorld.npc == null) {
						e.getWhoClicked().sendMessage(ThomiAPI.prefixGeneral + "§cDas NPC wurde noch nicht gesetzt.");
					} else {
						e.getWhoClicked().teleport(DuelWorld.npc);
						ThomiAPI.sendSound((Player) e.getWhoClicked(), Sound.ENDERMAN_TELEPORT, 15);
					}
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("zum win")) {
					
					if(DuelWorld.winholo == null) {
						e.getWhoClicked().sendMessage(ThomiAPI.prefixGeneral + "§cDieses Hologram wurde noch nicht gesetzt.");
					} else {
						e.getWhoClicked().teleport(DuelWorld.winholo);
						ThomiAPI.sendSound((Player) e.getWhoClicked(), Sound.ENDERMAN_TELEPORT, 15);
					}
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("zum rank")) {
					if(DuelWorld.rankholo == null) {
						e.getWhoClicked().sendMessage(ThomiAPI.prefixGeneral + "§cDieses Hologram wurde noch nicht gesetzt.");
					} else {
						e.getWhoClicked().teleport(DuelWorld.rankholo);
						ThomiAPI.sendSound((Player) e.getWhoClicked(), Sound.ENDERMAN_TELEPORT, 15);
					}
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("npc")) {
					e.getWhoClicked().closeInventory();
					setNPC((Player) e.getWhoClicked());
				}
			}
			if(e.getInventory().getName().equals("§eDuelWorld-Arenas")) {
				e.setCancelled(true);
				if(ThomiAPI.isBack(e.getCurrentItem())) {
					openMenu((Player) e.getWhoClicked());
				} else {
					String arena = e.getCurrentItem().getItemMeta().getDisplayName().replace("§e", "");
					File file = new File(plugin.getDataFolder() + "/arenas/" + arena + ".yml");
					FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
					boolean enabled = true;
					if(fc.getString("arena.enabled") != null && !fc.getBoolean("arena.enabled")) enabled = false;
					openArenaEdit((Player) e.getWhoClicked(), arena, enabled);
				}
			}
			if(e.getInventory().getName().equals("§eDuelWorld-Kits")) {
				e.setCancelled(true);
				if(ThomiAPI.isBack(e.getCurrentItem())) {
					openMenu((Player) e.getWhoClicked());
				} else {
					String kit = e.getCurrentItem().getItemMeta().getDisplayName().replace("§e", "");
					File file = new File(plugin.getDataFolder() + "/kits/" + kit + ".yml");
					FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
					boolean enabled = true;
					if(fc.getString("kit.enabled") != null && !fc.getBoolean("kit.enabled")) enabled = false;
					openKitEdit((Player) e.getWhoClicked(), kit, enabled);
				}
			}
			if(e.getInventory().getName().startsWith("§eDW-Arena ")) {
				e.setCancelled(true);
				String arena = e.getInventory().getName().replace("§eDW-Arena ", "");
				if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("spawn 1")) {
					e.getWhoClicked().closeInventory();
					setSpawn(((Player) e.getWhoClicked()), arena, 1);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("spawn 2")) {
					e.getWhoClicked().closeInventory();
					setSpawn(((Player) e.getWhoClicked()), arena, 2);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("teleport")) {
					e.getWhoClicked().closeInventory();
					tpToArena(((Player) e.getWhoClicked()), arena);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("aktiviert") || e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("deaktiviert")) {
					toggleArena(((Player) e.getWhoClicked()), arena);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("mitte setzen")) {
					setMid(((Player) e.getWhoClicked()), arena);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("erbauer")) {
					if(!newsErbauer.containsKey((Player) e.getWhoClicked())) {
						e.getWhoClicked().closeInventory();
						newsErbauer.put((Player) e.getWhoClicked(), arena);
						e.getWhoClicked().sendMessage(ThomiAPI.prefixGeneral + "§aWie heisst der Erbauer?");
						e.getWhoClicked().sendMessage(ThomiAPI.prefixGeneral + "§eAbbrechen mit '§ccancel§e'.");
					} else {
						e.getWhoClicked().sendMessage(ThomiAPI.prefixGeneral + "§cSetzen des Erbauers abgebrochen.");
						openArenaEdit((Player) e.getWhoClicked(), arena);
						newsErbauer.remove((Player) e.getWhoClicked());
					}
				} 
				if(ThomiAPI.isBack(e.getCurrentItem())) editArenaSel((Player) e.getWhoClicked());
			}
			if(e.getInventory().getName().startsWith("§eDW-Kit ")) {
				e.setCancelled(true);
				String kit = e.getInventory().getName().replace("§eDW-Kit ", "");
				if(e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
					if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("items setzen")) {
						setItems(((Player) e.getWhoClicked()), kit);
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("icon setzen")) {
						setIcon(((Player) e.getWhoClicked()), kit);
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("aktiviert") || e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase().contains("deaktiviert")) {
							toggleKit(((Player) e.getWhoClicked()), kit);
						}
				}
				if(ThomiAPI.isBack(e.getCurrentItem())) editKitSel((Player) e.getWhoClicked());
			}
		}
	}

}
