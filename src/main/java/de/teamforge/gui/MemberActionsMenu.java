package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.sub.DemoteCommand;
import de.teamforge.command.sub.KickCommand;
import de.teamforge.command.sub.PromoteCommand;
import de.teamforge.command.sub.TransferCommand;
import de.teamforge.model.Team;
import de.teamforge.model.TeamRole;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MemberActionsMenu extends AbstractMenu {

    private final Team team;
    private final UUID target;

    public MemberActionsMenu(TeamForgePlugin plugin, Player viewer, Team team, UUID target) {
        super(plugin, viewer);
        this.team = team;
        this.target = target;
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.member-actions.title",
                Placeholder.unparsed("player", team.getMemberName(target)));
    }

    @Override
    protected void build() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        TeamRole role = team.getRole(target);

        setItem(4, ItemBuilder.of(Material.PLAYER_HEAD).skull(target)
                .name(plugin.getMessages().get("gui.members.entry.name",
                        Placeholder.unparsed("player", team.getMemberName(target)))).build());

        boolean owner = team.isOwner(viewer.getUniqueId());

        if (owner && role == TeamRole.MEMBER) {
            setItem(11, ItemBuilder.of(Material.LIME_DYE)
                    .name(plugin.getMessages().get("gui.member-actions.promote.name")).build(), e -> {
                if (PromoteCommand.doPromote(plugin, team, target, viewer)) {
                    successSound();
                }
                refresh();
            });
        }
        if (owner && role == TeamRole.OFFICER) {
            setItem(13, ItemBuilder.of(Material.YELLOW_DYE)
                    .name(plugin.getMessages().get("gui.member-actions.demote.name")).build(), e -> {
                if (DemoteCommand.doDemote(plugin, team, target, viewer)) {
                    successSound();
                }
                refresh();
            });
        }
        if (plugin.getTeamManager().canKick(team, viewer.getUniqueId(), target)) {
            setItem(15, ItemBuilder.of(Material.RED_DYE)
                    .name(plugin.getMessages().get("gui.member-actions.kick.name")).build(), e -> {
                errorSound();
                new ConfirmMenu(plugin, viewer,
                        plugin.getMessages().get("gui.member-actions.kick.name"),
                        plugin.getMessages().getList("gui.member-actions.kick.lore"),
                        () -> {
                            KickCommand.doKick(plugin, team, target, viewer);
                            new MembersMenu(plugin, viewer, team).open();
                        },
                        () -> new MemberActionsMenu(plugin, viewer, team, target).open()).open();
            });
        }
        if (owner && role != TeamRole.OWNER) {
            setItem(22, ItemBuilder.of(Material.BEACON)
                    .name(plugin.getMessages().get("gui.member-actions.transfer.name"))
                    .lore(plugin.getMessages().getList("gui.member-actions.transfer.lore")).build(), e -> {
                errorSound();
                new ConfirmMenu(plugin, viewer,
                        plugin.getMessages().get("gui.member-actions.transfer.name"),
                        plugin.getMessages().getList("gui.member-actions.transfer.lore"),
                        () -> {
                            TransferCommand.doTransfer(plugin, team, target, viewer);
                            new MainMenu(plugin, viewer, team).open();
                        },
                        () -> new MemberActionsMenu(plugin, viewer, team, target).open()).open();
            });
        }

        setItem(18, ItemBuilder.of(Material.BARRIER)
                .name(plugin.getMessages().get("gui.common.back")).build(), e -> {
            clickSound();
            new MembersMenu(plugin, viewer, team).open();
        });
    }
}
