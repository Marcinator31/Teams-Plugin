package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RenameCommand extends SubCommand {

    public RenameCommand(TeamForgePlugin plugin) {
        super(plugin, "rename", "teams.use", true, "umbenennen");
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
            msg().send(player, "rename.usage");
            return;
        }
        tryRename(plugin, team, player, args[0]);
    }

    public static boolean tryRename(TeamForgePlugin plugin, Team team, Player by, String name) {
        String error = plugin.getTeamManager().validateName(name);
        if (error != null) {
            plugin.getMessages().send(by, error);
            return false;
        }
        if (!plugin.charge(by, plugin.getConfigManager().costRename)) {
            plugin.getMessages().send(by, "bank.insufficient-player");
            return false;
        }
        plugin.getTeamManager().rename(team, name);
        plugin.getMessages().send(by, "rename.success",
                Placeholder.component("team", team.displayName()));
        return true;
    }
}
