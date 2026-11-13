package com.enderdevelopment.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

final class GraveboundCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private static final List<String> SUBCOMMANDS = List.of("recruit", "follow", "hold", "attack", "release", "list");

    GraveboundCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ComponentSupport.message("Only players can command a gravebound army."));
            return true;
        }
        if (!player.hasPermission("gravebound.use")) {
            player.sendMessage(ComponentSupport.message("You do not have permission to command a gravebound army."));
            return true;
        }
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "recruit" -> recruit(player);
            case "follow" -> {
                plugin.setArmyAI(player, true);
                player.sendMessage(ComponentSupport.message("Your army is following and defending you."));
            }
            case "hold" -> {
                plugin.setArmyAI(player, false);
                player.sendMessage(ComponentSupport.message("Your army is holding its position."));
            }
            case "attack" -> attack(player);
            case "release" -> {
                int released = plugin.release(player);
                player.sendMessage(ComponentSupport.message("Released " + released + " army unit(s)."));
            }
            case "list" -> player.sendMessage(ComponentSupport.message("Your nearby army contains " + plugin.countArmy(player) + " unit(s)."));
            default -> sendHelp(player);
        }
        return true;
    }

    private void recruit(Player player) {
        if (plugin.recruit(player)) {
            player.sendMessage(ComponentSupport.message("Use /gravebound follow, /gravebound hold, or /gravebound attack."));
        } else {
            player.sendMessage(ComponentSupport.message("No fallen mob is nearby. Defeat a mob, then recruit its reanimated remains."));
        }
    }

    private void attack(Player player) {
        Entity target = player.getTargetEntity(32);
        if (!(target instanceof LivingEntity living) || plugin.isOwned(living, player)) {
            player.sendMessage(ComponentSupport.message("Look at a living enemy within 32 blocks, then use /gravebound attack."));
            return;
        }
        plugin.commandAttack(player, living);
        player.sendMessage(ComponentSupport.message("Your monster units are attacking the target."));
    }

    private void sendHelp(Player player) {
        player.sendMessage(ComponentSupport.message("Gravebound Army: /gravebound recruit | follow | hold | attack | release | list"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        String prefix = args[0].toLowerCase();
        List<String> matches = new ArrayList<>();
        for (String subcommand : SUBCOMMANDS) {
            if (subcommand.startsWith(prefix)) {
                matches.add(subcommand);
            }
        }
        return matches;
    }
}
