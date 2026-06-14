package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvpCommand extends SubCommand {

    public PvpCommand(TeamForgePlugin plugin) {
        super(plugin, "pvp", "teams.use", true, "ff");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        if (!teams().canEditSettings(team, player.getUniqueId())) {
            msg().send(player, "error.no-permission");
            return;
        }
        toggle(plugin, team, player);
    }

    public static void toggle(TeamForgePlugin plugin, Team team, Player by) {
        if (!plugin.getConfigManager().ffToggleable) {
            plugin.getMessages().send(by, "pvp.locked");
            return;
        }
        team.setFriendlyFire(!team.isFriendlyFire());
        plugin.getScoreboardService().updateMeta(team);
        plugin.getTeamManager().markDirty();
        plugin.getMessages().send(by, team.isFriendlyFire() ? "pvp.enabled" : "pvp.disabled");
    }
}
