package de.teamforge.command;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.sub.DisbandCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TeamAdminCommand implements TabExecutor {

    private final TeamForgePlugin plugin;

    public TeamAdminCommand(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("teams.admin")) {
            plugin.getMessages().send(sender, "error.no-permission");
            return true;
        }
        if (args.length == 0) {
            plugin.getMessages().send(sender, "admin.usage");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload":
                plugin.getConfigManager().reload();
                plugin.getMessages().reload();
                plugin.getMessages().send(sender, "admin.reloaded");
                return true;
            case "disband": {
                if (args.length < 2) {
                    plugin.getMessages().send(sender, "admin.usage");
                    return true;
                }
                Team team = plugin.getTeamManager().getTeamByName(args[1]);
                if (team == null) {
                    plugin.getMessages().send(sender, "error.team-not-found");
                    return true;
                }
                DisbandCommand.doDisband(plugin, team, sender.getName());
                plugin.getMessages().send(sender, "admin.disbanded",
                        Placeholder.unparsed("team", args[1]));
                return true;
            }
            case "forcejoin": {
                if (args.length < 3) {
                    plugin.getMessages().send(sender, "admin.usage");
                    return true;
                }
                Player target = plugin.getServer().getPlayerExact(args[1]);
                if (target == null) {
                    plugin.getMessages().send(sender, "error.player-not-found");
                    return true;
                }
                Team team = plugin.getTeamManager().getTeamByName(args[2]);
                if (team == null) {
                    plugin.getMessages().send(sender, "error.team-not-found");
                    return true;
                }
                if (plugin.getTeamManager().getTeam(target) != null) {
                    plugin.getMessages().send(sender, "error.already-in-team");
                    return true;
                }
                plugin.getTeamManager().addMember(team, target);
                plugin.getMessages().send(sender, "admin.forcejoin",
                        Placeholder.unparsed("target", target.getName()),
                        Placeholder.unparsed("team", team.getName()));
                return true;
            }
            case "forcekick": {
                if (args.length < 2) {
                    plugin.getMessages().send(sender, "admin.usage");
                    return true;
                }
                OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
                Team team = plugin.getTeamManager().getTeamOf(target.getUniqueId());
                if (team == null) {
                    plugin.getMessages().send(sender, "error.not-in-team");
                    return true;
                }
                if (team.isOwner(target.getUniqueId())) {
                    plugin.getMessages().send(sender, "admin.cannot-kick-owner");
                    return true;
                }
                plugin.getTeamManager().removeMember(team, target.getUniqueId());
                plugin.getMessages().send(sender, "admin.forcekick",
                        Placeholder.unparsed("target", args[1]));
                return true;
            }
            case "spy": {
                if (!(sender instanceof Player)) {
                    plugin.getMessages().send(sender, "error.player-only");
                    return true;
                }
                boolean on = plugin.getChatService().toggleSpy(((Player) sender).getUniqueId());
                plugin.getMessages().send(sender, on ? "admin.spy-on" : "admin.spy-off");
                return true;
            }
            default:
                plugin.getMessages().send(sender, "admin.usage");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String label, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String option : Arrays.asList("reload", "disband", "forcejoin", "forcekick", "spy")) {
                if (option.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    result.add(option);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("disband"))) {
            for (Team team : plugin.getTeamManager().getTeams()) {
                result.add(team.getName());
            }
        }
        return result;
    }
}
