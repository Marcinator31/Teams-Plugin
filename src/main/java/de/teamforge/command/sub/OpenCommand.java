package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenCommand extends SubCommand {

    public OpenCommand(TeamForgePlugin plugin) {
        super(plugin, "open", "teams.use", true, "offen");
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
        if (!plugin.getConfigManager().openTeamsAllowed) {
            plugin.getMessages().send(by, "open.locked");
            return;
        }
        team.setOpen(!team.isOpen());
        plugin.getTeamManager().markDirty();
        plugin.getMessages().send(by, team.isOpen() ? "open.enabled" : "open.disabled");
    }
}
