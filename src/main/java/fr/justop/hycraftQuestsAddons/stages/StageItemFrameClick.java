package fr.justop.hycraftQuestsAddons.stages;

import fr.justop.hycraftQuestsAddons.objects.BQLocation;
import fr.skytasul.quests.api.editors.WaitBlockClick;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.stages.types.Locatable;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Locatable.LocatableType(types = { Locatable.LocatedType.BLOCK, Locatable.LocatedType.OTHER })
public class StageItemFrameClick extends AbstractStage implements Locatable.PreciseLocatable, Listener {

    private final @NotNull BQLocation lc;

    private Locatable.Located.LocatedBlock locatedBlock;

    public StageItemFrameClick(@NotNull StageController controller, @NotNull BQLocation location) {
        super(controller);
        this.lc = new BQLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public @NotNull Location getLocation() {
        return lc;
    }

    @Override
    public Locatable.Located getLocated() {
        if (lc == null)
            return null;
        if (locatedBlock == null) {
            Block realBlock = lc.getBlock();
            if (realBlock != null)
                locatedBlock = Locatable.Located.LocatedBlock.create(realBlock);
        }
        return locatedBlock;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getType() != EntityType.ITEM_FRAME) return;

        ItemFrame itemFrame = (ItemFrame) e.getRightClicked();
        if (!lc.equals(BQLocation.of(getAttachedBlock(itemFrame).getLocation()))) return;

        Player p = e.getPlayer();

        if (hasApplicableQuester(p) && matchesRequirements(p)) {
            e.setCancelled(true);
            controller.getApplicableQuesters(p).forEach(this::finishStage);
        }
    }

    @Override
    public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
        return "Clic droit sur un Item Frame à une position donnée.";
    }

    @Override
    protected void serialize(ConfigurationSection section) {
        section.set("location", lc.serialize());
    }

    public static StageItemFrameClick deserialize(ConfigurationSection section, StageController controller) {
        return new StageItemFrameClick(controller,
                BQLocation.deserialize(section.getConfigurationSection("location").getValues(false)));
    }

    public Block getAttachedBlock(ItemFrame itemFrame) {
        BlockFace attachedFace = itemFrame.getAttachedFace();
        return itemFrame.getLocation().getBlock().getRelative(attachedFace);
    }


    public static class Creator extends StageCreation<StageItemFrameClick> {

        private BQLocation location;

        public Creator(@NotNull StageCreationContext<StageItemFrameClick> context) {
            super(context);
        }

        @Override
        public void setupLine(@NotNull StageGuiLine line) {
            super.setupLine(line);

            line.setItem(7, ItemUtils.item(XMaterial.COMPASS, Lang.blockLocation.toString()), event -> {
                Lang.CLICK_BLOCK.send(event.getPlayer());
                new WaitBlockClick(event.getPlayer(), event::reopen, obj -> {
                    setLocation(obj);
                    event.reopen();
                }, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).start();
            });
        }

        public void setLocation(@NotNull Location location) {
            this.location = BQLocation.of(Objects.requireNonNull(location));
            getLine().refreshItemLoreOptionValue(7, Lang.Location.format(this.location));
        }

        @Override
        public void start(Player p) {
            super.start(p);
            Lang.CLICK_BLOCK.send(p);
            new WaitBlockClick(p, context::removeAndReopenGui, obj -> {
                setLocation(obj);
                context.reopenGui();
            }, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).start();
        }

        @Override
        public void edit(StageItemFrameClick stage) {
            super.edit(stage);
            setLocation(stage.getLocation());
        }

        @Override
        public StageItemFrameClick finishStage(StageController controller) {
            return new StageItemFrameClick(controller, location);
        }
    }
}