package de.brentspine.monsterArmyBattle.listeners;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.util.*;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class FarmListeners implements Listener {

    public static final EntityType[] BLOCKED_LIVING_ENTITIES = {
        EntityType.ARMOR_STAND,
        EntityType.ENDER_DRAGON,
        EntityType.ELDER_GUARDIAN,
        EntityType.PLAYER,
        EntityType.GIANT,
        EntityType.VEX
    };

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(MonsterArmyBattle.gameState != GameState.FARM) return;

        // Get items dropped by the block
        for(ItemStack item : event.getBlock().getDrops()) {
            Material newItem = RandomDropManager.getInstance().getRandomizedDrop(event.getPlayer(), item.getType());
            if(item.getAmount() <= 0) continue;
            event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(),
                    new ItemBuilder(newItem, item.getAmount())
                            .toItemStack()
            );
        }
        event.setDropItems(false);
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if(MonsterArmyBattle.gameState != GameState.FARM) return;
        if(event.getEntity().getKiller() == null) return;
        // Check if event.getEntityType() is in BLOCKED_LIVING_ENTITIES
        for(EntityType type : BLOCKED_LIVING_ENTITIES) {
            if(event.getEntityType() == type) {
                event.getEntity().getKiller().sendMessage(Component.text("§7Der Mob wurde nicht gezählt, da " + type.name() + " geblockt ist."));
                return;
            }
        }
        Player killer = event.getEntity().getKiller();
        Team team = TeamManager.getTeamByPlayer(killer);
        if(team == null) return;
        team.getArmy().add(event.getEntity(), 1);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if(MonsterArmyBattle.gameState != GameState.FARM) return;
        Player player = event.getPlayer();
        Team team = TeamManager.getTeamByPlayer(player);
        if(team == null) {
            player.sendMessage("§cEs konnte keine Respawn-Location gefunden werden.");
            return;
        }
        player.teleport(team.getFarmWorld().getSpawnLocation());
        event.setRespawnLocation(team.getFarmWorld().getSpawnLocation());
    }

    @EventHandler
    public void onBlockBreakOtherThanPlayer(BlockBreakBlockEvent event) {
        if(MonsterArmyBattle.gameState != GameState.FARM) return;
        event.getBlock().getWorld().getNearbyPlayers(event.getBlock().getLocation(), 5, 5, 5)
                .forEach(player -> {
                    for(ItemStack item : event.getBlock().getDrops()) {
                        Material newItem = RandomDropManager.getInstance().getRandomizedDrop(player, item.getType());
                        if(item.getAmount() <= 0) continue;
                        event.getBlock().getWorld().dropItemNaturally(
                                event.getBlock().getLocation(),
                                new ItemBuilder(newItem, item.getAmount())
                                        .toItemStack()
                        );
                    }
                });
        event.getDrops().clear();
    }

    /*@EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerTeleportThroughPortal(PlayerTeleportEvent event) {
        if(MonsterArmyBattle.gameState != GameState.FARM) return;
        if(event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) return;

        Player player = event.getPlayer();
        Team team = TeamManager.getTeamByPlayer(player);
        if(team == null) {
            player.sendMessage("§cEs konnte kein Team gefunden werden.");
            return;
        }

        World fromWorld = event.getFrom().getWorld();
        World toWorld = event.getTo().getWorld();
        Location newLoc = event.getTo().clone();

        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Portal teleport: " + player.getName() +
                " from " + fromWorld.getName() + " to " + toWorld.getName() +
                " via " + event.getCause());

        // If player is in team's overworld and going to nether
        if(fromWorld.getName().equals(team.getFarmWorld().getName()) && event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            newLoc.setWorld(team.getFarmWorldNether());
            MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Redirecting to team nether: " + team.getFarmWorldNether().getName());
        }
        // If player is in team's nether and returning to overworld
        else if(fromWorld.getName().equals(team.getFarmWorldNether().getName()) && event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            newLoc.setWorld(team.getFarmWorld());
            MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Redirecting to team overworld: " + team.getFarmWorld().getName());
        }
        // If player is in team's overworld and going to end
        else if(fromWorld.getName().equals(team.getFarmWorld().getName()) && event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            newLoc.setWorld(team.getFarmWorldEnd());
            MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Redirecting to team end: " + team.getFarmWorldEnd().getName());
        }
        // If player is in team's end and returning to overworld
        else if(fromWorld.getName().equals(team.getFarmWorldEnd().getName()) && event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            newLoc.setWorld(team.getFarmWorld());
            MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Redirecting to team overworld: " + team.getFarmWorld().getName());
        }
        // Force players back to their team worlds if they're somehow in the wrong dimension
        else if(!fromWorld.getName().equals(team.getFarmWorld().getName()) && !fromWorld.getName().equals(team.getFarmWorldNether().getName()) && !fromWorld.getName().equals(team.getFarmWorldEnd().getName())) {
            newLoc.setWorld(team.getFarmWorld());
            MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Player in wrong world, returning to team world: " + team.getFarmWorld().getName());
        }

        // Fill hole
        Block block;
        for(int x = -2; x <= 0; x++) {
            for(int y = -1; y <= 3; y++) {
                for(int z = -3; z <= 2; z++) {
                    block = newLoc.getWorld().getBlockAt(newLoc.getBlockX()+x, newLoc.getBlockY()+y, newLoc.getBlockZ()+z);
                    block.setType(Material.AIR);
                }
            }
        }

        // Generate fake portal
        World world = newLoc.getWorld();
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()-2, newLoc.getBlockZ()+1).setType(Material.OBSIDIAN);
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()-1, newLoc.getBlockZ()+1).setType(Material.OBSIDIAN);
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()-0, newLoc.getBlockZ()+1).setType(Material.OBSIDIAN);
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()+1, newLoc.getBlockZ()+1).setType(Material.OBSIDIAN);
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()+2, newLoc.getBlockZ()+1).setType(Material.OBSIDIAN);

        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()-2, newLoc.getBlockZ()).setType(Material.OBSIDIAN);
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()-2, newLoc.getBlockZ()-1).setType(Material.OBSIDIAN);
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()+2, newLoc.getBlockZ()).setType(Material.OBSIDIAN);
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()+2, newLoc.getBlockZ()-1).setType(Material.OBSIDIAN);

        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()-2, newLoc.getBlockZ()-2).setType(Material.OBSIDIAN);
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()-1, newLoc.getBlockZ()-2).setType(Material.OBSIDIAN);
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()-0, newLoc.getBlockZ()-2).setType(Material.OBSIDIAN);
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()+1, newLoc.getBlockZ()-2).setType(Material.OBSIDIAN);
        world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()+2, newLoc.getBlockZ()-2).setType(Material.OBSIDIAN);

        for(int i = -2; i <= 1; i++) {
            Block b = world.getBlockAt(newLoc.getBlockX(), newLoc.getBlockY()-2, newLoc.getBlockZ()+i);
            if(b.getType() == Material.AIR) {
                b.setType(Material.OBSIDIAN);
            }
            b = world.getBlockAt(newLoc.getBlockX()-2, newLoc.getBlockY()-2, newLoc.getBlockZ()+i);
            if(b.getType() == Material.AIR) {
                b.setType(Material.OBSIDIAN);
            }
        }

        // 30 ticks after teleport, place fire
        Bukkit.getScheduler().runTaskLater(MonsterArmyBattle.instance, () -> {
            Block b = world.getBlockAt(newLoc.getBlockX()-1, newLoc.getBlockY()-1, newLoc.getBlockZ()-1);
            b.setType(Material.FIRE);
        }, 30L);


        event.setTo(newLoc);
        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Final destination set to: " + newLoc.getWorld().getName());
    }*/

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if(MonsterArmyBattle.gameState != GameState.FARM) return;
        Player player = event.getPlayer();
        MonsterArmyBattle.instance.getLogger().log(Level.INFO, event.getTo() + " " + event.getFrom() + " " + event.getCause());
        Team team = TeamManager.getTeamByPlayer(player);
        assert team != null;
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if(event.getFrom().getWorld().getName().equals(team.getFarmWorld().getName())) {
                event.getTo().setWorld(team.getFarmWorldNether());
                return;
            } else if(event.getFrom().getWorld().getName().equals(team.getFarmWorldNether().getName())) {
                event.getTo().setWorld(team.getFarmWorld());
                return;
            }
            // Fallback for any other case
            event.setTo(team.getFarmWorld().getSpawnLocation());
            return;
        }
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            if(event.getFrom().getWorld().getName().equals(team.getFarmWorld().getName())) {
                event.getTo().setWorld(team.getFarmWorldEnd());
                return;
            } else if(event.getFrom().getWorld().getName().equals(team.getFarmWorldEnd().getName())) {
                event.getTo().setWorld(team.getFarmWorld());
                return;
            }
            // Fallback for any other case
            event.setTo(team.getFarmWorld().getSpawnLocation());
            return;
        }
    }

}
