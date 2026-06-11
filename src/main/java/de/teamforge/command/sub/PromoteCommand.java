package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import de.teamforge.model.TeamRole;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PromoteCommand extends SubCommand {

    public PromoteCommand(TeamForgePlugin plugin) {
        super(plugin, "promote", "teams.use", true, "befoerdern");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        if (!team.isOwner(player.getUniqueId())) {
            msg().send(player, "error.not-owner");
            return;
        }
        if (args.length < 1) {
            msg().send(player, "promote.usage");
            return;
        }
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
        doPromote(plugin, team, target.getUniqueId(), player);
    }

    public static boolean doPromote(TeamForgePlugin plugin, Team team, UUID target, Player by) {
        TeamRole role = team.getRole(target);
        if (role == null) {
            plugin.getMessages().send(by, "kick.target");
            return false;
        }
        if (role != TeamRole.MEMBER) {
            plugin.getMessages().send(by, "promote.already");
            return false;
        }
        plugin.getTeamManager().setRole(team, target, TeamRole.OFFICER);
        plugin.getMessages().send(by, "promote.success",
                Placeholder.unparsed("target", team.getMemberName(target)));
        return true;
    }
}
