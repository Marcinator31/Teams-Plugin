package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TagCommand extends SubCommand {

    public TagCommand(TeamForgePlugin plugin) {
        super(plugin, "tag", "teams.use", true, "kuerzel");
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
            msg().send(player, "tag.usage");
            return;
        }
        tryTag(plugin, team, player, args[0]);
    }

    public static boolean tryTag(TeamForgePlugin plugin, Team team, Player by, String tag) {
        String error = plugin.getTeamManager().validateTag(tag);
        if (error != null) {
            plugin.getMessages().send(by, error);
            return false;
        }
        if (!plugin.charge(by, plugin.getConfigManager().costTag)) {
            plugin.getMessages().send(by, "bank.insufficient-player");
            return false;
        }
        plugin.getTeamManager().setTag(team, tag);
        plugin.getMessages().send(by, "tag.success",
                Placeholder.component("tag", team.coloredTag()));
        return true;
    }
}
