package fr.justop.hycraftQuestsAddons.listeners;

import fr.justop.hycraftQuestsAddons.HycraftQuestsAddons;
import fr.justop.hycraftQuestsAddons.objects.ComposterLaunchTask;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.questers.Quester;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ComposterLaunch implements Listener {

	private static final int LAUNCH_HEIGHT = 200;
	private static Set<Player> slowFallPlayers = new HashSet<>();
	private static int indice = 0;
	public static final Location TARGET_LOCATION = new Location(Bukkit.getWorld("prehistoire"), -34.5, 256, 207.5, 90f, 40f);
	static List<String> xandrosDialog1 = Arrays.asList("§6§lXandros §r§e: Hahaha! Tu vois ça, ma vieille bricole fonctionne toujours à merveille!", "§6§lXandros §r§e: Ok, j'ai oublié de te préciser un petit détail... Le diplodocus est très sensible sur certaines parties de sa peau.",
			"§6§lXandros §r§e: Seuls les blocs les plus durs sauront tromper ses sensation, c'est le cas §6§ldu béton bleu §r§eou bien du §6§lcorail§r§e!",
			"§6§lXandros §r§e: Si tu as le malheur de marcher sur le reste de sa peau, sa colère risque d'être terrible",
			"§6§lXandros §r§e: Bon ceci dit, à toi de jouer, va récupérer la corne et ensuite je te ramènerai à moi.");

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();

		if (message.equalsIgnoreCase("Kaboum!")) {
			Block block = player.getLocation().getBlock();
			QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
			Quester acc = questsAPI.getPlugin().getPlayersManager().getQuester(player);

			if (!(acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(115))).getStage().getAsInt() == 1)) return;

			if (block.getType() == Material.COMPOSTER) {

				event.setCancelled(true);

				new BukkitRunnable() {
					@Override
					public void run() {
						new ComposterLaunchTask(player).runTask(HycraftQuestsAddons.getInstance());
					}
				}.runTask(HycraftQuestsAddons.getInstance());
			}
		}
	}


	public static void launchPlayer(Player player) {
		World world = player.getWorld();
		World temporaryWorld = Bukkit.getWorld("mondesurvie");
		player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);

		new BukkitRunnable() {
			double velocity = 5.0;
			double height = 0;

			@Override
			public void run() {
				if (height < LAUNCH_HEIGHT) {
					velocity *= 0.97;
					player.setVelocity(new Vector(0, velocity, 0));
					world.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
					height = player.getLocation().getY();
				} else {
					this.cancel();

					new BukkitRunnable() {
						@Override
						public void run() {
							if (temporaryWorld != null) {
								player.teleport(new Location(temporaryWorld, 0, 100, 0));
							}

							new BukkitRunnable() {
								@Override
								public void run() {
									player.teleport(TARGET_LOCATION);
									player.setFlying(false);
									HycraftQuestsAddons.getInstance().getPhase1().put(player.getUniqueId(), "active");
									slowFall(player);
								}
							}.runTaskLater(HycraftQuestsAddons.getInstance(), 20);
						}
					}.runTaskLater(HycraftQuestsAddons.getInstance(), 20);
				}
			}
		}.runTaskTimer(HycraftQuestsAddons.getInstance(), 0, 2);
	}

	public static void slowFall(Player player)
	{
		slowFallPlayers.add(player);
		player.setGameMode(GameMode.ADVENTURE);
		player.sendMessage("");
		new BukkitRunnable() {
			double velocity = -0.095;

			@Override
			public void run() {
				if (player.getLocation().getY() > TARGET_LOCATION.getY() - 41) {
					player.setVelocity(new Vector(0, velocity, 0));
					player.setGameMode(GameMode.ADVENTURE);

					if (player.getTicksLived() % 90 == 0) {
						player.sendMessage(xandrosDialog1.get(indice));
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
						player.sendMessage("");
						indice++;
					}
				} else {
					this.cancel();
					slowFallPlayers.remove(player);
					indice = 0;
				}
			}
		}.runTaskTimer(HycraftQuestsAddons.getInstance(), 0, 1);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player player) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && slowFallPlayers.contains(player)) {
				event.setCancelled(true);
			}
		}
	}
}

