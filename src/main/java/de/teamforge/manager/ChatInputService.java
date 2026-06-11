package de.teamforge.manager;

import de.teamforge.TeamForgePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Captures a player's next chat message (e.g. after a GUI click).
 */
public class ChatInputService {

    private final TeamForgePlugin plugin;
    private final Map<UUID, Consumer<String>> pending = new HashMap<>();

    public ChatInputService(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    public void await(Player player, Component prompt, Consumer<String> callback) {
        pending.put(player.getUniqueId(), callback);
        player.closeInventory();
        player.sendMessage(prompt);
        plugin.getMessages().send(player, "input.cancel-hint");
    }

    public boolean isAwaiting(UUID player) {
        return pending.containsKey(player);
    }

    public void cancel(UUID player) {
        pending.remove(player);
    }

    /** Returns true if the event was consumed. */
    public boolean handle(Player player, String message) {
        Consumer<String> callback = pending.remove(player.getUniqueId());
        if (callback == null) {
            return false;
        }
        String cancelWord = plugin.getMessages().raw("input.cancel-word");
        if (message.equalsIgnoreCase(cancelWord)) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    plugin.getMessages().send(player, "input.cancelled"));
            return true;
        }
        Bukkit.getScheduler().runTask(plugin, () -> callback.accept(message));
        return true;
    }
}
