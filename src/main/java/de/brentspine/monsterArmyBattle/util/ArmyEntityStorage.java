package de.brentspine.monsterArmyBattle.util;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;

import java.util.ArrayList;

public class ArmyEntityStorage {

    private final EntityType type;
    private int amount;
    private int used;
    private final ArrayList<LivingEntity> entities;
    private int index;

    public ArmyEntityStorage(EntityType type) {
        this.type = type;
        this.amount = 0;
        this.used = 0;
        this.index = 0;
        this.entities = new ArrayList<>();
    }

    public boolean hasNext() {
        return index < amount;
    }

    public LivingEntity getNext() {
        if (!hasNext()) {
            throw new IllegalStateException("No more entities available. Check hasNext() before calling getNext().");
        }
        if (index >= entities.size()) {
            return null;
        }
        LivingEntity entity = entities.get(index);
        reviveEntity(entity);
        index++;
        return entity;
    }

    public void addEntity(LivingEntity entity) {
        //entities.add(entity);
    }

    public void removeEntity() {
        if (!entities.isEmpty()) {
            entities.removeLast();
        }
    }

    public void clear() {
        for (LivingEntity entity : entities) {
            if (!entity.isDead()) {
                entity.remove();
            }
        }
        entities.clear();
        index = 0;
        amount = 0;
        used = 0;
    }

    public EntityType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public void addUsed(int amount) {
        used += amount;
    }

    public void addAmount(int amount) {
        this.amount += amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public static void reviveEntity(LivingEntity entity) {
        if (entity.isDead()) {
            /*entity.setHealth(entity.getMaxHealth());
            entity.setFireTicks(0);
            entity.setGlowing(false);*/
            // Revive entity by creating a new instance and cloning properties
            LivingEntity newEntity = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());

        }
    }

    public static void processEntityForSpawn(LivingEntity livingEntity, Team team) {
        // Basic settings for all living entities
        livingEntity.setCanPickupItems(false);
        livingEntity.setPersistent(true);
        livingEntity.setRemoveWhenFarAway(false);

        // Select a random player to target
        Player targetPlayer = team.getPlayers().get((int) (Math.random() * team.getPlayers().size()));

        // Entity-type specific handling
        if(livingEntity instanceof Mob mob) {
            mob.setTarget(targetPlayer);
            mob.setAware(true);
            mob.setAggressive(true);

            livingEntity.setMemory(MemoryKey.UNIVERSAL_ANGER, true);
            livingEntity.setMemory(MemoryKey.ANGRY_AT, targetPlayer.getUniqueId());

            // Special handling for Iron Golems
            if(livingEntity instanceof IronGolem ironGolem) {
                ironGolem.setPlayerCreated(false); // Ensure it's not considered "player-created"
            }

            // Apply a tiny damage to trigger aggression mechanics
            mob.damage(0.01, targetPlayer);

            // Schedule periodic re-targeting to maintain aggression
            Bukkit.getScheduler().runTaskTimer(MonsterArmyBattle.instance, () -> {
                if(mob.isDead() || !mob.isValid()) return;
                mob.setTarget(getNearestPlayer(mob));
            }, 100L, 100L); // Re-target every 5 seconds
        }
    }

    private static Player getNearestPlayer(Entity entity) {
        return entity.getWorld().getNearbyPlayers(entity.getLocation(), 10).stream().findFirst().orElse(null);
    }


}
