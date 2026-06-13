package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractMenu implements InventoryHolder {

    protected final TeamForgePlugin plugin;
    protected final Player viewer;
    protected Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();

    protected AbstractMenu(TeamForgePlugin plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    protected abstract int rows();

    protected abstract Component title();

    protected abstract void build();

    public void open() {
        inventory = Bukkit.createInventory(this, rows() * 9, title());
        actions.clear();
        build();
        viewer.openInventory(inventory);
    }

    public void refresh() {
        inventory.clear();
        actions.clear();
        build();
    }

    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    protected void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        inventory.setItem(slot, item);
        actions.put(slot, action);
    }

    protected void fillBorder(Material material) {
        ItemStack filler = de.teamforge.util.ItemBuilder.of(material)
                .name(Component.text(" ")).build();
        int size = rows() * 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == rows() - 1 || col == 0 || col == 8) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, filler);
                }
            }
        }
    }

    public void handleClick(InventoryClickEvent event) {
        Consumer<InventoryClickEvent> action = actions.get(event.getRawSlot());
        if (action != null) {
            action.accept(event);
        }
    }

    protected void clickSound() {
        viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
    }

    protected void successSound() {
        viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1f);
    }

    protected void errorSound() {
        viewer.playSound(viewer.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1f);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
