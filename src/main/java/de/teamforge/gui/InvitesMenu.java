package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.sub.DenyCommand;
import de.teamforge.command.sub.JoinCommand;
import de.teamforge.model.Team;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InvitesMenu extends PaginatedMenu<Team> {

    public InvitesMenu(TeamForgePlugin plugin, Player viewer) {
        super(plugin, viewer);
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.invites.title");
    }

    @Override
    protected List<Team> entries() {
        return plugin.getTeamManager().getInvitesFor(viewer.getUniqueId());
    }

    @Override
    protected ItemStack render(Team team) {
        long expiry = team.getInvites().getOrDefault(viewer.getUniqueId(), 0L);
        long secs = Math.max(0, (expiry - System.currentTimeMillis()) / 1000);
        return ItemBuilder.of(team.getIcon())
                .name(plugin.getMessages().get("gui.invites.entry.name",
                        Placeholder.component("team", team.displayName())))
                .lore(plugin.getMessages().getList("gui.invites.entry.lore",
                        Placeholder.unparsed("time", String.valueOf(secs)))).build();
    }

    @Override
    protected void onEntryClick(Team team, InventoryClickEvent event) {
        if (event.isRightClick()) {
            DenyCommand.deny(plugin, viewer, team);
            clickSound();
            refresh();
        } else {
            JoinCommand.tryJoin(plugin, viewer, team);
            viewer.closeInventory();
        }
    }

    @Override
    protected void buildExtras() {
        backButton(45, () -> new NoTeamMenu(plugin, viewer).open());
    }
}
