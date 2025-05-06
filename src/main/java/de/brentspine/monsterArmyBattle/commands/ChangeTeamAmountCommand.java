package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.inventories.TeamSelectionInventory;
import de.brentspine.monsterArmyBattle.util.TeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ChangeTeamAmountCommand implements CommandExecutor {

    private final String PERMISSION = "monsterarmybattle.changeteamamount";
    private final int MIN_TEAMS = 2;
    private final int MAX_TEAMS = 36;

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /" + label + " <amount>");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[0]);

            if (amount < MIN_TEAMS) {
                sender.sendMessage("§cThe team amount cannot be less than " + MIN_TEAMS + ".");
                return true;
            }

            if (amount > MAX_TEAMS) {
                sender.sendMessage("§cThe team amount cannot be more than " + MAX_TEAMS + ".");
                return true;
            }

            TeamManager.changeTeamAmount(amount);
            sender.sendMessage("§aSuccessfully changed team amount to " + amount + ".");
            TeamSelectionInventory.updateAll();

        } catch (NumberFormatException e) {
            sender.sendMessage("§cPlease enter a valid number.");
        }

        return true;
    }
}