package de.teamforge.hook;

import de.teamforge.TeamForgePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * Optional integration with AdvancedTeleport (AT).
 *
 * When AT is installed, the team-home teleport is routed through AT so that
 * AT's warmup runs and addons such as ATAddon display their hotbar countdown.
 *
 * Everything is done via reflection so TeamForge keeps compiling and running
 * without AdvancedTeleport on the classpath. Only methods whose signatures
 * were verified against AT's public API are used:
 *   ATPlayer.getPlayer(Player) -> ATPlayer
 *   ATPlayer.hasHome(String) -> boolean
 *   ATPlayer.addHome(String, Location, Player) -> CompletableFuture
 *   ATPlayer.moveHome(String, Location, CommandSender) -> CompletableFuture
 *
 * The teleport itself is then triggered with AT's own command
 * (advancedteleport:home &lt;name&gt;), which is exactly what ATAddon intercepts
 * to start its countdown.
 */
public class ATHook {

    /** Reserved internal home name used only to route team-home teleports. */
    private static final String RESERVED_HOME = "tfteamhome";

    private final TeamForgePlugin plugin;
    private boolean active;

    private Method mGetPlayer;
    private Method mHasHome;
    private Method mAddHome;
    private Method mMoveHome;

    public ATHook(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("AdvancedTeleport") == null) {
            return false;
        }
        try {
            Class<?> atPlayer = Class.forName(
                    "io.github.niestrat99.advancedteleport.api.ATPlayer");
            mGetPlayer = atPlayer.getMethod("getPlayer", Player.class);
            mHasHome = atPlayer.getMethod("hasHome", String.class);
            mAddHome = atPlayer.getMethod("addHome", String.class, Location.class, Player.class);
            mMoveHome = atPlayer.getMethod("moveHome", String.class, Location.class,
                    org.bukkit.command.CommandSender.class);
            active = true;
            plugin.getLogger().info("AdvancedTeleport hooked - team home uses AT warmup/countdown.");
        } catch (Throwable t) {
            active = false;
            plugin.getLogger().warning("Could not hook AdvancedTeleport: " + t.getMessage());
        }
        return active;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Routes the team-home teleport through AdvancedTeleport.
     * Returns true if AT took over (so TeamForge should not teleport itself).
     */
    public boolean teleportToTeamHome(Player player, Location home) {
        if (!active) {
            return false;
        }
        try {
            Object atPlayer = mGetPlayer.invoke(null, player);
            if (atPlayer == null) {
                return false;
            }
            boolean exists = (Boolean) mHasHome.invoke(atPlayer, RESERVED_HOME);
            if (exists) {
                mMoveHome.invoke(atPlayer, RESERVED_HOME, home, player);
            } else {
                mAddHome.invoke(atPlayer, RESERVED_HOME, home, player);
            }
            // Run the AT command on the next tick so the home write has settled.
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.performCommand("advancedteleport:home " + RESERVED_HOME));
            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("AT team-home teleport failed, using fallback: " + t.getMessage());
            return false;
        }
    }
}
