package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DenyCommand extends SubCommand {

    public DenyCommand(TeamForgePlugin plugin) {
        super(plugin, "deny", "teams.use", true, "ablehnen");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            msg().send(player, "invite.usage");
            return;
        }
        Team team = teams().getTeamByName(args[0]);
        if (team == null || !teams().hasInvite(team, player.getUniqueId())) {
            msg().send(player, "join.no-invite");
            return;
        }
        deny(plugin, player, team);
    }

    public static void deny(TeamForgePlugin plugin, Player player, Team team) {
        plugin.getTeamManager().removeInvite(team, player.getUniqueId());
        plugin.getMessages().send(player, "deny.success");
    }
}
