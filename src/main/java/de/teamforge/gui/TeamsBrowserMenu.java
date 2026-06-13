package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TeamsBrowserMenu extends PaginatedMenu<Team> {

    public TeamsBrowserMenu(TeamForgePlugin plugin, Player viewer) {
        super(plugin, viewer);
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.browser.title");
    }

    @Override
    protected List<Team> entries() {
        List<Team> list = new ArrayList<>(plugin.getTeamManager().getTeams());
        list.sort(Comparator.comparingInt((Team t) -> t.getMembers().size()).reversed());
        return list;
    }

    @Override
    protected ItemStack render(Team team) {
        return ItemBuilder.of(team.getIcon())
                .name(plugin.getMessages().get("gui.browser.entry.name",
                        Placeholder.component("team", team.displayName())))
                .lore(plugin.getMessages().getList("gui.browser.entry.lore",
                        Placeholder.component("tag", team.coloredTag()),
                        Placeholder.unparsed("owner", team.getMemberName(team.getOwner())),
                        Placeholder.unparsed("members", String.valueOf(team.getMembers().size())),
                        Placeholder.unparsed("members_max", String.valueOf(team.getMaxMembers())),
                        Placeholder.component("open", plugin.getMessages().onOffComponent(team.isOpen())),
                        Placeholder.unparsed("allies", String.valueOf(team.getAllies().size()))))
                .build();
    }

    @Override
    protected void onEntryClick(Team team, InventoryClickEvent event) {
        clickSound();
        new TeamViewMenu(plugin, viewer, team).open();
    }

    @Override
    protected void buildExtras() {
        Team own = plugin.getTeamManager().getTeam(viewer);
        backButton(45, () -> {
            if (own != null) {
                new MainMenu(plugin, viewer, own).open();
            } else {
                new NoTeamMenu(plugin, viewer).open();
            }
        });
    }
}
