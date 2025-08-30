package de.brentspine.monsterArmyBattle.util;

public enum GameState {

    LOBBY("Lobby"),
    FARM("Farm"),
    CONFIGURATION("Configuration"),
    BATTLE("Battle");

    private final String name;

    GameState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
