package fr.justop.hycraftQuestsAddons.listeners;

import fr.justop.hycraftQuestsAddons.BossQuestUtils;
import fr.justop.hycraftQuestsAddons.HycraftQuestsAddons;
import fr.justop.hycraftQuestsAddons.utils.MusicUtils;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.questers.Quester;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
import java.util.OptionalInt;
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
                BossQuestUtils.updateBossBar(player, mode);

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
                if(mob == null) return;

                if (mob.getMobType().equalsIgnoreCase("Boss_prehistoire_quete")) {
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§aVous êtes venu a bout de Tuméride! Téléportation dans quelques secondes...");
                    HycraftQuestsAddons.removeNearbyEntities(player);
                    MusicUtils.playVictoryMusic(player, HycraftQuestsAddons.getInstance());
                    launchFireworks(player);
                    player.sendTitle("§aVictoire !", "§aVous avez vaincu Tuméride!");
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

        Entity damager = event.getDamager();
        ActiveMob mob =  MythicBukkit.inst().getMobManager().getActiveMob(event.getEntity().getUniqueId()).orElse(null);

        if (mob != null && mob.getMobType().equalsIgnoreCase("Boss_prehistoire_quete")) {

            if (damager instanceof Arrow) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(HycraftQuestsAddons.getInstance(), () -> {
            HycraftQuestsAddons.getInstance().restoreInventory(player);
            if (player.getGameMode() == GameMode.ADVENTURE) {
                player.setGameMode(GameMode.SURVIVAL);
            }

        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        HycraftQuestsAddons instance = HycraftQuestsAddons.getInstance();

        boolean inChallenge = instance.getActivePlayers().containsKey(uuid);
        boolean inShield = instance.getShieldPlayers().containsKey(uuid);
        boolean inBoss = instance.getBossPlayers().containsKey(uuid);

        if (inChallenge || inShield || inBoss) {

            HycraftQuestsAddons.removeNearbyEntities(player);

            if (instance.getActiveTasks().containsKey(uuid)) {
                instance.getActiveTasks().get(uuid).cancel();
                instance.getActiveTasks().remove(uuid);
            }

            if (instance.getBossBars().containsKey(uuid)) {
                instance.getBossBars().get(uuid).removeAll();
                instance.getBossBars().remove(uuid);
            }

            if (instance.getBosses().containsKey(uuid)) {
                instance.getBosses().get(uuid).remove();
                instance.getBosses().remove(uuid);
            }

            instance.restoreInventory(player);

            instance.getActivePlayers().remove(uuid);
            instance.getShieldPlayers().remove(uuid);
            instance.getBossPlayers().remove(uuid);

            instance.getRemainingMobs().remove(uuid);
            instance.getMobsKilled().remove(uuid);

            HycraftQuestsAddons.getInstance().getLogger().info("Le joueur " + player.getName() + " a quitté en plein challenge. Inventaire restauré et arène libérée.");
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
            Quester acc = questsAPI.getPlugin().getPlayersManager().getQuester(player);

            if (!(acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(124))).getStage().orElse(-1) == 6)) return;

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
                    Entity bukkitEntity = boss.getEntity().getBukkitEntity();
                    updateAttribute(bukkitEntity, Attribute.GENERIC_ATTACK_DAMAGE, 24.0);
                    player.playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f,1.0f);
                    player.sendTitle("§5Phase 3", "§5Le boss est furieux !");
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§eLe boss invoque les esprits du sanctuaire pour lui octroyer l'invulnérabilité! Neutralisez les en fichant une flèche bien placée en leur §6§ljoyau §e(levez la tête!)");
                }

                if (healthPercentage <= 0.5 && HycraftQuestsAddons.getInstance().getBossPhase().get(player.getUniqueId()) == 1) {
                    HycraftQuestsAddons.getInstance().getBossPhase().put(player.getUniqueId(), 2);
                    HycraftQuestsAddons.getInstance().getSpiritPlayers().put(player.getUniqueId(),true);
                    BossQuestUtils.invokeSpirit(player);
                    BossQuestUtils.startDisplayingActionBar(player);
                    Entity bukkitEntity = boss.getEntity().getBukkitEntity();
                    updateAttribute(bukkitEntity, Attribute.GENERIC_ATTACK_DAMAGE, 35.0);
                    player.playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f,1.0f);
                    player.sendTitle("§ePhase 2", "§eLe boss est en colère !");
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§eLe boss invoque les esprits du sanctuaire pour lui octroyer l'invulnérabilité! Neutralisez les en fichant une flèche bien placée en leur §6§ljoyau §e(levez la tête!)");
                }

                if (HycraftQuestsAddons.getInstance().getSpiritPlayers().containsKey(player.getUniqueId()))
                {
                    event.setCancelled(true);
                    player.sendMessage(HycraftQuestsAddons.PREFIX + "§cLe boss est temporairement invulnérable! Détruisez les esprits qui le protègent (Levez la tête!)");
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
        Quester acc = questsAPI.getPlugin().getPlayersManager().getQuester(player);

        if(mode == 0){
            acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(118))).setStage(OptionalInt.of(7));
        }else {
            acc.getDataHolder().getQuestData(Objects.requireNonNull(questsAPI.getQuestsManager().getQuest(138))).setStage(OptionalInt.of(1));
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

    private void launchFireworks(Player player) {
        new BukkitRunnable() {
            int count = 5;
            @Override
            public void run() {
                if (count <= 0) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation();

                Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                FireworkMeta fwm = fw.getFireworkMeta();

                FireworkEffect effect = FireworkEffect.builder()
                        .withColor(Color.LIME, Color.YELLOW)
                        .withFade(Color.WHITE)
                        .with(FireworkEffect.Type.STAR)
                        .flicker(true)
                        .trail(true)
                        .build();

                fwm.addEffect(effect);
                fwm.setPower(1);
                fw.setFireworkMeta(fwm);

                count--;
            }
        }.runTaskTimer(HycraftQuestsAddons.getInstance(), 0, 20);
    }

    private void updateAttribute(Entity entity, Attribute attr, double value) {
        if (entity instanceof org.bukkit.entity.LivingEntity le) {
            AttributeInstance instance = le.getAttribute(attr);
            if (instance != null) instance.setBaseValue(value);
        }
    }


}
