package de.teamforge.command.sub;

import de.teamforge.TeamForgePlugin;
import de.teamforge.command.SubCommand;
import de.teamforge.model.Team;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankCommand extends SubCommand {

    public BankCommand(TeamForgePlugin plugin) {
        super(plugin, "bank", "teams.use", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (!plugin.isBankActive()) {
            msg().send(player, "bank.disabled");
            return;
        }
        Team team = teams().getTeam(player);
        if (team == null) {
            msg().send(player, "error.not-in-team");
            return;
        }
        if (args.length == 0) {
            msg().send(player, "bank.balance",
                    Placeholder.unparsed("amount", plugin.formatMoney(team.getBank())));
            return;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("balance") || sub.equals("guthaben")) {
            msg().send(player, "bank.balance",
                    Placeholder.unparsed("amount", plugin.formatMoney(team.getBank())));
            return;
        }
        if (args.length < 2) {
            msg().send(player, "bank.usage");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException ex) {
            msg().send(player, "error.invalid-number");
            return;
        }
        if (amount <= 0) {
            msg().send(player, "error.invalid-number");
            return;
        }
        if (sub.equals("deposit") || sub.equals("einzahlen")) {
            deposit(plugin, team, player, amount);
        } else if (sub.equals("withdraw") || sub.equals("abheben")) {
            withdraw(plugin, team, player, amount);
        } else {
            msg().send(player, "bank.usage");
        }
    }

    public static void deposit(TeamForgePlugin plugin, Team team, Player by, double amount) {
        if (team.getBank() + amount > plugin.getConfigManager().bankMax) {
            plugin.getMessages().send(by, "bank.full");
            return;
        }
        if (!plugin.getVaultHook().has(by, amount)) {
            plugin.getMessages().send(by, "bank.insufficient-player");
            return;
        }
        plugin.getVaultHook().withdraw(by, amount);
        team.setBank(team.getBank() + amount);
        plugin.getTeamManager().markDirty();
        plugin.getMessages().send(by, "bank.deposited",
                Placeholder.unparsed("amount", plugin.formatMoney(amount)));
    }

    public static void withdraw(TeamForgePlugin plugin, Team team, Player by, double amount) {
        if (!plugin.getTeamManager().canWithdrawBank(team, by.getUniqueId())) {
            plugin.getMessages().send(by, "bank.no-withdraw-perm");
            return;
        }
        if (team.getBank() < amount) {
            plugin.getMessages().send(by, "bank.insufficient-team");
            return;
        }
        team.setBank(team.getBank() - amount);
        plugin.getVaultHook().deposit(by, amount);
        plugin.getTeamManager().markDirty();
        plugin.getMessages().send(by, "bank.withdrawn",
                Placeholder.unparsed("amount", plugin.formatMoney(amount)));
    }
}
