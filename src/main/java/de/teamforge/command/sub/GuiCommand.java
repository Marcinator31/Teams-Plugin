package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.gui.MainMenu;
import de.teamforge.gui.NoTeamMenu;
import de.teamforge.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuiCommand extends SubCommand {

    public GuiCommand(TeamForgePlugin plugin) {
        super(plugin, "gui", "teams.use", true, "menu");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teams().getTeam(player);
        if (team != null) {
            new MainMenu(plugin, player, team).open();
        } else {
            new NoTeamMenu(plugin, player).open();
        }
    }
}
