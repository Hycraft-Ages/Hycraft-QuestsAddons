package fr.justop.hycraftQuestsAddons.listeners;

import fr.justop.hycraftQuestsAddons.HycraftQuestsAddons;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.players.PlayerAccount;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class GeneralListener implements Listener {

    @EventHandler
    public void onPlayerDismount(VehicleExitEvent event) {
        if (event.getExited() instanceof Player player && event.getVehicle() instanceof Horse horse) {
            QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
            PlayerAccount acc = questsAPI.getPlugin().getPlayersManager().getAccount(player);
            if(horse.getCustomName() == null) return;
            if (201.0 < player.getLocation().getX() && player.getLocation().getX() < 209.1 && player.getLocation().getZ() < -352.0 && player.getLocation().getZ() > -361.0) return;
            if(!(horse.getCustomName().equalsIgnoreCase("Nicolas")) || !(acc.getQuestDatas(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(132))).getStage() == 1)) return;
            player.sendMessage(HycraftQuestsAddons.PREFIX + "\u00a7cTu ne peux pas descendre de ton cheval pendant la course !");
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();
        QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
        PlayerAccount acc = questsAPI.getPlugin().getPlayersManager().getAccount(player);

        if(acc.getQuestDatas(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(132))).getStage() == 1)
        {
            event.setCancelled(true);
            player.sendMessage(HycraftQuestsAddons.PREFIX + "§cTu ne peux pas lâcher d'item durant la course!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        new BukkitRunnable()
        {

            @Override
            public void run() {
                QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
                PlayerAccount acc = questsAPI.getPlugin().getPlayersManager().getAccount(player);
                if(!(acc.getQuestDatas(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(132))).getStage() == 1)) return;

                acc.getQuestDatas(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(132))).setStage(0);
            }

        }.runTaskLater(HycraftQuestsAddons.getInstance(),10);

    }
}
