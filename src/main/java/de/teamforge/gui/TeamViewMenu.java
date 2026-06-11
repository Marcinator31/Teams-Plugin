package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.sub.AllyCommand;
import de.teamforge.command.sub.JoinCommand;
import de.teamforge.model.Team;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamViewMenu extends AbstractMenu {

    private final Team team;

    public TeamViewMenu(TeamForgePlugin plugin, Player viewer, Team team) {
        super(plugin, viewer);
        this.team = team;
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.view.title",
                Placeholder.component("team", team.displayName()));
    }

    @Override
    protected void build() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);

        setItem(4, ItemBuilder.of(team.getIcon())
                .name(plugin.getMessages().get("gui.browser.entry.name",
                        Placeholder.component("team", team.displayName())))
                .lore(plugin.getMessages().getList("gui.browser.entry.lore",
                        Placeholder.component("tag", team.coloredTag()),
                        Placeholder.unparsed("owner", team.getMemberName(team.getOwner())),
                        Placeholder.unparsed("members", String.valueOf(team.getMembers().size())),
                        Placeholder.unparsed("members_max", String.valueOf(team.getMaxMembers())),
                        Placeholder.component("open", plugin.getMessages().onOffComponent(team.isOpen())),
                        Placeholder.unparsed("allies", String.valueOf(team.getAllies().size()))))
                .glow(true).build());

        Team own = plugin.getTeamManager().getTeam(viewer);
        boolean invited = plugin.getTeamManager().hasInvite(team, viewer.getUniqueId());

        if (own == null && (team.isOpen() || invited)) {
            setItem(11, ItemBuilder.of(Material.LIME_CONCRETE)
                    .name(plugin.getMessages().get("gui.view.join.name")).build(), e -> {
                JoinCommand.tryJoin(plugin, viewer, team);
                viewer.closeInventory();
            });
        } else if (own == null) {
            setItem(11, ItemBuilder.of(Material.BARRIER)
                    .name(plugin.getMessages().get("gui.view.join-locked.name"))
                    .lore(plugin.getMessages().getList("gui.view.join-locked.lore")).build());
        }

        // Mitglieder-Vorschau
        StringBuilder names = new StringBuilder();
        int count = 0;
        for (UUID m : team.getMembers().keySet()) {
            if (count++ >= 8) {
                break;
            }
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(team.getMemberName(m));
        }
        setItem(13, ItemBuilder.of(Material.PLAYER_HEAD)
                .name(plugin.getMessages().get("gui.view.members.name"))
                .lore(plugin.getMessages().getList("gui.view.members.lore",
                        Placeholder.unparsed("names", names.toString()))).build());

        if (own != null && plugin.getConfigManager().alliesEnabled
                && plugin.getTeamManager().canManageAllies(own, viewer.getUniqueId())
                && !own.getId().equals(team.getId())) {
            setItem(15, ItemBuilder.of(Material.GOLDEN_APPLE)
                    .name(plugin.getMessages().get("gui.view.ally.name")).build(), e -> {
                clickSound();
                AllyCommand.requestOrAccept(plugin, own, team, viewer);
                viewer.closeInventory();
            });
        }

        setItem(22, ItemBuilder.of(Material.BARRIER)
                .name(plugin.getMessages().get("gui.common.back")).build(), e -> {
            clickSound();
            new TeamsBrowserMenu(plugin, viewer).open();
        });
    }
}
