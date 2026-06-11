package de.teamforge.command;

import de.teamforge.TeamForgePlugin;
import de.teamforge.config.ConfigManager;
import de.teamforge.config.Messages;
import de.teamforge.manager.TeamManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public abstract class SubCommand {

    protected final TeamForgePlugin plugin;
    private final String name;
    private final String permission;
    private final boolean playerOnly;
    private final String[] aliases;

    protected SubCommand(TeamForgePlugin plugin, String name, String permission,
                         boolean playerOnly, String... aliases) {
        this.plugin = plugin;
        this.name = name;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }

    public String[] getAliases() {
        return aliases;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public List<String> tab(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    protected Messages msg() {
        return plugin.getMessages();
    }

    protected ConfigManager cfg() {
        return plugin.getConfigManager();
    }

    protected TeamManager teams() {
        return plugin.getTeamManager();
    }
}
