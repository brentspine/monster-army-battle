package de.brentspine.monsterArmyBattle;

import de.brentspine.monsterArmyBattle.commands.*;
import de.brentspine.monsterArmyBattle.listeners.BattlePhaseListeners;
import de.brentspine.monsterArmyBattle.listeners.FarmListeners;
import de.brentspine.monsterArmyBattle.listeners.LobbyListeners;
import de.brentspine.monsterArmyBattle.listeners.PlayerJoinListener;
import de.brentspine.monsterArmyBattle.util.*;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class MonsterArmyBattle extends JavaPlugin {

    // Essentials: Timer, Scoreboard, Backpack, More Settings

    // Lobby Phase:
    // - Keine Zeit, Hunger, Schaden
    // - Team Auswahl

    // Welten erstellen

    // Farm Phase:
    // - Monster speichern

    // Battle Phase:
    // - Arena tp
    // - Monster spawnen

    public static MonsterArmyBattle instance;
    public static GameState gameState = GameState.LOBBY;
    public static Location lobbySpawnLocation;
    public static PlayerJoinListener playerJoinListener;
    public static LobbyListeners lobbyListeners;

    @Override
    public void onEnable() {
        instance = this;

        // Registering events
        playerJoinListener = new PlayerJoinListener();
        lobbyListeners = new LobbyListeners();
        getServer().getPluginManager().registerEvents(playerJoinListener, this);
        getServer().getPluginManager().registerEvents(lobbyListeners, this);
        getServer().getPluginManager().registerEvents(new FarmListeners(), this);
        getServer().getPluginManager().registerEvents(new BattlePhaseListeners(), this);

        // Register commands
        Objects.requireNonNull(getCommand("changeteamamount")).setExecutor(new ChangeTeamAmountCommand());
        Objects.requireNonNull(getCommand("start")).setExecutor(new StartCommand());
        Objects.requireNonNull(getCommand("resetteams")).setExecutor(new ResetTeamsCommand());
        Objects.requireNonNull(getCommand("timer")).setExecutor(new TimerCommand());
        Objects.requireNonNull(getCommand("findrandomizermapping")).setExecutor(new FindRandomizerMappingCommand());
        Objects.requireNonNull(getCommand("currentarmy")).setExecutor(new CurrentArmyCommand());
        Objects.requireNonNull(getCommand("startwaveconfig")).setExecutor(new StartWaveConfigCommand());
        Objects.requireNonNull(getCommand("waves")).setExecutor(new WavesCommand());
        Objects.requireNonNull(getCommand("startbattle")).setExecutor(new StartBattleCommand());
        Objects.requireNonNull(getCommand("editworlds")).setExecutor(new EditWorldsCommand());
        Objects.requireNonNull(getCommand("locationhelper")).setExecutor(new LocationHelperCommand(this));
        Objects.requireNonNull(getCommand("addtoarmy")).setExecutor(new AddToArmyCommand());
        Objects.requireNonNull(getCommand("backpack")).setExecutor(new BackpackCommand());
        Objects.requireNonNull(getCommand("mabsettings")).setExecutor(new MabSettingsCommand());
        Objects.requireNonNull(getCommand("announcebattleresult")).setExecutor(new AnnounceBattleResultCommand());
        Objects.requireNonNull(getCommand("addglowing")).setExecutor(new AddGlowingCommand());

        // Sets lobbySpawnLocation
        Resetter.resetWorlds();

        // Teleports all players to the lobby (normally no players are online at this point)
        getServer().getOnlinePlayers().forEach(player -> player.teleport(lobbySpawnLocation));

        // Starts the actionbar manager
        new ActionbarManager(this);
        Timer.init(this);

        TeamManager.changeTeamAmount(2);

        // Loads the random drop manager
        RandomDropManager.getInstance(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
