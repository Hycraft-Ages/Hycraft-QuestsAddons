package fr.justop.hycraftQuestsAddons.stages;

import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.types.AbstractItemStage;
import fr.skytasul.quests.api.utils.CountableObject;
import com.cryptomorin.xseries.XMaterial;
import net.momirealms.customfishing.api.event.FishingLootSpawnEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CustomFishingStage extends AbstractItemStage implements Listener {

    public CustomFishingStage(StageController controller, List<CountableObject<ItemStack>> fishes, ItemComparisonMap comparisons) {
        super(controller, fishes, comparisons);
    }

    public CustomFishingStage(StageController controller, ConfigurationSection section) {
        super(controller, section);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(FishingLootSpawnEvent e) {
        Player p = e.getPlayer();

        if (e.getLoot() instanceof Item item) {
            if (item.isDead() || !item.isValid()) return;

            ItemStack fish = item.getItemStack();
            event(p, fish, fish.getAmount());
        }
    }

    @Override
    public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
        return Lang.SCOREBOARD_FISH.toString();
    }

    public static CustomFishingStage deserialize(ConfigurationSection section, StageController controller) {
        return new CustomFishingStage(controller, section);
    }

    public static class Creator extends AbstractItemStage.Creator<CustomFishingStage> {

        private static final ItemStack editFishesItem = ItemUtils.item(XMaterial.FISHING_ROD, Lang.editFishes.toString());

        public Creator(@NotNull StageCreationContext<CustomFishingStage> context) {
            super(context);
        }

        @Override
        protected ItemStack getEditItem() {
            return editFishesItem;
        }

        @Override
        protected CustomFishingStage finishStage(StageController controller, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
            return new CustomFishingStage(controller, items, comparisons);
        }
    }
}