package de.brentspine.monsterArmyBattle.listeners;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.entity.EndermanEscapeEvent;
import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.util.GameState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;

import java.util.ArrayList;

public class BattlePhaseListeners implements Listener {

    // Entity deaths handled in WaveManager

    public enum ArenaBlockBreakSettings {
        NOTHING_ALLOWED,
        BREAK_TEAM_OWNED_BLOCKS,
        EVERYTHING_ALLOWED,
    }

    public static ArenaBlockBreakSettings ARENA_BLOCK_BREAK_SETTINGS = ArenaBlockBreakSettings.NOTHING_ALLOWED;
    private final ArrayList<Location> teamOwnedBlocks = new ArrayList<>();

    @EventHandler
    public void onBlockDestroy(BlockDestroyEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        switch (ARENA_BLOCK_BREAK_SETTINGS) {
            case NOTHING_ALLOWED:
                event.setCancelled(true);
                break;
            case BREAK_TEAM_OWNED_BLOCKS:
                // Check if block is team owned
                // We can't use .contains
                for(Location location : teamOwnedBlocks) {
                    if(location.getWorld().getName().equals(event.getBlock().getWorld().getName()) &&
                            location.getBlockX() == event.getBlock().getX() &&
                            location.getBlockY() == event.getBlock().getY() &&
                            location.getBlockZ() == event.getBlock().getZ()) {
                        return;
                    }
                }
                break;
            case EVERYTHING_ALLOWED:
                // Do nothing
                break;
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        if(event.getBlock().getType() == Material.FIRE) return;
        switch (ARENA_BLOCK_BREAK_SETTINGS) {
            case NOTHING_ALLOWED -> event.setCancelled(true);
            case BREAK_TEAM_OWNED_BLOCKS -> teamOwnedBlocks.add(event.getBlock().getLocation());
            case EVERYTHING_ALLOWED -> {}
        }
    }

    @EventHandler
    public void onPlayerPortal(EntityPortalEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        // Check if entity is in lava or fire
        if(event.getEntity().isInLava()) return;
        if(event.getEntity().getLocation().getBlock().getType() == Material.FIRE) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamageWhileDead(EntityDamageEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        if(!(event.getEntity() instanceof Player player)) return;
        if(player.getGameMode() != GameMode.ADVENTURE) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEndermanTeleport(EndermanEscapeEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        if(event.getBlock().getType() == Material.FIRE) return;
        if(event.getEntityType() == EntityType.PLAYER) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        if(event.getEntityType() == EntityType.PLAYER) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        event.blockList().clear();
    }

    /*@EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        event.setCancelled(true);
    }*/

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if(MonsterArmyBattle.gameState != GameState.BATTLE) return;
        event.setCancelled(true);
    }


}
