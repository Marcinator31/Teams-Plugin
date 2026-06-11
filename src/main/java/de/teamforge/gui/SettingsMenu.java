package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.sub.DescCommand;
import de.teamforge.command.sub.OpenCommand;
import de.teamforge.command.sub.PvpCommand;
import de.teamforge.command.sub.RenameCommand;
import de.teamforge.command.sub.TagCommand;
import de.teamforge.model.Team;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SettingsMenu extends AbstractMenu {

    private final Team team;

    public SettingsMenu(TeamForgePlugin plugin, Player viewer, Team team) {
        super(plugin, viewer);
        this.team = team;
    }

    @Override
    protected int rows() {
        return 4;
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.settings.title");
    }

    @Override
    protected void build() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        boolean owner = team.isOwner(viewer.getUniqueId());

        setItem(10, ItemBuilder.of(Material.IRON_SWORD)
                .name(plugin.getMessages().get("gui.settings.ff.name"))
                .lore(plugin.getMessages().getList("gui.settings.ff.lore",
                        Placeholder.unparsed("state", plugin.getMessages().onOff(team.isFriendlyFire()))))
                .glow(team.isFriendlyFire()).build(), e -> {
            PvpCommand.toggle(plugin, team, viewer);
            refresh();
        });

        setItem(12, ItemBuilder.of(Material.OAK_FENCE_GATE)
                .name(plugin.getMessages().get("gui.settings.open.name"))
                .lore(plugin.getMessages().getList("gui.settings.open.lore",
                        Placeholder.unparsed("state", plugin.getMessages().onOff(team.isOpen()))))
                .build(), e -> {
            OpenCommand.toggle(plugin, team, viewer);
            refresh();
        });

        setItem(14, ItemBuilder.of(Material.RED_WOOL)
                .name(plugin.getMessages().get("gui.settings.color.name")).build(), e -> {
            if (!plugin.getConfigManager().allowColor || !owner) {
                errorSound();
                return;
            }
            clickSound();
            new ColorMenu(plugin, viewer, team).open();
        });

        setItem(16, ItemBuilder.of(team.getIcon())
                .name(plugin.getMessages().get("gui.settings.icon.name")).build(), e -> {
            if (!owner) {
                errorSound();
                return;
            }
            clickSound();
            new IconMenu(plugin, viewer, team).open();
        });

        if (owner) {
            setItem(20, ItemBuilder.of(Material.NAME_TAG)
                    .name(plugin.getMessages().get("gui.settings.rename.name")).build(), e -> {
                clickSound();
                plugin.getChatInputService().await(viewer,
                        plugin.getMessages().get("input.rename"),
                        name -> RenameCommand.tryRename(plugin, team, viewer, name));
            });
            setItem(22, ItemBuilder.of(Material.OAK_SIGN)
                    .name(plugin.getMessages().get("gui.settings.tag.name")).build(), e -> {
                clickSound();
                plugin.getChatInputService().await(viewer,
                        plugin.getMessages().get("input.tag"),
                        tag -> TagCommand.tryTag(plugin, team, viewer, tag));
            });
        }

        setItem(24, ItemBuilder.of(Material.WRITABLE_BOOK)
                .name(plugin.getMessages().get("gui.settings.desc.name")).build(), e -> {
            clickSound();
            plugin.getChatInputService().await(viewer,
                    plugin.getMessages().get("input.desc"),
                    desc -> DescCommand.trySetDesc(plugin, team, viewer, desc));
        });

        setItem(31, ItemBuilder.of(Material.BARRIER)
                .name(plugin.getMessages().get("gui.common.back")).build(), e -> {
            clickSound();
            new MainMenu(plugin, viewer, team).open();
        });
    }
}
