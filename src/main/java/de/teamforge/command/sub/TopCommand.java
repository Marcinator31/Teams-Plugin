package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TopCommand extends SubCommand {

    public TopCommand(TeamForgePlugin plugin) {
        super(plugin, "top", "teams.use", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String type = args.length >= 1 ? args[0].toLowerCase() : "members";
        List<Team> teamsList = new ArrayList<>(teams().getTeams());
        Comparator<Team> comp;
        switch (type) {
            case "kills":
                comp = Comparator.comparingInt(Team::getKills).reversed();
                break;
            case "bank":
                comp = Comparator.comparingDouble(Team::getBank).reversed();
                break;
            case "members":
            case "mitglieder":
                comp = Comparator.comparingInt((Team t) -> t.getMembers().size()).reversed();
                type = "members";
                break;
            default:
                msg().send(sender, "top.usage");
                return;
        }
        teamsList.sort(comp);
        msg().send(sender, "top.header", Placeholder.unparsed("type", type));
        int rank = 1;
        for (Team team : teamsList) {
            if (rank > 10) {
                break;
            }
            String value;
            if (type.equals("kills")) {
                value = String.valueOf(team.getKills());
            } else if (type.equals("bank")) {
                value = plugin.formatMoney(team.getBank());
            } else {
                value = String.valueOf(team.getMembers().size());
            }
            msg().send(sender, "top.entry",
                    Placeholder.unparsed("rank", String.valueOf(rank++)),
                    Placeholder.component("team", team.displayName()),
                    Placeholder.unparsed("value", value));
        }
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(Arrays.asList("members", "kills", "bank"));
        }
        return new ArrayList<>();
    }
}
