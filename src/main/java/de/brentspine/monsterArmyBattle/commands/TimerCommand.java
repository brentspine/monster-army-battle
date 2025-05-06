package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.util.Timer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TimerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("monsterarmybattle.timer")) {
            sender.sendMessage("§cDu hast leider keine Berechtigung diesen Command auszuführen.");
            return true;
        }

        // No arguments provided, show usage
        if (args.length == 0) {
            showUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        Timer timer = Timer.getInstance();

        switch (subCommand) {
            case "start":
                timer.start();
                sender.sendMessage("§aTimer gestartet.");
                break;

            case "stop":
            case "pause":
                timer.stop();
                sender.sendMessage("§aTimer pausiert.");
                break;

            case "resume":
                timer.resume();
                sender.sendMessage("§aTimer fortgesetzt.");
                break;

            case "reset":
                timer.reset();
                sender.sendMessage("§aTimer zurückgesetzt.");
                break;

            case "set":
                if (args.length < 2) {
                    sender.sendMessage("§cBitte gib eine Zeit in Sekunden an. Verwendung: /timer set <Sekunden>");
                    return true;
                }
                try {
                    int seconds = Integer.parseInt(args[1]);
                    timer.set(seconds);
                    timer.resume();
                    sender.sendMessage("§aTimer auf " + seconds + " Sekunden gesetzt.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cBitte gib eine gültige Zahl an.");
                }
                break;

            case "direction":
                if (args.length < 2) {
                    sender.sendMessage("§cBitte gib eine Richtung an (up/down). Verwendung: /timer direction <up/down>");
                    return true;
                }
                String direction = args[1].toLowerCase();
                if (direction.equals("up")) {
                    timer.setAscending(true);
                    sender.sendMessage("§aTimer zählt jetzt aufwärts.");
                } else if (direction.equals("down")) {
                    timer.setAscending(false);
                    sender.sendMessage("§aTimer zählt jetzt abwärts.");
                } else {
                    sender.sendMessage("§cUngültige Richtung. Verwende 'up' oder 'down'.");
                }
                break;

            case "status":
                sendTimerStatus(sender, timer);
                break;

            default:
                showUsage(sender);
                break;
        }

        return true;
    }

    private void showUsage(CommandSender sender) {
        sender.sendMessage("§6Timer Befehle:");
        sender.sendMessage("§e/timer start §7- Startet den Timer");
        sender.sendMessage("§e/timer stop §7- Pausiert den Timer");
        sender.sendMessage("§e/timer resume §7- Setzt den Timer fort");
        sender.sendMessage("§e/timer reset §7- Setzt den Timer zurück");
        sender.sendMessage("§e/timer set <Sekunden> §7- Setzt den Timer auf einen bestimmten Wert");
        sender.sendMessage("§e/timer direction <up/down> §7- Legt fest, ob der Timer hoch- oder runterzählt");
        sender.sendMessage("§e/timer status §7- Zeigt den aktuellen Status des Timers");
    }

    private void sendTimerStatus(CommandSender sender, Timer timer) {
        sender.sendMessage("§6Timer Status:");
        sender.sendMessage("§eZeit: §f" + timer.getFormattedTime());
        sender.sendMessage("§eRohe Sekunden: §f" + timer.getSeconds());
        sender.sendMessage("§eStatus: §f" + (timer.isRunning() ? "Läuft" : "Pausiert"));
        sender.sendMessage("§eRichtung: §f" + (timer.isAscending() ? "Aufwärts" : "Abwärts"));
    }

}
