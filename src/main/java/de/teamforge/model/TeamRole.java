package de.teamforge.model;

/**
 * Rollen innerhalb eines Teams, sortiert nach Gewicht.
 */
public enum TeamRole {

    MEMBER(0, "member"),
    OFFICER(1, "officer"),
    OWNER(2, "owner");

    private final int weight;
    private final String key;

    TeamRole(int weight, String key) {
        this.weight = weight;
        this.key = key;
    }

    public int getWeight() {
        return weight;
    }

    /** Message-Key, z.B. "role.owner" */
    public String getKey() {
        return "role." + key;
    }

    public boolean isAtLeast(TeamRole other) {
        return this.weight >= other.weight;
    }

    public static TeamRole fromString(String input, TeamRole fallback) {
        if (input == null) {
            return fallback;
        }
        for (TeamRole role : values()) {
            if (role.name().equalsIgnoreCase(input)) {
                return role;
            }
        }
        return fallback;
    }
}
