package de.teamforge.hook;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import de.teamforge.model.TeamRole;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PapiExpansion extends PlaceholderExpansion {

    private final TeamForgePlugin plugin;

    public PapiExpansion(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "teamforge";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TeamForge";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        Team team = plugin.getTeamManager().getTeamOf(player.getUniqueId());
        if (params.equalsIgnoreCase("has_team")) {
            return team != null ? "true" : "false";
        }
        if (team == null) {
            return "";
        }
        switch (params.toLowerCase()) {
            case "name":
                return team.getName();
            case "tag":
                return team.getTag();
            case "tag_colored":
                return LegacyComponentSerializer.legacySection().serialize(team.coloredTag());
            case "color":
                return team.getColor().toString();
            case "role":
                TeamRole role = team.getRole(player.getUniqueId());
                return role != null ? role.name() : "";
            case "members":
                return String.valueOf(team.getMembers().size());
            case "members_max":
                return String.valueOf(team.getMaxMembers());
            case "online":
                return String.valueOf(team.getOnlineMembers().size());
            case "owner":
                return team.getMemberName(team.getOwner());
            case "bank":
                return plugin.formatMoney(team.getBank());
            case "kills":
                return String.valueOf(team.getKills());
            case "deaths":
                return String.valueOf(team.getDeaths());
            case "friendlyfire":
                return String.valueOf(team.isFriendlyFire());
            case "description":
                return team.getDescription();
            default:
                return "";
        }
    }
}
