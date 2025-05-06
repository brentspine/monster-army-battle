package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.util.ArmyEntityStorage;
import de.brentspine.monsterArmyBattle.util.Team;
import de.brentspine.monsterArmyBattle.util.TeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import java.util.Map;

public class CurrentArmyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Team team = TeamManager.getTeamByPlayer(player);

        if (team == null) {
            player.sendMessage("§cDu bist in keinem Team!");
            return true;
        }

        player.sendMessage("§6§lDeine Armee:");
        player.sendMessage("§7§m--------------------");

        Map<EntityType, ArmyEntityStorage> army = team.getArmy().getArmy();
        int totalEntities = 0;

        for (Map.Entry<EntityType, ArmyEntityStorage> entry : army.entrySet()) {
            if (entry.getValue().getAmount() > 0) {
                player.sendMessage("§a" + formatEntityName(entry.getKey()) + ": §e" + entry.getValue().getAmount());
                totalEntities += entry.getValue().getAmount();
            }
        }

        if (totalEntities == 0) {
            player.sendMessage("§cDu hast noch keine Monster in deiner Armee!");
        }

        player.sendMessage("§7§m--------------------");
        player.sendMessage("§6Gesamt: §e" + totalEntities + " Monster");

        return true;
    }

    private String formatEntityName(EntityType type) {
        String name = type.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}