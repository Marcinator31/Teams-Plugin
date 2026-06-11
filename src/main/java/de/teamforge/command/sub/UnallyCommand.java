package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnallyCommand extends SubCommand {

    public UnallyCommand(TeamForgePlugin plugin) {
        super(plugin, "unally", "teams.use", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        if (!teams().canManageAllies(team, player.getUniqueId())) {
            msg().send(player, "error.no-permission");
            return;
        }
        if (args.length < 1) {
            msg().send(player, "ally.usage");
            return;
        }
        Team target = teams().getTeamByName(args[0]);
        if (target == null || !teams().areAllied(team, target)) {
            msg().send(player, "error.team-not-found");
            return;
        }
        doUnally(plugin, team, target, player);
    }

    public static void doUnally(TeamForgePlugin plugin, Team own, Team target, Player by) {
        plugin.getTeamManager().removeAlly(own, target);
        plugin.getMessages().send(by, "ally.removed",
                Placeholder.component("team", target.displayName()));
        plugin.getTeamManager().broadcast(target, plugin.getMessages().get("ally.removed",
                Placeholder.component("team", own.displayName())));
    }
}
