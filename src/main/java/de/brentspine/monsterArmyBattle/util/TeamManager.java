package de.brentspine.monsterArmyBattle.util;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TeamManager {

    private static ArrayList<Team> teams = new ArrayList<Team>();

    public static void changeTeamAmount(int size) {
        if (teams.size() < size) {
            for (int i = teams.size(); i < size; i++) {
                teams.add(new Team(i+1, 99));
            }
        } else if (teams.size() > size) {
            for (int i = teams.size(); i > size; i--) {
                teams.remove(i - 1);
            }
        }
    }

    public static ArrayList<Team> getTeams() {
        return teams;
    }

    public static Team getTeam(int id) {
        if (id < 0 || id >= teams.size()) {
            return null;
        }
        return teams.get(id);
    }

    public static void addPlayerToTeam(int teamId, Player player) {
        // Remove from other team
        for (Team t : teams) {
            t.removePlayer(player);
        }
        teams.get(teamId).addPlayer(player);
    }

    public static Team getTeamByPlayer(Player player) {
        for (Team t : teams) {
            if (t.isInTeam(player)) {
                return t;
            }
        }
        return null;
    }

}
