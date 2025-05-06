package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.util.GameState;
import de.brentspine.monsterArmyBattle.util.Team;
import de.brentspine.monsterArmyBattle.util.TeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WavesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage("§cDu musst ein Spieler sein");
            return true;
        }
        if(MonsterArmyBattle.gameState != GameState.CONFIGURATION) {
            player.sendMessage("§cDu kannst die Wellen nur in der Konfigurationsphase bearbeiten");
            return true;
        }
        Team team = TeamManager.getTeamByPlayer(player);
        assert team != null;
        player.openInventory(team.getWaveConfigurationInventory());
        return true;
    }

}
