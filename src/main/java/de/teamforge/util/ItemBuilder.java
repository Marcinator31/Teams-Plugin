package de.teamforge.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Small builder for GUI items (name, lore, glow, skulls).
 */
public final class ItemBuilder {

    private final ItemStack item;

    private ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder name(Component name) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(wrap(name));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        ItemMeta meta = item.getItemMeta();
        List<Component> wrapped = new ArrayList<>();
        for (Component line : lore) {
            wrapped.add(wrap(line));
        }
        meta.lore(wrapped);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    public ItemBuilder glow(boolean glow) {
        if (glow) {
            ItemMeta meta = item.getItemMeta();
            meta.setEnchantmentGlintOverride(Boolean.TRUE);
            item.setItemMeta(meta);
        }
        return this;
    }

    /** Set a player head (works for offline players too). */
    public ItemBuilder skull(UUID owner) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) meta;
            OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
            skull.setOwningPlayer(player);
            item.setItemMeta(skull);
        }
        return this;
    }

    public ItemStack build() {
        return item;
    }

    /** Remove italic from items without overriding child styles. */
    private static Component wrap(Component component) {
        return Component.empty().decoration(TextDecoration.ITALIC, false).append(component);
    }

    /** ENUM_NAME -> "Enum Name" */
    public static String prettyEnum(String enumName) {
        String[] parts = enumName.toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
