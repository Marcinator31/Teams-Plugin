package de.teamforge.listener;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final TeamForgePlugin plugin;

    public ChatListener(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (plugin.getChatInputService().handle(player, message)) {
            event.setCancelled(true);
            return;
        }

        Team team = plugin.getTeamManager().getTeam(player);
        if (team != null && plugin.getChatService().isTeamChat(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getChatService().sendTeamChat(team, player, message);
            return;
        }
        if (team != null && plugin.getChatService().isAllyChat(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getChatService().sendAllyChat(team, player, message);
            return;
        }

        if (team != null && plugin.getConfigManager().decoratePublicChat) {
            final Team finalTeam = team;
            event.renderer(ChatRenderer.viewerUnaware((source, sourceName, msg) ->
                    plugin.getMessages().get("chat.public-format",
                            Placeholder.component("tag", finalTeam.coloredTag()),
                            Placeholder.component("name", sourceName),
                            Placeholder.component("message", msg))));
        }
    }
}
