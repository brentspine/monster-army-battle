package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.util.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class StartBattleCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("monsterarmybattle.startbattle")) {
            sender.sendMessage("§cDu hast leider keine Berechtigung diesen Command auszuführen.");
            return true;
        }

        if (MonsterArmyBattle.gameState != GameState.CONFIGURATION) {
            sender.sendMessage("§cWir befinden uns nicht in der Config-Phase.");
            return true;
        }

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Creating battle worlds");
        sender.sendMessage("§7Erstelle Kampfwelten...");

        // Use the specified world or default to "arena"
        String templateWorldName = args.length > 0 ? args[0] : "arena";
        World templateWorld = Bukkit.getWorld(templateWorldName);

        if (templateWorld == null) {
            sender.sendMessage("§cDie Welt \"" + templateWorldName + "\" konnte nicht gefunden werden.");
            return true;
        }

        templateWorld.getEntities().forEach(Entity::remove);
        templateWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        templateWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        templateWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        templateWorld.setStorm(false);
        templateWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);

        sender.sendMessage("§7Verwende Welt §6" + templateWorldName + "§7 als Vorlage.");

        // Copy world for each team
        int worldCounter = 1;
        for (Team team : TeamManager.getTeams()) {
            String battleWorldName = "battle_" + worldCounter;
            sender.sendMessage("§7Erstelle Kampfwelt für Team " + team.getId() + ": §6" + battleWorldName);

            // Delete the world folder if it exists (cleanup)
            File targetWorldFolder = new File(Bukkit.getWorldContainer(), battleWorldName);
            if (targetWorldFolder.exists()) {
                try {
                    // Delete session lock and uid.dat if they exist
                    File sessionLock = new File(targetWorldFolder, "session.lock");
                    if (sessionLock.exists()) sessionLock.delete();

                    File uidDat = new File(targetWorldFolder, "uid.dat");
                    if (uidDat.exists()) uidDat.delete();

                    // Delete the entire directory
                    FileUtils.deleteDirectory(targetWorldFolder);
                } catch (IOException e) {
                    sender.sendMessage("§cFehler beim Löschen der alten Weltdaten: " + e.getMessage());
                    MonsterArmyBattle.instance.getLogger().log(Level.SEVERE, "Error deleting world folder", e);
                    return true;
                }
            }

            // Copy the template world
            try {
                File sourceWorldFolder = templateWorld.getWorldFolder();

                // Create the target directory
                targetWorldFolder.mkdirs();

                // Delete lock files from the copied world
                File sessionLock = new File(sourceWorldFolder, "session.lock");
                if (sessionLock.exists()) sessionLock.delete();

                // Copy the world files
                FileUtils.copyDirectoryStructure(sourceWorldFolder, targetWorldFolder);

                File uidDat = new File(targetWorldFolder, "uid.dat");
                if (uidDat.exists()) uidDat.delete();

            } catch (IOException e) {
                sender.sendMessage("§cFehler beim Kopieren der Weltdaten: " + e.getMessage());
                MonsterArmyBattle.instance.getLogger().log(Level.SEVERE, "Error copying world data", e);
                return true;
            }

            // Load the copied world
            WorldCreator creator = new WorldCreator(battleWorldName);
            creator.environment(templateWorld.getEnvironment());
            World battleWorld = creator.createWorld();

            if (battleWorld != null) {
                // Assign the world to the team
                team.setArenaWorld(battleWorld);
                //sender.sendMessage("§7Kampfwelt für Team " + team.getId() + " wurde erstellt.");
            } else {
                sender.sendMessage("§cFehler beim Erstellen der Kampfwelt für Team " + team.getId());
                return true;
            }

            worldCounter++;
        }

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Starting timer");
        Timer.getInstance().setAscending(true);
        Timer.getInstance().set(0);
        Timer.getInstance().resume();

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Loading wave configurations and managers");
        for(Team team : TeamManager.getTeams()) {
            WaveManager waveManager = new WaveManager(team, templateWorldName);
            team.setWaveManager(waveManager);
        }

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Teleporting players to battle worlds");
        sender.sendMessage("§7Teleportiere Spieler in die Kampfwelt...");
        for (Team team : TeamManager.getTeams()) {
            team.getArenaWorld().getEntities().forEach(Entity::remove);
            team.getArenaWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            team.getArenaWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            team.getArenaWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            team.getArenaWorld().setStorm(false);
            team.getArenaWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
            team.getArenaWorld().setTime(0);
            team.getArenaWorld().setDifficulty(Difficulty.NORMAL);
            for (Player player : team.getPlayers()) {
                player.teleport(team.getWaveManager().getPlayerSpawnLocation());
                player.setRespawnLocation(team.getWaveManager().getDeathLocation());
                player.sendMessage("§7Teleportiere in die Kampfwelt");
            }
        }

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Battle phase started!");
        MonsterArmyBattle.gameState = GameState.BATTLE;
        sender.sendMessage("§aDie Kampfphase wurde gestartet.");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            // Return all world names for the first argument
            List<String> worldNames = Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(name -> !name.startsWith("farm_") && !name.startsWith("battle_") && !name.equals("lobby") && !name.equals("world") && !name.equals("world_nether") && !name.equals("world_the_end"))
                    .collect(Collectors.toList());

            if (args[0].isEmpty()) {
                return worldNames;
            } else {
                return worldNames.stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}