package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InviteCommand extends SubCommand {

    public InviteCommand(TeamForgePlugin plugin) {
        super(plugin, "invite", "teams.use", true, "einladen");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        if (!teams().canInvite(team, player.getUniqueId())) {
            msg().send(player, "error.no-permission");
            return;
        }
        if (args.length < 1) {
            msg().send(player, "invite.usage");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            msg().send(player, "error.player-not-found");
            return;
        }
        tryInvite(plugin, team, player, target);
    }

    public static boolean tryInvite(TeamForgePlugin plugin, Team team, Player by, Player target) {
        if (plugin.getTeamManager().getTeam(target) != null) {
            plugin.getMessages().send(by, "invite.target-already-team");
            return false;
        }
        if (plugin.getTeamManager().hasInvite(team, target.getUniqueId())) {
            plugin.getMessages().send(by, "invite.already-invited");
            return false;
        }
        if (team.getMembers().size() >= team.getMaxMembers()) {
            plugin.getMessages().send(by, "invite.team-full");
            return false;
        }
        plugin.getTeamManager().addInvite(team, target.getUniqueId());
        plugin.getMessages().send(by, "invite.sent",
                Placeholder.unparsed("target", target.getName()));
        plugin.getMessages().send(target, "invite.received",
                Placeholder.component("team", team.displayName()),
                Placeholder.unparsed("team_name", team.getName()));
        return true;
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
        }
        return list;
    }
}
