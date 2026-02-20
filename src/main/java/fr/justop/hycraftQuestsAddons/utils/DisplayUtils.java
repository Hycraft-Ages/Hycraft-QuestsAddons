package fr.justop.hycraftQuestsAddons.utils;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class DisplayUtils {
    public static void playRankUpgrade(Player player, Plugin plugin, int epoque) {
        String mainTitle = "§6§lRANG SUPÉRIEUR";

        List<String> epoques = Arrays.asList(
                "Préhistoire I",
                "Préhistoire II",
                "Préhistoire III",
                "Antiquité I",
                "Antiquité II",
                "Antiquité III",
                "Moyen-âge I",
                "Moyen-âge II",
                "Moyen-âge III"
        );

        player.sendTitle(mainTitle, "§e" + epoques.get(epoque), 10, 100, 20);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendTitle(mainTitle, "§e" + epoques.get(epoque) + " §7➔", 0, 85, 20);
            player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1.5, 0), 5, 0.2, 0.2, 0.2, 0.1);
        }, 15L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendTitle(mainTitle, "§e" + epoques.get(epoque) + " §7➔ §6§l" + epoques.get(epoque + 1), 0, 65, 20);
            player.getWorld().spawnParticle(Particle.TOTEM, player.getEyeLocation(), 40, 0.5, 0.5, 0.5, 0.2);
        }, 35L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendTitle("§e§l⭐ §6§lRANG SUPÉRIEUR §e§l⭐", "§e" + epoques.get(epoque) + " §7➔ §6§l" + epoques.get(epoque + 1), 0, 50, 20);

            for (int i = 0; i < 5; i++) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 10, 1, 1, 1, 0.1);
                }, i * 5L);
            }
        }, 50L);
    }
}
