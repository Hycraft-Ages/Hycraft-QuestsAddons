package fr.justop.hycraftQuestsAddons.placeholders;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.quests.Quest;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HycraftQuestsPlaceholder extends PlaceholderExpansion {

    private final QuestsAPI questsAPI;

    public HycraftQuestsPlaceholder(QuestsAPI questsAPI) {
        this.questsAPI = questsAPI;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "hycraftquests";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TonPseudo";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null || !player.isOnline()) {
            return "";
        }

        if (identifier.startsWith("active_step_")) {
            String questIDStr = identifier.replace("active_step_", "");

            try {
                int questID = Integer.parseInt(questIDStr);
                return getQuestStep(player, questID);
            } catch (NumberFormatException e) {
                return "§cID de quête invalide.";
            }
        }

        return null;
    }

    private String getQuestStep(Player player, int questID) {
        PlayerAccount acc = questsAPI.getPlugin().getPlayersManager().getAccount(player);

        if (acc == null) {
            return "§7Statut: Ꙧ";
        }

        Quest quest = questsAPI.getQuestsManager().getQuest(questID);

        if (quest == null) {
            return "§cErreur (contactez un membre du staff).";
        }

        PlayerQuestDatas questData = acc.getQuestDatas(quest);

        if (questData == null || !questData.hasStarted()) {
            return "§7Statut: §fꙦ";
        }

        if (questData.isFinished()) {
            return "§7Statut: §fꙥ";
        }

        int stage = questData.getStage();

        if (quest.getBranchesManager().getPlayerBranch(acc) != null) {
            String desc = quest.getBranchesManager()
                    .getPlayerBranch(acc)
                    .getRegularStage(stage)
                    .getDescriptionLine(acc, DescriptionSource.MENU);

            if (desc != null && !desc.isEmpty()) {
                return "§7Statut: Ꙥ\n\n§a" + desc;
            } else {
                return "§eAucune description pour cette étape.";
            }
        }

        return "§cImpossible de récupérer l'étape actuelle.";
    }
}

