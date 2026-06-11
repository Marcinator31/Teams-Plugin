package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class HelpCommand extends SubCommand {

    public HelpCommand(TeamForgePlugin plugin) {
        super(plugin, "help", "teams.use", false, "hilfe", "?");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (Component line : msg().getList("help")) {
            sender.sendMessage(line);
        }
    }
}
