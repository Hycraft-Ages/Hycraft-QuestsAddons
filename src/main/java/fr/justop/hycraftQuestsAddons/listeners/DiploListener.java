package fr.justop.hycraftQuestsAddons.listeners;

import fr.justop.hycraftQuestsAddons.HycraftQuestsAddons;
import fr.justop.hycraftQuestsAddons.objects.CuboidRegion;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.data.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class DiploListener implements Listener
{
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
        Quester acc = questsAPI.getPlugin().getPlayersManager().getQuester(player);

        CuboidRegion diploRegion = HycraftQuestsAddons.getInstance().getRegions().get("DiploOpenRegion");
        if (diploRegion != null && diploRegion.isInside(event.getTo())) {
            if (HycraftQuestsAddons.getInstance().getPhase1().containsKey(player.getUniqueId())) {

                Location backLoc = new Location(player.getWorld(), -46.0, 217.0, 207.5, -90.0f, 0.0f);
                player.teleport(backLoc);

                player.playSound(backLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendMessage(HycraftQuestsAddons.PREFIX + "§cTu n'as pas accès à cette zone pour le moment !");
            }
        }

		if(!HycraftQuestsAddons.getInstance().getPhase2().containsKey(player.getUniqueId())) return;
		if(!HycraftQuestsAddons.getInstance().getPhase2().get(player.getUniqueId()).equals("active") || !(acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(115))).getStage().orElse(-1) == 4)) return;

		if (player.getVelocity().getY() > 0) {
			return;
		}

		if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
				event.getFrom().getBlockY() != event.getTo().getBlockY() ||
				event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {

			Location belowPlayer = event.getTo().clone().subtract(0, 1, 0);
			Material blockType = belowPlayer.getBlock().getType();
			player.sendMessage();

			if (!HycraftQuestsAddons.getInstance().getAllowedBlocks().contains(blockType)) {
				player.teleport(new Location(Bukkit.getWorld("Prehistoire"), -53, 219, 207, 90f, 0f));
				player.sendMessage(HycraftQuestsAddons.PREFIX + "§cTu as attisé la colère du diplodocus. Recommence et tâche d'être plus discret!");
				player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
			}
		}
	}

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if(event.getMessage().equalsIgnoreCase("/q interrupt") || event.getMessage().equalsIgnoreCase("/q rejoin")) return;
		if (HycraftQuestsAddons.getInstance().getPhase2().containsKey(player.getUniqueId())) {
			if(HycraftQuestsAddons.getInstance().getPhase2().get(player.getUniqueId()) == "active")
			{
				event.setCancelled(true);
				player.sendMessage(HycraftQuestsAddons.PREFIX + "§eVous ne pouvez pas executer de commande tant que la quête est en cours! Utilisez §6/q interrupt §epour interrompre la quête, vous pourrez la rejoindre plus tard.");
				return;
			}

		}
		if (HycraftQuestsAddons.getInstance().getPhase1().containsKey(player.getUniqueId())) {
			if(HycraftQuestsAddons.getInstance().getPhase1().get(player.getUniqueId()) == "active")
			{
				event.setCancelled(true);
				player.sendMessage(HycraftQuestsAddons.PREFIX + "§eVous ne pouvez pas executer de commande tant que la quête est en cours! Utilisez §6/q interrupt §epour interrompre la quête, vous pourrez la rejoindre plus tard.");
			}

		}
	}

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
                Quester acc = questsAPI.getPlugin().getPlayersManager().getQuester(player);

                if (acc == null) {
                    player.sendMessage("§cEnvoie un mp à JustOp (code d'erreur 112)");
                    return;
                }
                Quest quest115 = questsAPI.getQuestsManager().getQuest(115);
                if (quest115 == null) return;

                QuesterQuestData data = acc.getDataHolder().getQuestData(quest115);
                if (!data.hasStarted() || !data.getStage().isPresent()) return;

                int stageIndex = data.getStage().getAsInt();

                if (stageIndex == 2 || stageIndex == 3) {
                    HycraftQuestsAddons.getInstance().getPhase1().put(player.getUniqueId(), "inactive");
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§eVous avez une quête en cours ! Exécutez §b/q rejoin §epour rejoindre la zone de quête.");
                }
                if (stageIndex == 4) {
                    HycraftQuestsAddons.getInstance().getPhase2().put(player.getUniqueId(), "inactive");
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§eVous avez une quête en cours ! Exécutez §b/q rejoin §epour rejoindre la zone de quête.");
                }
            }
        }.runTaskLater(HycraftQuestsAddons.getInstance(), 10);
    }
}

