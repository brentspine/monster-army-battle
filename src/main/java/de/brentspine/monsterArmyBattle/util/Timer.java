package de.brentspine.monsterArmyBattle.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Timer {

    // Singleton instance
    private static Timer instance;

    // Timer state
    private int seconds = 0;
    private boolean running = false;
    private boolean ascending = true;  // false means counting down
    private boolean expired = false;

    // Task ID for the Bukkit scheduler
    private int taskId = -1;

    // Plugin instance
    private static JavaPlugin plugin;

    // Private constructor for singleton
    private Timer() {}

    // Initialize with plugin reference
    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }

    // Get singleton instance
    public static Timer getInstance() {
        if (instance == null) {
            instance = new Timer();
        }
        return instance;
    }

    // Start the timer
    public void start() {
        if (running || plugin == null) return;
        running = true;

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (ascending) {
                seconds++;
            } else {
                seconds--;
                if (seconds <= 0) {
                    seconds = 0;
                    stop();
                    expired = true;
                }
            }
        }, 20L, 20L);  // 20 ticks = 1 second
    }

    // Stop the timer
    public void stop() {
        if (!running) return;
        running = false;

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    // Reset the timer to 0 without changing running state
    public void reset() {
        seconds = 0;
        running = false;
        expired = false;
    }

    // Resume the timer if it was stopped
    public void resume() {
        if (running) return;
        expired = false;
        start();
    }

    // Set the timer to a specific value
    public void set(int seconds) {
        this.seconds = Math.max(0, seconds); // Ensure non-negative
    }

    // Set the direction (true = ascending/counting up, false = descending/counting down)
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    // Get current seconds
    public int getSeconds() {
        return seconds;
    }

    // Check if timer is running
    public boolean isRunning() {
        return running;
    }

    // Check if timer is ascending
    public boolean isAscending() {
        return ascending;
    }

    // Format time as HH:MM:SS
    public String getFormattedTime() {
        if(seconds <= 0 && expired) {
            return "§l§o§cDer Timer ist abgelaufen";
        }
        if(!isRunning()) {
            return "§l§o§9Der Timer ist pausiert";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if(hours > 0) return "§l§9" + String.format("%02d:%02d:%02d", hours, minutes, secs);
        return "§l§9" + String.format("%02d:%02d", minutes, secs);
    }
}
