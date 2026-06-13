package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TransferCommand extends SubCommand {

    public TransferCommand(TeamForgePlugin plugin) {
        super(plugin, "transfer", "teams.use", true, "uebergeben");
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
            msg().send(player, "transfer.usage");
            return;
        }
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
        if (!team.isMember(target.getUniqueId()) || team.isOwner(target.getUniqueId())) {
            msg().send(player, "kick.target");
            return;
        }
        doTransfer(plugin, team, target.getUniqueId(), player);
    }

    public static void doTransfer(TeamForgePlugin plugin, Team team, UUID newOwner, Player by) {
        plugin.getTeamManager().transferOwnership(team, newOwner);
        plugin.getMessages().send(by, "transfer.success",
                Placeholder.unparsed("target", team.getMemberName(newOwner)));
        plugin.getTeamManager().broadcast(team, plugin.getMessages().get("transfer.broadcast",
                Placeholder.unparsed("target", team.getMemberName(newOwner))));
    }
}
