package de.teamforge.manager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sends per-viewer entity metadata so that team members appear glowing
 * only to their own teammates. Opponents never see the glow.
 * Requires ProtocolLib. If ProtocolLib is absent, this service does nothing.
 */
public class GlowService {

    private final TeamForgePlugin plugin;
    private final boolean active;
    private ProtocolManager protocol;

    // Index 0 = shared entity flags byte; bit 0x40 = glowing
    private static final int FLAGS_INDEX = 0;
    private static final byte GLOWING_BIT = 0x40;

    public GlowService(TeamForgePlugin plugin) {
        this.plugin = plugin;
        boolean ok = false;
        if (plugin.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            try {
                this.protocol = ProtocolLibrary.getProtocolManager();
                ok = true;
            } catch (Throwable t) {
                plugin.getLogger().warning("ProtocolLib present but could not be initialised: " + t.getMessage());
            }
        }
        this.active = ok;
        if (active) {
            plugin.getLogger().info("ProtocolLib hooked - team glow enabled.");
        }
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Refresh the glow state of one target for every online viewer.
     */
    public void refreshFor(Player target) {
        if (!active) {
            return;
        }
        Team targetTeam = plugin.getTeamManager().getTeam(target);
        boolean targetGlowEnabled = targetTeam != null && targetTeam.isGlow();
        byte baseFlags = entityFlags(target);
        for (Player viewer : plugin.getServer().getOnlinePlayers()) {
            if (viewer.equals(target)) {
                continue;
            }
            boolean glow = targetGlowEnabled
                    && targetTeam.isMember(viewer.getUniqueId());
            sendFlags(viewer, target, glow ? (byte) (baseFlags | GLOWING_BIT) : baseFlags);
        }
    }

    /**
     * Refresh how this viewer sees every other online player.
     */
    public void refreshViewer(Player viewer) {
        if (!active) {
            return;
        }
        for (Player target : plugin.getServer().getOnlinePlayers()) {
            if (target.equals(viewer)) {
                continue;
            }
            Team targetTeam = plugin.getTeamManager().getTeam(target);
            boolean glow = targetTeam != null && targetTeam.isGlow()
                    && targetTeam.isMember(viewer.getUniqueId());
            byte flags = entityFlags(target);
            sendFlags(viewer, target, glow ? (byte) (flags | GLOWING_BIT) : flags);
        }
    }

    /**
     * Refresh the glow for an entire team (e.g. after toggling the setting).
     */
    public void refreshTeam(Team team) {
        if (!active) {
            return;
        }
        for (Player member : team.getOnlineMembers()) {
            refreshFor(member);
        }
    }

    public void refreshAll() {
        if (!active) {
            return;
        }
        for (Player viewer : plugin.getServer().getOnlinePlayers()) {
            refreshViewer(viewer);
        }
    }

    private byte entityFlags(Player player) {
        byte flags = 0;
        if (player.getFireTicks() > 0) {
            flags |= 0x01;
        }
        if (player.isSneaking()) {
            flags |= 0x02;
        }
        if (player.isSprinting()) {
            flags |= 0x08;
        }
        if (player.isSwimming()) {
            flags |= 0x10;
        }
        if (player.isInvisible()) {
            flags |= 0x20;
        }
        if (player.isGlowing()) {
            flags |= GLOWING_BIT;
        }
        if (player.isGliding()) {
            flags |= (byte) 0x80;
        }
        return flags;
    }

    private void sendFlags(Player viewer, Player target, byte flags) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, target.getEntityId());

        WrappedDataWatcher.Serializer byteSerializer =
                WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataValue value = new WrappedDataValue(
                FLAGS_INDEX, byteSerializer, flags);
        List<WrappedDataValue> values = new ArrayList<>(Collections.singletonList(value));
        packet.getDataValueCollectionModifier().write(0, values);

        try {
            protocol.sendServerPacket(viewer, packet);
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to send glow packet: " + ex.getMessage());
        }
    }
}
