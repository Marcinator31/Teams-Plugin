package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand extends SubCommand {

    public DelHomeCommand(TeamForgePlugin plugin) {
        super(plugin, "delhome", "teams.use", true);
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
        team.setHome(null);
        teams().markDirty();
        msg().send(player, "home.deleted");
    }
}
