package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.model.Team;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class IconMenu extends PaginatedMenu<Material> {

    private final Team team;
    private static final List<Material> ICONS = Arrays.asList(
            Material.WHITE_BANNER, Material.RED_BANNER, Material.BLUE_BANNER, Material.GREEN_BANNER,
            Material.YELLOW_BANNER, Material.BLACK_BANNER, Material.PURPLE_BANNER, Material.ORANGE_BANNER,
            Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.BOW, Material.CROSSBOW,
            Material.SHIELD, Material.TOTEM_OF_UNDYING, Material.CREEPER_HEAD, Material.ZOMBIE_HEAD,
            Material.SKELETON_SKULL, Material.WITHER_SKELETON_SKULL, Material.DRAGON_HEAD,
            Material.DIAMOND, Material.EMERALD, Material.NETHERITE_INGOT, Material.GOLD_INGOT,
            Material.IRON_INGOT, Material.ENDER_PEARL, Material.ENDER_EYE, Material.TNT,
            Material.BEACON, Material.CRAFTING_TABLE, Material.ANVIL, Material.CHEST,
            Material.ENCHANTED_BOOK, Material.GOLDEN_APPLE, Material.DRAGON_EGG, Material.AMETHYST_SHARD,
            Material.HEART_OF_THE_SEA, Material.NETHER_STAR, Material.BLAZE_ROD, Material.TRIDENT,
            Material.ELYTRA, Material.FIREWORK_ROCKET, Material.CAMPFIRE, Material.LANTERN
    );

    public IconMenu(TeamForgePlugin plugin, Player viewer, Team team) {
        super(plugin, viewer);
        this.team = team;
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.icon.title");
    }

    @Override
    protected List<Material> entries() {
        return ICONS;
    }

    @Override
    protected ItemStack render(Material material) {
        return ItemBuilder.of(material)
                .name(Component.text(ItemBuilder.prettyEnum(material.name())))
                .glow(team.getIcon() == material).build();
    }

    @Override
    protected void onEntryClick(Material material, InventoryClickEvent event) {
        team.setIcon(material);
        plugin.getTeamManager().markDirty();
        successSound();
        refresh();
    }

    @Override
    protected void buildExtras() {
        backButton(45, () -> new SettingsMenu(plugin, viewer, team).open());
    }
}
