package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisbandCommand extends SubCommand {

    public DisbandCommand(TeamForgePlugin plugin) {
        super(plugin, "disband", "teams.use", true, "aufloesen");
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
        if (args.length < 1 || !args[0].equalsIgnoreCase("confirm")) {
            msg().send(player, "disband.confirm-needed");
            return;
        }
        doDisband(plugin, team, player.getName());
        msg().send(player, "disband.success");
    }

    public static void doDisband(TeamForgePlugin plugin, Team team, String byName) {
        if (plugin.getConfigManager().broadcastDisband) {
            plugin.getServer().sendMessage(plugin.getMessages().get("disband.broadcast",
                    Placeholder.component("team", team.displayName()),
                    Placeholder.unparsed("player", byName)));
        }
        plugin.getTeamManager().disband(team);
    }
}
