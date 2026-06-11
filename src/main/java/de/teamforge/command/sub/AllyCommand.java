package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AllyCommand extends SubCommand {

    public AllyCommand(TeamForgePlugin plugin) {
        super(plugin, "ally", "teams.use", true, "verbuenden");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (!cfg().alliesEnabled) {
            msg().send(player, "error.unknown-command");
            return;
        }
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
        if (target == null) {
            msg().send(player, "error.team-not-found");
            return;
        }
        requestOrAccept(plugin, team, target, player);
    }

    public static void requestOrAccept(TeamForgePlugin plugin, Team own, Team target, Player by) {
        if (own.getId().equals(target.getId())) {
            plugin.getMessages().send(by, "ally.self");
            return;
        }
        if (plugin.getTeamManager().areAllied(own, target)) {
            plugin.getMessages().send(by, "ally.already");
            return;
        }
        if (own.getAllies().size() >= plugin.getConfigManager().maxAllies) {
            plugin.getMessages().send(by, "ally.limit");
            return;
        }
        if (plugin.getTeamManager().hasAllyRequest(own, target)) {
            plugin.getTeamManager().acceptAlly(own, target);
            plugin.getMessages().send(by, "ally.accepted",
                    Placeholder.component("team", target.displayName()));
            plugin.getTeamManager().broadcast(target, plugin.getMessages().get("ally.accepted",
                    Placeholder.component("team", own.displayName())));
            return;
        }
        plugin.getTeamManager().requestAlly(own, target);
        plugin.getMessages().send(by, "ally.request-sent",
                Placeholder.component("team", target.displayName()));
        plugin.getTeamManager().broadcast(target, plugin.getMessages().get("ally.request-received",
                Placeholder.component("team", own.displayName()),
                Placeholder.unparsed("team_name", own.getName())));
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            for (Team t : teams().getTeams()) {
                list.add(t.getName());
            }
        }
        return list;
    }
}
