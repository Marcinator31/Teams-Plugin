package de.teamforge.manager;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import de.teamforge.model.TeamRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all teams in memory along with indices and core logic.
 */
public class TeamManager {

    private final TeamForgePlugin plugin;
    private final Map<UUID, Team> teams = new HashMap<>();
    private final Map<String, UUID> byName = new HashMap<>();
    private final Map<String, UUID> byTag = new HashMap<>();
    private final Map<UUID, UUID> playerTeams = new HashMap<>();

    private boolean dirty;

    public TeamManager(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        teams.clear();
        byName.clear();
        byTag.clear();
        playerTeams.clear();
        for (Team team : plugin.getStorage().load()) {
            index(team);
        }
        plugin.getLogger().info(teams.size() + " teams loaded.");
    }

    private void index(Team team) {
        teams.put(team.getId(), team);
        byName.put(team.getName().toLowerCase(Locale.ROOT), team.getId());
        byTag.put(team.getTag().toLowerCase(Locale.ROOT), team.getId());
        for (UUID member : team.getMembers().keySet()) {
            playerTeams.put(member, team.getId());
        }
    }

    public Collection<Team> getTeams() {
        return teams.values();
    }

    public Team getTeam(UUID id) {
        return teams.get(id);
    }

    public Team getTeamByName(String name) {
        UUID id = byName.get(name.toLowerCase(Locale.ROOT));
        return id != null ? teams.get(id) : null;
    }

    public Team getTeamByTag(String tag) {
        UUID id = byTag.get(tag.toLowerCase(Locale.ROOT));
        return id != null ? teams.get(id) : null;
    }

    public Team getTeam(Player player) {
        return getTeamOf(player.getUniqueId());
    }

    public Team getTeamOf(UUID player) {
        UUID id = playerTeams.get(player);
        return id != null ? teams.get(id) : null;
    }

    public boolean sameTeam(UUID a, UUID b) {
        UUID ta = playerTeams.get(a);
        UUID tb = playerTeams.get(b);
        return ta != null && ta.equals(tb);
    }

    // ------------------------------------------------------------
    // Validation -> returns a message key or null if ok
    // ------------------------------------------------------------

    public String validateName(String name) {
        if (name.length() < plugin.getConfigManager().nameMin
                || name.length() > plugin.getConfigManager().nameMax) {
            return "create.name-invalid-length";
        }
        if (!plugin.getConfigManager().namePattern.matcher(name).matches()) {
            return "create.name-invalid-chars";
        }
        if (plugin.getConfigManager().blockedNames.contains(name.toLowerCase(Locale.ROOT))) {
            return "create.name-blocked";
        }
        if (byName.containsKey(name.toLowerCase(Locale.ROOT))) {
            return "create.name-taken";
        }
        return null;
    }

    public String validateTag(String tag) {
        if (tag.length() < plugin.getConfigManager().tagMin
                || tag.length() > plugin.getConfigManager().tagMax) {
            return "create.tag-invalid-length";
        }
        if (!plugin.getConfigManager().tagPattern.matcher(tag).matches()) {
            return "create.tag-invalid-chars";
        }
        if (byTag.containsKey(tag.toLowerCase(Locale.ROOT))) {
            return "create.tag-taken";
        }
        return null;
    }

    // ------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------

    public Team createTeam(Player owner, String name, String tag) {
        Team team = new Team(UUID.randomUUID(), name, tag, owner.getUniqueId());
        team.setColor(plugin.getConfigManager().defaultColor);
        team.setFriendlyFire(plugin.getConfigManager().ffDefault);
        team.putMember(owner.getUniqueId(), TeamRole.OWNER, owner.getName());
        index(team);
        recalcMaxMembers(team, true);
        plugin.getScoreboardService().applyPlayer(owner);
        markDirty();
        return team;
    }

    public void disband(Team team) {
        for (UUID member : new ArrayList<>(team.getMembers().keySet())) {
            playerTeams.remove(member);
            plugin.getChatService().clearPlayer(member);
        }
        // remove from alliances
        for (Team other : teams.values()) {
            other.getAllies().remove(team.getId());
            other.getAllyRequests().remove(team.getId());
        }
        byName.remove(team.getName().toLowerCase(Locale.ROOT));
        byTag.remove(team.getTag().toLowerCase(Locale.ROOT));
        teams.remove(team.getId());
        plugin.getScoreboardService().remove(team);
        markDirty();
    }

    public void addMember(Team team, Player player) {
        team.putMember(player.getUniqueId(), TeamRole.MEMBER, player.getName());
        playerTeams.put(player.getUniqueId(), team.getId());
        // remove invites everywhere
        for (Team other : teams.values()) {
            other.getInvites().remove(player.getUniqueId());
        }
        plugin.getScoreboardService().applyPlayer(player);
        if (plugin.getGlowService().isActive()) {
            plugin.getGlowService().refreshViewer(player);
            plugin.getGlowService().refreshTeam(team);
        }
        markDirty();
    }

    public void removeMember(Team team, UUID player) {
        String name = team.getMemberName(player);
        team.removeMemberInternal(player);
        playerTeams.remove(player);
        plugin.getChatService().clearPlayer(player);
        Player online = Bukkit.getPlayer(player);
        if (online != null) {
            plugin.getScoreboardService().applyPlayer(online);
        } else {
            plugin.getScoreboardService().removeEntryName(name);
        }
        if (plugin.getGlowService().isActive()) {
            if (online != null) {
                plugin.getGlowService().refreshViewer(online);
                plugin.getGlowService().refreshFor(online);
            }
            plugin.getGlowService().refreshTeam(team);
        }
        markDirty();
    }

    public void setRole(Team team, UUID player, TeamRole role) {
        team.getMembers().put(player, role);
        markDirty();
    }

    public void transferOwnership(Team team, UUID newOwner) {
        UUID oldOwner = team.getOwner();
        team.getMembers().put(oldOwner, TeamRole.OFFICER);
        team.getMembers().put(newOwner, TeamRole.OWNER);
        team.setOwner(newOwner);
        Player online = Bukkit.getPlayer(newOwner);
        recalcMaxMembers(team, online != null);
        markDirty();
    }

    public void rename(Team team, String name) {
        byName.remove(team.getName().toLowerCase(Locale.ROOT));
        team.setName(name);
        byName.put(name.toLowerCase(Locale.ROOT), team.getId());
        markDirty();
    }

    public void setTag(Team team, String tag) {
        byTag.remove(team.getTag().toLowerCase(Locale.ROOT));
        team.setTag(tag);
        byTag.put(tag.toLowerCase(Locale.ROOT), team.getId());
        plugin.getScoreboardService().updateMeta(team);
        markDirty();
    }

    public void setColor(Team team, NamedTextColor color) {
        team.setColor(color);
        plugin.getScoreboardService().updateMeta(team);
        markDirty();
    }

    // ------------------------------------------------------------
    // Einladungen
    // ------------------------------------------------------------

    public void addInvite(Team team, UUID player) {
        long expiry = System.currentTimeMillis() + plugin.getConfigManager().inviteExpirySeconds * 1000L;
        team.getInvites().put(player, expiry);
        markDirty();
    }

    public void removeInvite(Team team, UUID player) {
        team.getInvites().remove(player);
    }

    public boolean hasInvite(Team team, UUID player) {
        Long expiry = team.getInvites().get(player);
        if (expiry == null) {
            return false;
        }
        if (expiry < System.currentTimeMillis()) {
            team.getInvites().remove(player);
            return false;
        }
        return true;
    }

    public List<Team> getInvitesFor(UUID player) {
        List<Team> result = new ArrayList<>();
        for (Team team : teams.values()) {
            if (hasInvite(team, player)) {
                result.add(team);
            }
        }
        return result;
    }

    // ------------------------------------------------------------
    // Allianzen
    // ------------------------------------------------------------

    public boolean areAllied(Team a, Team b) {
        return a != null && b != null && a.getAllies().contains(b.getId());
    }

    public void requestAlly(Team from, Team to) {
        to.getAllyRequests().add(from.getId());
        markDirty();
    }

    public boolean hasAllyRequest(Team team, Team from) {
        return team.getAllyRequests().contains(from.getId());
    }

    public void acceptAlly(Team team, Team other) {
        team.getAllies().add(other.getId());
        other.getAllies().add(team.getId());
        team.getAllyRequests().remove(other.getId());
        other.getAllyRequests().remove(team.getId());
        markDirty();
    }

    public void removeAlly(Team team, Team other) {
        team.getAllies().remove(other.getId());
        other.getAllies().remove(team.getId());
        markDirty();
    }

    public void removeAllyRequest(Team team, Team from) {
        team.getAllyRequests().remove(from.getId());
        markDirty();
    }

    // ------------------------------------------------------------
    // Member limit via permissions
    // ------------------------------------------------------------

    public void recalcMaxMembers(Team team, boolean ownerOnline) {
        int limit = plugin.getConfigManager().defaultMaxMembers;
        Player owner = Bukkit.getPlayer(team.getOwner());
        if (ownerOnline && owner != null) {
            for (int value : plugin.getConfigManager().memberLimits) {
                if (owner.hasPermission("teams.limit." + value)) {
                    limit = Math.max(limit, value);
                    break;
                }
            }
        }
        team.setMaxMembers(Math.max(limit, team.getMembers().size()));
    }

    // ------------------------------------------------------------
    // Permission helpers
    // ------------------------------------------------------------

    public boolean canInvite(Team team, UUID player) {
        TeamRole role = team.getRole(player);
        if (role == null) {
            return false;
        }
        if (role == TeamRole.OWNER) {
            return true;
        }
        return role == TeamRole.OFFICER && plugin.getConfigManager().officersCanInvite;
    }

    public boolean canKick(Team team, UUID by, UUID target) {
        TeamRole byRole = team.getRole(by);
        TeamRole targetRole = team.getRole(target);
        if (byRole == null || targetRole == null || by.equals(target)) {
            return false;
        }
        if (!byRole.isAtLeast(TeamRole.OFFICER) || byRole.getWeight() <= targetRole.getWeight()) {
            return false;
        }
        if (byRole == TeamRole.OWNER) {
            return true;
        }
        return plugin.getConfigManager().officersCanKick;
    }

    public boolean canEditSettings(Team team, UUID player) {
        TeamRole role = team.getRole(player);
        if (role == null) {
            return false;
        }
        if (role == TeamRole.OWNER) {
            return true;
        }
        return role == TeamRole.OFFICER && plugin.getConfigManager().officersCanEditSettings;
    }

    public boolean canManageAllies(Team team, UUID player) {
        TeamRole role = team.getRole(player);
        if (role == null) {
            return false;
        }
        if (role == TeamRole.OWNER) {
            return true;
        }
        return role == TeamRole.OFFICER && plugin.getConfigManager().officersCanManageAllies;
    }

    public boolean canSetHome(Team team, UUID player) {
        TeamRole role = team.getRole(player);
        if (role == null) {
            return false;
        }
        if (role == TeamRole.OWNER) {
            return true;
        }
        return role == TeamRole.OFFICER && plugin.getConfigManager().officersCanSetHome;
    }

    public boolean canWithdrawBank(Team team, UUID player) {
        TeamRole role = team.getRole(player);
        return role != null && role.isAtLeast(plugin.getConfigManager().bankWithdrawMinRole);
    }

    // ------------------------------------------------------------
    // Misc
    // ------------------------------------------------------------

    public void broadcast(Team team, Component message) {
        for (Player player : team.getOnlineMembers()) {
            player.sendMessage(message);
        }
    }

    public void pruneInvites() {
        long now = System.currentTimeMillis();
        for (Team team : teams.values()) {
            Iterator<Map.Entry<UUID, Long>> it = team.getInvites().entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue() < now) {
                    it.remove();
                }
            }
        }
    }

    public void markDirty() {
        dirty = true;
    }

    public void saveNow(boolean async) {
        plugin.getStorage().save(teams, async);
        dirty = false;
    }

    public void autosaveTick() {
        if (dirty) {
            saveNow(true);
        }
    }
}
