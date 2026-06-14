package de.teamforge.command;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.sub.*;
import de.teamforge.gui.MainMenu;
import de.teamforge.gui.NoTeamMenu;
import de.teamforge.model.Team;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeamCommand implements TabExecutor {

    private final TeamForgePlugin plugin;
    private final Map<String, SubCommand> registry = new LinkedHashMap<>();
    private final Map<String, SubCommand> aliases = new LinkedHashMap<>();

    public TeamCommand(TeamForgePlugin plugin) {
        this.plugin = plugin;
        register(new CreateCommand(plugin));
        register(new DisbandCommand(plugin));
        register(new InviteCommand(plugin));
        register(new UninviteCommand(plugin));
        register(new JoinCommand(plugin));
        register(new DenyCommand(plugin));
        register(new LeaveCommand(plugin));
        register(new KickCommand(plugin));
        register(new PromoteCommand(plugin));
        register(new DemoteCommand(plugin));
        register(new TransferCommand(plugin));
        register(new InfoCommand(plugin));
        register(new ListCommand(plugin));
        register(new TopCommand(plugin));
        register(new ChatCommand(plugin));
        register(new AllyChatCommand(plugin));
        register(new SetHomeCommand(plugin));
        register(new HomeCommand(plugin));
        register(new DelHomeCommand(plugin));
        register(new AllyCommand(plugin));
        register(new UnallyCommand(plugin));
        register(new RenameCommand(plugin));
        register(new TagCommand(plugin));
        register(new DescCommand(plugin));
        register(new ColorCommand(plugin));
        register(new PvpCommand(plugin));
        register(new OpenCommand(plugin));
        register(new BankCommand(plugin));
        register(new GuiCommand(plugin));
        register(new InvitesCommand(plugin));
        register(new HelpCommand(plugin));
    }

    private void register(SubCommand command) {
        registry.put(command.getName().toLowerCase(Locale.ROOT), command);
        for (String alias : command.getAliases()) {
            aliases.put(alias.toLowerCase(Locale.ROOT), command);
        }
    }

    private SubCommand find(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        SubCommand command = registry.get(key);
        return command != null ? command : aliases.get(key);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("teams.use")) {
            plugin.getMessages().send(sender, "error.no-permission");
            return true;
        }
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Team team = plugin.getTeamManager().getTeam(player);
                if (team != null) {
                    new MainMenu(plugin, player, team).open();
                } else {
                    new NoTeamMenu(plugin, player).open();
                }
            } else {
                find("help").execute(sender, new String[0]);
            }
            return true;
        }

        SubCommand command = find(args[0]);
        if (command == null) {
            plugin.getMessages().send(sender, "error.unknown-command");
            return true;
        }
        if (command.isPlayerOnly() && !(sender instanceof Player)) {
            plugin.getMessages().send(sender, "error.player-only");
            return true;
        }
        if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
            plugin.getMessages().send(sender, "error.no-permission");
            return true;
        }
        String[] sub = new String[args.length - 1];
        System.arraycopy(args, 1, sub, 0, sub.length);
        command.execute(sender, sub);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String label, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (SubCommand command : registry.values()) {
                if (command.getName().startsWith(prefix)
                        && (command.getPermission() == null || sender.hasPermission(command.getPermission()))) {
                    result.add(command.getName());
                }
            }
            return result;
        }
        SubCommand command = find(args[0]);
        if (command != null) {
            String[] sub = new String[args.length - 1];
            System.arraycopy(args, 1, sub, 0, sub.length);
            String prefix = sub[sub.length - 1].toLowerCase(Locale.ROOT);
            for (String option : command.tab(sender, sub)) {
                if (option.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    result.add(option);
                }
            }
        }
        return result;
    }
}
