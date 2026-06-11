package de.teamforge.manager;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportService {

    private final TeamForgePlugin plugin;
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public TeleportService(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    public void startHome(Player player, Team team) {
        if (!plugin.getConfigManager().homeEnabled) {
            plugin.getMessages().send(player, "home.disabled");
            return;
        }
        if (plugin.getConfigManager().homeDisabledWorlds.contains(
                player.getWorld().getName().toLowerCase())) {
            plugin.getMessages().send(player, "home.world-disabled");
            return;
        }
        Location home = team.getHome();
        if (home == null || home.getWorld() == null) {
            plugin.getMessages().send(player, "home.none");
            return;
        }
        long now = System.currentTimeMillis();
        Long until = cooldowns.get(player.getUniqueId());
        if (until != null && until > now) {
            plugin.getMessages().send(player, "home.cooldown");
            return;
        }
        int warmup = plugin.getConfigManager().homeWarmup;
        if (warmup <= 0) {
            teleport(player, home);
            return;
        }
        plugin.getMessages().send(player, "home.teleporting");
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            tasks.remove(player.getUniqueId());
            teleport(player, home);
        }, warmup * 20L);
        tasks.put(player.getUniqueId(), task);
    }

    private void teleport(Player player, Location home) {
        cooldowns.put(player.getUniqueId(),
                System.currentTimeMillis() + plugin.getConfigManager().homeCooldown * 1000L);
        player.teleportAsync(home);
        plugin.getMessages().send(player, "home.success");
    }

    public void handleMove(Player player) {
        if (plugin.getConfigManager().homeCancelOnMove && tasks.containsKey(player.getUniqueId())) {
            cancel(player.getUniqueId());
            plugin.getMessages().send(player, "home.cancelled-move");
        }
    }

    public void handleDamage(Player player) {
        if (plugin.getConfigManager().homeCancelOnDamage && tasks.containsKey(player.getUniqueId())) {
            cancel(player.getUniqueId());
            plugin.getMessages().send(player, "home.cancelled-damage");
        }
    }

    public void cancel(UUID player) {
        BukkitTask task = tasks.remove(player);
        if (task != null) {
            task.cancel();
        }
    }

    public void cancelAll() {
        for (BukkitTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
    }
}
