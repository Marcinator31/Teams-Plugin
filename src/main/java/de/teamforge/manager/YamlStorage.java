package de.teamforge.manager;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import de.teamforge.model.TeamRole;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Persistenz der Teams in plugins/TeamForge/teams.yml.
 * Serialisierung erfolgt synchron, der Schreibvorgang optional asynchron.
 */
public class YamlStorage {

    private final TeamForgePlugin plugin;
    private final File file;

    public YamlStorage(TeamForgePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "teams.yml");
    }

    public List<Team> load() {
        List<Team> teams = new ArrayList<>();
        if (!file.exists()) {
            return teams;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            try {
                teams.add(loadTeam(key, section));
            } catch (Exception ex) {
                plugin.getLogger().warning("Could not load team '" + key + "': " + ex.getMessage());
            }
        }
        return teams;
    }

    private Team loadTeam(String key, ConfigurationSection section) {
        UUID id = UUID.fromString(key);
        String name = section.getString("name", "Team");
        String tag = section.getString("tag", "TEAM");
        UUID owner = UUID.fromString(section.getString("owner"));

        Team team = new Team(id, name, tag, owner);
        team.setDescription(section.getString("description", ""));
        NamedTextColor color = NamedTextColor.NAMES.value(
                section.getString("color", "gray").toLowerCase(Locale.ROOT));
        team.setColor(color != null ? color : NamedTextColor.GRAY);
        team.setFriendlyFire(section.getBoolean("friendly-fire", false));
        team.setOpen(section.getBoolean("open", false));
        team.setGlow(section.getBoolean("glow", true));
        team.setBank(section.getDouble("bank", 0));
        team.setKills(section.getInt("kills", 0));
        team.setDeaths(section.getInt("deaths", 0));
        team.setCreated(section.getLong("created", System.currentTimeMillis()));
        Material icon = Material.matchMaterial(section.getString("icon", "WHITE_BANNER"));
        if (icon != null) {
            team.setIcon(icon);
        }

        // Home
        ConfigurationSection homeSection = section.getConfigurationSection("home");
        if (homeSection != null) {
            World world = Bukkit.getWorld(homeSection.getString("world", ""));
            if (world != null) {
                team.setHome(new Location(world,
                        homeSection.getDouble("x"),
                        homeSection.getDouble("y"),
                        homeSection.getDouble("z"),
                        (float) homeSection.getDouble("yaw"),
                        (float) homeSection.getDouble("pitch")));
            }
        }

        // Mitglieder
        ConfigurationSection membersSection = section.getConfigurationSection("members");
        if (membersSection != null) {
            for (String memberKey : membersSection.getKeys(false)) {
                UUID member = UUID.fromString(memberKey);
                TeamRole role = TeamRole.fromString(
                        membersSection.getString(memberKey + ".role"), TeamRole.MEMBER);
                String memberName = membersSection.getString(memberKey + ".name");
                team.putMember(member, role, memberName);
            }
        }

        // Einladungen
        ConfigurationSection invitesSection = section.getConfigurationSection("invites");
        if (invitesSection != null) {
            long now = System.currentTimeMillis();
            for (String inviteKey : invitesSection.getKeys(false)) {
                long expiry = invitesSection.getLong(inviteKey);
                if (expiry > now) {
                    team.getInvites().put(UUID.fromString(inviteKey), expiry);
                }
            }
        }

        // Allianzen
        for (String ally : section.getStringList("allies")) {
            team.getAllies().add(UUID.fromString(ally));
        }
        for (String request : section.getStringList("ally-requests")) {
            team.getAllyRequests().add(UUID.fromString(request));
        }

        return team;
    }

    /**
     * Speichert alle Teams. Serialisierung synchron, Schreiben optional async.
     */
    public void save(Map<UUID, Team> teams, boolean async) {
        final YamlConfiguration yaml = new YamlConfiguration();
        for (Team team : teams.values()) {
            saveTeam(yaml, team);
        }
        if (async && plugin.isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> write(yaml));
        } else {
            write(yaml);
        }
    }

    private void write(YamlConfiguration yaml) {
        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save teams.yml: " + ex.getMessage());
        }
    }

    private void saveTeam(YamlConfiguration yaml, Team team) {
        String base = team.getId().toString();
        yaml.set(base + ".name", team.getName());
        yaml.set(base + ".tag", team.getTag());
        yaml.set(base + ".description", team.getDescription());
        yaml.set(base + ".color", team.getColor().toString());
        yaml.set(base + ".owner", team.getOwner().toString());
        yaml.set(base + ".friendly-fire", team.isFriendlyFire());
        yaml.set(base + ".open", team.isOpen());
        yaml.set(base + ".glow", team.isGlow());
        yaml.set(base + ".bank", team.getBank());
        yaml.set(base + ".kills", team.getKills());
        yaml.set(base + ".deaths", team.getDeaths());
        yaml.set(base + ".created", team.getCreated());
        yaml.set(base + ".icon", team.getIcon().name());

        Location home = team.getHome();
        if (home != null && home.getWorld() != null) {
            yaml.set(base + ".home.world", home.getWorld().getName());
            yaml.set(base + ".home.x", home.getX());
            yaml.set(base + ".home.y", home.getY());
            yaml.set(base + ".home.z", home.getZ());
            yaml.set(base + ".home.yaw", home.getYaw());
            yaml.set(base + ".home.pitch", home.getPitch());
        }

        for (Map.Entry<UUID, TeamRole> entry : team.getMembers().entrySet()) {
            String memberBase = base + ".members." + entry.getKey();
            yaml.set(memberBase + ".role", entry.getValue().name());
            yaml.set(memberBase + ".name", team.getMemberName(entry.getKey()));
        }

        for (Map.Entry<UUID, Long> entry : team.getInvites().entrySet()) {
            yaml.set(base + ".invites." + entry.getKey(), entry.getValue());
        }

        List<String> allies = new ArrayList<>();
        for (UUID ally : team.getAllies()) {
            allies.add(ally.toString());
        }
        yaml.set(base + ".allies", allies);

        List<String> requests = new ArrayList<>();
        for (UUID request : team.getAllyRequests()) {
            requests.add(request.toString());
        }
        yaml.set(base + ".ally-requests", requests);
    }
}
