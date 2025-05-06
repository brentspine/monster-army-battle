package de.brentspine.monsterArmyBattle.util;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;

public class Resetter {

    public static void resetWorlds() {
        MonsterArmyBattle.playerJoinListener.allowJoin = false;
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(Component.text("Server is resetting")));

        File serverDir = Bukkit.getWorldContainer();
        File[] worldFolders = serverDir.listFiles(file -> file.isDirectory() &&
            (file.getName().startsWith("farm_") || file.getName().startsWith("battle_") || file.getName().equals("lobby")) || file.getName().equals("world_the_end"));

        if (worldFolders != null) {
            for (File worldFolder : worldFolders) {
                String worldName = worldFolder.getName();
                MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Found world folder " + worldName);

                // Check if world is loaded, and unload it first
                World loadedWorld = Bukkit.getWorld(worldName);
                if (loadedWorld != null) {
                    MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Unloading world " + worldName);
                    MonsterArmyBattle.instance.getServer().unloadWorld(loadedWorld, false);
                }

                // Delete the world folder
                MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Deleting world folder " + worldName);
                try {
                    FileUtils.deleteDirectory(worldFolder);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Creating lobby");
        WorldCreator worldCreator = new WorldCreator("lobby");
        World world = worldCreator.createWorld();
        assert world != null;
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        MonsterArmyBattle.lobbySpawnLocation = world.getSpawnLocation();
        MonsterArmyBattle.playerJoinListener.allowJoin = true;
        world.setStorm(false);
        world.setClearWeatherDuration(1000000);
        world.setTime(1000);
        world.setDifficulty(Difficulty.PEACEFUL);

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Recreating end");
        WorldCreator endWorldCreator = new WorldCreator("world_the_end");
        endWorldCreator.environment(World.Environment.THE_END);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world = endWorldCreator.createWorld();
        assert world != null;

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Resetter ran successfully");
    }

    public static void revokeAdvancements(Player player) {
        Iterator<Advancement> iterator = Bukkit.getServer().advancementIterator();
        while (iterator.hasNext())
        {
            AdvancementProgress progress = player.getAdvancementProgress(iterator.next());
            for (String criteria : progress.getAwardedCriteria())
                progress.revokeCriteria(criteria);
        }
    }
}
