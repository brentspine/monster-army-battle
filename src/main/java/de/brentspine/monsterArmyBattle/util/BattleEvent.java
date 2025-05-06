package de.brentspine.monsterArmyBattle.util;

public class BattleEvent {

    private static int placement = 0;

    public enum BattleEventType {
        BATTLE_START("§2§lBattle-Phase hat begonnen"), // 0:00 - Battle-Phase hat begonnen
        WAVE_DEFEATED("§6Team %0% hat Wave %1% von Team %2% besiegt §7(%3%/%4%)"), // 0:00 - Team x hat Wave y von Team z besiegt
        ALL_WAVES_BY_TEAM_DEFEATED("§6Team %0% hat alle Waves von Team %1% besiegt §7(%3%/%4%)"), // 0:00 - Team x hat alle Waves von Team y besiegt
        PLAYER_DEATH("§cSpieler §6%0% §cvon §6Team %1% §cist an §6%2% §cgestorben"), // 0:00 - Spieler x von Team y ist an z gestorben
        TEAM_WIN("§l§aTeam %0% ist fertig: §9%1%. Platz"); // 0:00 - Team x ist fertig (y. Platz)

        private final String text;

        BattleEventType(String text) {
            this.text = text;
        }

        public String getText(Object... args) {
            String result = text;

            for (int i = 0; i < args.length; i++) {
                result = result.replace("%" + i + "%", args[i].toString());
            }

            return result;
        }
    }

    public static BattleEvent createBattleStartEvent() {
        return new BattleEvent(BattleEventType.BATTLE_START, Timer.getInstance().getFormattedTime());
    }

    public static BattleEvent createWaveDefeatedEvent(Team teamA, Team teamB, int wave, int wavesDone, int wavesTotal) {
        return new BattleEvent(BattleEventType.WAVE_DEFEATED, Timer.getInstance().getFormattedTime(), teamA.getId(), wave, teamB.getId(), wavesDone, wavesTotal);
    }

    public static BattleEvent createAllWavesDefeatedEvent(Team teamA, Team teamB, int teamsDone, int teamsTotal) {
        return new BattleEvent(BattleEventType.ALL_WAVES_BY_TEAM_DEFEATED, Timer.getInstance().getFormattedTime(), teamA.getId(), teamB.getId(), teamsDone, teamsTotal);
    }

    public static BattleEvent createPlayerDeathEvent(String playerName, Team team, String cause) {
        return new BattleEvent(BattleEventType.PLAYER_DEATH, Timer.getInstance().getFormattedTime(), playerName, team.getId(), cause);
    }

    public static BattleEvent createTeamWinEvent(Team team) {
        placement++;
        return new BattleEvent(BattleEventType.TEAM_WIN, Timer.getInstance().getFormattedTime(), team.getId(), placement);
    }

    private final BattleEventType type;
    private final String message;
    private final String time;

    public BattleEvent(BattleEventType type, String time, Object... args) {
        this.type = type;
        this.time = time;
        this.message = type.getText(args);
    }

    /**
     * Gets the type of the battle event.
     *
     * @return The battle event type
     */
    public BattleEventType getType() {
        return type;
    }

    /**
     * Gets the formatted message of the battle event.
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the time when the battle event occurred.
     *
     * @return The time as a formatted string
     */
    public String getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "§l§9" + time + "§r§7 - " + message;
    }

}
