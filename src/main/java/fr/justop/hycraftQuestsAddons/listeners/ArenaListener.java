package fr.justop.hycraftQuestsAddons.listeners;

import fr.justop.hycraftQuestsAddons.BossQuestUtils;
import fr.justop.hycraftQuestsAddons.HycraftQuestsAddons;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.players.PlayerAccount;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.mechanics.FireworkEffect;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ArenaListener implements Listener
{

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(MythicMobDeathEvent event) {
        ActiveMob mob =  MythicBukkit.inst().getMobManager().getActiveMob(event.getEntity().getUniqueId()).orElse(null);
        if (event.getKiller() != null) {
            Player player = (Player) event.getKiller();
            if (HycraftQuestsAddons.getInstance().getActivePlayers().containsKey(player.getUniqueId()) || HycraftQuestsAddons.getInstance().getShieldPlayers().containsKey(player.getUniqueId())) {
                int mode = HycraftQuestsAddons.getInstance().getActivePlayers().containsKey(player.getUniqueId()) ? 0 : 2;
                event.getDrops().clear();
                player.getInventory().setItem(1, null);
                player.getInventory().setItem(2, null);
                int remaining = HycraftQuestsAddons.getInstance().getRemainingMobs().get(player.getUniqueId()) - 1;
                HycraftQuestsAddons.getInstance().getRemainingMobs().put(player.getUniqueId(), remaining);
                HycraftQuestsAddons.getInstance().getMobsKilled().put(player.getUniqueId(), HycraftQuestsAddons.getInstance().getMobsKilled().getOrDefault(player.getUniqueId(), 0) + 1);
                updateBossBar(player, mode);

                if (HycraftQuestsAddons.getInstance().getRemainingMobs().get(player.getUniqueId()) <= 0)
                {
                    if (HycraftQuestsAddons.getInstance().getShieldPlayers().containsKey(player.getUniqueId())){
                        if (HycraftQuestsAddons.getInstance().getMobsKilled().get(player.getUniqueId()) < 20) return;
                    }
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§aVous êtes parvenu à vaincre toutes les vagues! Téléportation dans 5 secondes...");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    launchFireworks(player);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            endArenaChallenge(player);
                        }
                    }.runTaskLater(HycraftQuestsAddons.getInstance(), 100);
                }
            }else if(HycraftQuestsAddons.getInstance().getBossPlayers().containsKey(player.getUniqueId()))
            {
                event.getDrops().clear();
                player.getInventory().setItem(3, null);
                player.getInventory().setItem(2, null);

                if(mob == null) return;

                if (mob.getMobType().equalsIgnoreCase("Mutant_flower")) {
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§aVous êtes parvenu à vaincre le boss! Téléportation dans quelques secondes...");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    launchFireworks(player);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, PotionEffect.INFINITE_DURATION, 2));
                            new BukkitRunnable()
                            {

                                @Override
                                public void run() {
                                    BossQuestUtils.endBossChallenge(player);
                                }

                            }.runTaskLater(HycraftQuestsAddons.getInstance(),60);
                        }
                    }.runTaskLater(HycraftQuestsAddons.getInstance(), 100);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if ((HycraftQuestsAddons.getInstance().getActivePlayers().containsKey(player.getUniqueId()) && (player.getHealth() - event.getFinalDamage()) <= 0) || (HycraftQuestsAddons.getInstance().getShieldPlayers().containsKey(player.getUniqueId()) && (player.getHealth() - event.getFinalDamage()) <= 0)) {
                event.setCancelled(true);
                player.setHealth(20.0);
                cancelArenaChallenge(player);
            }
            if (HycraftQuestsAddons.getInstance().getBossPlayers().containsKey(player.getUniqueId()) && (player.getHealth() - event.getFinalDamage()) <= 0)
            {
                event.setCancelled(true);
                player.setHealth(20.0);
                BossQuestUtils.cancelBossChallenge(player);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();

        if (entity instanceof LivingEntity && entity.getCustomName() != null && entity.getCustomName().equals("Mutant Flower")) {

            if (damager instanceof Arrow) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if(HycraftQuestsAddons.getInstance().getActivePlayers().containsKey(player.getUniqueId()) || HycraftQuestsAddons.getInstance().getShieldPlayers().containsKey(player.getUniqueId()))
        {
            player.setHealth(20.0);
            cancelArenaChallenge(player);
        }
        if (HycraftQuestsAddons.getInstance().getBossPlayers().containsKey(player.getUniqueId()))
        {
            player.setHealth(20.0);
            BossQuestUtils.cancelBossChallenge(player);
        }
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event)
    {
        Action action = event.getAction();
        Player player = event.getPlayer();
        if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.LECTERN && action.equals(Action.RIGHT_CLICK_BLOCK))
        {
            if(!(event.getClickedBlock().getLocation().equals(new Location(Bukkit.getWorld("Prehistoire"),-25,-21, -328)))) return;
            event.setCancelled(true);
            QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
            PlayerAccount acc = questsAPI.getPlugin().getPlayersManager().getAccount(player);

            if (!(acc.getQuestDatas(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(124))).getStage() == 6)) return;

            ItemStack book = new ItemStack(Material.BOOK);
            ItemMeta meta = book.getItemMeta();
            meta.setDisplayName("§6§lVieux grimoire");
            meta.setLore(Arrays.asList("§e§oVous ne parvenez pas à déchiffrer", "§e§ole contenu du livre..."));
            meta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            book.setItemMeta(meta);

            if(player.getInventory().contains(book)) return;

            player.getInventory().addItem(book);
            player.sendMessage(HycraftQuestsAddons.PREFIX + "§aVous obtenez un §b§lVieux grimoire §aque personne ne semble avoir touché depuis plusieurs siècles. Melheuresement, les pages sont couvertes de symboles indescriptibles... Faites le examiner à Erin.");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (HycraftQuestsAddons.getInstance().getBosses().containsKey(player.getUniqueId())) {
                ActiveMob boss = HycraftQuestsAddons.getInstance().getBosses().get(player.getUniqueId());
                ActiveMob entity = MythicBukkit.inst().getMobManager().getMythicMobInstance(event.getEntity());

                if(entity == null) return;
                if(!(entity.equals(boss))) return;

                double healthPercentage = boss.getEntity().getHealth() / boss.getEntity().getMaxHealth();

                if(healthPercentage <= 0.1 && HycraftQuestsAddons.getInstance().getBossPhase().get(player.getUniqueId()) == 2)
                {
                    HycraftQuestsAddons.getInstance().getBossPhase().put(player.getUniqueId(), 3);
                    HycraftQuestsAddons.getInstance().getSpiritPlayers().put(player.getUniqueId(),true);
                    BossQuestUtils.invokeSpirit(player);
                    BossQuestUtils.startDisplayingActionBar(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f,1.0f);
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§eLe boss invoque les esprits du sanctuaire pour lui octroyer l'invulnérabilité! Neutralisez les en fichant une flèche bien placée en leur §6§ljoyau §e(Shroom lights)");
                }

                if (healthPercentage <= 0.5 && HycraftQuestsAddons.getInstance().getBossPhase().get(player.getUniqueId()) == 1) {
                    HycraftQuestsAddons.getInstance().getBossPhase().put(player.getUniqueId(), 2);
                    HycraftQuestsAddons.getInstance().getSpiritPlayers().put(player.getUniqueId(),true);
                    BossQuestUtils.invokeSpirit(player);
                    BossQuestUtils.startDisplayingActionBar(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f,1.0f);
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§eLe boss invoque les esprits du sanctuaire pour lui octroyer l'invulnérabilité! Neutralisez les en fichant une flèche bien placée en leur joyau.");
                }

                if (HycraftQuestsAddons.getInstance().getSpiritPlayers().containsKey(player.getUniqueId()))
                {
                    event.setCancelled(true);
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§cLe boss est temporairement invulnérable! Détruisez les esprits qui protègent le boss.");
                }
            }
        }
    }

    private void cancelArenaChallenge(Player player)
    {
        int mode = 0;
        if (HycraftQuestsAddons.getInstance().getShieldPlayers().containsKey(player.getUniqueId())){
            mode = 2;
        }

        HycraftQuestsAddons.getInstance().getRemainingMobs().remove(player.getUniqueId());
        HycraftQuestsAddons.getInstance().getMobsKilled().remove(player.getUniqueId());
        if (HycraftQuestsAddons.getInstance().getBossBars().containsKey(player.getUniqueId())) {
            HycraftQuestsAddons.getInstance().getBossBars().get(player.getUniqueId()).removeAll();
            HycraftQuestsAddons.getInstance().getBossBars().remove(player.getUniqueId());
        }
        HycraftQuestsAddons.getInstance().restoreInventory(player);
        HycraftQuestsAddons.removeNearbyEntities(player);

        if (HycraftQuestsAddons.getInstance().getActiveTasks().containsKey(player.getUniqueId())) {
            HycraftQuestsAddons.getInstance().getActiveTasks().get(player.getUniqueId()).cancel();
            HycraftQuestsAddons.getInstance().getActiveTasks().remove(player.getUniqueId());
        }

        Location loc = mode == 0 ? new Location(Bukkit.getWorld("Prehistoire"), -44.5 , -18, -293.5, 180.0f, 0.0f) : new Location(Bukkit.getWorld("Prehistoire"), -51.5, -1, -514.5, -45f, 0f);
        player.teleport(loc);
        player.setHealth(20.0);
        HycraftQuestsAddons.getInstance().getActivePlayers().remove(player.getUniqueId());
        HycraftQuestsAddons.getInstance().getShieldPlayers().remove(player.getUniqueId());

        QuestsAPI questsAPI = HycraftQuestsAddons.getQuestsAPI();
        PlayerAccount acc = questsAPI.getPlugin().getPlayersManager().getAccount(player);

        if(mode == 0){
            acc.getQuestDatas(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(118))).setStage(7);
        }else {
            acc.getQuestDatas(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(138))).setStage(1);
        }

        player.sendMessage(HycraftQuestsAddons.PREFIX + "§cTu as échoué! Tache d'être plus agile au prochain essai!");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);

    }


    private void endArenaChallenge(Player player) {
        int mode = 0;
        if (HycraftQuestsAddons.getInstance().getShieldPlayers().containsKey(player.getUniqueId())){
            mode = 2;
        }
        HycraftQuestsAddons.getInstance().restoreInventory(player);
        Location loc = mode == 0 ? new Location(Bukkit.getWorld("Prehistoire"), -44.5 , -18, -293.5, 180.0f, 0.0f) : new Location(Bukkit.getWorld("Prehistoire"), -51.5, -1, -514.5, -45f, 0f);
        player.teleport(loc);
        player.setHealth(20.0);
        HycraftQuestsAddons.getInstance().getActivePlayers().remove(player.getUniqueId());
        HycraftQuestsAddons.getInstance().getShieldPlayers().remove(player.getUniqueId());
        UUID playerId = player.getUniqueId();
        if (HycraftQuestsAddons.getInstance().getBossBars().containsKey(playerId)) {
            HycraftQuestsAddons.getInstance().getBossBars().get(playerId).removeAll();
            HycraftQuestsAddons.getInstance().getBossBars().remove(playerId);
        }

        if (HycraftQuestsAddons.getInstance().getActiveTasks().containsKey(player.getUniqueId())) {
            HycraftQuestsAddons.getInstance().getActiveTasks().get(player.getUniqueId()).cancel();
            HycraftQuestsAddons.getInstance().getActiveTasks().remove(player.getUniqueId());
        }
        String msg = mode == 0 ? HycraftQuestsAddons.PREFIX + "§aParlez à Donovan" : HycraftQuestsAddons.PREFIX + "§aRetournez voir Erin";
        player.sendMessage(msg);
        player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);
    }

    private void updateBossBar(Player player, int mode) {
        UUID playerId = player.getUniqueId();
        BossBar bossBar = HycraftQuestsAddons.getInstance().getBossBars().get(playerId);
        if (bossBar != null) {
            int killed = HycraftQuestsAddons.getInstance().getMobsKilled().getOrDefault(playerId, 0);
            int total = HycraftQuestsAddons.getInstance().getRemainingMobs().getOrDefault(playerId, 0) + killed;
            String c = mode == 0 ? "§e" : "§b";
            bossBar.setTitle(c + "Progression: " + killed + "/" + total);
            if (total > 0) {
                bossBar.setProgress(Math.max(0.01, (double) killed / total));
            } else {
                bossBar.setProgress(1.0);
            }
            BarColor color = mode == 0 ? BarColor.YELLOW : BarColor.BLUE;
            bossBar.setColor(color);
        }

    }

    private void launchFireworks(Player player) {
        new BukkitRunnable() {
            int count = 5;
            @Override
            public void run() {
                if (count <= 0) {
                    cancel();
                    return;
                }
                Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.setPower(1);
                firework.setFireworkMeta(meta);
                count--;
            }
        }.runTaskTimer(HycraftQuestsAddons.getInstance(), 0, 20);
    }


}
