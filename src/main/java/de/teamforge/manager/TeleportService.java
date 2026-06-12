package de.teamforge.manager;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

        // Show a live countdown over the hotbar during the warmup. We use a
        // repeating task (every 2 ticks) driven by the wall clock, so the
        // displayed seconds stay accurate even under minor server lag. When the
        // warmup elapses we stop the task and perform the teleport.
        final UUID id = player.getUniqueId();
        final long startMs = System.currentTimeMillis();
        final long totalMs = warmup * 1000L;

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                cancel(id);
                return;
            }

            long elapsed = System.currentTimeMillis() - startMs;

            // Warmup finished -> stop the countdown and teleport.
            if (elapsed >= totalMs) {
                BukkitTask t = tasks.remove(id);
                if (t != null) t.cancel();
                player.sendActionBar(Component.empty());
                teleport(player, home);
                return;
            }

            // Seconds remaining, rounded up (5..4001ms -> 5, 4000..3001 -> 4, ...).
            int secondsLeft = (int) ((totalMs - elapsed + 999) / 1000);
            if (secondsLeft < 1) secondsLeft = 1;

            player.sendActionBar(Component.text(
                    "\u23F1 Teleporting to team home in " + secondsLeft + "s... do not move!",
                    NamedTextColor.GREEN));
        }, 0L, 2L);

        tasks.put(id, task);
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
            player.sendActionBar(Component.text(
                    "\u2717 Teleport cancelled!", NamedTextColor.RED));
            plugin.getMessages().send(player, "home.cancelled-move");
        }
    }

    public void handleDamage(Player player) {
        if (plugin.getConfigManager().homeCancelOnDamage && tasks.containsKey(player.getUniqueId())) {
            cancel(player.getUniqueId());
            player.sendActionBar(Component.text(
                    "\u2717 Teleport cancelled!", NamedTextColor.RED));
            plugin.getMessages().send(player, "home.cancelled-damage");
        }
    }

    public void cancel(UUID player) {
        BukkitTask task = tasks.remove(player);
        if (task != null) {
            task.cancel();
        }
        // Clear any leftover countdown text from the hotbar.
        Player p = plugin.getServer().getPlayer(player);
        if (p != null) {
            p.sendActionBar(Component.empty());
        }
    }

    public void cancelAll() {
        for (BukkitTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
    }
}
