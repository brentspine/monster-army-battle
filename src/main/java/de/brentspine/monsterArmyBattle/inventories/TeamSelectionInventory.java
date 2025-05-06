package de.brentspine.monsterArmyBattle.inventories;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import de.brentspine.monsterArmyBattle.util.ItemBuilder;
import de.brentspine.monsterArmyBattle.util.TeamManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public class TeamSelectionInventory implements Listener {

    private Inventory inventory;
    private final Player player;
    private static final ArrayList<TeamSelectionInventory> inventories = new ArrayList<>();

    public TeamSelectionInventory(Player player) {
        this.player = player;
        create(player.getUniqueId());
        player.openInventory(inventory);
        MonsterArmyBattle.instance.getServer().getPluginManager().registerEvents(this, MonsterArmyBattle.instance);
        inventories.add(this);
    }

    public void create(UUID uuid) {
        Inventory inventory = Bukkit.createInventory(null, 9*6, Component.text("Team Auswahl"));
        this.inventory = inventory;
        ItemStack glassPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§k").toItemStack();
        for(int i = 0; i < 9; i++) {
            inventory.setItem(i, glassPane);
            inventory.setItem(9*6-1-i, glassPane);
        }
        update();
        inventory.setItem(49, new ItemBuilder(Material.BARRIER).setName("§cSchließen").toItemStack());
    }

    public void update() {
        for(int slot = 0; slot < TeamManager.getTeams().size(); slot++) {
            inventory.setItem(slot+9, TeamManager.getTeams().get(slot).getItem(this.player.getUniqueId()));
        }
        for(int slot = TeamManager.getTeams().size(); slot < 9*4; slot++) {
            inventory.setItem(slot+9, null);
        }
    }



    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!event.getInventory().equals(inventory)) return;
        inventories.remove(this);
        MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Inventory close handled for TeamSelectionInventory " + player.getName());
    }

    public static void updateAll() {
        for(TeamSelectionInventory inv : inventories) {
            inv.update();
        }
    }

    public static void closeAll() {
        for(TeamSelectionInventory inv : inventories) {
            inv.player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        if(event.getSlot() == 49) {
            event.getWhoClicked().closeInventory();
            return;
        }

        int team = event.getSlot() - 9;
        if(team < 0 || team >= TeamManager.getTeams().size()) return;
        if(TeamManager.getTeams().get(team).isInTeam(player.getUniqueId())) {
            player.sendMessage("§cDu bist bereits in diesem Team");
            return;
        }
        if(TeamManager.getTeams().get(team).isFull()) {
            player.sendMessage("§cDieses Team ist voll");
            return;
        }
        player.sendMessage("§aDu bist dem Team beigetreten");
        TeamManager.addPlayerToTeam(team, player);
        updateAll();
    }
}
