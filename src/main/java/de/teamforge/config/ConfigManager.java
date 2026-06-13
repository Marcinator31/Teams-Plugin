package de.teamforge.config;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.TeamRole;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Loads config.yml into typed fields. Just call reload() to re-read.
 */
public class ConfigManager {

    private final TeamForgePlugin plugin;

    // storage
    public int autosaveMinutes;

    // team
    public int nameMin;
    public int nameMax;
    public Pattern namePattern;
    public int tagMin;
    public int tagMax;
    public Pattern tagPattern;
    public int descMax;
    public List<String> blockedNames = new ArrayList<>();
    public int defaultMaxMembers;
    public List<Integer> memberLimits = new ArrayList<>();
    public boolean allowColor;
    public NamedTextColor defaultColor;
    public boolean openTeamsAllowed;

    // friendly fire
    public boolean ffDefault;
    public boolean ffToggleable;
    public boolean protectAllies;
    public boolean blockSplashPotions;

    // chat
    public boolean teamChatEnabled;
    public boolean allyChatEnabled;
    public boolean decoratePublicChat;

    // home
    public boolean homeEnabled;
    public int homeWarmup;
    public int homeCooldown;
    public boolean homeCancelOnMove;
    public boolean homeCancelOnDamage;
    public List<String> homeDisabledWorlds = new ArrayList<>();

    // invites
    public int inviteExpirySeconds;

    // allies
    public boolean alliesEnabled;
    public int maxAllies;

    // bank
    public boolean bankEnabled;
    public double bankMax;
    public TeamRole bankWithdrawMinRole;

    // costs
    public double costCreate;
    public double costRename;
    public double costTag;
    public double costSetHome;

    // nametags
    public boolean nametagsEnabled;
    public boolean nametagShowTag;
    public boolean nametagColor;

    // broadcasts
    public boolean broadcastCreate;
    public boolean broadcastDisband;

    // stats
    public boolean trackKills;

    // notifications
    public boolean notifyMemberJoin;

    // officers
    public boolean officersCanInvite;
    public boolean officersCanKick;
    public boolean officersCanSetHome;
    public boolean officersCanEditSettings;
    public boolean officersCanManageAllies;

    public ConfigManager(TeamForgePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        autosaveMinutes = cfg.getInt("storage.autosave-minutes", 5);

        nameMin = cfg.getInt("team.name.min-length", 3);
        nameMax = cfg.getInt("team.name.max-length", 16);
        namePattern = compile(cfg.getString("team.name.pattern", "^[A-Za-z0-9_]+$"), "^[A-Za-z0-9_]+$");
        tagMin = cfg.getInt("team.tag.min-length", 2);
        tagMax = cfg.getInt("team.tag.max-length", 6);
        tagPattern = compile(cfg.getString("team.tag.pattern", "^[A-Za-z0-9]+$"), "^[A-Za-z0-9]+$");
        descMax = cfg.getInt("team.description-max-length", 64);
        blockedNames = lower(cfg.getStringList("team.blocked-names"));
        defaultMaxMembers = cfg.getInt("team.max-members.default", 8);
        memberLimits = new ArrayList<>(cfg.getIntegerList("team.max-members.limits"));
        Collections.sort(memberLimits, Collections.reverseOrder());
        allowColor = cfg.getBoolean("team.allow-color", true);
        NamedTextColor parsed = NamedTextColor.NAMES.value(
                cfg.getString("team.default-color", "gray").toLowerCase(Locale.ROOT));
        defaultColor = parsed != null ? parsed : NamedTextColor.GRAY;
        openTeamsAllowed = cfg.getBoolean("team.open-teams-allowed", true);

        ffDefault = cfg.getBoolean("friendly-fire.default", false);
        ffToggleable = cfg.getBoolean("friendly-fire.allow-toggle", true);
        protectAllies = cfg.getBoolean("friendly-fire.protect-allies", true);
        blockSplashPotions = cfg.getBoolean("friendly-fire.block-splash-potions", true);

        teamChatEnabled = cfg.getBoolean("chat.team-chat-enabled", true);
        allyChatEnabled = cfg.getBoolean("chat.ally-chat-enabled", true);
        decoratePublicChat = cfg.getBoolean("chat.decorate-public-chat", true);

        homeEnabled = cfg.getBoolean("home.enabled", true);
        homeWarmup = cfg.getInt("home.warmup-seconds", 3);
        homeCooldown = cfg.getInt("home.cooldown-seconds", 30);
        homeCancelOnMove = cfg.getBoolean("home.cancel-on-move", true);
        homeCancelOnDamage = cfg.getBoolean("home.cancel-on-damage", true);
        homeDisabledWorlds = lower(cfg.getStringList("home.disabled-worlds"));

        inviteExpirySeconds = cfg.getInt("invites.expiry-seconds", 120);

        alliesEnabled = cfg.getBoolean("allies.enabled", true);
        maxAllies = cfg.getInt("allies.max-allies", 3);

        bankEnabled = cfg.getBoolean("bank.enabled", true);
        bankMax = cfg.getDouble("bank.max-balance", 1000000);
        bankWithdrawMinRole = TeamRole.fromString(
                cfg.getString("bank.withdraw-min-role", "OFFICER"), TeamRole.OFFICER);

        costCreate = cfg.getDouble("costs.create", 0);
        costRename = cfg.getDouble("costs.rename", 0);
        costTag = cfg.getDouble("costs.set-tag", 0);
        costSetHome = cfg.getDouble("costs.set-home", 0);

        nametagsEnabled = cfg.getBoolean("nametags.enabled", true);
        nametagShowTag = cfg.getBoolean("nametags.show-tag", true);
        nametagColor = cfg.getBoolean("nametags.color-names", true);

        broadcastCreate = cfg.getBoolean("broadcasts.team-create", true);
        broadcastDisband = cfg.getBoolean("broadcasts.team-disband", true);

        trackKills = cfg.getBoolean("stats.track-kills", true);

        notifyMemberJoin = cfg.getBoolean("notifications.member-join-server", true);

        officersCanInvite = cfg.getBoolean("officers.can-invite", true);
        officersCanKick = cfg.getBoolean("officers.can-kick", true);
        officersCanSetHome = cfg.getBoolean("officers.can-set-home", true);
        officersCanEditSettings = cfg.getBoolean("officers.can-edit-settings", true);
        officersCanManageAllies = cfg.getBoolean("officers.can-manage-allies", true);
    }

    private Pattern compile(String pattern, String fallback) {
        try {
            return Pattern.compile(pattern);
        } catch (Exception ex) {
            plugin.getLogger().warning("Invalid regex pattern in config: " + pattern);
            return Pattern.compile(fallback);
        }
    }

    private List<String> lower(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String entry : list) {
            result.add(entry.toLowerCase(Locale.ROOT));
        }
        return result;
    }
}
