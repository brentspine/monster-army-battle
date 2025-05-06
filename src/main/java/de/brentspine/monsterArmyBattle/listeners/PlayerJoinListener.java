package de.brentspine.monsterArmyBattle.listeners;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.util.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class PlayerJoinListener implements Listener {

    public boolean allowJoin = true;
    private ItemStack teamSelectorItem;

    public PlayerJoinListener() {
        ItemBuilder im = new ItemBuilder(Material.RED_BED);
        im.setName("§aTeam Auswahl");
        im.setLore("§7 - Wähle dein Team!");
        this.teamSelectorItem = im.toItemStack();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(!allowJoin) {
            event.getPlayer().kick(Component.text("Server is resetting"));
            return;
        }
        Player player = event.getPlayer();
        player.setInvulnerable(false);
        switch(MonsterArmyBattle.gameState) {
            case LOBBY:
                player.getInventory().clear();
                player.teleport(MonsterArmyBattle.lobbySpawnLocation);
                player.getInventory().setItem(8, teamSelectorItem);
                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(6);
                player.setExp(0);
                player.setLevel(0);
                player.setGameMode(GameMode.SURVIVAL);
                // Reset advancements and recipes
                player.undiscoverRecipes(player.getDiscoveredRecipes());
                Resetter.revokeAdvancements(player);
                player.getEnderChest().clear();
                break;
            default:
                for(Team team : TeamManager.getTeams()) {
                    for(Player current : team.getPlayers()) {
                        if(current.getUniqueId().equals(player.getUniqueId())) {
                            player.setGameMode(GameMode.SURVIVAL);
                            team.getPlayers().remove(current);
                            team.getPlayers().add(player);
                            return;
                        }
                    }
                }
                // Player is not in a team
                player.setGameMode(GameMode.SPECTATOR);
                break;
        }
    }
}