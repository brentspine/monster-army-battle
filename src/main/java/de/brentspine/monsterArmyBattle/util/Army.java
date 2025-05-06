package de.brentspine.monsterArmyBattle.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
I need to store more information about the Entities. This means I need to use something else than EntityType. We can keep "army", "used" and even "wavesMap" to use EntityType, but the wavesList should be able to store more information about the Entity, so we can spawn a copy of that mob.

Suggest a new method and how the code can be improved in general
 */
public class Army {

    public static final int WAVE_COUNT = 3;

    // Now stores amount, used amount, and a list of entities
    private final Map<EntityType, ArmyEntityStorage> army;

    // Wir brauchen Reihenfolge, Mobs pro Wave
    // Stores the amount of entities used in a wave
    private final ArrayList<Map<EntityType, Integer>> wavesMap;
    // Stores the order of entities in a wave
    private final ArrayList<ArrayList<EntityType>> wavesList;


    public Army() {
        this.army = new HashMap<>();
        this.wavesMap = new ArrayList<>();
        this.wavesList = new ArrayList<>();
        for(int i = 0; i < WAVE_COUNT; i++) {
            wavesMap.add(new HashMap<>());
            wavesList.add(new ArrayList<>());
        }
        // Add every living entity type to the army
        for(EntityType type : EntityType.values()) {
            if(type.isAlive()) {
                army.put(type, new ArmyEntityStorage(type));
                for(int i = 0; i < WAVE_COUNT; i++) {
                    wavesMap.get(i).put(type, 0);
                }
            }
        }
    }

    public void add(EntityType type, int amount, Team team) {
        int newAmount = amount + army.get(type).getAmount();
        if(newAmount < 0) {
            newAmount = 0;
        }
        army.get(type).setAmount(newAmount);
        if(amount < 0) {
            for(int i = 0; i < -amount; i++) {
                army.get(type).removeEntity();
            }
        }
    }

    public void set(EntityType type, int amount) {
        add(type, army.get(type).getAmount()-amount, null);
    }

    public void add(LivingEntity entity, int amount) {
        int newAmount = amount + army.get(entity.getType()).getAmount();
        if(newAmount < 0) {
            newAmount = 0;
        }
        army.get(entity.getType()).setAmount(newAmount);
        army.get(entity.getType()).addEntity(entity);
    }

    /*public void set(EntityType type, int amount) {
        if(amount < 0) {
            amount = 0;
        }
        army.get(type).setAmount(amount);
    }*/

    public void clear() {
        for(ArmyEntityStorage storage : army.values()) {
            storage.clear();
        }
        for(ArrayList<EntityType> wave : wavesList) {
            wave.clear();
        }
    }

    public boolean addToWave(int wave, EntityType type, int amount) {
        if(wave < 0 || wave >= WAVE_COUNT) {
            return false;
        }
        if(army.get(type).getUsed() + amount < 0) {
            return false;
        }
        if(army.get(type).getUsed() + amount > army.get(type).getAmount()) {
            return false;
        }
        wavesMap.get(wave).put(type, wavesMap.get(wave).get(type) + amount);
        army.get(type).addUsed(amount);
        if(amount > 0) {
            for(int i = 0; i < amount; i++) {
                wavesList.get(wave).add(type);
            }
        } else {
            for(int i = 0; i < -amount; i++) {
                wavesList.get(wave).remove(type);
            }
        }
        return true;
    }

    public LivingEntity getNextMob(int wave, int mobCounter) {
        if(wave < 0 || wave > WAVE_COUNT) {
            throw new IndexOutOfBoundsException("Wave out of bounds: " + wave);
        }
        if(wavesList.get(wave-1).size() <= mobCounter) {
            throw new IndexOutOfBoundsException("Mob counter out of bounds: " + mobCounter);
        }
        return army.get(wavesList.get(wave-1).get(mobCounter)).getNext();
    }

    public EntityType getNextMobType(int wave, int mobCounter) {
        if(wave < 0 || wave > WAVE_COUNT) {
            throw new IndexOutOfBoundsException("Wave out of bounds: " + wave);
        }
        if(wavesList.get(wave-1).size() <= mobCounter) {
            throw new IndexOutOfBoundsException("Mob counter out of bounds: " + mobCounter);
        }
        return wavesList.get(wave-1).get(mobCounter);
    }

    public void resetWave(int wave) {
        for(EntityType type : wavesMap.get(wave).keySet()) {
            int usedAmount = wavesMap.get(wave).get(type);
            army.get(type).addUsed(-usedAmount);
            wavesMap.get(wave).put(type, 0);
            wavesList.get(wave).add(type);
        }
    }

    public void resetWaves() {
        for(int i = 0; i < WAVE_COUNT; i++) {
            resetWave(i);
        }
    }

    public Map<EntityType, ArmyEntityStorage> getArmy() {
        return army;
    }

    public Map<EntityType, Integer> getWave(int wave) {
        return wavesMap.get(wave);
    }

    public int getUsed(EntityType type) {
        return army.get(type).getUsed();
    }

    public void setUsed(EntityType type, int amount) {
        army.get(type).setUsed(amount);
    }

    public int addUsed(EntityType type, int amount) {
        army.get(type).addUsed(amount);
        return army.get(type).getUsed();
    }

    public int getAmount(EntityType type) {
        return army.get(type).getAmount();
    }

    public ArrayList<ArrayList<EntityType>> getWavesList() {
        return wavesList;
    }

    public ArrayList<Map<EntityType, Integer>> getWavesMap() {
        return wavesMap;
    }
}
