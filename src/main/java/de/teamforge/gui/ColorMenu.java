package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.sub.ColorCommand;
import de.teamforge.model.Team;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class ColorMenu extends AbstractMenu {

    private final Team team;
    private static final Map<NamedTextColor, Material> COLORS = new LinkedHashMap<>();

    static {
        COLORS.put(NamedTextColor.BLACK, Material.BLACK_WOOL);
        COLORS.put(NamedTextColor.DARK_BLUE, Material.BLUE_WOOL);
        COLORS.put(NamedTextColor.DARK_GREEN, Material.GREEN_WOOL);
        COLORS.put(NamedTextColor.DARK_AQUA, Material.CYAN_WOOL);
        COLORS.put(NamedTextColor.DARK_RED, Material.RED_WOOL);
        COLORS.put(NamedTextColor.DARK_PURPLE, Material.PURPLE_WOOL);
        COLORS.put(NamedTextColor.GOLD, Material.ORANGE_WOOL);
        COLORS.put(NamedTextColor.GRAY, Material.LIGHT_GRAY_WOOL);
        COLORS.put(NamedTextColor.DARK_GRAY, Material.GRAY_WOOL);
        COLORS.put(NamedTextColor.BLUE, Material.LIGHT_BLUE_WOOL);
        COLORS.put(NamedTextColor.GREEN, Material.LIME_WOOL);
        COLORS.put(NamedTextColor.AQUA, Material.LIGHT_BLUE_CONCRETE);
        COLORS.put(NamedTextColor.RED, Material.RED_CONCRETE);
        COLORS.put(NamedTextColor.LIGHT_PURPLE, Material.MAGENTA_WOOL);
        COLORS.put(NamedTextColor.YELLOW, Material.YELLOW_WOOL);
        COLORS.put(NamedTextColor.WHITE, Material.WHITE_WOOL);
    }

    public ColorMenu(TeamForgePlugin plugin, Player viewer, Team team) {
        super(plugin, viewer);
        this.team = team;
    }

    @Override
    protected int rows() {
        return 4;
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.color.title");
    }

    @Override
    protected void build() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29};
        int i = 0;
        for (Map.Entry<NamedTextColor, Material> entry : COLORS.entrySet()) {
            final NamedTextColor color = entry.getKey();
            setItem(slots[i++], ItemBuilder.of(entry.getValue())
                    .name(Component.text(de.teamforge.util.ItemBuilder.prettyEnum(color.toString()), color))
                    .glow(team.getColor() == color).build(), e -> {
                ColorCommand.doSetColor(plugin, team, viewer, color);
                successSound();
                refresh();
            });
        }
        setItem(31, ItemBuilder.of(Material.BARRIER)
                .name(plugin.getMessages().get("gui.common.back")).build(), e -> {
            clickSound();
            new SettingsMenu(plugin, viewer, team).open();
        });
    }
}
