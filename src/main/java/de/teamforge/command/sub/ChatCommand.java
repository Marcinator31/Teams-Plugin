package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatCommand extends SubCommand {

    public ChatCommand(TeamForgePlugin plugin) {
        super(plugin, "chat", "teams.use", true, "c");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (!cfg().teamChatEnabled) {
            msg().send(player, "chat.disabled");
            return;
        }
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        if (args.length > 0) {
            plugin.getChatService().sendTeamChat(team, player, String.join(" ", args));
            return;
        }
        boolean on = plugin.getChatService().toggleTeamChat(player.getUniqueId());
        msg().send(player, on ? "chat.toggle-on" : "chat.toggle-off");
    }
}
