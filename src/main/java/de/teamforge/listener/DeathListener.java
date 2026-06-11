package de.teamforge.listener;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final TeamForgePlugin plugin;

    public DeathListener(TeamForgePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().trackKills) {
            return;
        }
        Player victim = event.getEntity();
        Team victimTeam = plugin.getTeamManager().getTeam(victim);
        if (victimTeam != null) {
            victimTeam.setDeaths(victimTeam.getDeaths() + 1);
            plugin.getTeamManager().markDirty();
        }
        Player killer = victim.getKiller();
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            Team killerTeam = plugin.getTeamManager().getTeam(killer);
            if (killerTeam != null && (victimTeam == null
                    || !killerTeam.getId().equals(victimTeam.getId()))) {
                killerTeam.setKills(killerTeam.getKills() + 1);
                plugin.getTeamManager().markDirty();
            }
        }
    }
}
