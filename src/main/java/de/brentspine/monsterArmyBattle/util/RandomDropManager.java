package de.brentspine.monsterArmyBattle.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class RandomDropManager {

    private static RandomDropManager instance;

    private final JavaPlugin plugin;
    private final Map<UUID, Map<Material, Material>> playerDropMappings = new HashMap<>();

    // Materials that can be used as drops
    private final List<Material> validDrops;

    private RandomDropManager(JavaPlugin plugin) {
        this.plugin = plugin;

        // Initialize valid drops (items only)
        this.validDrops = Arrays.stream(Material.values())
                .filter(Material::isItem)
                .filter(material -> !material.isAir())
                .toList();
    }

    public static RandomDropManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new RandomDropManager(plugin);
        }
        return instance;
    }

    public static RandomDropManager getInstance() {
        return instance;
    }

    public Material getRandomizedDrop(Player player, Material originalMaterial) {
        Map<Material, Material> playerMappings = playerDropMappings.get(player.getUniqueId());
        if (playerMappings == null) {
            return originalMaterial;
        }

        return playerMappings.getOrDefault(originalMaterial, originalMaterial);
    }

    public void randomizeDropsForPlayer(Player player) {
        UUID playerId = player.getUniqueId();

        Map<Material, Material> playerMappings = getRandomMapping();

        playerDropMappings.put(playerId, playerMappings);
    }

    public Map<Material, Material> getRandomMapping() {
        Map<Material, Material> mapping = new HashMap<>();
        // Get block materials
        List<Material> blockMaterials = Arrays.stream(Material.values())
                .toList();

        // Create random mappings for the player
        Random random = new Random();
        for (Material blockMaterial : blockMaterials) {
            Material randomDrop = validDrops.get(random.nextInt(validDrops.size()));
            mapping.put(blockMaterial, randomDrop);
        }
        return mapping;
    }

    public void randomizeDropsForTeam(Team team, boolean eachPlayer) {
        if(eachPlayer) {
            for(Player player : team.getPlayers()) {
                randomizeDropsForPlayer(player);
            }
            return;
        }
        Map<Material, Material> playerMappings = getRandomMapping();
        for (Player player : team.getPlayers()) {
            playerDropMappings.put(player.getUniqueId(), playerMappings);
        }
    }

    public Map<Material, Material> getPlayerMapping(Player player) {
        return playerDropMappings.get(player.getUniqueId());
    }


}