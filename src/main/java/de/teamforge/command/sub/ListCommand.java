package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.gui.TeamsBrowserMenu;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommand extends SubCommand {

    public ListCommand(TeamForgePlugin plugin) {
        super(plugin, "list", "teams.use", false, "liste");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            new TeamsBrowserMenu(plugin, (Player) sender).open();
            return;
        }
        for (Team team : teams().getTeams()) {
            sender.sendMessage(msg().get("top.entry",
                    Placeholder.unparsed("rank", "-"),
                    Placeholder.component("team", team.displayName()),
                    Placeholder.unparsed("value", String.valueOf(team.getMembers().size()))));
        }
    }
}
