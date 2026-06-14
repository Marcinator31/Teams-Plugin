package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ConfirmMenu extends AbstractMenu {

    private final Component infoName;
    private final List<Component> infoLore;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmMenu(TeamForgePlugin plugin, Player viewer, Component infoName,
                       List<Component> infoLore, Runnable onConfirm, Runnable onCancel) {
        super(plugin, viewer);
        this.infoName = infoName;
        this.infoLore = infoLore;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.confirm.title");
    }

    @Override
    protected void build() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        setItem(11, ItemBuilder.of(Material.LIME_CONCRETE)
                .name(plugin.getMessages().get("gui.common.confirm")).build(), e -> {
            successSound();
            onConfirm.run();
        });
        setItem(13, ItemBuilder.of(Material.PAPER).name(infoName).lore(infoLore).build());
        setItem(15, ItemBuilder.of(Material.RED_CONCRETE)
                .name(plugin.getMessages().get("gui.common.cancel")).build(), e -> {
            clickSound();
            onCancel.run();
        });
    }
}
