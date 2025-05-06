package de.brentspine.monsterArmyBattle.listeners;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.inventories.TeamSelectionInventory;
import de.brentspine.monsterArmyBattle.util.GameState;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.inventory.EquipmentSlot;

public class LobbyListeners implements Listener {

    /*private MonsterArmyBattle plugin;

    public LobbyListeners(MonsterArmyBattle plugin) {
        this.plugin = plugin;
    }*/

    @EventHandler
    public void onTeamSelectClick(PlayerInteractEvent event) {
        if(MonsterArmyBattle.gameState != GameState.LOBBY) return;
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(event.getPlayer().getInventory().getHeldItemSlot() != 8) return;
        new TeamSelectionInventory(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(MonsterArmyBattle.gameState != GameState.LOBBY) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if(MonsterArmyBattle.gameState != GameState.LOBBY && MonsterArmyBattle.gameState != GameState.CONFIGURATION)  return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(MonsterArmyBattle.gameState != GameState.LOBBY && MonsterArmyBattle.gameState != GameState.CONFIGURATION)  return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(MonsterArmyBattle.gameState != GameState.LOBBY && MonsterArmyBattle.gameState != GameState.CONFIGURATION)  return;
        if(event.getPlayer().getInventory().getHeldItemSlot() == 8 && MonsterArmyBattle.gameState == GameState.LOBBY) {
            new TeamSelectionInventory(event.getPlayer());
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if(MonsterArmyBattle.gameState != GameState.LOBBY && MonsterArmyBattle.gameState != GameState.CONFIGURATION)  return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(MonsterArmyBattle.gameState != GameState.LOBBY && MonsterArmyBattle.gameState != GameState.CONFIGURATION)  return;
        event.setCancelled(true);
    }

    /*@EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        if(MonsterArmyBattle.gameState != GameState.LOBBY && MonsterArmyBattle.gameState != GameState.CONFIGURATION)  return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if(MonsterArmyBattle.gameState != GameState.LOBBY && MonsterArmyBattle.gameState != GameState.CONFIGURATION)  return;
        event.setCancelled(true);
    }*/
}