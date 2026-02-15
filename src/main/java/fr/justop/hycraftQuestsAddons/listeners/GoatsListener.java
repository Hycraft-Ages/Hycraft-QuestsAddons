package fr.justop.hycraftQuestsAddons.listeners;

import fr.justop.hycraftQuestsAddons.HycraftQuestsAddons;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.players.PlayerAccount;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.momirealms.customnameplates.api.CNPlayer;
import net.momirealms.customnameplates.api.CustomNameplatesAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GoatsListener implements Listener
{
	@EventHandler
	public void onRightClickMobHead(PlayerInteractEvent event) {
		if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
		if (event.getHand() != EquipmentSlot.HAND) return;
		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null) return;
		if (clickedBlock.getType() != Material.PLAYER_HEAD) return;
		Location targetLocation = new Location(Bukkit.getWorld("Prehistoire"), -435, 2, 302);
		if (!clickedBlock.getLocation().equals(targetLocation)) return;

		if(HycraftQuestsAddons.getInstance().getGoatsCounter().get(event.getPlayer().getUniqueId()).contains(94))
		{
			event.getPlayer().sendMessage(HycraftQuestsAddons.PREFIX + "§cVous avez déjà examiné cette tête de chèvre!");
			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
			return;
		}

		HycraftQuestsAddons.getInstance().getGoatsCounter().get(event.getPlayer().getUniqueId()).add(94);
		event.getPlayer().sendMessage(HycraftQuestsAddons.PREFIX + "§aVous avez trouvé euh... Ce qu'il reste de l'une des chèvres de M.Sevin! §e(" +
				HycraftQuestsAddons.getInstance().getGoatsCounter().get(event.getPlayer().getUniqueId()).size() +
				"/11)");
		event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_FIRECHARGE_USE,1.0F, 1.0F);

		if(HycraftQuestsAddons.getInstance().getGoatsCounter().get(event.getPlayer().getUniqueId()).size() == 11)
		{
			LuckPerms luckPerms = LuckPermsProvider.get();
			HycraftQuestsAddons.addPermission(luckPerms.getPlayerAdapter(Player.class).getUser(event.getPlayer()), "hycraft.questsaddons.hasfoundgoats");
			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
			event.getPlayer().sendMessage(HycraftQuestsAddons.PREFIX + "§aVous avez retrouvé toutes les chèvres de §eM.Sevin §a. Allez lui apprendre la nouvelle!");

		}

	}

	@EventHandler
	public void onClicNpc(NPCRightClickEvent event){
		NPC npc = event.getNPC();
		Player player = event.getClicker();
		if(npc.getId() <= 93 && npc.getId() >= 84)
		{
			QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
			PlayerAccount acc = questsAPI.getPlugin().getPlayersManager().getAccount(player);
			List<Integer> playerList = HycraftQuestsAddons.getInstance().getGoatsCounter().get(player.getUniqueId());

			if(acc.getQuestDatas(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(114))).getStage() == 1)
			{
				if(!(playerList.contains(npc.getId())))
				{
					playerList.add(npc.getId());
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aVous avez retrouvé §e" +
							playerList.size() +
							"/11 §achèvres de M.Sevin");
					player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE,1.0F, 1.0F);

					if(playerList.size() == 11)
					{
						LuckPerms luckPerms = LuckPermsProvider.get();
						HycraftQuestsAddons.addPermission(luckPerms.getPlayerAdapter(Player.class).getUser(player), "hycraft.questsaddons.hasfoundgoats");
						player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
						player.sendMessage(HycraftQuestsAddons.PREFIX + "§aVous avez retrouvé toutes les chèvres de §eM.Sevin §a. Allez lui apprendre la nouvelle!");

					}
					return;
				}
				player.sendMessage(HycraftQuestsAddons.PREFIX + "§cVous avez déjà trouvé cette chèvre!");
				player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
			}

		}
	}
	public static ItemStack createTrackingCompass() {
		ItemStack compass = new ItemStack(Material.RECOVERY_COMPASS);
		ItemMeta meta = compass.getItemMeta();
		if (meta != null) {
			meta.setDisplayName("§6Traqueur de chèvres");
			compass.setItemMeta(meta);
		}
		return compass;
	}

	public static void startTrackingTask() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					ItemStack item = player.getInventory().getItemInMainHand();
					if (isTrackingCompass(item)) {
						Location playerLocation = player.getLocation();
						CNPlayer cnPlayer = CustomNameplatesAPI.getInstance().getPlayer(player.getUniqueId());

						CompletableFuture.supplyAsync(() -> findNearestNpc(player, playerLocation))
								.thenAccept(nearest -> {
									if (nearest != null && player.isOnline()) {
										UUID uuid = player.getUniqueId();
										String reserveId = "questsaddon:traqueur-" + uuid;

										if (cnPlayer != null) {
											cnPlayer.acquireActionBar(reserveId);
											player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + "Chèvre détectée à " + ChatColor.RED + nearest.distance(playerLocation) + ChatColor.YELLOW + " blocks"));
										}
									}
								});
					}
				}
			}
		}.runTaskTimer(HycraftQuestsAddons.getInstance(), 0, 10);
	}

	private static boolean isTrackingCompass(ItemStack item) {
		return item != null && item.getType() == Material.RECOVERY_COMPASS && item.hasItemMeta() &&
				"§6Traqueur de chèvres".equalsIgnoreCase(item.getItemMeta().getDisplayName());
	}

	private static Location findNearestNpc(Player player, Location playerLocation) {
		NPC nearestNpc = null;
		double minDistance = Double.MAX_VALUE;
		List<Integer> remainingGoats = new ArrayList<>();

		for (int i = 84 ; i < 94 ; i++)
		{
			if (!(HycraftQuestsAddons.getInstance().getGoatsCounter().get(player.getUniqueId()).contains(i)))
			{
				remainingGoats.add(i);
			}
		}

		for (int npcId : remainingGoats) {
			NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
			if (npc != null) {
				double distance = playerLocation.distance(npc.getEntity().getLocation());
				if (distance < minDistance) {
					minDistance = distance;
					nearestNpc = npc;
				}
			}
		}

		double distance2 = playerLocation.distance(new Location(Bukkit.getWorld("Prehistoire"), -435, 2, 302));
		if(!(HycraftQuestsAddons.getInstance().getGoatsCounter().get(player.getUniqueId()).contains(94)))
		{
			if(distance2 < minDistance){
				return new Location(Bukkit.getWorld("Prehistoire"), -435, 2, 302);
			}
		}

		if(nearestNpc == null) return null;
		return nearestNpc.getEntity().getLocation();
	}
}


