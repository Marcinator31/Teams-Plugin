package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AllyChatCommand extends SubCommand {

    public AllyChatCommand(TeamForgePlugin plugin) {
        super(plugin, "allychat", "teams.use", true, "ac");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (!cfg().allyChatEnabled) {
            msg().send(player, "chat.disabled");
            return;
        }
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        if (args.length > 0) {
            plugin.getChatService().sendAllyChat(team, player, String.join(" ", args));
            return;
        }
        boolean on = plugin.getChatService().toggleAllyChat(player.getUniqueId());
        msg().send(player, on ? "chat.ally-toggle-on" : "chat.ally-toggle-off");
    }
}
