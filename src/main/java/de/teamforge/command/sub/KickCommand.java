package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KickCommand extends SubCommand {

    public KickCommand(TeamForgePlugin plugin) {
        super(plugin, "kick", "teams.use", true, "rauswerfen");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        if (args.length < 1) {
            msg().send(player, "kick.usage");
            return;
        }
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
        UUID targetId = target.getUniqueId();
        if (!team.isMember(targetId)) {
            msg().send(player, "kick.target");
            return;
        }
        if (!teams().canKick(team, player.getUniqueId(), targetId)) {
            msg().send(player, "kick.cannot");
            return;
        }
        doKick(plugin, team, targetId, player);
    }

    public static void doKick(TeamForgePlugin plugin, Team team, UUID target, Player by) {
        String name = team.getMemberName(target);
        plugin.getTeamManager().removeMember(team, target);
        plugin.getMessages().send(by, "kick.success", Placeholder.unparsed("target", name));
        plugin.getTeamManager().broadcast(team, plugin.getMessages().get("kick.broadcast",
                Placeholder.unparsed("target", name)));
        Player online = plugin.getServer().getPlayer(target);
        if (online != null) {
            plugin.getMessages().send(online, "kick.target");
        }
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1 && sender instanceof Player) {
            Team team = teams().getTeam((Player) sender);
            if (team != null) {
                for (UUID m : team.getMembers().keySet()) {
                    list.add(team.getMemberName(m));
                }
            }
        }
        return list;
    }
}
