package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.util.BattleEvent;
import de.brentspine.monsterArmyBattle.util.WaveManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnnounceBattleResultCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("monsterarmybattle.announcebattleresult")) {
            sender.sendMessage("§cDu hast keine Berechtigung, diesen Befehl zu nutzen.");
            return true;
        }

        List<BattleEvent> battleEvents = WaveManager.battleEvents;

        if (battleEvents.isEmpty()) {
            sender.sendMessage("§cKeine Battle-Events zum Anzeigen vorhanden.");
            return true;
        }

        for(Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("§6§l=== Ereignisse ===");
            for (BattleEvent event : battleEvents) {
                player.sendMessage(event.toString());
            }
            player.sendMessage("§6§l======================");
        }


        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        return new ArrayList<>(); // No tab completion for this command
    }
}