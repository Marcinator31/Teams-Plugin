package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DescCommand extends SubCommand {

    public DescCommand(TeamForgePlugin plugin) {
        super(plugin, "desc", "teams.use", true, "beschreibung");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        if (!teams().canEditSettings(team, player.getUniqueId())) {
            msg().send(player, "error.no-permission");
            return;
        }
        if (args.length < 1) {
            msg().send(player, "desc.usage");
            return;
        }
        trySetDesc(plugin, team, player, String.join(" ", args));
    }

    public static boolean trySetDesc(TeamForgePlugin plugin, Team team, Player by, String desc) {
        if (desc.length() > plugin.getConfigManager().descMax) {
            desc = desc.substring(0, plugin.getConfigManager().descMax);
        }
        team.setDescription(desc);
        plugin.getTeamManager().markDirty();
        plugin.getMessages().send(by, "desc.success");
        return true;
    }
}
