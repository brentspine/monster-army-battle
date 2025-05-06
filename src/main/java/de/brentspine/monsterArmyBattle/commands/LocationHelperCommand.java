package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.util.LocationHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LocationHelperCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final File locationsFile;
    private FileConfiguration locationsConfig;
    private static final String PERMISSION_BASE = "monsterarmy.location";
    private static final String PERMISSION_SAVE = PERMISSION_BASE + ".save";
    private static final String PERMISSION_DELETE = PERMISSION_BASE + ".delete";
    private static final String PERMISSION_TP = PERMISSION_BASE + ".teleport";
    private static final String PERMISSION_LIST = PERMISSION_BASE + ".list";

    public LocationHelperCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.locationsFile = new File(plugin.getDataFolder(), "locations.yml");
        if (!locationsFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                locationsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create locations file: " + e.getMessage());
            }
        }
        loadLocationsConfig();
    }

    private void loadLocationsConfig() {
        if (!locationsFile.exists()) {
            plugin.saveResource("locations.yml", false);
        }
        locationsConfig = YamlConfiguration.loadConfiguration(locationsFile);
    }

    private void saveLocationsConfig() {
        try {
            locationsConfig.save(locationsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save locations config: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "save":
                if (!player.hasPermission(PERMISSION_SAVE)) {
                    player.sendMessage("§cYou don't have permission to save locations.");
                    return true;
                }
                handleSave(player, args);
                break;
            case "saveblock":
                if (!player.hasPermission(PERMISSION_SAVE)) {
                    player.sendMessage("§cYou don't have permission to save block locations.");
                    return true;
                }
                handleSaveBlock(player, args);
                break;
            case "list":
                if (!player.hasPermission(PERMISSION_LIST)) {
                    player.sendMessage("§cYou don't have permission to list locations.");
                    return true;
                }
                handleList(player, args);
                break;
            case "delete":
                if (!player.hasPermission(PERMISSION_DELETE)) {
                    player.sendMessage("§cYou don't have permission to delete locations.");
                    return true;
                }
                handleDelete(player, args);
                break;
            case "tp":
                if (!player.hasPermission(PERMISSION_TP)) {
                    player.sendMessage("§cYou don't have permission to teleport to locations.");
                    return true;
                }
                handleTeleport(player, args);
                break;
            case "help":
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleSave(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cPlease specify a name for this location.");
            return;
        }

        String name = args[1];
        Location location;

        // Check if we're saving by current position or by coordinates
        if (args.length >= 6) {
            // Parse coordinates from arguments
            try {
                World world = player.getWorld();
                if (args.length >= 7) {
                    world = Bukkit.getWorld(args[6]);
                    if (world == null) {
                        player.sendMessage("§cWorld '" + args[6] + "' not found.");
                        return;
                    }
                }

                double x = Double.parseDouble(args[2]);
                double y = Double.parseDouble(args[3]);
                double z = Double.parseDouble(args[4]);
                float yaw = args.length >= 8 ? Float.parseFloat(args[7]) : 0f;
                float pitch = args.length >= 9 ? Float.parseFloat(args[8]) : 0f;

                location = new Location(world, x, y, z, yaw, pitch);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid coordinates. Use numbers for x, y, z, yaw, and pitch.");
                return;
            }
        } else {
            // Use player's current position
            location = player.getLocation();
        }

        // Save the location
        saveLocation(name, location);
        player.sendMessage("§aLocation '" + name + "' saved successfully!");
    }

    private void handleSaveBlock(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cPlease specify a name for this block location.");
            return;
        }

        String name = args[1];
        Location location;

        // Check if we're saving by target block or by coordinates
        if (args.length >= 6) {
            // Parse coordinates from arguments
            try {
                World world = player.getWorld();
                if (args.length >= 7) {
                    world = Bukkit.getWorld(args[6]);
                    if (world == null) {
                        player.sendMessage("§cWorld '" + args[6] + "' not found.");
                        return;
                    }
                }

                int x = Integer.parseInt(args[2]);
                int y = Integer.parseInt(args[3]);
                int z = Integer.parseInt(args[4]);

                location = new Location(world, x, y, z);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid coordinates. Use integer numbers for x, y, z.");
                return;
            }
        } else {
            // Use player's targeted block
            Block targetBlock = getTargetBlock(player, 100);
            if (targetBlock == null) {
                player.sendMessage("§cYou're not looking at any block within range.");
                return;
            }
            location = targetBlock.getLocation();
        }

        // Save the location
        saveBlockLocation(name, location);
        player.sendMessage("§aBlock location '" + name + "' saved successfully!");
    }

    private void handleList(Player player, String[] args) {
        String filter = args.length > 1 ? args[1].toLowerCase() : null;

        ConfigurationSection locations = locationsConfig.getConfigurationSection("locations");
        ConfigurationSection blockLocations = locationsConfig.getConfigurationSection("blocks");

        List<String> locationNames = new ArrayList<>();
        List<String> blockLocationNames = new ArrayList<>();

        if (locations != null) {
            Set<String> keys = locations.getKeys(false);
            for (String key : keys) {
                if (filter == null || key.toLowerCase().contains(filter)) {
                    locationNames.add(key);
                }
            }
        }

        if (blockLocations != null) {
            Set<String> keys = blockLocations.getKeys(false);
            for (String key : keys) {
                if (filter == null || key.toLowerCase().contains(filter)) {
                    blockLocationNames.add(key);
                }
            }
        }

        player.sendMessage("§a=== Saved Locations " +
                (filter != null ? "(filter: " + filter + ")" : "") + " ===");

        if (locationNames.isEmpty() && blockLocationNames.isEmpty()) {
            player.sendMessage("§eNo locations found.");
            return;
        }

        if (!locationNames.isEmpty()) {
            player.sendMessage("§6Regular Locations:");
            for (String name : locationNames) {
                String path = "locations." + name;
                Location location = LocationHelper.loadLocation(locationsConfig, path);
                if (location != null) {
                    player.sendMessage("§e- " + name + ": " +
                            "§7" + String.format("(World: %s, X: %.2f, Y: %.2f, Z: %.2f)",
                            location.getWorld().getName(), location.getX(), location.getY(), location.getZ()));
                }
            }
        }

        if (!blockLocationNames.isEmpty()) {
            player.sendMessage("§6Block Locations:");
            for (String name : blockLocationNames) {
                String path = "blocks." + name;
                Location location = LocationHelper.loadBlockLocation(locationsConfig, path);
                if (location != null) {
                    player.sendMessage("§e- " + name + ": " +
                            "§7" + String.format("(World: %s, X: %d, Y: %d, Z: %d)",
                            location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                }
            }
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cPlease specify a name for the location to delete.");
            return;
        }

        String name = args[1];
        boolean deleted = false;

        // Check for regular locations
        if (locationsConfig.isConfigurationSection("locations")) {
            for (String path : getAllLocationPaths("locations")) {
                if (path.equals(name)) {
                    locationsConfig.set("locations." + path, null);
                    deleted = true;
                    break;
                }
            }
        }

        // Check for block locations
        if (!deleted && locationsConfig.isConfigurationSection("blocks")) {
            for (String path : getAllLocationPaths("blocks")) {
                if (path.equals(name)) {
                    locationsConfig.set("blocks." + path, null);
                    deleted = true;
                    break;
                }
            }
        }

        if (deleted) {
            saveLocationsConfig();
            player.sendMessage("§aLocation '" + name + "' deleted successfully!");
        } else {
            player.sendMessage("§cNo location found with the name '" + name + "'.");
        }
    }

    private void handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cPlease specify a name for the location to teleport to.");
            return;
        }

        String name = args[1];
        Location location = null;

        // Search all location paths
        List<String> regularPaths = getAllLocationPaths("locations");
        for (String path : regularPaths) {
            if (path.equals(name)) {
                location = LocationHelper.loadLocation(locationsConfig, "locations." + path);
                break;
            }
        }

        // If not found in regular locations, try block locations
        if (location == null) {
            List<String> blockPaths = getAllLocationPaths("blocks");
            for (String path : blockPaths) {
                if (path.equals(name)) {
                    location = LocationHelper.loadBlockLocation(locationsConfig, "blocks." + path);
                    if (location != null) {
                        location = LocationHelper.toBlockCenter(location).clone();
                        location.setY(location.getY() + 0.5); // Position player above the block center
                    }
                    break;
                }
            }
        }

        if (location == null) {
            player.sendMessage("§cNo location found with the name '" + name + "' or world doesn't exist.");
            return;
        }

        player.teleport(location);
        player.sendMessage("§aTeleported to '" + name + "'.");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§a=== Location Helper Commands ===");
        player.sendMessage("§6/loc save <name>§7 - Save your current location");
        player.sendMessage("§6/loc save <name> <x> <y> <z> [world] [yaw] [pitch]§7 - Save a specific location");
        player.sendMessage("§6/loc saveblock <name>§7 - Save the block you're looking at");
        player.sendMessage("§6/loc saveblock <name> <x> <y> <z> [world]§7 - Save a specific block location");
        player.sendMessage("§6/loc list [filter]§7 - List all saved locations");
        player.sendMessage("§6/loc delete <name>§7 - Delete a location");
        player.sendMessage("§6/loc tp <name>§7 - Teleport to a location");
        player.sendMessage("§6/loc help§7 - Show this help message");
    }

    private void saveLocation(String name, Location location) {
        String path = "locations." + name;
        LocationHelper.saveLocation(location, locationsConfig, path);
        saveLocationsConfig();
    }

    private void saveBlockLocation(String name, Location location) {
        String path = "blocks." + name;
        LocationHelper.saveBlockLocation(location, locationsConfig, path);
        saveLocationsConfig();
    }

    private Block getTargetBlock(Player player, int range) {
        BlockIterator iterator = new BlockIterator(player, range);
        Block block;

        while (iterator.hasNext()) {
            block = iterator.next();
            if (block.getType().isSolid()) {
                return block;
            }
        }

        return null;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands
            String[] subCommands = {"save", "saveblock", "list", "delete", "tp", "help"};
            return filterCompletions(Arrays.asList(subCommands), args[0]);
        } else if (args.length == 2) {
            // Second argument - location name or filter
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("tp")) {
                return getAllLocationNames(args[1]);
            }
        } else if (args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("saveblock")) {
            if (args.length == 7) {
                // World names
                return filterCompletions(getWorldNames(), args[6]);
            }
        }

        return completions;
    }

    private List<String> getAllLocationNames(String prefix) {
        List<String> names = new ArrayList<>();

        names.addAll(getAllLocationPaths("locations"));
        names.addAll(getAllLocationPaths("blocks"));

        return filterCompletions(names, prefix);
    }

    private List<String> getAllLocationPaths(String baseSection) {
        List<String> paths = new ArrayList<>();
        ConfigurationSection section = locationsConfig.getConfigurationSection(baseSection);

        if (section != null) {
            collectNestedPaths(section, "", paths);
        }

        return paths;
    }

    private void collectNestedPaths(ConfigurationSection section, String parentPath, List<String> paths) {
        for (String key : section.getKeys(false)) {
            String currentPath = parentPath.isEmpty() ? key : parentPath + "." + key;

            if (section.isConfigurationSection(key)) {
                // This is a nested section, go deeper
                collectNestedPaths(Objects.requireNonNull(section.getConfigurationSection(key)), currentPath, paths);
            } else if (section.contains(key + ".world")) {
                // This is a location entry (contains world field)
                paths.add(currentPath);
            }
        }
    }

    private List<String> getWorldNames() {
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .collect(Collectors.toList());
    }

    private List<String> filterCompletions(List<String> options, String prefix) {
        if (prefix.isEmpty()) {
            return options;
        }

        String lowerPrefix = prefix.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }
}