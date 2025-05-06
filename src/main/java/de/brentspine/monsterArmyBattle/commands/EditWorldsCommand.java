package de.brentspine.monsterArmyBattle.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EditWorldsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("monsterarmybattle.editworlds")) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.");
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                createWorld(sender, args);
                break;
            case "list":
                listWorlds(sender);
                break;
            case "copy":
                copyWorld(sender, args);
                break;
            case "delete":
                deleteWorld(sender, args);
                break;
            case "tp":
            case "teleport":
                teleportToWorld(sender, args);
                break;
            case "options":
                showWorldOptions(sender, args);
                break;
            case "help":
                showHelp(sender);
                break;
            default:
                sender.sendMessage("§cUnbekannter Befehl. Nutze §6/editworlds help§c für Hilfe.");
                break;
        }

        return true;
    }

    private static void createWorld(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cBitte gib einen Namen für die Welt an.");
            return;
        }

        String worldName = args[1];
        if (Bukkit.getWorld(worldName) != null) {
            sender.sendMessage("§cEine Welt mit diesem Namen existiert bereits.");
            return;
        }

        sender.sendMessage("§7Erstelle Welt §6" + worldName + "§7...");
        WorldCreator worldCreator = new WorldCreator(worldName);

        // Optional environment type
        if (args.length >= 3) {
            try {
                World.Environment env = World.Environment.valueOf(args[2].toUpperCase());
                worldCreator.environment(env);
                sender.sendMessage("§7Umgebungstyp: §6" + env.name());
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cUngültiger Umgebungstyp. Nutze NORMAL, NETHER oder THE_END.");
                return;
            }
        }

        World world = worldCreator.createWorld();
        if (world != null) {
            sender.sendMessage("§aWelt §6" + worldName + "§a wurde erfolgreich erstellt.");
        } else {
            sender.sendMessage("§cFehler beim Erstellen der Welt §6" + worldName + "§c.");
        }
    }

    private void listWorlds(CommandSender sender) {
        sender.sendMessage("§6§lVerfügbare Welten:");
        for (World world : Bukkit.getWorlds()) {
            if (sender instanceof Player player) {
                // Create clickable component
                TextComponent worldComponent = Component.text("- ", NamedTextColor.GRAY)
                        .append(Component.text(world.getName(), NamedTextColor.GREEN)
                                .hoverEvent(HoverEvent.showText(Component.text("Klicke für Aktionen", NamedTextColor.YELLOW)))
                                .clickEvent(ClickEvent.runCommand("/editworlds options " + world.getName()))
                        )
                        .append(Component.text(" (Typ: ", NamedTextColor.GRAY))
                        .append(Component.text(world.getEnvironment().toString(), NamedTextColor.WHITE))
                        .append(Component.text(", Spieler: ", NamedTextColor.GRAY))
                        .append(Component.text(String.valueOf(world.getPlayers().size()), NamedTextColor.WHITE))
                        .append(Component.text(")", NamedTextColor.GRAY));

                player.sendMessage(worldComponent);
            } else {
                // For console, send regular message
                sender.sendMessage("§7- §a" + world.getName() +
                        " §7(Typ: §f" + world.getEnvironment() +
                        "§7, Spieler: §f" + world.getPlayers().size() + "§7)");
            }
        }
    }

    private void copyWorld(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cBenutzung: /editworlds copy <Quellwelt> <Zielwelt>");
            return;
        }

        String sourceWorldName = args[1];
        String targetWorldName = args[2];

        World sourceWorld = Bukkit.getWorld(sourceWorldName);
        if (sourceWorld == null) {
            sender.sendMessage("§cDie Quellwelt §6" + sourceWorldName + "§c existiert nicht.");
            return;
        }

        if (Bukkit.getWorld(targetWorldName) != null) {
            sender.sendMessage("§cEine Welt mit dem Namen §6" + targetWorldName + "§c existiert bereits.");
            return;
        }

        sender.sendMessage("§7Kopiere Welt §6" + sourceWorldName + "§7 nach §6" + targetWorldName + "§7...");

        WorldCreator worldCreator = new WorldCreator(targetWorldName);
        worldCreator.copy(sourceWorld);
        World newWorld = worldCreator.createWorld();

        if (newWorld != null) {
            sender.sendMessage("§aWelt §6" + sourceWorldName + "§a wurde erfolgreich nach §6" + targetWorldName + "§a kopiert.");
        } else {
            sender.sendMessage("§cFehler beim Kopieren der Welt.");
        }
    }

    private void deleteWorld(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cBitte gib den Namen der zu löschenden Welt an.");
            return;
        }

        String worldName = args[1];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            sender.sendMessage("§cDie Welt §6" + worldName + "§c existiert nicht.");
            return;
        }

        // Teleport all players out of the world
        for (Player player : world.getPlayers()) {
            player.teleport(Objects.requireNonNull(Bukkit.getWorlds().get(0).getSpawnLocation()));
            player.sendMessage("§cDu wurdest teleportiert, weil die Welt gelöscht wird.");
        }

        // Unload the world
        if (Bukkit.unloadWorld(world, false)) {
            // Try to delete the world folder
            try {
                File worldFolder = world.getWorldFolder();
                deleteRecursively(worldFolder);
                sender.sendMessage("§aWelt §6" + worldName + "§a wurde erfolgreich gelöscht.");
            } catch (IOException e) {
                sender.sendMessage("§cFehler beim Löschen der Weltdateien: " + e.getMessage());
            }
        } else {
            sender.sendMessage("§cFehler beim Entladen der Welt §6" + worldName + "§c.");
        }
    }

    private void deleteRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                deleteRecursively(child);
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete " + file);
        }
    }

    private void teleportToWorld(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cBitte gib den Namen der Zielwelt an.");
            return;
        }

        String worldName = args[1];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            sender.sendMessage("§cDie Welt §6" + worldName + "§c existiert nicht.");
            return;
        }

        player.teleport(world.getSpawnLocation());
        player.sendMessage("§aDu wurdest zur Welt §6" + worldName + "§a teleportiert.");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== EditWorlds Hilfe ===");
        sender.sendMessage("§a/editworlds create <Name> [Umgebung] §7- Erstellt eine neue Welt");
        sender.sendMessage("§a/editworlds list §7- Zeigt alle verfügbaren Welten");
        sender.sendMessage("§a/editworlds copy <Quellwelt> <Zielwelt> §7- Kopiert eine Welt");
        sender.sendMessage("§a/editworlds delete <Name> §7- Löscht eine Welt");
        sender.sendMessage("§a/editworlds tp <Name> §7- Teleportiert zur angegebenen Welt");
        sender.sendMessage("§a/editworlds help §7- Zeigt diese Hilfe");

        if (sender.hasPermission("monsterarmybattle.admin")) {
            sender.sendMessage("§6§l=== Admin Info ===");
            sender.sendMessage("§7Weitere mögliche Funktionen, die ergänzt werden könnten:");
            sender.sendMessage("§7- setspawn: Festlegen des Spawnpunkts einer Welt");
            sender.sendMessage("§7- gamerule: Ändern der Spielregeln einer Welt");
            sender.sendMessage("§7- border: Verwaltung der Weltbegrenzung");
            sender.sendMessage("§7- seed: Anzeigen oder Setzen des World Seeds");
            sender.sendMessage("§7- time: Ändern der Tageszeit einer Welt");
            sender.sendMessage("§7- weather: Ändern des Wetters in einer Welt");
        }
    }

    private void showWorldOptions(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cBitte gib einen Weltnamen an.");
            return;
        }

        String worldName = args[1];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage("§cDie Welt §6" + worldName + "§c existiert nicht.");
            return;
        }

        Player player = (Player) sender;
        player.sendMessage("§6§l=== Optionen für Welt: §e" + worldName + " §6§l===");

        // Delete option
        TextComponent deleteComponent = Component.text("» ", NamedTextColor.GOLD)
                .append(Component.text("Welt löschen", NamedTextColor.RED, TextDecoration.BOLD))
                .hoverEvent(HoverEvent.showText(Component.text("Klicke, um die Welt zu löschen", NamedTextColor.RED)))
                .clickEvent(ClickEvent.runCommand("/editworlds delete " + worldName));

        // Teleport self option
        TextComponent tpSelfComponent = Component.text("» ", NamedTextColor.GOLD)
                .append(Component.text("Dich teleportieren", NamedTextColor.GREEN, TextDecoration.BOLD))
                .hoverEvent(HoverEvent.showText(Component.text("Klicke, um dich in diese Welt zu teleportieren", NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.runCommand("/editworlds tp " + worldName));

        // Teleport player option
        TextComponent tpPlayerComponent = Component.text("» ", NamedTextColor.GOLD)
                .append(Component.text("Spieler teleportieren", NamedTextColor.AQUA, TextDecoration.BOLD))
                .hoverEvent(HoverEvent.showText(Component.text("Klicke, um einen Spieler in diese Welt zu teleportieren", NamedTextColor.AQUA)))
                .clickEvent(ClickEvent.suggestCommand("/tp @p " + worldName + " ~ ~ ~"));

        player.sendMessage(deleteComponent);
        player.sendMessage(tpSelfComponent);
        player.sendMessage(tpPlayerComponent);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommand
            List<String> subCommands = Arrays.asList("create", "list", "copy", "delete", "tp", "teleport", "help");
            return filterCompletions(subCommands, args[0]);
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "create":
                    if (args.length == 3) {
                        // Environment types
                        return filterCompletions(Arrays.asList("NORMAL", "NETHER", "THE_END"), args[2]);
                    }
                    break;
                case "copy":
                case "delete":
                case "tp":
                case "teleport":
                    // Suggest world names
                    if (args.length == 2) {
                        List<String> worldNames = Bukkit.getWorlds().stream()
                                .map(World::getName)
                                .collect(Collectors.toList());
                        return filterCompletions(worldNames, args[1]);
                    }
                    break;
            }
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> options, String input) {
        if (input.isEmpty()) {
            return options;
        }

        return options.stream()
                .filter(opt -> opt.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}