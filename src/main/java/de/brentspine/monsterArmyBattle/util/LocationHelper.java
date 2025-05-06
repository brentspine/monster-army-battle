package de.brentspine.monsterArmyBattle.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Utility class for handling Location serialization and deserialization
 * to and from configuration files.
 */
public class LocationHelper {

    /**
     * Saves a location to a configuration section
     *
     * @param location The location to save
     * @param config   The configuration section to save to
     * @param path     The path within the configuration section
     */
    public static void saveLocation(Location location, ConfigurationSection config, String path) {
        if (location == null || config == null) return;
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }

    /**
     * Loads a location from a configuration section
     *
     * @param config The configuration section to load from
     * @param path   The path within the configuration section
     * @return The loaded location or null if it couldn't be loaded
     */
    public static Location loadLocation(ConfigurationSection config, String path) {
        if (config == null || !config.contains(path)) return null;
        String worldName = config.getString(path + ".world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().warning("Could not find world " + worldName + " for location at " + path);
            return null;
        }
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw", 0.0);
        float pitch = (float) config.getDouble(path + ".pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Creates a location string representation for storage
     * Format: worldName:x:y:z:yaw:pitch
     *
     * @param location The location to convert
     * @return String representation of the location
     */
    public static String locationToString(Location location) {
        if (location == null || location.getWorld() == null) return null;
        return String.format("%s:%f:%f:%f:%f:%f", location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Parses a location from its string representation
     * Format: worldName:x:y:z:yaw:pitch
     *
     * @param str The string to parse
     * @return The parsed location or null if invalid
     */
    public static Location locationFromString(String str) {
        if (str == null) return null;
        String[] parts = str.split(":");
        if (parts.length < 4) return null;
        try {
            String worldName = parts[0];
            World world = Bukkit.getWorld(worldName);
            if (world == null) return null;
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);

            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0f;

            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Saves a block location to a configuration section
     *
     * @param location The location of the block to save
     * @param config   The configuration section to save to
     * @param path     The path within the configuration section
     */
    public static void saveBlockLocation(Location location, ConfigurationSection config, String path) {
        if (location == null || config == null) return;
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getBlockX());
        config.set(path + ".y", location.getBlockY());
        config.set(path + ".z", location.getBlockZ());
    }

    /**
     * Loads a block location from a configuration section
     *
     * @param config The configuration section to load from
     * @param path   The path within the configuration section
     * @return The loaded block location or null if it couldn't be loaded
     */
    public static Location loadBlockLocation(ConfigurationSection config, String path) {
        if (config == null || !config.contains(path)) return null;
        String worldName = config.getString(path + ".world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().warning("Could not find world " + worldName + " for block location at " + path);
            return null;
        }
        int x = config.getInt(path + ".x");
        int y = config.getInt(path + ".y");
        int z = config.getInt(path + ".z");
        return new Location(world, x, y, z);
    }

    /**
     * Creates a block location string representation for storage
     * Format: worldName:x:y:z
     *
     * @param location The block location to convert
     * @return String representation of the block location
     */
    public static String blockLocationToString(Location location) {
        if (location == null || location.getWorld() == null) return null;
        return String.format("%s:%d:%d:%d", location.getWorld().getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Parses a block location from its string representation
     * Format: worldName:x:y:z
     *
     * @param str The string to parse
     * @return The parsed block location or null if invalid
     */
    public static Location blockLocationFromString(String str) {
        if (str == null) return null;
        String[] parts = str.split(":");
        if (parts.length < 4) return null;
        try {
            String worldName = parts[0];
            World world = Bukkit.getWorld(worldName);
            if (world == null) return null;
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Gets the block at a specific location
     *
     * @param location The location
     * @return The block at the location or null if location is null
     */
    public static Block getBlockAt(Location location) {
        if (location == null || location.getWorld() == null) return null;
        return location.getBlock();
    }

    /**
     * Converts a regular location to a block location (center of the block)
     *
     * @param location The precise location
     * @return The block location with centered coordinates
     */
    public static Location toBlockCenter(Location location) {
        if (location == null || location.getWorld() == null) return null;
        return new Location(
                location.getWorld(),
                location.getBlockX() + 0.5,
                location.getBlockY() + 0.5,
                location.getBlockZ() + 0.5,
                location.getYaw(),
                location.getPitch()
        );
    }
}