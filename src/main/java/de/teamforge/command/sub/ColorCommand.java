package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ColorCommand extends SubCommand {

    public ColorCommand(TeamForgePlugin plugin) {
        super(plugin, "color", "teams.use", true, "farbe");
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
        if (!cfg().allowColor) {
            msg().send(player, "color.disabled");
            return;
        }
        if (args.length < 1) {
            msg().send(player, "color.usage");
            return;
        }
        NamedTextColor color = NamedTextColor.NAMES.value(args[0].toLowerCase(Locale.ROOT));
        if (color == null) {
            msg().send(player, "color.invalid");
            return;
        }
        doSetColor(plugin, team, player, color);
    }

    public static void doSetColor(TeamForgePlugin plugin, Team team, Player by, NamedTextColor color) {
        plugin.getTeamManager().setColor(team, color);
        plugin.getMessages().send(by, "color.success");
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            for (NamedTextColor c : NamedTextColor.NAMES.values()) {
                list.add(c.toString());
            }
        }
        return list;
    }
}
