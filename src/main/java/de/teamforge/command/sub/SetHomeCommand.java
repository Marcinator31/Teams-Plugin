package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand extends SubCommand {

    public SetHomeCommand(TeamForgePlugin plugin) {
        super(plugin, "sethome", "teams.use", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        if (!teams().canSetHome(team, player.getUniqueId())) {
            msg().send(player, "error.no-permission");
            return;
        }
        trySetHome(plugin, team, player);
    }

    public static boolean trySetHome(TeamForgePlugin plugin, Team team, Player by) {
        if (!plugin.getConfigManager().homeEnabled) {
            plugin.getMessages().send(by, "home.disabled");
            return false;
        }
        if (!plugin.charge(by, plugin.getConfigManager().costSetHome)) {
            plugin.getMessages().send(by, "bank.insufficient-player");
            return false;
        }
        team.setHome(by.getLocation());
        plugin.getTeamManager().markDirty();
        plugin.getMessages().send(by, "home.set");
        return true;
    }
}
