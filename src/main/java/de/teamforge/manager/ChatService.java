package de.teamforge.manager;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import de.teamforge.model.TeamRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatService {

    private final TeamForgePlugin plugin;
    private final Set<UUID> teamChat = new HashSet<>();
    private final Set<UUID> allyChat = new HashSet<>();
    private final Set<UUID> spy = new HashSet<>();

    public ChatService(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean toggleTeamChat(UUID player) {
        allyChat.remove(player);
        if (teamChat.contains(player)) {
            teamChat.remove(player);
            return false;
        }
        teamChat.add(player);
        return true;
    }

    public boolean toggleAllyChat(UUID player) {
        teamChat.remove(player);
        if (allyChat.contains(player)) {
            allyChat.remove(player);
            return false;
        }
        allyChat.add(player);
        return true;
    }

    public boolean isTeamChat(UUID player) {
        return teamChat.contains(player);
    }

    public boolean isAllyChat(UUID player) {
        return allyChat.contains(player);
    }

    public boolean toggleSpy(UUID player) {
        if (spy.contains(player)) {
            spy.remove(player);
            return false;
        }
        spy.add(player);
        return true;
    }

    public void clearPlayer(UUID player) {
        teamChat.remove(player);
        allyChat.remove(player);
    }

    private TagResolver[] base(Team team, Player sender, String message) {
        TeamRole role = team.getRole(sender.getUniqueId());
        return new TagResolver[]{
                Placeholder.component("team", team.displayName()),
                Placeholder.component("tag", team.coloredTag()),
                Placeholder.unparsed("player", sender.getName()),
                Placeholder.component("role", plugin.getMessages().get(role.getKey())),
                Placeholder.unparsed("message", message)
        };
    }

    public void sendTeamChat(Team team, Player sender, String message) {
        Component formatted = plugin.getMessages().get("chat.team-format", base(team, sender, message));
        for (Player member : team.getOnlineMembers()) {
            member.sendMessage(formatted);
        }
        sendSpy(team, sender, message, "chat.spy-format");
        Bukkit.getConsoleSender().sendMessage(formatted);
    }

    public void sendAllyChat(Team team, Player sender, String message) {
        Component formatted = plugin.getMessages().get("chat.ally-format", base(team, sender, message));
        for (Player member : team.getOnlineMembers()) {
            member.sendMessage(formatted);
        }
        for (UUID allyId : team.getAllies()) {
            Team ally = plugin.getTeamManager().getTeam(allyId);
            if (ally != null) {
                for (Player member : ally.getOnlineMembers()) {
                    member.sendMessage(formatted);
                }
            }
        }
        sendSpy(team, sender, message, "chat.spy-format");
        Bukkit.getConsoleSender().sendMessage(formatted);
    }

    private void sendSpy(Team team, Player sender, String message, String key) {
        if (spy.isEmpty()) {
            return;
        }
        Component formatted = plugin.getMessages().get(key, base(team, sender, message));
        for (UUID spyId : spy) {
            Player spyPlayer = Bukkit.getPlayer(spyId);
            if (spyPlayer != null && !team.isMember(spyId)) {
                spyPlayer.sendMessage(formatted);
            }
        }
    }
}
