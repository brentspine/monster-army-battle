package de.brentspine.monsterArmyBattle.commands;

import de.brentspine.monsterArmyBattle.MonsterArmyBattle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class AddGlowingCommand implements CommandExecutor {

    private static final int MAX_ENTITIES = 10;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        List<LivingEntity> nearbyEntities = player.getWorld().getLivingEntities().stream()
                .filter(entity -> !(entity instanceof Player))
                .limit(MAX_ENTITIES)
                .toList();

        if (nearbyEntities.isEmpty()) {
            player.sendMessage("§cNo entities found in your world.");
            return true;
        }

        int affectedCount = 0;
        for (LivingEntity entity : nearbyEntities) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0, false, false)); // 30 seconds
            affectedCount++;
            MonsterArmyBattle.instance.getLogger().log(Level.INFO, "Applied glowing effect to " + entity.getType().name());
        }

        player.sendMessage("§aApplied glowing effect to " + affectedCount + " entities.");
        return true;
    }
}