package de.teamforge.listener;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CombatListener implements Listener {

    private final TeamForgePlugin plugin;
    private final Map<UUID, Long> lastNotify = new HashMap<>();
    private static final Set<PotionEffectType> HARMFUL = new HashSet<>(Arrays.asList(
            PotionEffectType.INSTANT_DAMAGE, PotionEffectType.POISON, PotionEffectType.WITHER,
            PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS, PotionEffectType.MINING_FATIGUE,
            PotionEffectType.BLINDNESS, PotionEffectType.HUNGER, PotionEffectType.NAUSEA,
            PotionEffectType.DARKNESS, PotionEffectType.UNLUCK, PotionEffectType.LEVITATION
    ));

    public CombatListener(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    private Player resolveAttacker(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        }
        if (damager instanceof Projectile) {
            ProjectileSource source = ((Projectile) damager).getShooter();
            if (source instanceof Player) {
                return (Player) source;
            }
        }
        if (damager instanceof TNTPrimed) {
            org.bukkit.entity.Entity src = ((TNTPrimed) damager).getSource();
            if (src instanceof Player) {
                return (Player) src;
            }
        }
        if (damager instanceof AreaEffectCloud) {
            ProjectileSource source = ((AreaEffectCloud) damager).getSource();
            if (source instanceof Player) {
                return (Player) source;
            }
        }
        return null;
    }

    /** Returns true if the damage should be blocked. */
    private boolean shouldBlock(Player attacker, Player victim) {
        if (attacker.getUniqueId().equals(victim.getUniqueId())) {
            return false;
        }
        Team attackerTeam = plugin.getTeamManager().getTeam(attacker);
        Team victimTeam = plugin.getTeamManager().getTeam(victim);
        if (attackerTeam == null || victimTeam == null) {
            return false;
        }
        if (attackerTeam.getId().equals(victimTeam.getId())) {
            return !isFfOn(attackerTeam);
        }
        if (plugin.getConfigManager().protectAllies
                && plugin.getTeamManager().areAllied(attackerTeam, victimTeam)) {
            return true;
        }
        return false;
    }

    private boolean isFfOn(Team team) {
        if (plugin.getConfigManager().ffToggleable) {
            return team.isFriendlyFire();
        }
        return plugin.getConfigManager().ffDefault;
    }

    private boolean sameOrAlly(Player attacker, Player victim) {
        return shouldBlock(attacker, victim);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) {
            return;
        }
        if (shouldBlock(attacker, victim)) {
            event.setCancelled(true);
            notify(attacker, victim);
        }
    }

    private void notify(Player attacker, Player victim) {
        Team attackerTeam = plugin.getTeamManager().getTeam(attacker);
        Team victimTeam = plugin.getTeamManager().getTeam(victim);
        long now = System.currentTimeMillis();
        Long last = lastNotify.get(attacker.getUniqueId());
        if (last != null && now - last < 2000) {
            return;
        }
        lastNotify.put(attacker.getUniqueId(), now);
        boolean ally = attackerTeam != null && victimTeam != null
                && !attackerTeam.getId().equals(victimTeam.getId());
        attacker.sendActionBar(plugin.getMessages().get(
                ally ? "combat.ally-protected" : "combat.friendly-fire-blocked"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onSplash(PotionSplashEvent event) {
        if (event.getPotion().getEffects().stream().noneMatch(e -> HARMFUL.contains(e.getType()))) {
            return;
        }
        ProjectileSource source = event.getPotion().getShooter();
        if (!(source instanceof Player)) {
            return;
        }
        Player thrower = (Player) source;
        if (!plugin.getConfigManager().blockSplashPotions) {
            return;
        }
        for (org.bukkit.entity.LivingEntity affected : event.getAffectedEntities()) {
            if (affected instanceof Player && sameOrAlly(thrower, (Player) affected)) {
                event.setIntensity(affected, 0.0);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCloud(AreaEffectCloudApplyEvent event) {
        if (!plugin.getConfigManager().blockSplashPotions) {
            return;
        }
        AreaEffectCloud cloud = event.getEntity();
        boolean harmful = cloud.getCustomEffects().stream()
                .anyMatch(e -> HARMFUL.contains(e.getType()));
        if (!harmful && cloud.getBasePotionType() != null) {
            for (PotionEffect e : cloud.getBasePotionType().getPotionEffects()) {
                if (HARMFUL.contains(e.getType())) {
                    harmful = true;
                    break;
                }
            }
        }
        if (!harmful) {
            return;
        }
        ProjectileSource source = cloud.getSource();
        if (!(source instanceof Player)) {
            return;
        }
        Player thrower = (Player) source;
        event.getAffectedEntities().removeIf(entity ->
                entity instanceof Player && sameOrAlly(thrower, (Player) entity));
    }
}
