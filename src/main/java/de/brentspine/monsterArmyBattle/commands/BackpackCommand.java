package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.util.Team;
import de.brentspine.monsterArmyBattle.util.TeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BackpackCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        Team team = TeamManager.getTeamByPlayer(player);
        if(team == null) {
            player.sendMessage("§cKein Team/Backpack konnte gefunden werden.");
            return true;
        }
        player.openInventory(team.getBackpack());
        return true;
    }

}
