package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.inventories.TeamSelectionInventory;
import de.brentspine.monsterArmyBattle.util.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class StartCommand implements CommandExecutor {

    private static final boolean RANDOMIZE_EACH_PLAYER = true;
    private static Difficulty FARM_DIFFICULTY = Difficulty.NORMAL;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(!sender.hasPermission("monsterarmybattle.start")) {
            sender.sendMessage("§cDu hast leider keine Berechtigung diesen Command auszuführen.");
            return true;
        }
        if(MonsterArmyBattle.gameState != GameState.LOBBY) {
            sender.sendMessage("§cWir befinden uns bereits in der "+ MonsterArmyBattle.gameState.getName() +" Phase.");
            return true;
        }
        // Check for players not in teams
        boolean hasConfirm = strings.length > 0 && strings[0].equalsIgnoreCase("confirm");

        // Find all players not in any team
        java.util.List<org.bukkit.entity.Player> playersNotInTeam = new java.util.ArrayList<>();
        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            boolean isInTeam = false;
            for (de.brentspine.monsterArmyBattle.util.Team team : TeamManager.getTeams()) {
                if (team.isInTeam(player)) {
                    isInTeam = true;
                    break;
                }
            }
            if (!isInTeam) {
                playersNotInTeam.add(player);
            }
        }

        // Require confirmation if players are not in teams
        if (!playersNotInTeam.isEmpty() && !hasConfirm) {
            sender.sendMessage("§c" + playersNotInTeam.size() + " Spieler sind nicht in einem Team!");
            sender.sendMessage("§cNutze '/start confirm' um trotzdem zu starten.");
            StringBuilder playerList = new StringBuilder();
            for (org.bukkit.entity.Player player : playersNotInTeam) {
                playerList.append(player.getName()).append(", ");
            }
            playerList.delete(playerList.length() - 2, playerList.length());
            sender.sendMessage("§7(" + playerList + ")");
            return true;
        }

        // Check if there are teams with no players
        int teamCount = TeamManager.getTeams().size();
        for (Team team : TeamManager.getTeams()) {
            if (team.getPlayers().isEmpty()) {
                teamCount--;
            }
        }
        if(teamCount < 2) {
            sender.sendMessage("§cEs müssen mindestens 2 Teams mit mindestens einem Spieler besetzt werden.");
            return true;
        }
        if(TeamManager.getTeams().size() - teamCount > 0) {
            sender.sendMessage("§eINFO: Es wurden "+ (TeamManager.getTeams().size() - teamCount) +" Teams ohne Spieler entfernt.");
            TeamManager.getTeams().removeIf(team -> team.getPlayers().isEmpty());
            // Reassign team IDs consecutively to avoid gaps
            for (int i = 0; i < TeamManager.getTeams().size(); i++) {
                Team team = TeamManager.getTeams().get(i);
                team.setId(i + 1); // Set IDs starting from 1
            }
        }
        TeamManager.getTeams().removeIf(team -> team.getPlayers().isEmpty());


        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Creating farm worlds");
        sender.sendMessage("§7Erstelle Farmwelten...");
        for(int i = 1; i <= TeamManager.getTeams().size(); i++) {
            WorldCreator worldCreator = new WorldCreator("farm_" + i);
            World world = worldCreator.createWorld();
            assert world != null;
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setDifficulty(FARM_DIFFICULTY);
            TeamManager.getTeams().get(i-1).setFarmWorld(world);

            worldCreator = new WorldCreator("farm_nether_" + i);
            worldCreator.environment(World.Environment.NETHER);
            world = worldCreator.createWorld();
            assert world != null;
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setDifficulty(FARM_DIFFICULTY);
            TeamManager.getTeams().get(i-1).setFarmWorldNether(world);

            /*worldCreator = new WorldCreator("farm_the_end_" + i);
            world = worldCreator.createWorld();
            worldCreator.environment(World.Environment.THE_END);
            assert world != null;
            TeamManager.getTeams().get(i-1).setFarmWorldEnd(world);*/
            TeamManager.getTeams().get(i-1).setFarmWorldEnd(Bukkit.getWorld("world_the_end"));
        }

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Teleporting players");
        sender.sendMessage("§7Teleportiere Spieler in die Welten...");
        for (Team team : TeamManager.getTeams()) {
            for (Player player : team.getPlayers()) {
                player.teleport(team.getFarmWorld().getSpawnLocation());
                player.setRespawnLocation(team.getFarmWorld().getSpawnLocation());
            }
        }

        sender.sendMessage("§7Erstelle Randomizer...");
        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Creating randomizer");
        for(Team team : TeamManager.getTeams()) {
            RandomDropManager.getInstance().randomizeDropsForTeam(team, RANDOMIZE_EACH_PLAYER);
            MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Randomizer for team "+ team.getId() +" created");
        }

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Starting timer and changing game state");
        MonsterArmyBattle.gameState = GameState.FARM;
        Timer.getInstance().start();
        Timer.getInstance().setAscending(false);
        Timer.getInstance().set(60*90);

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Farmphase init complete");
        sender.sendMessage("§aDie Farmphase wurde gestartet.");
        //TeamSelectionInventory.closeAll();
        return true;
    }

}
