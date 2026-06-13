package de.teamforge.listener;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private final TeamForgePlugin plugin;

    public ConnectionListener(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getScoreboardService().applyPlayer(player);
        if (plugin.getGlowService().isActive()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getGlowService().refreshViewer(player);
                plugin.getGlowService().refreshFor(player);
            }, 20L);
        }
        Team team = plugin.getTeamManager().getTeam(player);
        if (team != null) {
            // update name
            team.getMemberNames().put(player.getUniqueId(), player.getName());
            if (team.isOwner(player.getUniqueId())) {
                plugin.getTeamManager().recalcMaxMembers(team, true);
            }
            if (plugin.getConfigManager().notifyMemberJoin) {
                plugin.getTeamManager().broadcast(team,
                        plugin.getMessages().get("notifications.member-join",
                                Placeholder.unparsed("player", player.getName())));
            }
        } else {
            if (!plugin.getTeamManager().getInvitesFor(player.getUniqueId()).isEmpty()) {
                plugin.getMessages().send(player, "invite.pending-on-join");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getChatInputService().cancel(player.getUniqueId());
        plugin.getTeleportService().cancel(player.getUniqueId());
    }
}
