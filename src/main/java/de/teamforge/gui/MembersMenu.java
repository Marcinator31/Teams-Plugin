package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import de.teamforge.model.TeamRole;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MembersMenu extends PaginatedMenu<UUID> {

    private final Team team;

    public MembersMenu(TeamForgePlugin plugin, org.bukkit.entity.Player viewer, Team team) {
        super(plugin, viewer);
        this.team = team;
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.members.title");
    }

    @Override
    protected List<UUID> entries() {
        List<UUID> members = new ArrayList<>(team.getMembers().keySet());
        members.sort(Comparator.comparingInt((UUID u) ->
                team.getRole(u).getWeight()).reversed());
        return members;
    }

    @Override
    protected ItemStack render(UUID member) {
        TeamRole role = team.getRole(member);
        boolean online = Bukkit.getPlayer(member) != null;
        return ItemBuilder.of(Material.PLAYER_HEAD)
                .skull(member)
                .name(plugin.getMessages().get("gui.members.entry.name",
                        Placeholder.unparsed("player", team.getMemberName(member))))
                .lore(plugin.getMessages().getList("gui.members.entry.lore",
                        Placeholder.component("role", plugin.getMessages().get(role.getKey())),
                        Placeholder.unparsed("status", plugin.getMessages().raw(
                                online ? "word.online" : "word.offline"))))
                .build();
    }

    @Override
    protected void onEntryClick(UUID member, InventoryClickEvent event) {
        if (plugin.getTeamManager().canKick(team, viewer.getUniqueId(), member)
                || (team.isOwner(viewer.getUniqueId()) && !member.equals(viewer.getUniqueId()))) {
            clickSound();
            new MemberActionsMenu(plugin, viewer, team, member).open();
        } else {
            errorSound();
        }
    }

    @Override
    protected void buildExtras() {
        backButton(45, () -> new MainMenu(plugin, viewer, team).open());
    }
}
