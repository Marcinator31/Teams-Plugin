package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.sub.DisbandCommand;
import de.teamforge.command.sub.HomeCommand;
import de.teamforge.command.sub.LeaveCommand;
import de.teamforge.command.sub.SetHomeCommand;
import de.teamforge.model.Team;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Collections;

public class MainMenu extends AbstractMenu {

    private final Team team;

    public MainMenu(TeamForgePlugin plugin, Player viewer, Team team) {
        super(plugin, viewer);
        this.team = team;
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.main.title",
                Placeholder.component("team", team.displayName()));
    }

    private TagResolver[] infoPlaceholders() {
        String created = new SimpleDateFormat("dd.MM.yyyy").format(team.getCreated());
        return new TagResolver[]{
                Placeholder.component("team", team.displayName()),
                Placeholder.component("tag", team.coloredTag()),
                Placeholder.unparsed("owner", team.getMemberName(team.getOwner())),
                Placeholder.unparsed("members", String.valueOf(team.getMembers().size())),
                Placeholder.unparsed("members_max", String.valueOf(team.getMaxMembers())),
                Placeholder.unparsed("online", String.valueOf(team.getOnlineMembers().size())),
                Placeholder.unparsed("bank", plugin.formatMoney(team.getBank())),
                Placeholder.unparsed("kills", String.valueOf(team.getKills())),
                Placeholder.unparsed("deaths", String.valueOf(team.getDeaths())),
                Placeholder.unparsed("created", created),
                Placeholder.component("ff", plugin.getMessages().onOffComponent(team.isFriendlyFire())),
                Placeholder.component("open", plugin.getMessages().onOffComponent(team.isOpen())),
                Placeholder.unparsed("description",
                        team.getDescription().isEmpty() ? "-" : team.getDescription())
        };
    }

    @Override
    protected void build() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);

        setItem(4, ItemBuilder.of(team.getIcon())
                .name(plugin.getMessages().get("gui.main.info.name", infoPlaceholders()))
                .lore(plugin.getMessages().getList("gui.main.info.lore", infoPlaceholders()))
                .glow(true).build());

        setItem(10, ItemBuilder.of(Material.PLAYER_HEAD)
                .name(plugin.getMessages().get("gui.main.members.name")).build(), e -> {
            clickSound();
            new MembersMenu(plugin, viewer, team).open();
        });

        setItem(12, ItemBuilder.of(Material.COMPARATOR)
                .name(plugin.getMessages().get("gui.main.settings.name")).build(), e -> {
            if (!plugin.getTeamManager().canEditSettings(team, viewer.getUniqueId())) {
                errorSound();
                plugin.getMessages().send(viewer, "error.no-permission");
                return;
            }
            clickSound();
            new SettingsMenu(plugin, viewer, team).open();
        });

        if (plugin.getConfigManager().alliesEnabled) {
            setItem(14, ItemBuilder.of(Material.GOLDEN_APPLE)
                    .name(plugin.getMessages().get("gui.main.allies.name")).build(), e -> {
                clickSound();
                new AlliesMenu(plugin, viewer, team).open();
            });
        }

        if (plugin.isBankActive()) {
            setItem(16, ItemBuilder.of(Material.GOLD_INGOT)
                    .name(plugin.getMessages().get("gui.main.bank.name")).build(), e -> {
                clickSound();
                new BankMenu(plugin, viewer, team).open();
            });
        }

        if (plugin.getConfigManager().homeEnabled) {
            setItem(19, ItemBuilder.of(Material.RED_BED)
                    .name(plugin.getMessages().get("gui.main.home.name"))
                    .lore(plugin.getMessages().getList("gui.main.home.lore")).build(), e -> {
                if (e.isRightClick()) {
                    SetHomeCommand.trySetHome(plugin, team, viewer);
                } else {
                    viewer.closeInventory();
                    HomeCommand.tryHome(plugin, team, viewer);
                }
            });
        }

        setItem(21, ItemBuilder.of(Material.PAPER)
                .name(plugin.getMessages().get("gui.main.chat.name"))
                .lore(Collections.singletonList(plugin.getMessages().get("gui.main.chat.state",
                        Placeholder.component("state", plugin.getMessages().onOffComponent(
                                plugin.getChatService().isTeamChat(viewer.getUniqueId()))))))
                .build(), e -> {
            boolean on = plugin.getChatService().toggleTeamChat(viewer.getUniqueId());
            clickSound();
            plugin.getMessages().send(viewer, on ? "chat.toggle-on" : "chat.toggle-off");
            refresh();
        });

        setItem(23, ItemBuilder.of(Material.COMPASS)
                .name(plugin.getMessages().get("gui.main.browse.name")).build(), e -> {
            clickSound();
            new TeamsBrowserMenu(plugin, viewer).open();
        });

        if (team.isOwner(viewer.getUniqueId())) {
            setItem(25, ItemBuilder.of(Material.TNT)
                    .name(plugin.getMessages().get("gui.main.disband.name"))
                    .lore(plugin.getMessages().getList("gui.main.disband.lore")).build(), e -> {
                errorSound();
                new ConfirmMenu(plugin, viewer,
                        plugin.getMessages().get("gui.main.disband.name"),
                        plugin.getMessages().getList("gui.main.disband.lore"),
                        () -> {
                            DisbandCommand.doDisband(plugin, team, viewer.getName());
                            viewer.closeInventory();
                            plugin.getMessages().send(viewer, "disband.success");
                        },
                        () -> new MainMenu(plugin, viewer, team).open()).open();
            });
        } else {
            setItem(25, ItemBuilder.of(Material.OAK_DOOR)
                    .name(plugin.getMessages().get("gui.main.leave.name")).build(), e -> {
                clickSound();
                LeaveCommand.tryLeave(plugin, viewer, team);
                viewer.closeInventory();
            });
        }
    }
}
