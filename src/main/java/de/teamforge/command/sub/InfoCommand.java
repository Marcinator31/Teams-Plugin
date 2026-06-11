package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;

public class InfoCommand extends SubCommand {

    public InfoCommand(TeamForgePlugin plugin) {
        super(plugin, "info", "teams.use", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Team team;
        if (args.length >= 1) {
            team = teams().getTeamByName(args[0]);
        } else if (sender instanceof Player) {
            team = teams().getTeam((Player) sender);
        } else {
            team = null;
        }
        if (team == null) {
            msg().send(sender, "error.team-not-found");
            return;
        }
        String created = new SimpleDateFormat("dd.MM.yyyy").format(team.getCreated());
        TagResolver[] ph = {
                Placeholder.component("team", team.displayName()),
                Placeholder.component("tag", team.coloredTag()),
                Placeholder.unparsed("owner", team.getMemberName(team.getOwner())),
                Placeholder.unparsed("members", String.valueOf(team.getMembers().size())),
                Placeholder.unparsed("members_max", String.valueOf(team.getMaxMembers())),
                Placeholder.unparsed("online", String.valueOf(team.getOnlineMembers().size())),
                Placeholder.unparsed("kills", String.valueOf(team.getKills())),
                Placeholder.unparsed("deaths", String.valueOf(team.getDeaths())),
                Placeholder.unparsed("created", created),
                Placeholder.unparsed("description",
                        team.getDescription().isEmpty() ? "-" : team.getDescription())
        };
        for (Component line : msg().getList("info.format", ph)) {
            sender.sendMessage(line);
        }
    }
}
