package net.thomi100;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.List;

public class DuelListener implements Listener {

	private static DuelWorlds plugin;

	public DuelListener(DuelWorlds main) {
		
		DuelListener.plugin = main;
		DuelListener.plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
	}
	
	public void ohDeath(Player loser) {
		loser.damage(0);
		String arena = plugin.DuelArenas().getGame(loser);
		ArrayList<Player> player1 = new ArrayList<>();
		ArrayList<Player> player2 = new ArrayList<>();
		player1.addAll(plugin.DuelArenas().ingames.get(arena).keySet());
		player2.addAll(plugin.DuelArenas().ingames.get(arena).values());
		Player a = player1.get(0);
		Player b = player2.get(0);
		Player winner = null;
		if(a.getName().equals(loser.getName())) {
			winner = b;
		} else {
			winner = a;
		}

		winner.sendMessage(ThomiAPI.prefixGeneral + "§aDu gewinnst gegen §e" + loser.getName() + "§a!");
		loser.sendMessage(ThomiAPI.prefixGeneral + "§e" + winner.getName() + "§c hat den Kampf gewonnen!");



		int ELO = -1;
		boolean isRanked = plugin.DuelArenas().ingameRankeds.get(arena);
		if(isRanked) {
			double health = winner.getHealth();
			double dist = -1;
			if(winner.getLocation().getWorld() == loser.getLocation().getWorld()) dist = winner.getLocation().distance(loser.getLocation());
			if(dist > 1) {
				ELO = DuelRanks.add(winner, loser, dist, health);
			}
		}
		plugin.DuelStats().win(winner, loser, arena, plugin.DuelArenas().ingameKits.get(arena), plugin.DuelArenas().ingameDamage.get(arena), isRanked, ELO);



		int winnerCredits = 5;

        loser.sendMessage(ThomiAPI.line);
		winner.sendMessage(ThomiAPI.prefixGeneral + "§6§lErhaltene " + CreditAPI.CURRENCY_NAME_PL + ":");
		winner.sendMessage(ThomiAPI.prefixGeneral + "§aSieg§7: §6" + winnerCredits + " " + CreditAPI.CURRENCY_NAME_PL);
		if(winner.getHealth() == winner.getMaxHealth()) {
			winnerCredits += 3;
			winner.sendMessage(ThomiAPI.prefixGeneral + "§aLeben voll§7: §63 " + CreditAPI.CURRENCY_NAME_PL);
		}
		if(plugin.DuelArenas().ingameDamage.get(arena) > 75) {
		    winnerCredits += 2;
		    winner.sendMessage(ThomiAPI.prefixGeneral + "§aHoher Schaden§7: §62" + CreditAPI.CURRENCY_NAME_PL);
        }
		if(isRanked) {
			winner.sendMessage(ThomiAPI.prefixGeneral + "§aRanked§7: §6+30% " + CreditAPI.CURRENCY_NAME_PL);
			winnerCredits += ((winnerCredits / 100) * 30);
		}
        winner.sendMessage(ThomiAPI.prefixGeneral + "§a" + CreditAPI.CURRENCY_NAME_PL + " total§7: §6" + winnerCredits + " " + CreditAPI.CURRENCY_NAME_PL);
        loser.sendMessage(ThomiAPI.line);
		CreditAPI.updateCoins(winner, winnerCredits);


        int loserCredits = 3;

        loser.sendMessage(ThomiAPI.line);
        loser.sendMessage(ThomiAPI.prefixGeneral + "§6§lErhaltene " + CreditAPI.CURRENCY_NAME_PL + ":");
        loser.sendMessage(ThomiAPI.prefixGeneral + "§cNiederlage§7: §6" + loserCredits + " " + CreditAPI.CURRENCY_NAME_PL);
        if(winner.getHealth() == winner.getMaxHealth()) {
            loserCredits -= 2;
            loser.sendMessage(ThomiAPI.prefixGeneral + "§aGegner Leben voll§7: §6-2 " + CreditAPI.CURRENCY_NAME_PL);
        }
        if(plugin.DuelArenas().ingameDamage.get(arena) > 75) {
            loserCredits += 2;
            loser.sendMessage(ThomiAPI.prefixGeneral + "§aHoher Schaden§7: §62" + CreditAPI.CURRENCY_NAME_PL);
        }
        if(isRanked) {
            loser.sendMessage(ThomiAPI.prefixGeneral + "§aRanked§7: §6+30% " + CreditAPI.CURRENCY_NAME_PL);
            loserCredits += ((loserCredits / 100) * 30);
        }
        loser.sendMessage(ThomiAPI.prefixGeneral + "§a" + CreditAPI.CURRENCY_NAME_PL + " total§7: §6" + loserCredits + " " + CreditAPI.CURRENCY_NAME_PL);
        loser.sendMessage(ThomiAPI.line);
        CreditAPI.updateCoins(loser, loserCredits);

		ThomiAPI.clearPlayer(winner, true);
		ThomiAPI.clearPlayer(loser, true);

		Location ending = DuelWorld.ending;
		if(ending != null) {
			
			winner.teleport(DuelWorld.ending);
			
			loser.teleport(DuelWorld.ending);
		} else {
			winner.teleport(Bukkit.getWorld("Lobby").getSpawnLocation());
			loser.teleport(Bukkit.getWorld("Lobby").getSpawnLocation());
		}
		
		plugin.DuelArenas().ingameDamage.remove(arena);
		plugin.DuelArenas().ingames.remove(arena);
		plugin.DuelArenas().ingameKits.remove(arena);
		plugin.DuelArenas().ingameRankeds.remove(arena);
		
		plugin.DuelArenas().arenaTask.get(arena).cancel();
		plugin.DuelArenas().arenaTask.remove(arena);
		plugin.DuelArenas().arenaTime.remove(arena);

        ThomiAPI.sendSound(loser, Sound.ENDERMAN_DEATH, 15);
        ThomiAPI.sendSound(loser, Sound.BAT_DEATH, 15);

        ThomiAPI.sendSound(winner, Sound.LEVEL_UP, 15);
        ThomiAPI.sendSound(winner, Sound.ORB_PICKUP, 15);
	}
	
	public static List<Player> starting = new ArrayList<>();
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if(starting.contains(e.getPlayer())) {
			if(e.getFrom().distance(e.getTo()) > 0) {
				Location l = e.getFrom();
				l.setY(l.getWorld().getHighestBlockYAt(l));
				e.getPlayer().teleport(l);
			}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if(e.getClickedBlock().getState() instanceof Sign) {
				Sign sign = (Sign) e.getClickedBlock().getState();
				if(sign.getLine(0).equals(plugin.DuelWorld().sign0)) {
					e.setCancelled(true);
					if(sign.getLine(2).equals(plugin.DuelWorld().sign2_j)) {
						plugin.DuelArenas().joinQueue(e.getPlayer());
					} else if(sign.getLine(2).equals(plugin.DuelWorld().sign2_l)) {
						plugin.DuelArenas().leave(e.getPlayer());
					}
				}
			}
		}
		if(plugin.DuelArenas().isIngame(e.getPlayer())) {
			if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if(e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType().equals(Material.MUSHROOM_SOUP)) {
					if(e.getPlayer().getHealth() != e.getPlayer().getMaxHealth()) {
						e.getPlayer().setHealth(e.getPlayer().getHealth() + 3);
						e.getPlayer().setItemInHand(null);	
						ThomiAPI.sendSound(e.getPlayer(), Sound.BURP, 15);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if(e.getPlayer().hasPermission("DuelWorld.signs")) {
			if(e.getLine(0).equalsIgnoreCase("[duelworld]")) {
				if(e.getLine(2) != null && e.getLine(2).length() > 0) {
					if(e.getLine(2).equalsIgnoreCase("join")) {
						e.setLine(0, plugin.DuelWorld().sign0);
						e.setLine(1, plugin.DuelWorld().sign1);
						e.setLine(2, plugin.DuelWorld().sign2_j);
						e.setLine(3, plugin.DuelWorld().sign3_j);
					} else if(e.getLine(2).equalsIgnoreCase("leave")) {
						e.setLine(0, plugin.DuelWorld().sign0);
						e.setLine(1, plugin.DuelWorld().sign1);
						e.setLine(2, plugin.DuelWorld().sign2_l);
						e.setLine(3, plugin.DuelWorld().sign3_l);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if(plugin.DuelArenas().isIngame(e.getPlayer())) {
			ohDeath(e.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		DuelRanks.create(e.getPlayer());
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Player) {
			Player p = ((Player) e.getDamager());
			if(plugin.DuelArenas().isIngame(p)) {
				Double damage = e.getDamage();
				String arena = plugin.DuelArenas().getGame(p).replace(".yml", "");
				if(plugin.DuelArenas().ingameDamage.containsKey(arena)) {
					damage += plugin.DuelArenas().ingameDamage.get(arena);
					plugin.DuelArenas().ingameDamage.remove(arena);
				}
				plugin.DuelArenas().ingameDamage.put(arena, damage);
			} else {
			    e.setCancelled(true);
            }
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player damaged = (Player) e.getEntity();
			if(plugin.DuelArenas().isIngame(damaged)) {
				Double damage = e.getDamage();
				if(damage >= damaged.getHealth()) {
					e.setCancelled(true);
					ohDeath(damaged);
				}
			}
			if(plugin.DuelArenas().isIngame(((Player) e.getEntity()))) {
				if(e.getCause() == DamageCause.FALL) e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		if(plugin.DuelArenas().isIngame(e.getPlayer())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ThomiAPI.prefixGeneral + "§cDu darfst keine Befehle ingame ausführen.");
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if(plugin.DuelArenas().isIngame(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if(plugin.DuelArenas().isIngame(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity2(EntityDamageByEntityEvent e) {
		Entity ent = e.getEntity();
		
		if(ent.getType() == EntityType.ARMOR_STAND && ent instanceof CraftLivingEntity && DuelWorld.npc != null && ent.getLocation().distance(DuelWorld.npc) <= 1 && ((CraftLivingEntity) ent).getEquipment().getItemInHand().getType().equals(Material.IRON_SWORD) && ((CraftLivingEntity) ent).getEquipment().getHelmet().getType().equals(Material.SKULL_ITEM)) {
			e.setCancelled(true);
			if(e.getDamager() instanceof Player) {
				Player p = (Player) e.getDamager();
				if(p.isSneaking()) {
					plugin.DuelArenas().leave(p);
				} else {
					plugin.DuelArenas().joinQueue(p);
				}
			}
			
		}
	}
	
	@EventHandler
	public void onPlayerEntityInteract(PlayerInteractAtEntityEvent e) {
		Entity ent = e.getRightClicked();
		
		if(ent.getType() == EntityType.ARMOR_STAND && ent instanceof CraftLivingEntity && DuelWorld.npc != null && ent.getLocation().distance(DuelWorld.npc) <= 1 && ((CraftLivingEntity) ent).getEquipment().getItemInHand().getType().equals(Material.IRON_SWORD) && ((CraftLivingEntity) ent).getEquipment().getHelmet().getType().equals(Material.SKULL_ITEM)) {
			e.setCancelled(true);
			if(e.getPlayer().isSneaking()) {
				plugin.DuelArenas().leave(e.getPlayer());
			} else {
				plugin.DuelArenas().joinQueue(e.getPlayer());
			}
		}
	}
}
