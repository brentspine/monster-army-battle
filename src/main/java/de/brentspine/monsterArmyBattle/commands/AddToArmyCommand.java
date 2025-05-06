package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.util.GameState;
import de.brentspine.monsterArmyBattle.listeners.FarmListeners;
import de.brentspine.monsterArmyBattle.util.Team;
import de.brentspine.monsterArmyBattle.util.TeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AddToArmyCommand implements CommandExecutor, TabCompleter {

    private final List<EntityType> USABLE_ENTITY_TYPES;
    private final Random random = new Random();

    public AddToArmyCommand() {
        // Initialize usable entity types (all living entities except blocked ones)
        List<EntityType> livingEntityTypes = Arrays.stream(EntityType.values())
                .filter(type -> {
                    try {
                        return type.getEntityClass() != null && LivingEntity.class.isAssignableFrom(type.getEntityClass());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        // Remove blocked entities
        livingEntityTypes.removeAll(Arrays.asList(FarmListeners.BLOCKED_LIVING_ENTITIES));
        USABLE_ENTITY_TYPES = livingEntityTypes;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("monsterarmybattle.addtoarmy")) {
            player.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (MonsterArmyBattle.gameState != GameState.FARM && MonsterArmyBattle.gameState != GameState.CONFIGURATION) {
            player.sendMessage("§cThis command can only be used before the battle phase.");
            return true;
        }

        if (args.length < 2) {
            sendUsage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        int teamId;

        try {
            teamId = Integer.parseInt(args[1]) - 1; // Convert to 0-based index
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid team ID. Please provide a number.");
            return true;
        }

        Team team = TeamManager.getTeam(teamId);
        if (team == null) {
            player.sendMessage("§cTeam with ID " + (teamId + 1) + " does not exist.");
            return true;
        }

        switch (subCommand) {
            case "add":
                handleAddCommand(player, team, args);
                break;
            case "remove":
                handleRemoveCommand(player, team, args);
                break;
            case "set":
                handleSetCommand(player, team, args);
                break;
            case "list":
                handleListCommand(player, team);
                break;
            case "clear":
                handleClearCommand(player, team);
                break;
            case "fillrandom":
                handleFillRandomCommand(player, team, args);
                break;
            case "fillall":
                handleFillAllCommand(player, team, args);
                break;
            default:
                sendUsage(player);
                break;
        }

        return true;
    }

    private void handleAddCommand(Player player, Team team, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUsage: /addtoarmy add <teamid> <type> <amount>");
            return;
        }

        try {
            EntityType type = EntityType.valueOf(args[2].toUpperCase());
            int amount = Integer.parseInt(args[3]);

            if (!USABLE_ENTITY_TYPES.contains(type)) {
                player.sendMessage("§cEntity type not allowed or does not exist.");
                return;
            }

            team.getArmy().add(type, amount, team);
            player.sendMessage("§aAdded " + amount + " " + type.name() + " to Team " + team.getId() + "'s army.");
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid entity type or amount. Check your input.");
        }
    }

    private void handleRemoveCommand(Player player, Team team, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUsage: /addtoarmy remove <teamid> <type> <amount>");
            return;
        }

        try {
            EntityType type = EntityType.valueOf(args[2].toUpperCase());
            int amount = Integer.parseInt(args[3]);

            if (!USABLE_ENTITY_TYPES.contains(type)) {
                player.sendMessage("§cEntity type not allowed or does not exist.");
                return;
            }

            team.getArmy().add(type, -amount, team);
            player.sendMessage("§aRemoved " + amount + " " + type.name() + " from Team " + team.getId() + "'s army.");
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid entity type or amount. Check your input.");
        }
    }

    private void handleSetCommand(Player player, Team team, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUsage: /addtoarmy set <teamid> <type> <amount>");
            return;
        }

        try {
            EntityType type = EntityType.valueOf(args[2].toUpperCase());
            int amount = Integer.parseInt(args[3]);

            if (!USABLE_ENTITY_TYPES.contains(type)) {
                player.sendMessage("§cEntity type not allowed or does not exist.");
                return;
            }

            team.getArmy().set(type, amount);
            player.sendMessage("§aSet " + type.name() + " to " + amount + " in Team " + team.getId() + "'s army.");
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid entity type or amount. Check your input.");
        }
    }

    private void handleListCommand(Player player, Team team) {
        player.sendMessage("§e--- Army of Team " + team.getId() + " ---");

        AtomicBoolean isEmpty = new AtomicBoolean(true);
        team.getArmy().getArmy().forEach((type, storage) -> {
            if (storage.getAmount() <= 0) return; // Skip if amount is zero or negative
            isEmpty.set(false);
            player.sendMessage("§7- " + type.name() + ": §b" + storage.getAmount());
        });
        if (isEmpty.get()) {
            player.sendMessage("§7This team's army is empty.");
        }
    }

    private void handleClearCommand(Player player, Team team) {
        team.getArmy().clear();
        player.sendMessage("§aCleared the army of Team " + team.getId() + ".");
    }

    private void handleFillRandomCommand(Player player, Team team, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /addtoarmy fillrandom <teamid> <amount>");
            return;
        }

        try {
            int amount = Integer.parseInt(args[2]);

            for (int i = 0; i < amount; i++) {
                EntityType randomType = USABLE_ENTITY_TYPES.get(random.nextInt(USABLE_ENTITY_TYPES.size()));
                team.getArmy().add(randomType, 1, team);
            }

            player.sendMessage("§aAdded " + amount + " random mobs to Team " + team.getId() + "'s army.");
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount. Please provide a number.");
        }
    }

    private void handleFillAllCommand(Player player, Team team, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /addtoarmy fillall <teamid> <amount>");
            return;
        }

        try {
            int amount = Integer.parseInt(args[2]);

            for (EntityType type : USABLE_ENTITY_TYPES) {
                team.getArmy().add(type, amount, team);
            }

            player.sendMessage("§aAdded " + amount + " of each usable mob type to Team " + team.getId() + "'s army.");
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount. Please provide a number.");
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage("§c--- AddToArmy Command Usage ---");
        player.sendMessage("§c/addtoarmy add <teamid> <type> <amount>");
        player.sendMessage("§c/addtoarmy remove <teamid> <type> <amount>");
        player.sendMessage("§c/addtoarmy set <teamid> <type> <amount>");
        player.sendMessage("§c/addtoarmy list <teamid>");
        player.sendMessage("§c/addtoarmy clear <teamid>");
        player.sendMessage("§c/addtoarmy fillrandom <teamid> <amount>");
        player.sendMessage("§c/addtoarmy fillall <teamid> <amount>");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Subcommands
            List<String> subCommands = Arrays.asList("add", "remove", "set", "list", "clear", "fillrandom", "fillall");
            return filterStartsWith(subCommands, args[0]);
        } else if (args.length == 2) {
            // Team IDs
            for (int i = 1; i <= TeamManager.getTeams().size(); i++) {
                completions.add(String.valueOf(i));
            }
            return filterStartsWith(completions, args[1]);
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("add") ||
                args[0].equalsIgnoreCase("remove") ||
                args[0].equalsIgnoreCase("set"))) {
            // Entity Types
            for (EntityType type : USABLE_ENTITY_TYPES) {
                completions.add(type.name());
            }
            return filterStartsWith(completions, args[2]);
        } else if (args.length == 4 && (args[0].equalsIgnoreCase("add") ||
                args[0].equalsIgnoreCase("remove") ||
                args[0].equalsIgnoreCase("set"))) {
            // Suggest some common amounts
            return filterStartsWith(Arrays.asList("1", "5", "10", "25", "50", "100"), args[3]);
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("fillrandom") ||
                args[0].equalsIgnoreCase("fillall"))) {
            // Suggest some common amounts
            return filterStartsWith(Arrays.asList("1", "5", "10", "25"), args[2]);
        }

        return completions;
    }

    private List<String> filterStartsWith(List<String> list, String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }
}
