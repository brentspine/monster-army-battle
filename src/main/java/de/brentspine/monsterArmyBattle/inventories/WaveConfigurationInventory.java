package de.brentspine.monsterArmyBattle.inventories;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.util.Army;
import de.brentspine.monsterArmyBattle.util.ArmyEntityStorage;
import de.brentspine.monsterArmyBattle.util.ItemBuilder;
import de.brentspine.monsterArmyBattle.util.Team;
import io.papermc.paper.tag.EntityTags;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class WaveConfigurationInventory implements Listener {

    // TODO was passiert bei mehr als 45 Monstern?
    public Inventory mainInventory;
    public ArrayList<Inventory> waveInventory;
    private final Team team;

    public WaveConfigurationInventory(Team team) {
        MonsterArmyBattle.instance.getServer().getPluginManager().registerEvents(this, MonsterArmyBattle.instance);
        this.team = team;
        this.mainInventory = Bukkit.createInventory(null, 9, Component.text("Wave Übersicht"));
        for(int i = 0; i < Army.WAVE_COUNT; i++) {
            ItemStack item = new ItemBuilder(Material.IRON_SWORD).setName("§aWave " + (i+1)).setLore("§7Leer", "", "§7Klicke zum öffnen").toItemStack();
            mainInventory.setItem(i, item);
        }
        this.waveInventory = new ArrayList<>();
        ItemStack backItem = new ItemBuilder(Material.RED_WOOL).setName("§cZurück").toItemStack();
        ItemStack whitePane = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setName("§a").toItemStack();
        ItemStack blackPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§a").toItemStack();
        for(int i = 0; i < Army.WAVE_COUNT; i++) {
            Inventory inv = Bukkit.createInventory(null, 9*6, Component.text("Wave " + (i+1)));
            inv.setItem(0, backItem);
            for(int j = 1; j < 9; j++) {
                inv.setItem(j, whitePane);
            }
            for(int j = 9; j < 9*6; j++) {
                inv.setItem(j, blackPane);
            }
            waveInventory.add(inv);
        }
        fillMobs();
    }

    public void fillMobs() {
        Map<EntityType, ArmyEntityStorage> mobCount = team.getArmy().getArmy();
        for(int i = 0; i < Army.WAVE_COUNT; i++) {
            Inventory inv = waveInventory.get(i);
            int j = -1;
            for(EntityType type : mobCount.keySet()) {
                j++;
                int usedInWave = team.getArmy().getWave(i).getOrDefault(type, 0);
                int available = mobCount.get(type).getAmount() - team.getArmy().getUsed(type);
                if(available + usedInWave == 0) {
                    j--;
                    continue;
                }
                if(j >= 45) {
                    MonsterArmyBattle.instance.getLogger().log(Level.WARNING, "Too many mobs in wave " + i + " for team " + team.getId());
                    MonsterArmyBattle.instance.getLogger().log(Level.WARNING, "Mobs in Wave: " + j);
                    break;
                }
                inv.setItem(j+9, buildMobItem(type, usedInWave, available));
            }
        }
    }

    private ItemStack buildMobItem(EntityType type, int usedInWave, int available) {
        return new ItemBuilder(getSpawnEggMaterial(type))
                .setName(formatMobName(type.name()))
                .setLore(
                        "§7In Wave: " + usedInWave,
                        "§7Verfügbar: " + available
                )
                .toItemStack();
    }

    @EventHandler
    public void onInventoryClickOverview(InventoryClickEvent event) {
        if(!event.getInventory().equals(mainInventory)) return;
        event.setCancelled(true);
        int wave = event.getSlot();
        if(wave < 0 || wave >= Army.WAVE_COUNT) return;
        event.getWhoClicked().openInventory(waveInventory.get(wave));
    }

    @EventHandler
    public void onInventoryClickWave(InventoryClickEvent event) {
        int wave = waveInventory.indexOf(event.getInventory());
        if(wave == -1) return;
        event.setCancelled(true);
        if(event.getSlot() == 0) {
            event.getWhoClicked().openInventory(mainInventory);
        }
        if(event.getSlot() < 9) return;
        EntityType type = getEntityTypeFromSpawnEgg(Objects.requireNonNull(event.getCurrentItem()).getType());
        int available = team.getArmy().getArmy().get(type).getAmount() - team.getArmy().getUsed(type);
        int usedInWave = team.getArmy().getWave(wave).getOrDefault(type, 0);
        if(event.getClick().isLeftClick()) {
            if(available > 0) {
                team.getArmy().addToWave(wave, type, 1);
            }
        } else if(event.getClick().isRightClick()) {
            if(usedInWave > 0) {
                team.getArmy().addToWave(wave, type, -1);
            }
        }
        for(int i = 0; i < Army.WAVE_COUNT; i++) {
            Inventory inv = waveInventory.get(i);
            inv.setItem(
                    event.getSlot(),
                    buildMobItem(
                            type,
                            team.getArmy().getWave(i).getOrDefault(type,0),
                            team.getArmy().getArmy().get(type).getAmount() - team.getArmy().getUsed(type)
                    )
            );
            ArrayList<String> lore = new ArrayList<>();
            for(EntityType t : team.getArmy().getWave(i).keySet()) {
                int amount = team.getArmy().getWave(i).get(t);
                if(amount <= 0) continue;
                lore.add("§7 - " + amount + "x " + formatMobName(t.name()));
            }
            lore.add(" ");
            lore.add("§7Klicke zum öffnen");
            mainInventory.setItem(i, new ItemBuilder(Material.IRON_SWORD).setName("§aWave " + (i+1)).setLore(lore).toItemStack());
        }
    }

    private String formatMobName(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase().replace("_", " ");
    }

    /**
     * Converts an EntityType to its corresponding spawn egg Material
     * @param type The EntityType to convert
     * @return The spawn egg Material for the EntityType
     */
    public static Material getSpawnEggMaterial(EntityType type) {
        if(type == EntityType.ILLUSIONER) return Material.BLUE_BANNER;
        return Material.valueOf(type.name().toUpperCase() + "_SPAWN_EGG");
    }

    /**
     * Converts a spawn egg Material back to its corresponding EntityType
     * @param material The spawn egg Material to convert
     * @return The EntityType of the spawn egg, or null if not a spawn egg
     */
    public static EntityType getEntityTypeFromSpawnEgg(Material material) {
        if(material == Material.BLUE_BANNER) return EntityType.ILLUSIONER;
        if (!material.name().endsWith("_SPAWN_EGG")) {
            return null;
        }
        String entityName = material.name().replace("_SPAWN_EGG", "");
        return EntityType.valueOf(entityName);
    }

}
