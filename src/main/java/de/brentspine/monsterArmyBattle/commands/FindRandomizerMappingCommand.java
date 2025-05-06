package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.util.GameState;
import de.brentspine.monsterArmyBattle.util.RandomDropManager;
import de.brentspine.monsterArmyBattle.util.Team;
import de.brentspine.monsterArmyBattle.util.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class FindRandomizerMappingCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("monsterarmybattle.findrandomizermapping")) {
            sender.sendMessage("§cDu hast keine Berechtigung diesen Command auszuführen.");
            return true;
        }

        if (MonsterArmyBattle.gameState == GameState.LOBBY) {
            sender.sendMessage("§cEs wurden noch keine Randomizer erstellt. Starte das Spiel zuerst.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cVerwendung: /findrandomizermapping <material>");
            return true;
        }

        String materialName = args[0].toUpperCase();
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cUngültiges Material: " + materialName);
            return true;
        }

        RandomDropManager randomDropManager = RandomDropManager.getInstance();

        // Find what this material drops
        sender.sendMessage("§6§l" + material.name() + " §edroppt:");

        for (Team team : TeamManager.getTeams()) {
            sender.sendMessage("§7Team " + team.getId() + " §8(§7" + team.getPlayers().size() + " Spieler§8):");

            // Check if each player has unique mappings
            boolean uniqueMappings = false;
            Material firstPlayerDrop = null;

            for (Player player : team.getPlayers()) {
                Material drop = randomDropManager.getRandomizedDrop(player, material);

                if (firstPlayerDrop == null) {
                    firstPlayerDrop = drop;
                } else if (!drop.equals(firstPlayerDrop)) {
                    uniqueMappings = true;
                    break;
                }
            }

            if (uniqueMappings) {
                // Show drop for each player
                for (Player player : team.getPlayers()) {
                    Material drop = randomDropManager.getRandomizedDrop(player, material);
                    sender.sendMessage("  §8- §7" + player.getName() + ": §a" + drop.name());
                }
            } else if (!team.getPlayers().isEmpty()) {
                // All players have same drop
                Material drop = randomDropManager.getRandomizedDrop(team.getPlayers().get(0), material);
                sender.sendMessage("  §8- §aAlle: §a" + drop.name());
            }
        }

        // Find materials that drop this
        sender.sendMessage("\n§6§l" + material.name() + " §ekann gedroppt werden von:");

        for (Team team : TeamManager.getTeams()) {
            sender.sendMessage("§7Team " + team.getId() + ":");

            Map<Player, List<Material>> playerSources = new HashMap<>();

            // For each player, find materials that drop the target
            for (Player player : team.getPlayers()) {
                List<Material> sourceMaterials = new ArrayList<>();

                // Get the player's mapping to check what drops the target material
                UUID playerId = player.getUniqueId();
                Map<Material, Material> mapping = randomDropManager.getPlayerMapping(player);

                if (mapping != null) {
                    for (Map.Entry<Material, Material> entry : mapping.entrySet()) {
                        if (entry.getValue() == material) {
                            sourceMaterials.add(entry.getKey());
                        }
                    }
                }

                playerSources.put(player, sourceMaterials);
            }

            // Check if all players have the same sources
            boolean sameSources = team.getPlayers().size() > 1;
            List<Material> firstPlayerSources = team.getPlayers().isEmpty() ?
                    Collections.emptyList() : playerSources.get(team.getPlayers().get(0));

            for (Player player : team.getPlayers()) {
                if (!playerSources.get(player).equals(firstPlayerSources)) {
                    sameSources = false;
                    break;
                }
            }

            if (sameSources && !team.getPlayers().isEmpty()) {
                // Display common sources
                if (firstPlayerSources.isEmpty()) {
                    sender.sendMessage("  §8- §cKeine Quellen gefunden");
                } else {
                    sender.sendMessage("  §8- §aAlle: §7" + formatMaterialList(firstPlayerSources));
                }
            } else {
                // Display per player
                for (Player player : team.getPlayers()) {
                    List<Material> sources = playerSources.get(player);
                    if (sources.isEmpty()) {
                        sender.sendMessage("  §8- §7" + player.getName() + ": §cKeine Quellen gefunden");
                    } else {
                        sender.sendMessage("  §8- §7" + player.getName() + ": §7" + formatMaterialList(sources));
                    }
                }
            }
        }

        return true;
    }

    private String formatMaterialList(List<Material> materials) {
        if (materials.size() <= 3) {
            return materials.stream()
                    .map(Material::name)
                    .collect(Collectors.joining(", "));
        } else {
            return materials.stream()
                    .limit(3)
                    .map(Material::name)
                    .collect(Collectors.joining(", ")) +
                    " §8(+" + (materials.size() - 3) + " weitere)";
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String current = args[0].toUpperCase();
            return Arrays.stream(Material.values())
                    .map(Material::name)
                    .filter(name -> name.startsWith(current))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}