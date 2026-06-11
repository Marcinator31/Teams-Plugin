package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class CreateCommand extends SubCommand {

    public CreateCommand(TeamForgePlugin plugin) {
        super(plugin, "create", "teams.create", true, "erstellen");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            msg().send(player, "create.usage");
            return;
        }
        String name = args[0];
        String tag = args.length >= 2 ? args[1] : defaultTag(name);
        tryCreate(plugin, player, name, tag);
    }

    private static String defaultTag(String name) {
        String tag = name.length() > 4 ? name.substring(0, 4) : name;
        return tag.toUpperCase(Locale.ROOT).replaceAll("[^A-Za-z0-9]", "");
    }

    public static boolean tryCreate(TeamForgePlugin plugin, Player player, String name, String tag) {
        if (plugin.getTeamManager().getTeam(player) != null) {
            plugin.getMessages().send(player, "error.already-in-team");
            return false;
        }
        String nameError = plugin.getTeamManager().validateName(name);
        if (nameError != null) {
            plugin.getMessages().send(player, nameError);
            return false;
        }
        String tagError = plugin.getTeamManager().validateTag(tag);
        if (tagError != null) {
            plugin.getMessages().send(player, tagError);
            return false;
        }
        if (!plugin.charge(player, plugin.getConfigManager().costCreate)) {
            plugin.getMessages().send(player, "bank.insufficient-player");
            return false;
        }
        Team team = plugin.getTeamManager().createTeam(player, name, tag);
        plugin.getMessages().send(player, "create.success",
                Placeholder.component("team", team.displayName()));
        if (plugin.getConfigManager().broadcastCreate) {
            plugin.getServer().sendMessage(plugin.getMessages().get("create.broadcast",
                    Placeholder.unparsed("player", player.getName()),
                    Placeholder.component("team", team.displayName())));
        }
        return true;
    }
}
