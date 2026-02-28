package fr.justop.hycraftQuestsAddons.stages;

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
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Locatable.LocatableType(types = { Locatable.LocatedType.BLOCK, Locatable.LocatedType.OTHER })
public class StageItemFrameClick extends AbstractStage implements Locatable.PreciseLocatable, Listener {

    private final String worldName;
    private final int x, y, z;

    private Locatable.Located.LocatedBlock locatedBlock;

    public StageItemFrameClick(@NotNull StageController controller, String worldName, int x, int y, int z) {
        super(controller);
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public @NotNull Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        return new Location(world, x, y, z);
    }

    @Override
    public Locatable.Located getLocated() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null; // Sécurité si le monde n'est pas encore chargé

        if (locatedBlock == null) {
            Block realBlock = world.getBlockAt(x, y, z);
            locatedBlock = Locatable.Located.LocatedBlock.create(realBlock);
        }
        return locatedBlock;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getType() != EntityType.ITEM_FRAME) return;

        ItemFrame itemFrame = (ItemFrame) e.getRightClicked();
        Location attachedLoc = getAttachedBlock(itemFrame).getLocation();

        if (attachedLoc.getWorld() == null || !attachedLoc.getWorld().getName().equals(this.worldName) ||
                attachedLoc.getBlockX() != this.x ||
                attachedLoc.getBlockY() != this.y ||
                attachedLoc.getBlockZ() != this.z) {
            return;
        }

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
        section.set("world", worldName);
        section.set("x", x);
        section.set("y", y);
        section.set("z", z);
    }

    public static StageItemFrameClick deserialize(ConfigurationSection section, StageController controller) {
        if (section.contains("location")) {
            ConfigurationSection locSec = section.getConfigurationSection("location");
            if (locSec != null && locSec.contains("world")) {
                return new StageItemFrameClick(controller, locSec.getString("world"), locSec.getInt("x"), locSec.getInt("y"), locSec.getInt("z"));
            }
        }

        return new StageItemFrameClick(
                controller,
                section.getString("world"),
                section.getInt("x"),
                section.getInt("y"),
                section.getInt("z")
        );
    }

    public Block getAttachedBlock(ItemFrame itemFrame) {
        BlockFace attachedFace = itemFrame.getAttachedFace();
        return itemFrame.getLocation().getBlock().getRelative(attachedFace);
    }

    public static class Creator extends StageCreation<StageItemFrameClick> {

        private Location location;

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
            this.location = location;

            String worldName = location.getWorld() != null ? location.getWorld().getName() : "Monde inconnu";
            String formattedText = worldName + " (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";

            getLine().refreshItemLoreOptionValue(7, new HasPlaceholders() {
                @Override
                public @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
                    PlaceholderRegistry registry = new PlaceholderRegistry();
                    registry.register("location", () -> formattedText);
                    return registry;
                }
            });
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
            return new StageItemFrameClick(controller, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }
    }
}