package de.brentspine.monsterArmyBattle.util;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WaveManager implements Listener {

    private static final int RESPAWN_TIME = 20; // seconds
    private static final int INVINCIBILITY_TIME = 5; // seconds

    // Static config parts
    private static File locationsFile;
    private static ConfigurationSection config;
    private static boolean configInitialized = false;
    public static final ArrayList<BattleEvent> battleEvents = new ArrayList<>();

    // Non-static instance parts
    private final Team team;
    private final String arenaName;
    private final Location playerSpawnLocation;
    private final Location deathLocation;
    private final ArrayList<Location> spawnerLocations;
    private int teamCounterToTrackWhichTeamsArmyThisIs;
    private int wave;
    private int mobCounter;
    private int additionalMobs;
    private int mobsKilled;
    private int spawnTimer;
    private boolean isOver;
    private final ItemStack[] backpackBackup;
    private final ItemStack[][] playerInventoryBackup;
    private final Map<UUID, Integer> respawnTimers;

    private BukkitTask spawningRunnable;

    /**
     * Initializes the static configuration if not already done
     */
    private static void initializeConfig() {
        if (configInitialized) return;

        battleEvents.add(BattleEvent.createBattleStartEvent());

        Logger logger = MonsterArmyBattle.instance.getLogger();
        locationsFile = new File(MonsterArmyBattle.instance.getDataFolder(), "locations.yml");
        if (!locationsFile.exists()) {
            MonsterArmyBattle.instance.getDataFolder().mkdirs();
            try {
                locationsFile.createNewFile();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not create locations file: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(locationsFile);
        configInitialized = true;

        //headItem = new ItemBuilder(Material.LEATHER_HELMET)
        //        .setLeatherArmorColor(Color.GREEN)
        //        .toItemStack();
    }

    public WaveManager(Team team, String arenaName) {
        // Initialize static config if needed
        initializeConfig();

        // Register this class as an event listener
        Bukkit.getPluginManager().registerEvents(this, MonsterArmyBattle.instance);

        // Backup player inventories
        // 4*9 Inventory
        // 1 Offhand
        // 4 Armor
        // 3*9 Enderchest
        this.playerInventoryBackup = new ItemStack[team.getPlayers().size()][7 * 9 + 5];
        for (int i = 0; i < team.getPlayers().size(); i++) {
            Player p = team.getPlayers().get(i);
            this.playerInventoryBackup[i] = new ItemStack[7 * 9 + 5];
            for (int j = 0; j < 4 * 9; j++) {
                this.playerInventoryBackup[i][j] = p.getInventory().getItem(j);
            }
            this.playerInventoryBackup[i][4 * 9] = p.getInventory().getItemInOffHand();
            this.playerInventoryBackup[i][4 * 9 + 1] = p.getInventory().getHelmet();
            this.playerInventoryBackup[i][4 * 9 + 2] = p.getInventory().getChestplate();
            this.playerInventoryBackup[i][4 * 9 + 3] = p.getInventory().getLeggings();
            this.playerInventoryBackup[i][4 * 9 + 4] = p.getInventory().getBoots();
            for (int j = 0; j < 3 * 9; j++) {
                this.playerInventoryBackup[i][4 * 9 + 5 + j] = p.getEnderChest().getItem(j);
            }
        }
        this.backpackBackup = new ItemStack[team.getBackpack().getSize()];
        for (int i = 0; i < team.getBackpack().getSize(); i++) {
            this.backpackBackup[i] = team.getBackpack().getItem(i);
        }

        this.team = team;
        this.arenaName = arenaName.toLowerCase();
        this.spawnerLocations = new ArrayList<>();
        this.wave = 1;
        this.mobCounter = 0;
        this.additionalMobs = 0;
        this.teamCounterToTrackWhichTeamsArmyThisIs = 1;
        this.mobsKilled = 0;
        this.respawnTimers = new HashMap<>();

        // Load player spawn location
        this.playerSpawnLocation = LocationHelper.loadLocation(config, "locations." + arenaName + ".spawn");
        if (playerSpawnLocation == null) {
            MonsterArmyBattle.instance.getLogger().warning("Player spawn location not found for arena: " + arenaName);
            throw new IllegalArgumentException("Player spawn location not found for arena: " + arenaName);
        }
        this.playerSpawnLocation.setWorld(team.getArenaWorld());

        // Load death location
        this.deathLocation = LocationHelper.loadLocation(config, "locations." + arenaName + ".death");
        if (deathLocation == null) {
            MonsterArmyBattle.instance.getLogger().warning("Death location not found for arena: " + arenaName);
            throw new IllegalArgumentException("Death location not found for arena: " + arenaName);
        }
        this.deathLocation.setWorld(team.getArenaWorld());

        // Load all spawner locations
        int spawnerCounter = 1;
        boolean foundAnySpawner = false;
        while (true) {
            String spawnerPath = "locations." + arenaName + ".spawner" + spawnerCounter;
            Location spawnerLocation = LocationHelper.loadLocation(config, spawnerPath);
            if (spawnerLocation == null) {
                break; // No more spawners found
            }
            spawnerLocation.setWorld(team.getArenaWorld());
            spawnerLocations.add(spawnerLocation);
            foundAnySpawner = true;
            spawnerCounter++;
        }

        if (!foundAnySpawner) {
            MonsterArmyBattle.instance.getLogger().severe("No spawner locations found for arena: " + arenaName);
            throw new IllegalArgumentException("No spawner locations found for arena: " + arenaName);
        }

        createSpawningRunnable();
    }

    public void createSpawningRunnable() {
        this.spawnTimer = 25;
        spawningRunnable = Bukkit.getScheduler().runTaskTimer(MonsterArmyBattle.instance, () -> {
            if (isOver) {
                return;
            }
            checkNextWave();
            for(Player player : team.getPlayers()) {
                Integer remainingTime = respawnTimers.get(player.getUniqueId());
                if(remainingTime == null) continue;
                remainingTime--;
                respawnTimers.put(player.getUniqueId(), remainingTime);
                if(remainingTime <= 0) {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.teleport(playerSpawnLocation);
                    player.setInvulnerable(true);
                    player.sendMessage(Component.text("§aDu wurdest wiederbelebt!"));
                    player.showTitle(
                            Title.title(
                                    Component.text("§6Wiederbelebt!"),
                                    Component.text("§7Du hast §6" + INVINCIBILITY_TIME + " Sekunden §7Unverwundbarkeit"),
                                    Title.Times.times(
                                            Duration.ofMillis(250),
                                            Duration.ofMillis(1250),
                                            Duration.ofMillis(250)
                                    )
                            )
                    );
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.3F, 1F);
                    respawnTimers.remove(player.getUniqueId());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if(!player.isInvulnerable()) return;
                            player.setInvulnerable(false);
                            player.sendMessage("§7Du bist jetzt verwundbar.");
                            player.playSound(player.getLocation(), Sound.BLOCK_BASALT_BREAK, 0.5F, 1F);
                        }
                    }.runTaskLater(MonsterArmyBattle.instance, (INVINCIBILITY_TIME+1) * 20L);
                } else {
                    switch (remainingTime) {
                        case 60:
                        case 45:
                        case 30:
                        case 20:
                        case 15:
                        case 10:
                        case 5:
                        case 4:
                        case 3:
                        case 2:
                        case 1:
                            player.sendMessage("§7Du wirst in §a" + remainingTime + "§7 Sekunden wiederbelebt.");
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4F, 1F);
                            break;
                    }
                }
            }
            if (spawnTimer >= 0) {
                spawnTimer--;
                switch (spawnTimer) {
                    case 20:
                    case 15:
                    case 10:
                    case 5:
                    case 4:
                    case 3:
                    case 2:
                    case 1:
                        for (Player player : team.getPlayers()) {
                            player.sendMessage("§7Monster spawnen in §a" + spawnTimer + "§7 Sekunden.");
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4F, 1F);
                        }
                        break;
                    case 0:
                        for (Player player : team.getPlayers()) {
                            player.sendMessage("§7Monster spawnen jetzt!");
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8F, 1F);
                        }
                        break;
                }
                return;
            }
            if(mobCounter >= getCurrentWave().size()) {
                return;
            }
            mobCounter++;
            MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Spawning entity " + mobCounter + " of wave " + wave + " made by team " + getCurrentTeamId() + " for team " + team.getId() + " ("+ mobCounter + "/" + getCurrentWave().size() +")");
            LivingEntity nextEntity = getCurrentArmy().getNextMob(wave, mobCounter - 1);
            Location spawnLocation = spawnerLocations.get((int) (Math.random() * spawnerLocations.size()));
            if (nextEntity == null) {
                Entity entity = team.getArenaWorld().spawnEntity(spawnLocation, getCurrentArmy().getNextMobType(wave, mobCounter - 1));
                if(!(entity instanceof LivingEntity livingEntity)) {
                    mobsKilled++;
                    MonsterArmyBattle.instance.getLogger().log(Level.WARNING, "Entity is not a LivingEntity: " + entity.getType());
                    return;
                }
                if(entity instanceof MagmaCube) {
                    ((MagmaCube) entity).setSize(1);
                }
                if(entity instanceof Slime) {
                    ((Slime) entity).setSize(1);
                }
                nextEntity = livingEntity;
            } else {
                nextEntity.teleport(spawnLocation);
            }
            ArmyEntityStorage.processEntityForSpawn(nextEntity, team);
            MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Spawned entity " + nextEntity.getType() + " at " + spawnLocation);
        }, 0, 20);
    }

    public int getCurrentTeamId() {
        int totalTeams = TeamManager.getTeams().size();
        // Calculate which team's army this team should fight against
        // We want to ensure teams don't fight their own armies
        int targetTeamId = (team.getId() + teamCounterToTrackWhichTeamsArmyThisIs) % totalTeams;

        // If we get 0 from modulo, we should use the last team
        if (targetTeamId == 0) {
            targetTeamId = totalTeams;
        }

        return targetTeamId;
    }

    public Team getCurrentTeam() {
        return TeamManager.getTeams().get(getCurrentTeamId() - 1);
    }

    public Army getCurrentArmy() {
        return getCurrentTeam().getArmy();
    }

    public ArrayList<EntityType> getCurrentWave() {
        return getCurrentArmy().getWavesList().get(wave-1);
    }

    public void checkNextWave() {
        if(isOver) return;
        if(mobsKilled < additionalMobs + getCurrentWave().size() - mobCounter) return;
        // All mobs of the current wave are dead
        if(wave >= getCurrentArmy().getWavesList().size()) {
            // All waves for the current Army are done
            // Check if there is a next Army
            if(teamCounterToTrackWhichTeamsArmyThisIs+1 >= TeamManager.getTeams().size()) {
                // All armies are done
                for(Player player : team.getPlayers()) {
                    player.sendMessage("§aIhr habt alle Waves besiegt!");
                    player.sendMessage("§7Ihr habt insgesamt " + Timer.getInstance().getFormattedTime() + "§r§7 gebraucht.");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.8F, 1F);
                    player.setGameMode(GameMode.SPECTATOR);
                }
                battleEvents.add(BattleEvent.createTeamWinEvent(team));
                isOver = true;
                return;
            }
            // There is a next Army
            int totalWaves = (TeamManager.getTeams().size()-1) * Army.WAVE_COUNT;
            int wavesDone = ((teamCounterToTrackWhichTeamsArmyThisIs-1) * Army.WAVE_COUNT) + wave;
            battleEvents.add(BattleEvent.createWaveDefeatedEvent(team, getCurrentTeam(), wave, wavesDone, totalWaves));
            battleEvents.add(BattleEvent.createAllWavesDefeatedEvent(team, getCurrentTeam(), teamCounterToTrackWhichTeamsArmyThisIs, TeamManager.getTeams().size()-1));
            teamCounterToTrackWhichTeamsArmyThisIs++;
            wave = 1;
            spawnTimer = 8;
            mobsKilled = 0;
            mobCounter = 0;
            additionalMobs = 0;
            restoreInventories();
            for(Player player : team.getPlayers()) {
                player.sendMessage("§aIhr habt alle " + wave + " Waves von Team " + getCurrentTeamId() + " besiegt!");
                player.sendMessage("§7Die nächste Wave spawnt in §a" + spawnTimer + "§7 Sekunden.");
                player.showTitle(
                    Title.title(
                        Component.text("§6Inventar-Reset"),
                        Component.text("§6Alle Waves von Team " + getCurrentTeamId() + " wurden besiegt!"),
                        Title.Times.times(
                                Duration.ofMillis(250),
                                Duration.ofMillis(1250),
                                Duration.ofMillis(500)
                        )
                    )
                );
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8F, 1F);
            }
            // Remove items on floor
            MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Removing ground items in arena " + team.getArenaWorld().getName());
            for(Entity entity : team.getArenaWorld().getEntities()) {
                if(entity instanceof Item) {
                    entity.remove();
                }
            }
            return;
        }
        int totalWaves = (TeamManager.getTeams().size()-1) * Army.WAVE_COUNT;
        int wavesDone = ((teamCounterToTrackWhichTeamsArmyThisIs-1) * Army.WAVE_COUNT) + wave;
        battleEvents.add(BattleEvent.createWaveDefeatedEvent(team, getCurrentTeam(), wave, wavesDone, totalWaves));
        wave++;
        mobCounter = 0;
        mobsKilled = 0;
        spawnTimer = 8;
        additionalMobs = 0;
        for(Player player : team.getPlayers()) {
            player.sendMessage("§aIhr habt Wave " + (wave-1) + " besiegt! §7(von Team " + getCurrentTeamId() + ")");
            player.sendMessage("§7Die nächste Wave spawnt in §a" + spawnTimer + "§7 Sekunden.");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8F, 1F);
        }
    }

    public void restoreInventories() {
        // Restore player inventories
        for(Player player : team.getPlayers()) {
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(6);
            if(player.getGameMode() == GameMode.SPECTATOR) {
                player.setGameMode(GameMode.SURVIVAL);
                player.teleport(playerSpawnLocation);
                player.setInvulnerable(false);
                player.sendMessage(Component.text("§aDu wurdest durch den Inventar-Reset wiederbelebt!"));
            }
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            player.getEnderChest().clear();
            for (int i = 0; i < 4*9; i++) {
                if(playerInventoryBackup[team.getPlayers().indexOf(player)][i] != null) {
                    player.getInventory().setItem(i, playerInventoryBackup[team.getPlayers().indexOf(player)][i]);
                }
            }
            player.getInventory().setItemInOffHand(playerInventoryBackup[team.getPlayers().indexOf(player)][4*9]);
            player.getInventory().setHelmet(playerInventoryBackup[team.getPlayers().indexOf(player)][4*9+1]);
            player.getInventory().setChestplate(playerInventoryBackup[team.getPlayers().indexOf(player)][4*9+2]);
            player.getInventory().setLeggings(playerInventoryBackup[team.getPlayers().indexOf(player)][4*9+3]);
            player.getInventory().setBoots(playerInventoryBackup[team.getPlayers().indexOf(player)][4*9+4]);
            for(int i = 0; i < 3*9; i++) {
                if(playerInventoryBackup[team.getPlayers().indexOf(player)][4*9+5+i] != null) {
                    player.getEnderChest().setItem(i, playerInventoryBackup[team.getPlayers().indexOf(player)][4*9+5+i]);
                }
            }
        }

        // Restore backpack
        for (int i = 0; i < team.getBackpack().getSize(); i++) {
            if(backpackBackup[i] != null) {
                team.getBackpack().setItem(i, backpackBackup[i]);
            }
        }
    }

    @EventHandler
    public void onEntityRemove(EntityRemoveEvent event) {
        if(event.getCause() == EntityRemoveEvent.Cause.PLUGIN || event.getCause() == EntityRemoveEvent.Cause.DESPAWN) return;
        if(!event.getEntity().getWorld().getName().equals(team.getArenaWorld().getName())) return;
        if(event.getEntity() instanceof Player) return;
        if(!(event.getEntity() instanceof LivingEntity)) return;
        //if(event.getCause() == EntityRemoveEvent.Cause.TRANSFORMATION) return;
        //if(event.getEntityType() == EntityType.VEX) return;
        if(!event.getEntity().isPersistent()) {
            MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Monsterkill was not counted, because it was not persistent: " + event.getEntity().getType());
            return;
        }
        // mobCounter -> mobs spawned
        // additionalMobs -> mobs spawned + additional mobs
        // getCurrentWave().size() -> mobs in wave
        // mobsKilled -> mobs killed

        // getCurrentWave().size() - mobCounter -> remaining mobs to spawn
        // additionalMobs + getCurrentWave().size() - mobCounter -> remaining mobs to kill
        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Removing " + event.getEntity() + " due to " + event.getCause().name());
        event.getEntity().remove();
        mobsKilled++;
        checkNextWave();
    }

    // Todo Add Entity Spawn Event for Vexes and Transformations?
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if(!event.getEntity().getWorld().getName().equals(team.getArenaWorld().getName())) return;
        if(event.getEntity() instanceof Player) return;
        if(!(event.getEntity() instanceof LivingEntity)) return;
        additionalMobs++;
        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Entity spawned: " + event.getEntity().getType() + " (additionalMobs: " + additionalMobs + ")");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!event.getEntity().getWorld().getName().equals(team.getArenaWorld().getName())) return;
        event.setCancelled(true);
        Player player = event.getEntity();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(6);
        player.setFireTicks(0);
        player.setLevel(0);
        player.setExp(0);
        player.clearActivePotionEffects();
        player.teleport(deathLocation);
        player.showTitle(Title.title(
                Component.text("§cDu bist gestorben!"),
                Component.text("§7Du wirst in " + RESPAWN_TIME + " Sekunden wiederbelebt."),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(500)))
        );
        player.setGameMode(GameMode.ADVENTURE);
        player.setInvulnerable(true);
        respawnTimers.put(player.getUniqueId(), RESPAWN_TIME);
        String cause = "Unbekannt";
        try {
            cause = event.getDamageSource().getCausingEntity().getType().name();
        } catch (Exception ignored) {}
        battleEvents.add(BattleEvent.createPlayerDeathEvent(player.getName(), team, cause));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 0.5F, 1F);
    }


    public Location getPlayerSpawnLocation() {
        return playerSpawnLocation;
    }

    public Location getDeathLocation() {
        return deathLocation;
    }

}