package fr.justop.hycraftQuestsAddons.commands;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import fr.justop.hycraftQuestsAddons.BossQuestUtils;
import fr.justop.hycraftQuestsAddons.HycraftQuestsAddons;
import fr.justop.hycraftQuestsAddons.listeners.FireListener;
import fr.justop.hycraftQuestsAddons.listeners.GoatsListener;
import fr.justop.hycraftQuestsAddons.utils.DisplayUtils;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.Quest;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class ConsoleCommand implements CommandExecutor {
    private final List<String> stage4Dialog = Arrays.asList("",
            "§6§lXandros §r§e: Je sens déjà son aura depuis ma position. C'est impressionant... Dès que je saurais en sa possession, je redeviendrai le mage que j'ai été...",
            "",
            "§6§lXandros §r§e: Voici ce que nous allons faire: tu ... et ensuite ... je .. ok?",
            "",
            "§6§lXandros §r§e: Je crois que... je...",
            "§6§lXandros §r§e: perds...",
            "§6§lXandros §r§e: le...",
            "§6§lXandros §r§e: signal...",
            "§6§lXandros §r§e: ...");

    private final int raceTimeLimit = 90;
    private final Location startLocation = new Location(Bukkit.getWorld("Prehistoire"), -103, -19, 444, -180f, 0f);
    private final Location spawnLocation = new Location(Bukkit.getWorld("Prehistoire"), -88, -23, 446, 90f, 0f);
    private final String finishRegion = "horse_region";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player) return false;
        if(command.getName().equalsIgnoreCase("registerPlayerGoats"))
        {
            Player player = Bukkit.getPlayer(args[0]);
            HycraftQuestsAddons.getInstance().getGoatsCounter().put(player.getUniqueId(), new ArrayList<>());
            return true;
        }
        if (command.getName().equalsIgnoreCase("endDiplo"))
        {
            Player player = Bukkit.getPlayer(args[0]);
            HycraftQuestsAddons.getInstance().getPhase2().remove(player.getUniqueId());
            return true;
        }

        if(command.getName().equals("startStage4"))
        {
            Player player = Bukkit.getPlayer(args[0]);
            addBlock();

            new BukkitRunnable()
            {
                int count = 0;
                @Override
                public void run()
                {
                    if(count == 2)
                    {
                        player.sendMessage(stage4Dialog.get(0));
                        player.sendMessage(stage4Dialog.get(1));
                        player.playSound(player.getLocation().add(0, 5, 0), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
                    } else if (count == 5) {
                        player.sendMessage(stage4Dialog.get(2));
                        player.sendMessage(stage4Dialog.get(3));
                        player.playSound(player.getLocation().add(0, 5, 0), Sound.ENTITY_VILLAGER_AMBIENT, 0.7f, 1.0f);
                    } else if (count == 7) {
                        player.sendMessage(stage4Dialog.get(4));
                        player.sendMessage(stage4Dialog.get(5));
                        player.playSound(player.getLocation().add(0, 5, 0), Sound.ENTITY_VILLAGER_AMBIENT, 0.4f, 0.8f);
                    } else if (count > 7) {
                        player.sendMessage(stage4Dialog.get(count - 2));
                        player.playSound(player.getLocation().add(0, 5, 0), Sound.ENTITY_VILLAGER_AMBIENT, 0.1f, 0.6f);
                    }
                    if(count == 11)
                    {
                        this.cancel();
                        removeBlock();
                        player.sendMessage("");
                        player.sendMessage(HycraftQuestsAddons.PREFIX + "§eIl semblerait que vous ayez perdu contact avec votre locuteur Xandros. Il vous faudra trouvez un autre moyen de vous extirper de cet endroit... Rappellez vous: si vous ne voulez pas attiser" +
                                " la colère du diplodocus vous devez absolument éviter de marcher sur les blocs autres que §6§lle béton bleu §r§eou bien le §6§lcorail§r§e!");
                        player.sendMessage("");
                        player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);

                        if (HycraftQuestsAddons.getInstance().getPhase1().containsKey(player.getUniqueId())) HycraftQuestsAddons.getInstance().getPhase1().remove(player.getUniqueId());
                        HycraftQuestsAddons.getInstance().getPhase2().put(player.getUniqueId(), "active");
                    }
                    count ++;
                }

            }.runTaskTimer(HycraftQuestsAddons.getInstance(), 0, 20);
            return true;
        }
        if(command.getName().equalsIgnoreCase("arena"))
        {
            Player player = Bukkit.getPlayer(args[0]);
            int mode = Integer.parseInt(args[1]);
            try {
                startArenaChallenge(player, mode);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        if(command.getName().equalsIgnoreCase("boss"))
        {
            Player player = Bukkit.getPlayer(args[0]);
            if (args[1].equalsIgnoreCase("1")) {
                try {
                    BossQuestUtils.startBossFight(player);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }
        if(command.getName().equalsIgnoreCase("sword"))
        {
            Player player = Bukkit.getPlayer(args[0]);

            ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
            ItemMeta im = sword.getItemMeta();
            im.setDisplayName("§6§lEpée de Jasper");
            im.setLore(Arrays.asList("§eRamenez l'arme à Jasper"));
            im.addEnchant(Enchantment.QUICK_CHARGE, 1, false);
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            sword.setItemMeta(im);

            player.getInventory().addItem(sword);
        }
        if(command.getName().equalsIgnoreCase("horse"))
        {
            Player player = Bukkit.getPlayer(args[0]);
            startRace(player);
        }
        if(command.getName().equalsIgnoreCase("giveCompassGoats"))
        {
            Player player = Bukkit.getPlayer(args[0]);
            ItemStack item = GoatsListener.createTrackingCompass();
            if (player.getInventory().firstEmpty() == -1)
            {
                QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
                Quester quester = questsAPI.getPlugin().getPlayersManager().getQuester(player);

                Quest quest = questsAPI.getQuestsManager().getQuest(114);
                quester.getDataHolder().getQuestData(quest).setStage(OptionalInt.of(0));
                HycraftQuestsAddons.getInstance().getGoatsCounter().remove(player.getUniqueId());

                player.sendMessage(HycraftQuestsAddons.PREFIX + "&cVotre inventaire est plein! Videz le et revenez parler à M.Sevin");
            }

            player.getInventory().addItem(item);
            return true;

        }
        if(command.getName().equalsIgnoreCase("rankuptitle"))
        {
            Player player = Bukkit.getPlayer(args[0]);
            int epoque = Integer.parseInt(args[1]);
            DisplayUtils.playRankUpgrade(player, HycraftQuestsAddons.getInstance(), epoque);
            return true;

        }


        return false;
    }

    private void removeBlock()
    {
        Location loc = new Location(Bukkit.getWorld("Prehistoire"), -37, 215, 207);
        Block block = Objects.requireNonNull(Bukkit.getWorld("Prehistoire")).getBlockAt(loc);
        Block block2 = Objects.requireNonNull(Bukkit.getWorld("Prehistoire")).getBlockAt(loc.add(0, 1, 0));
        block.setType(Material.AIR);
        block2.setType(Material.AIR);
    }

    private void addBlock()
    {
        Location loc = new Location(Bukkit.getWorld("Prehistoire"), -37, 215, 207);
        Block block = Objects.requireNonNull(Bukkit.getWorld("Prehistoire")).getBlockAt(loc);
        Block block2 = Objects.requireNonNull(Bukkit.getWorld("Prehistoire")).getBlockAt(loc.add(0, 1, 0));
        block.setType(Material.BARRIER);
        block2.setType(Material.BARRIER);

    }

    private void startArenaChallenge(Player player, int mode) throws IOException {
        HycraftQuestsAddons.saveInventory(player);
        player.getInventory().clear();

        ItemStack stoneSword = new ItemStack(Material.STONE_SWORD);
        ItemMeta meta = stoneSword.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            stoneSword.setItemMeta(meta);
        }
        player.getInventory().addItem(stoneSword);
        player.setHealth(20.0);
        player.getInventory().setItem(8, new ItemStack(Material.COOKED_BEEF, 10));
        player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));

        ItemStack ironHelmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack ironChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack ironLeggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack ironBoots = new ItemStack(Material.LEATHER_BOOTS);

        ironHelmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        ironChestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        ironLeggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        ironBoots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);

        player.getInventory().setArmorContents(new ItemStack[]{ironBoots, ironLeggings, ironChestplate, ironHelmet});

        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) continue;

            ItemMeta meta2 = item.getItemMeta();
            if (meta2 == null) continue;
            meta2.getPersistentDataContainer().set(HycraftQuestsAddons.getInstance().getKIT_ITEM_KEY(), PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta2);
        }

        int assignedIndex = BossQuestUtils.getAvailableIndex(mode);
        if (assignedIndex == -1) {
            player.sendMessage("§cAucune arène disponible.");
            HycraftQuestsAddons.getInstance().restoreInventory(player);
            return;
        }

        Location baseLoc = HycraftQuestsAddons.getInstance().getArenaLocations().get(assignedIndex);
        Location arenaLocation = baseLoc.clone();

        String worldName = (mode == 0) ? "Challenge" : (mode == 2 ? "araignees" : "BossFight1");
        arenaLocation.setWorld(Bukkit.getWorld(worldName));
        if (mode == 2) arenaLocation.subtract(0, 100, 0);

        UUID uuid = player.getUniqueId();
        if (mode == 0) {
            HycraftQuestsAddons.getInstance().getActivePlayers().put(uuid, assignedIndex);
        } else if (mode == 2) {
            HycraftQuestsAddons.getInstance().getShieldPlayers().put(uuid, assignedIndex);
        }
        HycraftQuestsAddons.getInstance().getRemainingMobs().put(player.getUniqueId(), 0);
        HycraftQuestsAddons.getInstance().getMobsKilled().put(player.getUniqueId(), 0);

        player.teleport(arenaLocation);

        BarColor color = mode == 0 ? BarColor.PURPLE : BarColor.BLUE;
        BossBar bossBar = Bukkit.createBossBar("§bProgression: 0/0", color, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
        HycraftQuestsAddons.getInstance().getBossBars().put(player.getUniqueId(), bossBar);

        player.setGameMode(GameMode.ADVENTURE);

        BukkitRunnable task = new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown > 0) {
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§aDébut dans §e" + countdown + "§a secondes...");
                    player.sendTitle("§e§l" + countdown, null);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
                    countdown--;
                } else {
                    player.teleport(arenaLocation);
                    player.sendTitle("§c§l" + "Combattez!", null);
                    BossQuestUtils.startMobWaves(player, arenaLocation, mode);
                    cancel();
                }
            }
        };
        HycraftQuestsAddons.getInstance().getActiveTasks().put(player.getUniqueId(), task);
        task.runTaskTimer(HycraftQuestsAddons.getInstance(), 0, 20);

    }

    public void startRace(Player player) {

        new BukkitRunnable()
        {

            @Override
            public void run() {
                for(ItemStack item : player.getInventory().getContents())
                {
                    if(item != null)
                    {
                        if(item.getItemMeta().hasCustomModelData()){
                            if(item.getItemMeta().getCustomModelData() == 5240)
                            {
                                player.teleport(startLocation);
                                Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
                                horse.setOwner(player);
                                horse.setCustomName("Nicolas");
                                horse.setInvulnerable(true);
                                horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                                horse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
                                horse.addPassenger(player);

                                startCountdown(player, horse);
                                return;
                            }
                        }
                    }


                }
                player.sendMessage(HycraftQuestsAddons.PREFIX + "§eAnnulation du défi: ton inventaire est plein! Libère ton inventaire et reviens voir Sacrofor.");
                QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
                Quester quester = questsAPI.getPlugin().getPlayersManager().getQuester(player);
                quester.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(132))).setStage(OptionalInt.of(0));
            }
        }.runTaskLater(HycraftQuestsAddons.getInstance(), 10);


    }

    private void startCountdown(Player player, Horse horse) {
        new BukkitRunnable() {
            int timeLeft = raceTimeLimit;

            @Override
            public void run() {
                if (timeLeft <= 0 || !player.isOnline() || !horse.isValid()) {
                    cancelRace(player, horse);
                    cancel();
                    return;
                }
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText("§eTemps restant: " + formatTime(timeLeft)));

                if (FireListener.isPlayerInRegion(player, finishRegion)) {
                    completeRace(player, horse);
                    cancel();
                    return;
                }

                timeLeft--;
            }
        }.runTaskTimer(HycraftQuestsAddons.getInstance(), 0, 20);
    }

    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    public void completeRace(Player player, Horse horse) {
        player.sendMessage(HycraftQuestsAddons.PREFIX + "\u00a7aFélicitations ! Vous êtes parvenu à temps au point de rendez-vous!");
        horse.remove();
    }

    public void cancelRace(Player player, Horse horse) {
        QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
        Quester quester = questsAPI.getPlugin().getPlayersManager().getQuester(player);

        quester.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(132))).setStage(OptionalInt.of(0));
        for(ItemStack im : player.getInventory().getContents()){
            if(im != null){
                if (im.hasItemMeta()){
                    if (im.getItemMeta().hasCustomModelData()){
                        if(im.getItemMeta().getCustomModelData() == 85481){
                            player.getInventory().remove(im);
                        }
                    }
                }
            }

        }

        player.sendMessage(HycraftQuestsAddons.PREFIX + "\u00a7cTemps écoulé ! Le pancréas s'est malheuresement déterioré...");
        horse.remove();
        player.teleport(spawnLocation);
    }

}