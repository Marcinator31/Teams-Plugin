package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UninviteCommand extends SubCommand {

    public UninviteCommand(TeamForgePlugin plugin) {
        super(plugin, "uninvite", "teams.use", true);
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
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
        if (!teams().hasInvite(team, target.getUniqueId())) {
            msg().send(player, "invite.none");
            return;
        }
        teams().removeInvite(team, target.getUniqueId());
        msg().send(player, "invite.revoked", Placeholder.unparsed("target", args[0]));
    }
}
