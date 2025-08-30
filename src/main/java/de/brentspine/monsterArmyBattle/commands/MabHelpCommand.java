package de.brentspine.monsterArmyBattle.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MabHelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cNur Spieler können diesen Command ausführen.");
            return false;
        }
        if(player.hasPermission("op")) showAdminHelp(player);
        else showNonAdminHelp(player);
        return true;
    }

    public void showNonAdminHelp(Player player) {
        player.sendMessage("§a====== §6§lMonster Army Battle Help §r§a======");
        player.sendMessage("§6/currentarmy §7- §aZeigt die aktuelle Armee deines Teams an.");
        player.sendMessage("§6/waves §7- §aKonfiguiere die Wellen.");
        player.sendMessage("§6/bp §7- §aÖffne den Backpack.");
        player.sendMessage("§6/addglowing §7- §aGibt 10 zufälligen Mobs in deiner Nähe Glowing.");
        player.sendMessage("§6/mabhelp §7- §aZeigt diese Hilfe an.");
    }

    public void showAdminHelp(Player player) {
        showNonAdminHelp(player);
        player.sendMessage("§c======== Admin Commands ========");
        player.sendMessage("§6/changeteamamount <amount> §7- §aÄndert die Teamanzahl.");
        player.sendMessage("§6/resetteams §7- §aSetzt alle Teams zurück.");
        player.sendMessage("§6/timer [help] §7- §aKonfiguriere den Timer.");
        player.sendMessage("§6/findrandomizermapping §7- §aFindet das Randomizer Mapping für Blöcke.");
        player.sendMessage("§6/start §7- §aStartet das Spiel.");
        player.sendMessage("§6/startwaveconfig §7- §aStartet die Wellen Konfiguration.");
        player.sendMessage("§6/startbattle §7- §aTeleportiert alle Spieler in die Arena und startet die Battle Phase.");
        player.sendMessage("§6/editworlds §7- §aUtil für das Managen von Welten.");
        player.sendMessage("§6/locationhelper §7- §aÖffnet das Location Helper Menü.");
        player.sendMessage("§6/addtoarmy §7- §aFüge Mobs zu Armeen der Teams hinzu.");
        player.sendMessage("§6/announcebattleresult §7- §aGibt die aktuellen Battle Ergebnisse bekannt.");
        player.sendMessage("§6/mabsettings §7- §aManage einige Einstellungen via cmd.");
    }

}
