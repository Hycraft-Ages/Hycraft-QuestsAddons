package fr.justop.hycraftQuestsAddons.commands;

import fr.justop.hycraftQuestsAddons.BossQuestUtils;
import fr.justop.hycraftQuestsAddons.HycraftQuestsAddons;
import fr.justop.hycraftQuestsAddons.listeners.ComposterLaunch;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.data.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.stages.StageController;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

public class PlayerCommand implements CommandExecutor, TabCompleter {
	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
		if(!(commandSender instanceof Player player)) return false;
		HycraftQuestsAddons instance = HycraftQuestsAddons.getInstance();
		QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
		Quester acc = questsAPI.getPlugin().getPlayersManager().getQuester(player);

		if (acc == null) {
			player.sendMessage(HycraftQuestsAddons.PREFIX + "§cAucun compte de quête trouvé. (contactez un membre du staff)");
			return false;
		}

		switch(strings[0]){
			case "interrupt":
				if (instance.getPhase1().containsKey(player.getUniqueId()))
				{
					instance.getPhase1().put(player.getUniqueId(), "inactive");
					player.setGameMode(GameMode.SURVIVAL);
					player.teleport(new Location(Bukkit.getWorld("Prehistoire"), -270, 13, 200, -90f, -15));
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§eVous avez interrompu la quête §b§lUne descente furtive§r§e. Utilisez §6/q rejoin §e à tout moment pour reprendre la où vous en étiez");

				}else if(instance.getPhase2().containsKey(player.getUniqueId())) {

					instance.getPhase2().put(player.getUniqueId(), "inactive");
					player.setGameMode(GameMode.SURVIVAL);
					player.teleport(new Location(Bukkit.getWorld("Prehistoire"), -270, 13, 200, -90f, -15));
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§eVous avez interrompu la quête §b§lUne descente furtive§r§e. Utilisez §6/q rejoin §e à tout moment pour reprendre la où vous en étiez");
				}
				break;

			case "rejoin":
				if (instance.getPhase1().containsKey(player.getUniqueId()))
				{

					acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(115))).setStage(OptionalInt.of(1));
					instance.getPhase1().put(player.getUniqueId(), "active");
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§eVous avez rejoint la quête avec succès la où vous en étiez.");
					player.teleport(ComposterLaunch.TARGET_LOCATION);
					player.setGameMode(GameMode.ADVENTURE);
					ComposterLaunch.slowFall(player);

				}else if(instance.getPhase2().containsKey(player.getUniqueId()))
				{
					instance.getPhase2().put(player.getUniqueId(), "active");
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§eVous avez rejoint la quête avec succès la où vous en étiez. Xandros ayant perdu le contact avec vous, vous devez trouver un moyen par vous même pour échapper au diplodocus.");
					player.teleport(new Location(Bukkit.getWorld("Prehistoire"), -35, 215, 207, 90f, 0f));
					player.setGameMode(GameMode.ADVENTURE);
				}
				break;

            case "info":
                boolean hasActiveQuest = false;

                for (Quest quest : questsAPI.getQuestsManager().getQuests()) {
                    QuesterQuestData questData = acc.getDataHolder().getQuestData(quest);

                    if (questData.hasStarted() && !questData.hasFinishedOnce()) {
                        QuestBranch playerBranch = quest.getBranchesManager().getPlayerBranch(acc);

                        if (playerBranch != null) {
                            OptionalInt stageIndex = questData.getStage();

                            if (stageIndex.isPresent()) {
                                int index = stageIndex.getAsInt();
                                if (index >= 0 && index < playerBranch.getRegularStages().size()) {
                                    StageController currentStage = playerBranch.getRegularStage(index);

                                    hasActiveQuest = true;
                                    player.sendMessage("§6§l➤ Quête en cours : §e" + quest.getName());
                                    player.sendMessage("§7-------------------------------");

                                    String desc = currentStage.getDescriptionLine(acc, DescriptionSource.FORCELINE);
                                    if (desc != null && !desc.isEmpty()) {
                                        for (String line : desc.split("\n")) {
                                            player.sendMessage("§f▪ " + line);
                                        }
                                    } else {
                                        player.sendMessage("§7§o(Aucune description disponible)");
                                    }
                                    player.sendMessage("§7-------------------------------");
                                }
                            }
                        }
                    }
                }

                if (!hasActiveQuest) {
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§eVous n'avez aucune quête en cours.");
                }
                break;

			case "enigme":

				if(acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(125))).getStage().orElse(-1) == 1)
				{
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aLors du temps d’angoisse, le brave guerrier brandira l’arc céleste et frappera en leur cœur les gardien du sanctuaire sacré du plus faible au plus puissant. On raconte que la taille de leur insigne déterminait leur puissance... ");
					player.sendMessage("§aUtilisez §b/q indice §asi vous avez du mal à comprendre la signification de ces mots.");
				}
				else if(acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(104))).getStage().orElse(-1) == 1)
				{
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§aLà où la vie est sortie du sol, un serpent naît. Encore trop faible pour ramper, il se soumet à la gravité. Il chute alors, puis il zigzague, avant de s’écraser et de rester inerte, flasque et paisible pour l’éternité. On raconte qu’au moment même où il abandonna tout espoir, là où sa chute commença, il laissa derrière lui ce qu’il avait de plus précieux, ce que les humains admirent et convoitent. ");
					player.sendMessage("§aUtilisez §b/q indice §asi vous avez du mal à comprendre la signification de ces mots.");
				}
				else{
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§cVous n'avez aucune énigme à résoudre!");
				}
				break;

			case "indice":
				if(acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(125))).getStage().orElse(-1) == 1)
				{
					player.sendMessage("§7-------------------------------");

					player.sendMessage("§f▪ §aChaque gardien du sanctuaire détient une insigne.");
					player.sendMessage("§f▪ §aLeur insigne se trouve à leur pieds");
					player.sendMessage("§f▪ §aVous devrez compter un nombre de §eShroom lights §apour déterminer la taille d'une insigne");
					player.sendMessage("§f▪ §aL'arc célèste n'est pas un arc comme les autres...");

					player.sendMessage("§7-------------------------------");
				}
				else if(acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(104))).getStage().orElse(-1) == 1)
				{
					player.sendMessage("§7-------------------------------");

					player.sendMessage("§f▪ §aLe serpent est une métaphore d'un cours d'eau");
					player.sendMessage("§f▪ §aIl se pourrait que vous trouviez quelque chose à la source de ce cours d'eau");

					player.sendMessage("§7-------------------------------");
				}
				else{
					player.sendMessage(HycraftQuestsAddons.PREFIX + "§cVous n'avez aucune énigme à résoudre!");
				}
				break;

			case "retry":
				if(acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(125))).hasStarted() && !acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(125))).hasFinishedOnce())
				{
                    player.sendMessage(String.valueOf(acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(125))).getStage().orElse(-1)));
					if(HycraftQuestsAddons.getInstance().getBossPlayers().containsKey(player.getUniqueId())){
						player.sendMessage(HycraftQuestsAddons.PREFIX + "§cVous êtes déjà en combat!");
						return true;
					}

                    try {
                        BossQuestUtils.startBossFight(player);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }else{
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§cVous n'avez aucun combat en suspend...");
                }
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		ArrayList<String> result = new ArrayList<>();
		String[] VALUES = {"interrupt", "rejoin", "enigme", "indice", "retry", "info"};

		if (command.getName().equalsIgnoreCase("q")) {
			switch (args.length) {
				case 1:
					for (String value : VALUES) {
						if (value.toLowerCase().startsWith(args[0].toLowerCase())) result.add(value);
					}
					break;

			}

		}
		return result;
	}
}
