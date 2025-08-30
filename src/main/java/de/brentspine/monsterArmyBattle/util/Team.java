package de.brentspine.monsterArmyBattle.util;

import de.brentspine.monsterArmyBattle.inventories.WaveConfigurationInventory;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class Team {

    private int id;
    private int maxPlayers;
    private ArrayList<Player> players;
    private Army army;
    private World farmWorld;
    private World farmWorldNether;
    private World farmWorldEnd;
    private World arenaWorld;
    private WaveManager waveManager;
    private Inventory backpack;

    private WaveConfigurationInventory waveConfigurationInventory;

    public Team(int id, int maxPlayers) {
        this.id = id;
        this.maxPlayers = maxPlayers;
        this.players = new ArrayList<>();
        this.army = new Army();
        this.waveConfigurationInventory = new WaveConfigurationInventory(this);
        this.backpack = Bukkit.createInventory(null, 3*9, Component.text("Backpack"));
    }

    public void callMeWhenConfigurationIsReady() {
        waveConfigurationInventory.fillMobs();
        for(Player p : players) {
            p.sendMessage(Component.text("§a[Waves Konfigurieren]").clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/waves")));
        }
    }

    public Inventory getWaveConfigurationInventory() {
        return waveConfigurationInventory.mainInventory;
    }

    public static Material getWoolColor(int id) {
        Material[] woolColors = {
                Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL,
                Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL,
                Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
                Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL
        };
        return woolColors[id % woolColors.length];
    }

    public static String randomColor() {
        String[] colors = {
                "§a", "§b", "§c", "§d", "§e", "§f", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", "§0"
        };
        return colors[(int) (Math.random() * colors.length)];
    }

    public ItemStack getItem(UUID uuid) {
        ItemBuilder ib = new ItemBuilder(getWoolColor(id), 1)
                .setName("§aTeam " + id);
        players.forEach(p -> ib.addLoreLine("§7" + p.getName()));
        if(!players.isEmpty()) ib.addLoreLine("§a");
        if(isInTeam(uuid)) {
            ib.addLoreLine("§7Du bist in diesem Team");
            ib.addEnchant(Enchantment.SOUL_SPEED, 1);
            ib.hideEnchants();
        } else if(players.size() < maxPlayers) {
            ib.addLoreLine("§7Klicke um beizutreten");
        } else {
            ib.addLoreLine("§cDieses Team ist voll");
        }
        return ib.toItemStack();
    }

    public int getId() {
        return id;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public boolean addPlayer(Player player) {
        if(players.size() >= maxPlayers) return false;
        players.add(player);
        return true;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void removePlayer(UUID uuid) {
        players.removeIf(player -> player.getUniqueId().equals(uuid));
    }

    public boolean isInTeam(Player player) {
        return players.contains(player);
    }

    public boolean isInTeam(UUID uuid) {
        return players.stream().anyMatch(player -> player.getUniqueId().equals(uuid));
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public World getFarmWorld() {
        return farmWorld;
    }

    public void setFarmWorld(World farmWorld) {
        this.farmWorld = farmWorld;
    }

    public World getFarmWorldNether() {
        return farmWorldNether;
    }

    public void setFarmWorldNether(World farmWorldNether) {
        this.farmWorldNether = farmWorldNether;
    }

    public World getFarmWorldEnd() {
        return farmWorldEnd;
    }

    public void setFarmWorldEnd(World farmWorldEnd) {
        this.farmWorldEnd = farmWorldEnd;
    }

    public World getArenaWorld() {
        return arenaWorld;
    }

    public void setArenaWorld(World arenaWorld) {
        this.arenaWorld = arenaWorld;
    }

    public Army getArmy() {
        return army;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public void setWaveManager(WaveManager waveManager) {
        this.waveManager = waveManager;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Inventory getBackpack() {
        return backpack;
    }

}
