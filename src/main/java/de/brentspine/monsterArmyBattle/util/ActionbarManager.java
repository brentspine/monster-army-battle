package de.brentspine.monsterArmyBattle.util;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import net.kyori.adventure.text.Component;

public class ActionbarManager {

    private boolean running = false;
    private MonsterArmyBattle plugin;

    public ActionbarManager(MonsterArmyBattle plugin) {
        this.plugin = plugin;
        this.run();
    }

    private void run() {
        if(running) return;
        running = true;
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            Component component;
            switch (MonsterArmyBattle.gameState) {
                case LOBBY:
                    component = Component.text("§aWarte auf Start");
                    break;
                case FARM:
                case CONFIGURATION:
                case BATTLE:
                    //component = Component.text("§cTimer steht hier...");
                    component = Component.text(Timer.getInstance().getFormattedTime());
                    break;
                default:
                    component = Component.text("§7Unknown");
                    break;
            }
            plugin.getServer().getOnlinePlayers().forEach(player -> {
                player.sendActionBar(component);
            });
        }, 0, 20);
    }

}
