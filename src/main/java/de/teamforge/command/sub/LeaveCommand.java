package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand extends SubCommand {

    public LeaveCommand(TeamForgePlugin plugin) {
        super(plugin, "leave", "teams.use", true, "verlassen");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        tryLeave(plugin, player, team);
    }

    public static boolean tryLeave(TeamForgePlugin plugin, Player player, Team team) {
        if (team.isOwner(player.getUniqueId())) {
            if (team.getMembers().size() > 1) {
                plugin.getMessages().send(player, "leave.owner-must-transfer");
                return false;
            }
            DisbandCommand.doDisband(plugin, team, player.getName());
            return true;
        }
        String name = player.getName();
        plugin.getTeamManager().removeMember(team, player.getUniqueId());
        plugin.getMessages().send(player, "leave.success");
        plugin.getTeamManager().broadcast(team, plugin.getMessages().get("leave.broadcast",
                Placeholder.unparsed("player", name)));
        return true;
    }
}
