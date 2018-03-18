package net.thomi100;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DuelWorlds extends JavaPlugin {
	
	private DuelArenas dwarenas;
	public DuelArenas DuelArenas() {
	      return dwarenas;
	}
	
	private DuelListener dwlistener;
	public DuelListener DuelListener() {
	      return dwlistener;
	}
	
	private DuelSetup dwsetup;
	public DuelSetup DuelSetup() {
	      return dwsetup;
	}
	
	private DuelWorld duelworld;
	public DuelWorld DuelWorld() {
	      return duelworld;
	}
	
	private DuelStats dwstats;
	public DuelStats DuelStats() {
	      return dwstats;
	}

	@Override
	public void onEnable() {
		System.out.println(ThomiAPI.getEnable(this));

		dwarenas = new DuelArenas(this);
		dwlistener = new DuelListener(this);
		dwsetup = new DuelSetup(this);
		duelworld = new DuelWorld(this);
		dwstats = new DuelStats(this);
		DuelWorld().setLocations();
		DuelArenas().init();
		DuelSetup.setNPC();
		new BukkitRunnable() {
			@Override
			public void run() {
				DuelRanks.checkOld();
			}
		}.runTaskLater(this, 5*20);
		
		DuelLobby.plugin = Bukkit.getPluginManager().getPlugin("DuelWorld");
		DuelLobby.refreshRankLead();
		DuelLobby.refreshWinLead();

	}
	
	@Override
	public void onDisable() {
		System.out.println(ThomiAPI.getDisable(this));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("duel")) {
			DuelWorld().command(sender, cmd, args);
			return true;
		}
		
		return false;
	}
}
