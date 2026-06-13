package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlliesMenu extends AbstractMenu {

    private final Team team;

    public AlliesMenu(TeamForgePlugin plugin, Player viewer, Team team) {
        super(plugin, viewer);
        this.team = team;
    }

    @Override
    protected int rows() {
        return 6;
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.allies.title");
    }

    @Override
    protected void build() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        boolean canManage = plugin.getTeamManager().canManageAllies(team, viewer.getUniqueId());

        setItem(4, ItemBuilder.of(Material.GOLDEN_APPLE)
                .name(plugin.getMessages().get("gui.allies.info.name"))
                .lore(plugin.getMessages().getList("gui.allies.info.lore",
                        Placeholder.unparsed("allies", String.valueOf(team.getAllies().size())),
                        Placeholder.unparsed("max", String.valueOf(plugin.getConfigManager().maxAllies))))
                .build());

        // Incoming requests (row 2)
        int[] reqSlots = {10, 11, 12, 13, 14, 15, 16};
        int i = 0;
        for (UUID reqId : new ArrayList<>(team.getAllyRequests())) {
            if (i >= reqSlots.length) {
                break;
            }
            Team requester = plugin.getTeamManager().getTeam(reqId);
            if (requester == null) {
                continue;
            }
            final Team req = requester;
            setItem(reqSlots[i++], ItemBuilder.of(Material.WRITABLE_BOOK)
                    .name(plugin.getMessages().get("gui.allies.request.name",
                            Placeholder.component("team", req.displayName())))
                    .lore(plugin.getMessages().getList("gui.allies.request.lore")).build(), e -> {
                if (!canManage) {
                    errorSound();
                    return;
                }
                if (e.isRightClick()) {
                    plugin.getTeamManager().removeAllyRequest(team, req);
                    clickSound();
                } else {
                    plugin.getTeamManager().acceptAlly(team, req);
                    successSound();
                }
                refresh();
            });
        }

        // Allies (row 4)
        int[] allySlots = {28, 29, 30, 31, 32, 33, 34};
        i = 0;
        for (UUID allyId : new ArrayList<>(team.getAllies())) {
            if (i >= allySlots.length) {
                break;
            }
            Team ally = plugin.getTeamManager().getTeam(allyId);
            if (ally == null) {
                continue;
            }
            final Team a = ally;
            setItem(allySlots[i++], ItemBuilder.of(a.getIcon())
                    .name(plugin.getMessages().get("gui.allies.ally.name",
                            Placeholder.component("team", a.displayName())))
                    .lore(plugin.getMessages().getList("gui.allies.ally.lore")).build(), e -> {
                if (!canManage) {
                    errorSound();
                    return;
                }
                new ConfirmMenu(plugin, viewer,
                        plugin.getMessages().get("gui.allies.ally.name",
                                Placeholder.component("team", a.displayName())),
                        plugin.getMessages().getList("gui.allies.ally.lore"),
                        () -> {
                            plugin.getTeamManager().removeAlly(team, a);
                            new AlliesMenu(plugin, viewer, team).open();
                        },
                        () -> new AlliesMenu(plugin, viewer, team).open()).open();
            });
        }

        setItem(49, ItemBuilder.of(Material.BARRIER)
                .name(plugin.getMessages().get("gui.common.back")).build(), e -> {
            clickSound();
            new MainMenu(plugin, viewer, team).open();
        });
    }
}
