package fr.justop.hycraftQuestsAddons.utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MusicUtils {
    public static void playVictoryMusic(Player player, Plugin plugin) {
        Sound instrument = Sound.BLOCK_NOTE_BLOCK_BELL;
        float volume = 1.0f;

        player.playSound(player.getLocation(), instrument, volume, 1.0f);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                player.playSound(player.getLocation(), instrument, volume, 1.0f), 3L);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                player.playSound(player.getLocation(), instrument, volume, 1.0f), 6L);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                player.playSound(player.getLocation(), instrument, volume, 1.5f), 9L);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                player.playSound(player.getLocation(), instrument, volume, 1.25f), 15L);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                player.playSound(player.getLocation(), instrument, volume, 1.5f), 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f), 20L);
    }
}
