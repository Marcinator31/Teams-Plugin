package de.teamforge.gui;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.sub.BankCommand;
import de.teamforge.model.Team;
import de.teamforge.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BankMenu extends AbstractMenu {

    private final Team team;

    public BankMenu(TeamForgePlugin plugin, Player viewer, Team team) {
        super(plugin, viewer);
        this.team = team;
    }

    @Override
    protected int rows() {
        return 4;
    }

    @Override
    protected Component title() {
        return plugin.getMessages().get("gui.bank.title");
    }

    @Override
    protected void build() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);

        setItem(4, ItemBuilder.of(Material.GOLD_BLOCK)
                .name(plugin.getMessages().get("gui.bank.balance.name",
                        Placeholder.unparsed("amount", plugin.formatMoney(team.getBank()))))
                .lore(plugin.getMessages().getList("gui.bank.balance.lore",
                        Placeholder.unparsed("max", plugin.formatMoney(plugin.getConfigManager().bankMax))))
                .glow(true).build());

        deposit(10, Material.GOLD_NUGGET, 100);
        deposit(11, Material.GOLD_INGOT, 1000);
        deposit(12, Material.GOLD_BLOCK, 10000);
        setItem(13, ItemBuilder.of(Material.PAPER)
                .name(plugin.getMessages().get("gui.bank.deposit-custom.name")).build(), e -> {
            clickSound();
            plugin.getChatInputService().await(viewer,
                    plugin.getMessages().get("input.bank-amount"), input -> {
                try {
                    BankCommand.deposit(plugin, team, viewer, Double.parseDouble(input));
                } catch (NumberFormatException ex) {
                    plugin.getMessages().send(viewer, "error.invalid-number");
                }
            });
        });

        boolean canWithdraw = plugin.getTeamManager().canWithdrawBank(team, viewer.getUniqueId());
        if (canWithdraw) {
            withdraw(19, Material.GOLD_NUGGET, 100);
            withdraw(20, Material.GOLD_INGOT, 1000);
            withdraw(21, Material.GOLD_BLOCK, 10000);
            setItem(22, ItemBuilder.of(Material.PAPER)
                    .name(plugin.getMessages().get("gui.bank.withdraw-custom.name")).build(), e -> {
                clickSound();
                plugin.getChatInputService().await(viewer,
                        plugin.getMessages().get("input.bank-amount"), input -> {
                    try {
                        BankCommand.withdraw(plugin, team, viewer, Double.parseDouble(input));
                    } catch (NumberFormatException ex) {
                        plugin.getMessages().send(viewer, "error.invalid-number");
                    }
                });
            });
        } else {
            setItem(20, ItemBuilder.of(Material.IRON_BARS)
                    .name(plugin.getMessages().get("gui.bank.withdraw-locked.name"))
                    .lore(plugin.getMessages().getList("gui.bank.withdraw-locked.lore")).build());
        }

        setItem(31, ItemBuilder.of(Material.BARRIER)
                .name(plugin.getMessages().get("gui.common.back")).build(), e -> {
            clickSound();
            new MainMenu(plugin, viewer, team).open();
        });
    }

    private void deposit(int slot, Material mat, double amount) {
        setItem(slot, ItemBuilder.of(mat)
                .name(plugin.getMessages().get("gui.bank.deposit.name",
                        Placeholder.unparsed("amount", plugin.formatMoney(amount)))).build(), e -> {
            BankCommand.deposit(plugin, team, viewer, amount);
            refresh();
        });
    }

    private void withdraw(int slot, Material mat, double amount) {
        setItem(slot, ItemBuilder.of(mat)
                .name(plugin.getMessages().get("gui.bank.withdraw.name",
                        Placeholder.unparsed("amount", plugin.formatMoney(amount)))).build(), e -> {
            BankCommand.withdraw(plugin, team, viewer, amount);
            refresh();
        });
    }
}
