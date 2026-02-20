package fr.justop.hycraftQuestsAddons.listeners;

import fr.justop.hycraftQuestsAddons.BossQuestUtils;
import fr.justop.hycraftQuestsAddons.HycraftQuestsAddons;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.questers.Quester;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.IOException;
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

        if (event.getHitBlock() == null) return;
        Location hitLocation = event.getHitBlock().getLocation();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (!mainHandItem.hasItemMeta() || !mainHandItem.getItemMeta().hasCustomModelData()) return;

        int customModelData = mainHandItem.getItemMeta().getCustomModelData();
        if (!(customModelData <= 3071 && customModelData >= 3061)) return;

        if (HycraftQuestsAddons.getInstance().getSpiritPlayers().containsKey(player.getUniqueId())) {
            if (HycraftQuestsAddons.getInstance().getActiveCristalPos().containsKey(player.getUniqueId())) {

                List<Location> crystals = HycraftQuestsAddons.getInstance().getActiveCristalPos().get(player.getUniqueId());
                double rayonTolerance = 0.8;
                Location hitCrystal = null;

                for (Location loc : crystals) {
                    if (loc.getWorld().equals(hitLocation.getWorld()) && loc.distance(hitLocation) <= rayonTolerance) {
                        hitCrystal = loc;
                        break;
                    }
                }
                if (hitCrystal != null) {
                    hitCrystal.getBlock().setType(Material.ANDESITE);
                    crystals.remove(hitCrystal);

                    if (crystals.isEmpty()) {

                        HycraftQuestsAddons.removeNearbyEntities(player);

                        if (HycraftQuestsAddons.getInstance().getActionbarTasks().containsKey(player.getUniqueId())) {
                            HycraftQuestsAddons.getInstance().getActionbarTasks().get(player.getUniqueId()).cancel();
                            HycraftQuestsAddons.getInstance().getActionbarTasks().remove(player.getUniqueId());
                        }

                        HycraftQuestsAddons.getInstance().getFrozenBosses().add(player.getUniqueId());
                        BossQuestUtils.freezeBoss(player, HycraftQuestsAddons.getInstance().getBosses().get(player.getUniqueId()), 15);

                        HycraftQuestsAddons.getInstance().getActiveCristalPos().remove(player.getUniqueId());
                        HycraftQuestsAddons.getInstance().getSpiritPlayers().remove(player.getUniqueId());

                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.8f);
                        player.sendMessage(HycraftQuestsAddons.PREFIX + "§aTous les cristaux sont détruits ! Le boss est vulnérable !");
                    } else {

                        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.2f);
                        player.sendMessage(HycraftQuestsAddons.PREFIX + "§eCristal détruit ! Encore §6" + crystals.size() + " §eà abattre.");
                    }
                }
            }
        }

		if (!puzzleSequence.contains(hitLocation)) {
			return;
		}

		QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
		Quester acc = questsAPI.getPlugin().getPlayersManager().getQuester(player);

		if (!(acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(125))).getStage().orElse(-1) == 1)) return;

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
                            try {
                                BossQuestUtils.startBossFight(player);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
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

    public static List<Location> getRandomCristals(Location arenaLocation, int n) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            indices.add(i);
        }

        Collections.shuffle(indices);

        List<Location> selectedLocations = new ArrayList<>();
        int limit = Math.min(n, indices.size());

        for (int i = 0; i < limit; i++) {
            int index = indices.get(i);
            Vector vec = relativeCristalsPos.get(index);
            selectedLocations.add(arenaLocation.clone().add(vec));
        }

        return selectedLocations;
    }
}
