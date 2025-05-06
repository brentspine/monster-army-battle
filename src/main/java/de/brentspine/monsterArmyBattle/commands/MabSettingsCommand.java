package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.listeners.BattlePhaseListeners;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MabSettingsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mab.settings")) {
            sender.sendMessage("§cDu hast keine Berechtigung, diesen Befehl zu benutzen.");
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        String settingType = args[0].toLowerCase();

        switch (settingType) {
            case "arenablockbreak", "abb" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cBitte gib einen Wert an.");
                    return true;
                }

                try {
                    BattlePhaseListeners.ArenaBlockBreakSettings setting = BattlePhaseListeners.ArenaBlockBreakSettings.valueOf(args[1].toUpperCase());
                    BattlePhaseListeners.ARENA_BLOCK_BREAK_SETTINGS = setting;
                    sender.sendMessage("§aArena Block Break Einstellung wurde auf §e" + setting + " §ageändert.");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cUngültiger Wert.");
                }
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lMAB Settings Befehle:");
        sender.sendMessage("§e/mabsettings arenaBlockBreak|abb <Wert> §7- Ändert die Block Break Einstellungen in der Arena");
        // Add more help lines for future settings
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - setting type
            String[] settingTypes = {"arenaBlockBreak", "abb"};
            for (String setting : settingTypes) {
                if (setting.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(setting);
                }
            }
        } else if (args.length == 2) {
            // Second argument - setting value
            if (args[0].equalsIgnoreCase("arenaBlockBreak") || args[0].equalsIgnoreCase("abb")) {
                return Arrays.stream(BattlePhaseListeners.ArenaBlockBreakSettings.values())
                        .map(Enum::name)
                        .filter(name -> name.startsWith(args[1].toUpperCase()))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}