package fr.justop.hycraftQuestsAddons.listeners;

import fr.justop.hycraftQuestsAddons.BossQuestUtils;
import fr.justop.hycraftQuestsAddons.HycraftQuestsAddons;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.players.PlayerAccount;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ArrowListener implements Listener
{
	private final List<Location> puzzleSequence = Arrays.asList(
			new Location(Bukkit.getWorld("Prehistoire"), -60, -1, -291),
			new Location(Bukkit.getWorld("Prehistoire"), -34, 2, -316),
			new Location(Bukkit.getWorld("Prehistoire"), -58, -2, -315),
			new Location(Bukkit.getWorld("Prehistoire"), -31, 1, -293),
			new Location(Bukkit.getWorld("Prehistoire"), -45, 0, -284),
			new Location(Bukkit.getWorld("Prehistoire"), -46, 0, -321),
			new Location(Bukkit.getWorld("Prehistoire"), -25, -1, -306),
			new Location(Bukkit.getWorld("Prehistoire"), -65, -4, -303)

	);

	private static final List<Vector> relativeCristalsPos = Arrays.asList(
			new Vector(20, 23, -4),
			new Vector(11, 26, -14),
			new Vector(-1, 24, -18),
			new Vector(-13, 22, -13),
			new Vector(-20, 20, -1),
			new Vector(-13, 22, 12),
			new Vector(1, 25, 17),
			new Vector(14, 26, 8)

	);

	@EventHandler
	public void onArrowHit(ProjectileHitEvent event) {
		Entity projectile = event.getEntity();
		if (!(projectile instanceof Arrow arrow) || !(arrow.getShooter() instanceof Player player)) return;

		if(event.getHitBlock() == null) return;

		Location hitLocation = event.getHitBlock().getLocation();
		if(arrow.getShooter() == null) return;
		if(!(Objects.requireNonNull(((Player) arrow.getShooter()).getInventory().getItemInMainHand().getItemMeta()).hasCustomModelData())) return;

		int customModelData = Objects.requireNonNull(((Player) arrow.getShooter()).getInventory().getItemInMainHand().getItemMeta()).getCustomModelData();
		if(! (customModelData <= 3071 && customModelData >= 3061)) return;

		if(HycraftQuestsAddons.getInstance().getSpiritPlayers().containsKey(player.getUniqueId()))
		{
			if (HycraftQuestsAddons.getInstance().getActiveCristalPos().containsKey(player.getUniqueId()))
			{
				Location loc = HycraftQuestsAddons.getInstance().getActiveCristalPos().get(player.getUniqueId());
				if(loc.equals(hitLocation))
				{
					loc.getBlock().setType(Material.ANDESITE);
					HycraftQuestsAddons.removeNearbyEntities(player);

					HycraftQuestsAddons.getInstance().getActionbarTasks().get(player.getUniqueId()).cancel();
					HycraftQuestsAddons.getInstance().getActionbarTasks().remove(player.getUniqueId());

					BossQuestUtils.freezeBoss(HycraftQuestsAddons.getInstance().getBosses().get(player.getUniqueId()), 15*20);
					HycraftQuestsAddons.getInstance().getActiveCristalPos().remove(player.getUniqueId());
					HycraftQuestsAddons.getInstance().getSpiritPlayers().remove(player.getUniqueId());
					player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL,1.0f, 1.8f);
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aVous avez temporairement mis le boss hors d'état de nuire. C'est le moment de contre-attaquer!");
				}
			}
		}

		if (!puzzleSequence.contains(hitLocation)) {
			return;
		}

		QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
		PlayerAccount acc = questsAPI.getPlugin().getPlayersManager().getAccount(player);

		if (!(acc.getQuestDatas(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(125))).getStage() == 1)) return;

		List<Location> progress = HycraftQuestsAddons.getInstance().getPuzzleProgress().computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
		if (progress.size() < puzzleSequence.size() && puzzleSequence.get(progress.size()).equals(hitLocation)) {
			progress.add(hitLocation);
			switch (progress.size())
			{
				case 1:
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aUn léger grincement se fait entendre... (1/8)");
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 0.5f);
					break;

				case 2:
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aLe grincement devient persistant... (2/8)");
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 0.7f);
					break;

				case 3:
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aLe grincement continue à gagner en puissance... (3/8)");
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 0.9f);
					break;

				case 4:
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aVous ressentez de légères secousses... (4/8)");
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 1.1f);
					break;

				case 5:
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aLes secousses augmentent en intensité... (5/8)");
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 1.3f);
					break;

				case 6:
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aLe terrain entier se met à trembler autour de vous... (6/8)");
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 1.5f);
					break;

				case 7:
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aVous entendez un rugissement venu de la terre... (7/8)");
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 1.7f);
					break;

				case 8:
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aEntre le bruit et les tremblements, impossible de tenir debout. Vous perdez connaissance. (8/8)");
					player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
					HycraftQuestsAddons.getInstance().getPuzzleProgress().remove(player.getUniqueId());
					player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, PotionEffect.INFINITE_DURATION, 2));

					new BukkitRunnable()
					{
						@Override
						public void run() {
							BossQuestUtils.startBossFight(player);
						}

					}.runTaskLater(HycraftQuestsAddons.getInstance(), 5*20);

					break;
			}
		} else {
			player.sendMessage(HycraftQuestsAddons.PREFIX + "§cPlus rien ne se passe... Il semblerait que vous ayez échoué... Rééssayez!");
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 0.5f);
			HycraftQuestsAddons.getInstance().getPuzzleProgress().remove(player.getUniqueId());
		}
	}

	public static Location getRandomCristal(Location arenaLocation)
	{
		Random random = new Random();
		Vector vec = relativeCristalsPos.get(random.nextInt(8));
		return arenaLocation.clone().add(vec);
	}
}
