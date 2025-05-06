package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.inventories.TeamSelectionInventory;
import de.brentspine.monsterArmyBattle.util.TeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ResetTeamsCommand implements CommandExecutor {

    private final String PERMISSION = "monsterarmybattle.resetteams";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        // Get the current team amount
        int currentAmount = TeamManager.getTeams().size();

        // Reset teams by setting amount to 0 and then back to original
        TeamManager.changeTeamAmount(0);
        TeamManager.changeTeamAmount(currentAmount);

        sender.sendMessage("§aAll teams have been reset successfully.");

        TeamSelectionInventory.updateAll();

        return true;
    }
}