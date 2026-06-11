package de.teamforge.manager;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardService {

    private final TeamForgePlugin plugin;

    public ScoreboardService(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    private Scoreboard board() {
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    private String key(Team team) {
        return "tf_" + team.getId().toString().replace("-", "").substring(0, 12);
    }

    private org.bukkit.scoreboard.Team scoreboardTeam(Team team, boolean create) {
        Scoreboard board = board();
        org.bukkit.scoreboard.Team sb = board.getTeam(key(team));
        if (sb == null && create) {
            sb = board.registerNewTeam(key(team));
        }
        return sb;
    }

    public void applyPlayer(Player player) {
        if (!plugin.getConfigManager().nametagsEnabled) {
            return;
        }
        // remove from all tf_ teams
        for (org.bukkit.scoreboard.Team sb : board().getTeams()) {
            if (sb.getName().startsWith("tf_") && sb.hasEntry(player.getName())) {
                sb.removeEntry(player.getName());
            }
        }
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            return;
        }
        org.bukkit.scoreboard.Team sb = scoreboardTeam(team, true);
        updateMeta(team, sb);
        sb.addEntry(player.getName());
    }

    public void updateMeta(Team team) {
        org.bukkit.scoreboard.Team sb = scoreboardTeam(team, false);
        if (sb != null) {
            updateMeta(team, sb);
        }
    }

    private void updateMeta(Team team, org.bukkit.scoreboard.Team sb) {
        if (plugin.getConfigManager().nametagShowTag) {
            sb.prefix(team.coloredTag().append(Component.text(" ")));
        } else {
            sb.prefix(Component.empty());
        }
        if (plugin.getConfigManager().nametagColor) {
            sb.color(team.getColor());
        }
        sb.setAllowFriendlyFire(isFfOn(team));
    }

    private boolean isFfOn(Team team) {
        if (plugin.getConfigManager().ffToggleable) {
            return team.isFriendlyFire();
        }
        return plugin.getConfigManager().ffDefault;
    }

    public void removeEntryName(String name) {
        for (org.bukkit.scoreboard.Team sb : board().getTeams()) {
            if (sb.getName().startsWith("tf_") && sb.hasEntry(name)) {
                sb.removeEntry(name);
            }
        }
    }

    public void remove(Team team) {
        org.bukkit.scoreboard.Team sb = scoreboardTeam(team, false);
        if (sb != null) {
            sb.unregister();
        }
    }

    public void clearAll() {
        for (org.bukkit.scoreboard.Team sb : board().getTeams()) {
            if (sb.getName().startsWith("tf_")) {
                sb.unregister();
            }
        }
    }
}
