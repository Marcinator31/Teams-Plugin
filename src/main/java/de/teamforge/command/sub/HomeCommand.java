package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand extends SubCommand {

    public HomeCommand(TeamForgePlugin plugin) {
        super(plugin, "home", "teams.use", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        tryHome(plugin, team, player);
    }

    public static void tryHome(TeamForgePlugin plugin, Team team, Player by) {
        plugin.getTeleportService().startHome(by, team);
    }
}
