package fr.justop.hycraftQuestsAddons.objects;

import fr.justop.hycraftQuestsAddons.HycraftQuestsAddons;
import fr.justop.hycraftQuestsAddons.listeners.ComposterLaunch;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;

public class ComposterLaunchTask extends BukkitRunnable {
	private final Player player;
	private int countdown = 3;

	public ComposterLaunchTask(Player player) {
		this.player = player;
	}

	@Override
	public void run() {
		HycraftQuestsAddons.getInstance().boxPlayer(player);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (countdown > 0) {
					String color = countdown == 1 ? "\u00a7c" : "\u00a7e";
					player.sendTitle(color + "" + countdown, "", 0, 20, 0);
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aLancement dans §e" + countdown + "§a...");
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f + (3 - countdown) * 0.2f);
					countdown--;
				} else {
					this.cancel();

					HycraftQuestsAddons.getInstance().unboxPlayer(player);
					player.sendMessage("");
					player.sendMessage("§6§lXandros §r§e: C'est parti mon kiki, on se reparle là-haut!");
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);

					ComposterLaunch.launchPlayer(player);
				}
			}
		}.runTaskTimer(HycraftQuestsAddons.getInstance(), 0, 20);
	}
}
