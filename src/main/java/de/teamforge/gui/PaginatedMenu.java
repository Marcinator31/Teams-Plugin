package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class PaginatedMenu<T> extends AbstractMenu {

    protected static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    protected int page = 0;

    protected PaginatedMenu(TeamForgePlugin plugin, Player viewer) {
        super(plugin, viewer);
    }

    @Override
    protected int rows() {
        return 6;
    }

    protected abstract List<T> entries();

    protected abstract ItemStack render(T entry);

    protected abstract void onEntryClick(T entry, InventoryClickEvent event);

    /** Optional: extra items in the bottom bar. */
    protected void buildExtras() {
    }

    @Override
    protected void build() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        List<T> all = entries();
        int perPage = CONTENT_SLOTS.length;
        int maxPage = Math.max(0, (all.size() - 1) / perPage);
        if (page > maxPage) {
            page = maxPage;
        }
        int start = page * perPage;
        for (int i = 0; i < perPage; i++) {
            int index = start + i;
            if (index >= all.size()) {
                break;
            }
            final T entry = all.get(index);
            setItem(CONTENT_SLOTS[i], render(entry), e -> onEntryClick(entry, e));
        }

        // Navigation
        if (page > 0) {
            setItem(48, ItemBuilder.of(Material.ARROW)
                    .name(plugin.getMessages().get("gui.common.prev")).build(), e -> {
                page--;
                clickSound();
                refresh();
            });
        }
        setItem(49, ItemBuilder.of(Material.PAPER)
                .name(plugin.getMessages().get("gui.common.page",
                        Placeholder.unparsed("page", String.valueOf(page + 1)),
                        Placeholder.unparsed("pages", String.valueOf(maxPage + 1))))
                .build());
        if (page < maxPage) {
            setItem(50, ItemBuilder.of(Material.ARROW)
                    .name(plugin.getMessages().get("gui.common.next")).build(), e -> {
                page++;
                clickSound();
                refresh();
            });
        }
        buildExtras();
    }

    protected void backButton(int slot, Runnable target) {
        setItem(slot, ItemBuilder.of(Material.BARRIER)
                .name(plugin.getMessages().get("gui.common.back")).build(), e -> {
            clickSound();
            target.run();
        });
    }
}
