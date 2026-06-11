package de.teamforge.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Datenmodell eines Teams. Reine Datenklasse - Logik liegt im TeamManager.
 */
public class Team {

    private final UUID id;
    private String name;
    private String tag;
    private String description;
    private NamedTextColor color;
    private UUID owner;

    /** Mitglieder mit Rolle (Reihenfolge = Beitrittsreihenfolge) */
    private final Map<UUID, TeamRole> members = new LinkedHashMap<>();
    /** Last known name per member (for offline display) */
    private final Map<UUID, String> memberNames = new HashMap<>();
    /** Pending invites: player -> expiry timestamp (ms) */
    private final Map<UUID, Long> invites = new HashMap<>();
    /** Allied teams */
    private final Set<UUID> allies = new HashSet<>();
    /** Eingehende Allianz-Anfragen anderer Teams */
    private final Set<UUID> allyRequests = new HashSet<>();

    private Location home;
    private boolean friendlyFire;
    private boolean open;
    private double bank;
    private int kills;
    private int deaths;
    private long created;
    private Material icon = Material.WHITE_BANNER;
    private int maxMembers;

    public Team(UUID id, String name, String tag, UUID owner) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.owner = owner;
        this.description = "";
        this.color = NamedTextColor.GRAY;
        this.created = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public void setColor(NamedTextColor color) {
        this.color = color;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Map<UUID, TeamRole> getMembers() {
        return members;
    }

    public Map<UUID, String> getMemberNames() {
        return memberNames;
    }

    public Map<UUID, Long> getInvites() {
        return invites;
    }

    public Set<UUID> getAllies() {
        return allies;
    }

    public Set<UUID> getAllyRequests() {
        return allyRequests;
    }

    public Location getHome() {
        return home;
    }

    public void setHome(Location home) {
        this.home = home;
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public double getBank() {
        return bank;
    }

    public void setBank(double bank) {
        this.bank = bank;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    // ------------------------------------------------------------
    // Hilfsmethoden
    // ------------------------------------------------------------

    /** Team-Name in Team-Farbe als Component */
    public Component displayName() {
        return Component.text(name, color);
    }

    /** Tag in square brackets, colored: [TAG] */
    public Component coloredTag() {
        return Component.text("[" + tag + "]", color);
    }

    public TeamRole getRole(UUID player) {
        return members.get(player);
    }

    public boolean isMember(UUID player) {
        return members.containsKey(player);
    }

    public boolean isOwner(UUID player) {
        return owner.equals(player);
    }

    public void putMember(UUID player, TeamRole role, String name) {
        members.put(player, role);
        if (name != null) {
            memberNames.put(player, name);
        }
    }

    public void removeMemberInternal(UUID player) {
        members.remove(player);
        memberNames.remove(player);
    }

    public List<Player> getOnlineMembers() {
        List<Player> online = new ArrayList<>();
        for (UUID uuid : members.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }

    /** Letzter bekannter Name eines Mitglieds (Fallback: "Unbekannt") */
    public String getMemberName(UUID player) {
        String cached = memberNames.get(player);
        if (cached != null) {
            return cached;
        }
        Player online = Bukkit.getPlayer(player);
        if (online != null) {
            return online.getName();
        }
        return "Unbekannt";
    }
}
