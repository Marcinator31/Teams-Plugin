package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class JoinCommand extends SubCommand {

    public JoinCommand(TeamForgePlugin plugin) {
        super(plugin, "join", "teams.use", true, "beitreten");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            msg().send(player, "join.usage");
            return;
        }
        Team team = teams().getTeamByName(args[0]);
        if (team == null) {
            msg().send(player, "error.team-not-found");
            return;
        }
        tryJoin(plugin, player, team);
    }

    public static boolean tryJoin(TeamForgePlugin plugin, Player player, Team team) {
        if (plugin.getTeamManager().getTeam(player) != null) {
            plugin.getMessages().send(player, "error.already-in-team");
            return false;
        }
        boolean invited = plugin.getTeamManager().hasInvite(team, player.getUniqueId());
        if (!team.isOpen() && !invited) {
            plugin.getMessages().send(player, "join.no-invite");
            return false;
        }
        if (team.getMembers().size() >= team.getMaxMembers()) {
            plugin.getMessages().send(player, "join.full");
            return false;
        }
        plugin.getTeamManager().addMember(team, player);
        plugin.getMessages().send(player, "join.success",
                Placeholder.component("team", team.displayName()));
        plugin.getTeamManager().broadcast(team, plugin.getMessages().get("join.broadcast",
                Placeholder.unparsed("player", player.getName())));
        return true;
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            for (Team team : teams().getTeams()) {
                if (team.isOpen()) {
                    list.add(team.getName());
                }
            }
        }
        return list;
    }
}
