package de.teamforge;

import de.teamforge.command.TeamAdminCommand;
import de.teamforge.command.TeamCommand;
import de.teamforge.config.ConfigManager;
import de.teamforge.config.Messages;
import de.teamforge.gui.MenuListener;
import de.teamforge.hook.PapiExpansion;
import de.teamforge.hook.VaultHook;
import de.teamforge.listener.ChatListener;
import de.teamforge.listener.CombatListener;
import de.teamforge.listener.ConnectionListener;
import de.teamforge.listener.DeathListener;
import de.teamforge.listener.MovementListener;
import de.teamforge.manager.ChatInputService;
import de.teamforge.manager.ChatService;
import de.teamforge.manager.ScoreboardService;
import de.teamforge.manager.TeamManager;
import de.teamforge.manager.TeleportService;
import de.teamforge.manager.YamlStorage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TeamForgePlugin extends JavaPlugin {

    private ConfigManager configManager;
    private Messages messages;
    private VaultHook vaultHook;
    private YamlStorage storage;
    private TeamManager teamManager;
    private ScoreboardService scoreboardService;
    private ChatService chatService;
    private ChatInputService chatInputService;
    private TeleportService teleportService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        configManager.reload();

        this.messages = new Messages(this);
        messages.reload();

        this.vaultHook = new VaultHook();
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            if (vaultHook.setup()) {
                getLogger().info("Vault hooked.");
            } else {
                getLogger().warning("Vault found, but no economy registered.");
            }
        }

        this.storage = new YamlStorage(this);
        this.teamManager = new TeamManager(this);
        this.scoreboardService = new ScoreboardService(this);
        this.chatService = new ChatService(this);
        this.chatInputService = new ChatInputService(this);
        this.teleportService = new TeleportService(this);

        teamManager.loadAll();

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);

        TeamCommand teamCommand = new TeamCommand(this);
        if (getCommand("team") != null) {
            getCommand("team").setExecutor(teamCommand);
            getCommand("team").setTabCompleter(teamCommand);
        }
        TeamAdminCommand adminCommand = new TeamAdminCommand(this);
        if (getCommand("teamadmin") != null) {
            getCommand("teamadmin").setExecutor(adminCommand);
            getCommand("teamadmin").setTabCompleter(adminCommand);
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PapiExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        int autosave = configManager.autosaveMinutes;
        if (autosave > 0) {
            long ticks = autosave * 1200L;
            getServer().getScheduler().runTaskTimer(this, () -> {
                teamManager.pruneInvites();
                teamManager.autosaveTick();
            }, ticks, ticks);
        }

        for (Player player : getServer().getOnlinePlayers()) {
            scoreboardService.applyPlayer(player);
        }

        getLogger().info("TeamForge enabled.");
    }

    @Override
    public void onDisable() {
        if (teamManager != null) {
            teamManager.saveNow(false);
        }
        if (scoreboardService != null) {
            scoreboardService.clearAll();
        }
        if (teleportService != null) {
            teleportService.cancelAll();
        }
    }

    // ------------------------------------------------------------
    // Getter
    // ------------------------------------------------------------

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Messages getMessages() {
        return messages;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public YamlStorage getStorage() {
        return storage;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public ScoreboardService getScoreboardService() {
        return scoreboardService;
    }

    public ChatService getChatService() {
        return chatService;
    }

    public ChatInputService getChatInputService() {
        return chatInputService;
    }

    public TeleportService getTeleportService() {
        return teleportService;
    }

    // ------------------------------------------------------------
    // Wirtschaft / Hilfsfunktionen
    // ------------------------------------------------------------

    public boolean isBankActive() {
        return configManager.bankEnabled && vaultHook.isActive();
    }

    public String formatMoney(double amount) {
        if (vaultHook.isActive()) {
            return vaultHook.format(amount);
        }
        return String.format(java.util.Locale.US, "%,.2f", amount);
    }

    public boolean charge(Player player, double amount) {
        if (amount <= 0) {
            return true;
        }
        if (!vaultHook.isActive()) {
            return true;
        }
        return vaultHook.has(player, amount) && vaultHook.withdraw(player, amount);
    }
}
