package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.sub.CreateCommand;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Locale;

public class NoTeamMenu extends AbstractMenu {

    public NoTeamMenu(TeamForgePlugin plugin, Player viewer) {
        super(plugin, viewer);
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.noteam.title");
    }

    @Override
    protected void build() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        setItem(11, ItemBuilder.of(Material.NETHER_STAR)
                .name(plugin.getMessages().get("gui.noteam.create.name"))
                .lore(plugin.getMessages().getList("gui.noteam.create.lore")).build(), e -> {
            clickSound();
            plugin.getChatInputService().await(viewer,
                    plugin.getMessages().get("input.create-name"), name -> {
                plugin.getChatInputService().await(viewer,
                        plugin.getMessages().get("input.create-tag"), tag -> {
                    String finalTag = tag.equalsIgnoreCase("-")
                            ? (name.length() > 4 ? name.substring(0, 4) : name)
                                    .toUpperCase(Locale.ROOT).replaceAll("[^A-Za-z0-9]", "")
                            : tag;
                    CreateCommand.tryCreate(plugin, viewer, name, finalTag);
                });
            });
        });
        setItem(13, ItemBuilder.of(Material.COMPASS)
                .name(plugin.getMessages().get("gui.noteam.browse.name"))
                .lore(plugin.getMessages().getList("gui.noteam.browse.lore")).build(), e -> {
            clickSound();
            new TeamsBrowserMenu(plugin, viewer).open();
        });
        setItem(15, ItemBuilder.of(Material.BOOK)
                .name(plugin.getMessages().get("gui.noteam.invites.name"))
                .lore(plugin.getMessages().getList("gui.noteam.invites.lore")).build(), e -> {
            clickSound();
            new InvitesMenu(plugin, viewer).open();
        });
    }
}
