package de.brentspine.monsterArmyBattle.commands;  import de.brentspine.monsterArmyBattle.MonsterArmyBattle; import de.brentspine.monsterArmyBattle.util.GameState;
import de.brentspine.monsterArmyBattle.util.Team;
import de.brentspine.monsterArmyBattle.util.TeamManager;
import org.bukkit.command.Command; import org.bukkit.command.CommandExecutor; import org.bukkit.command.CommandSender; import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StartWaveConfigCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        // Check if sender is a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can execute this command!");
            return true;
        }

        // Check if player has permission
        if (!player.hasPermission("monsterarmybattle.startwaveconfig")) {
            player.sendMessage("§cDu hast unzureichende Berechtigungen!");
            return true;
        }

        // Check if current game state is appropriate
        if (MonsterArmyBattle.gameState != GameState.FARM) {
            player.sendMessage("§cDieser Command kann nur nach der Farmphase ausgeführt werden!");
            return true;
        }

        // Change game state
        MonsterArmyBattle.gameState = GameState.CONFIGURATION;
        for(Team team : TeamManager.getTeams()) {
            team.callMeWhenConfigurationIsReady();
        }
        player.sendMessage("§aGame state geändert!");

        return true;
    }
}