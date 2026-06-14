package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.gui.InvitesMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvitesCommand extends SubCommand {

    public InvitesCommand(TeamForgePlugin plugin) {
        super(plugin, "invites", "teams.use", true, "einladungen");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        new InvitesMenu(plugin, (Player) sender).open();
    }
}
