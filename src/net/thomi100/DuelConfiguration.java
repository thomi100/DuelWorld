package net.thomi100;

import org.bukkit.plugin.Plugin;

/*
 * Created by thomi100 on 23.03.2018.
 */
public class DuelConfiguration {

    public static String prefix = "§8[§9Duel§eWorld§9] §r";

    public static void loadConfig(Plugin plugin) {
        boolean save = false;

        if(plugin.getConfig().getString("prefix") != null) {
            prefix = plugin.getConfig().getString("prefix");
        } else {
            plugin.getConfig().set("prefix", prefix.replace("§", ""));
            save = true;
        }

        // TODO

    }

}
