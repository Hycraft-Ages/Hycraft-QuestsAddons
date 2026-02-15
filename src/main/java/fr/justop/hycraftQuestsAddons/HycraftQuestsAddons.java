package fr.justop.hycraftQuestsAddons;

import fr.justop.hycraftQuestsAddons.commands.ConsoleCommand;
import fr.justop.hycraftQuestsAddons.commands.PlayerCommand;
import fr.justop.hycraftQuestsAddons.listeners.*;
import fr.justop.hycraftQuestsAddons.placeholders.HycraftQuestsPlaceholder;
import fr.justop.hycraftQuestsAddons.stages.CustomFishingStage;
import fr.justop.hycraftQuestsAddons.stages.StageItemFrameClick;
import fr.justop.hycraftQuestsAddons.objects.CuboidRegion;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.PlayerSetStageEvent;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.api.stages.StageTypeRegistry;
import fr.skytasul.quests.api.utils.XMaterial;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static fr.skytasul.quests.api.gui.ItemUtils.item;

public final class HycraftQuestsAddons extends JavaPlugin {

    private static QuestsAPI questsAPI;
    public static String PREFIX = "ꕭ §r";
    public static HycraftQuestsAddons instance;

    private final List<Location> triggerLocations = new ArrayList<>();
    private final HashMap<String, CuboidRegion> regions = new HashMap<>();
    private final HashMap<UUID, Boolean> playerConstraints = new HashMap<>();
    private final HashMap<UUID, List<Integer>> goatsCounter = new HashMap<>();
    private final Map<UUID, String> phase1 = new HashMap<>();
    private final Map<UUID, String> phase2 = new HashMap<>();
    private final List<Material> allowedBlocks = Arrays.asList(
            Material.TUBE_CORAL_BLOCK,
            Material.BLUE_CONCRETE,
            Material.SLIME_BLOCK,
            Material.AIR,
            Material.WATER,
            Material.GREEN_CONCRETE
    );

    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final List<Location> arenaLocations = Arrays.asList(
            new Location(Bukkit.getWorld("BossFight1"), 0, 100, 0),
            new Location(Bukkit.getWorld("BossFight1"), 500, 100, 0),
            new Location(Bukkit.getWorld("BossFight1"), 1000, 100, 0),
            new Location(Bukkit.getWorld("BossFight1"), 0, 100, 500),
            new Location(Bukkit.getWorld("BossFight1"), 500, 100, 500),
            new Location(Bukkit.getWorld("BossFight1"), 1000, 100, 500),
            new Location(Bukkit.getWorld("BossFight1"), 0, 100, 1000),
            new Location(Bukkit.getWorld("BossFight1"), 500, 100, 1000),
            new Location(Bukkit.getWorld("BossFight1"), 1000, 100, 1000)
    );
    private final Map<UUID, Integer> activePlayers = new HashMap<>();
    private final Map<UUID, Integer> bossPlayers = new HashMap<>();
    private final Map<UUID, Integer> remainingMobs = new HashMap<>();
    private final Map<UUID, Integer> mobsKilled = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();
    private final Map<UUID, Integer> bossPhase = new HashMap<>();
    private final Map<UUID, ActiveMob> bosses = new HashMap<>();
    private final Map<UUID, Boolean> spiritPlayers = new HashMap<>();
    private final Map<UUID, List<Location>> puzzleProgress = new HashMap<>();
    private final Set<ActiveMob> frozenBosses = new HashSet<>();
    private final Map<UUID, Location> activeCristalPos = new HashMap<>();
    private final Map<UUID, BukkitRunnable> actionbarTasks = new HashMap<>();
    private final Map<UUID, Integer> shieldPlayers = new HashMap<>();
    private DBManager database;



    @Override
    public void onEnable() {

        instance = this;
        questsAPI = QuestsAPI.getAPI();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new HycraftQuestsPlaceholder(HycraftQuestsAddons.getQuestsAPI()).register();
            getLogger().info("HycraftQuestsPlaceholder enregistré avec succès !");
        } else {
            getLogger().warning("PlaceholderAPI non détecté ! Le placeholder ne fonctionnera pas.");
        }

        database = new DBManager(this);
        database.connect();
        database.createTable();

        initializeStages();

        regions.put("TeleportRegion", new CuboidRegion(
                new Location(Bukkit.getWorld("prehistoire"), 380, 83, 385),
                new Location(Bukkit.getWorld("prehistoire"), 399, -61, 402),
                new Location(Bukkit.getWorld("prehistoire"), 386.0D, 91.0D, 392.0D, -45.0F, 0.0F)
        ));

        regions.put("ResetRegion", new CuboidRegion(
                new Location(Bukkit.getWorld("prehistoire"), 390.0D, 89.0D, 396.0D),
                new Location(Bukkit.getWorld("prehistoire"), 388.0D, 88.0D, 394.0D),
                null
        ));

        regions.put("DiploOpenRegion", new CuboidRegion(
                new Location(Bukkit.getWorld("prehistoire"), -50, 219, 208),
                new Location(Bukkit.getWorld("prehistoire"), -50, 217, 206),
                null
        ));

        GoatsListener.startTrackingTask();

        this.triggerLocations.add(new Location(getServer().getWorld("prehistoire"), 390.0D, 89.0D, 396.0D));
        this.triggerLocations.add(new Location(getServer().getWorld("prehistoire"), 390.0D, 89.0D, 395.0D));
        this.triggerLocations.add(new Location(getServer().getWorld("prehistoire"), 390.0D, 89.0D, 394.0D));
        this.triggerLocations.add(new Location(getServer().getWorld("prehistoire"), 389.0D, 89.0D, 396.0D));
        this.triggerLocations.add(new Location(getServer().getWorld("prehistoire"), 389.0D, 89.0D, 395.0D));
        this.triggerLocations.add(new Location(getServer().getWorld("prehistoire"), 389.0D, 89.0D, 394.0D));
        this.triggerLocations.add(new Location(getServer().getWorld("prehistoire"), 388.0D, 89.0D, 396.0D));
        this.triggerLocations.add(new Location(getServer().getWorld("prehistoire"), 388.0D, 89.0D, 395.0D));
        this.triggerLocations.add(new Location(getServer().getWorld("prehistoire"), 388.0D, 89.0D, 394.0D));

        onListeners();
        onCommands();

        Bukkit.getConsoleSender().sendMessage("Plugin initialisé");

    }

    @Override
    public void onDisable() {
        database.close();
    }

    private void onListeners()
    {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new DropperListener(), this);
        pm.registerEvents(new DiploListener(), this);
        pm.registerEvents(new GoatsListener(), this);
        pm.registerEvents(new ComposterLaunch(), this);
        pm.registerEvents(new ArenaListener(), this);
        pm.registerEvents(new ArrowListener(), this);
        pm.registerEvents(new FireListener(), this);
        pm.registerEvents(new FishingListener(), this);
        pm.registerEvents(new GeneralListener(), this);
    }

    private void onCommands()
    {
        this.getCommand("registerPlayerGoats").setExecutor(new ConsoleCommand());
        this.getCommand("startStage4").setExecutor(new ConsoleCommand());
        this.getCommand("endDiplo").setExecutor(new ConsoleCommand());
        this.getCommand("q").setExecutor(new PlayerCommand());
        this.getCommand("arena").setExecutor(new ConsoleCommand());
        this.getCommand("sword").setExecutor(new ConsoleCommand());
        this.getCommand("horse").setExecutor(new ConsoleCommand());
        this.getCommand("giveCompassGoats").setExecutor(new ConsoleCommand());
    }

    private void initializeStages()
    {
        StageTypeRegistry stages = QuestsAPI.getAPI().getStages();
        stages.register(new StageType<>("INTERACT_ITEM_FRAME", StageItemFrameClick.class,"§aItemFrame clic",
                StageItemFrameClick::deserialize, item(XMaterial.ITEM_FRAME, "§eItemFrame clic"),
                StageItemFrameClick.Creator::new));
        stages.register(new StageType<>("CUSTOM_FISH", CustomFishingStage.class,"§aPêche custom",
                CustomFishingStage::deserialize, item(XMaterial.ITEM_FRAME, "§ePêche custom"),
                CustomFishingStage.Creator::new));
    }

    public void boxPlayer(Player player)
    {
        player.setGameMode(GameMode.ADVENTURE);
        Bukkit.getScheduler().runTask(HycraftQuestsAddons.getInstance(), () -> {
            Location playerLoc = player.getLocation().add(0, 1, 0);
            World world = player.getWorld();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    world.getBlockAt(playerLoc.clone().add(x, 0, z)).setType(Material.BARRIER);
                    world.getBlockAt(playerLoc.clone().add(0, 1, 0)).setType(Material.BARRIER);
                }
            }
        });
    }

    public void unboxPlayer(Player player)
    {
        Bukkit.getScheduler().runTask(HycraftQuestsAddons.getInstance(), () -> {
            Location playerLoc = player.getLocation().add(0, 1, 0);
            World world1 = player.getWorld();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    Block barrierBlock = world1.getBlockAt(playerLoc.clone().add(x, 0, z));
                    if (barrierBlock.getType() == Material.BARRIER) {
                        barrierBlock.setType(Material.AIR);
                        world1.getBlockAt(playerLoc.clone().add(0, 1, 0)).setType(Material.AIR);
                    }
                }
            }
        });
    }
    public static void addPermission(User user, String permission) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        user.data().add(Node.builder(permission).build());
        luckPerms.getUserManager().saveUser(user);
    }

    public void restoreInventory(Player player) {
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try (PreparedStatement ps = database.getConnection().prepareStatement(
                    "SELECT * FROM player_inventory WHERE uuid = ?"
            )) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        getLogger().warning("Aucune sauvegarde trouvée pour " + player.getName());
                        return;
                    }

                    byte[] invData = rs.getBytes("inventory");
                    byte[] armorData = rs.getBytes("armor");
                    byte[] offhandData = rs.getBytes("offhand");

                    ItemStack[] inventory = ItemSerializer.deserialize(invData);
                    ItemStack[] armor = ItemSerializer.deserialize(armorData);
                    ItemStack[] offhand = ItemSerializer.deserialize(offhandData);

                    float xp = rs.getFloat("xp");
                    int level = rs.getInt("level");
                    double health = rs.getDouble("health");
                    int food = rs.getInt("food");

                    Bukkit.getScheduler().runTask(this, () -> {
                        try {
                            player.getInventory().setContents(inventory);
                            player.getInventory().setArmorContents(armor);
                            player.getInventory().setItemInOffHand(offhand[0]);

                            player.setExp(xp);
                            player.setLevel(level);
                            player.setHealth(health);
                            player.setFoodLevel(food);

                            removeInventoryBackup(uuid);
                        } catch (Exception e) {
                            getLogger().severe("Erreur lors de la restauration de l'inventaire pour " + player.getName());
                            e.printStackTrace();
                        }
                    });

                }
            } catch (Exception e) {
                getLogger().severe("Impossible de restaurer l'inventaire de " + player.getName());
                e.printStackTrace();
            }
        });
    }

    private void removeInventoryBackup(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try (PreparedStatement ps = database.getConnection().prepareStatement(
                    "DELETE FROM player_inventory WHERE uuid = ?"
            )) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                getLogger().severe("Impossible de supprimer l'inventaire sauvegardé pour " + uuid);
                e.printStackTrace();
            }
        });
    }

    public static void saveInventory(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> {
            try (PreparedStatement ps = getInstance().getDatabase().getConnection().prepareStatement(
                    "REPLACE INTO player_inventory VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            )) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setBytes(2, ItemSerializer.serialize(player.getInventory().getContents()));
                ps.setBytes(3, ItemSerializer.serialize(player.getInventory().getArmorContents()));
                ps.setBytes(4, ItemSerializer.serialize(
                        new ItemStack[]{player.getInventory().getItemInOffHand()}
                ));
                ps.setFloat(5, player.getExp());
                ps.setInt(6, player.getLevel());
                ps.setDouble(7, player.getHealth());
                ps.setInt(8, player.getFoodLevel());
                ps.setLong(9, System.currentTimeMillis());

                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        player.getInventory().clear();
    }

    public static void removeNearbyEntities(Player player) {
        World world = player.getWorld();
        world.getEntities().stream()
                .filter(entity -> entity.getLocation().distance(player.getLocation()) <= 100 && !(entity instanceof Player) &&
                        (isSpecificMythicMob(entity,"Plante_mutante") ||
                        isSpecificMythicMob(entity,"Mutant_Vine_SUMMON") ||
                        isSpecificMythicMob(entity,"Mutant_Vine") ||
                        isSpecificMythicMob(entity, "Stinger") ||
                        isSpecificMythicMob(entity,"Golem_de_pierre") ||
                        isSpecificMythicMob(entity, "Salamandre") ||
                        isSpecificMythicMob(entity, "VanillaSpider")))
                .forEach(Entity::remove);
    }

    public static boolean isSpecificMythicMob(Entity entity, String mobId) {
        ActiveMob mob = MythicBukkit.inst().getMobManager().getMythicMobInstance(entity);
        return mob != null && mob.getType().getInternalName().equalsIgnoreCase(mobId);
    }



    public List<Location> getTriggerLocations() {
        return this.triggerLocations;
    }

    public static QuestsAPI getQuestsAPI() {
        return questsAPI;
    }

    public static  HycraftQuestsAddons getInstance(){
        return instance;
    }

    public HashMap<String, CuboidRegion> getRegions()
    {
        return regions;
    }


    public HashMap<UUID, Boolean> getPlayerConstraints() {
        return playerConstraints;
    }

    public HashMap<UUID, List<Integer>> getGoatsCounter() {
        return goatsCounter;
    }

    public Map<UUID, String> getPhase2() {
        return phase2;
    }

    public Map<UUID, String> getPhase1() {
        return phase1;
    }

    public List<Material> getAllowedBlocks() {
        return allowedBlocks;
    }
    public Map<UUID, ItemStack[]> getSavedInventories() {
        return savedInventories;
    }

    public Map<UUID, Integer> getActivePlayers() {
        return activePlayers;
    }

    public Map<UUID, Integer> getRemainingMobs() {
        return remainingMobs;
    }


    public Map<UUID, Integer> getMobsKilled() {
        return mobsKilled;
    }

    public Map<UUID, BossBar> getBossBars() {
        return bossBars;
    }

    public List<Location> getArenaLocations() {
        return arenaLocations;
    }

    public Map<UUID, BukkitRunnable> getActiveTasks() {
        return activeTasks;
    }

    public Map<UUID, Integer> getBossPlayers() {
        return bossPlayers;
    }

    public Map<UUID, Integer> getBossPhase() {
        return bossPhase;
    }

    public Map<UUID, ActiveMob> getBosses() {
        return bosses;
    }

    public Map<UUID, Boolean> getSpiritPlayers() {
        return spiritPlayers;
    }

    public Map<UUID, List<Location>> getPuzzleProgress() {
        return puzzleProgress;
    }

    public Set<ActiveMob> getFrozenBosses() {
        return frozenBosses;
    }

    public Map<UUID, Location> getActiveCristalPos() {
        return activeCristalPos;
    }

    public Map<UUID, BukkitRunnable> getActionbarTasks() {
        return actionbarTasks;
    }

    public Map<UUID, Integer> getShieldPlayers() {return shieldPlayers;}

    public DBManager getDatabase() {return database;}
}
