package com.enderdevelopment.plugin;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class Main extends JavaPlugin implements Listener {
    private NamespacedKey ownerKey;
    private NamespacedKey fallenKey;
    private BukkitTask commandTask;

    @Override
    public void onEnable() {
        ownerKey = new NamespacedKey(this, "owner");
        fallenKey = new NamespacedKey(this, "fallen");
        Bukkit.getPluginManager().registerEvents(this, this);
        GraveboundCommand command = new GraveboundCommand(this);
        if (getCommand("gravebound") != null) {
            getCommand("gravebound").setExecutor(command);
            getCommand("gravebound").setTabCompleter(command);
        }
        commandTask = Bukkit.getScheduler().runTaskTimer(this, this::maintainArmy, 20L, 20L);
    }

    @Override
    public void onDisable() {
        if (commandTask != null) {
            commandTask.cancel();
        }
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead instanceof Player || dead.getType() == EntityType.ARMOR_STAND || !dead.getType().isSpawnable()) {
            return;
        }
        Location location = dead.getLocation().clone();
        EntityType type = dead.getType();
        event.getDrops().clear();
        event.setDroppedExp(0);
        Bukkit.getScheduler().runTask(this, () -> {
            if (!location.getChunk().isLoaded()) {
                return;
            }
            Entity spawned = location.getWorld().spawnEntity(location, type);
            if (spawned instanceof LivingEntity fallen) {
                fallen.getPersistentDataContainer().set(fallenKey, PersistentDataType.INTEGER, 1);
                fallen.setRemoveWhenFarAway(true);
            }
        });
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof LivingEntity living) || !isOwned(living, player)) {
            return;
        }
        event.setCancelled(true);
        living.setAI(!living.hasAI());
        String state = living.hasAI() ? "active" : "holding position";
        player.sendMessage(ComponentSupport.message("[Gravebound] " + living.getType().name() + " is now " + state + "."));
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof LivingEntity entity && isOwned(entity, null)
                && event.getTarget() instanceof Player target && isOwner(entity, target)) {
            event.setCancelled(true);
        }
    }

    public NamespacedKey ownerKey() {
        return ownerKey;
    }

    public NamespacedKey fallenKey() {
        return fallenKey;
    }

    public boolean isOwned(LivingEntity entity, Player player) {
        String owner = entity.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
        return owner != null && (player == null || owner.equals(player.getUniqueId().toString()));
    }

    private boolean isOwner(LivingEntity entity, Player player) {
        return isOwned(entity, player);
    }

    public int countArmy(Player player) {
        return army(player).size();
    }

    public List<LivingEntity> army(Player player) {
        List<LivingEntity> result = new ArrayList<>();
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 128, 64, 128)) {
            if (entity instanceof LivingEntity living && isOwned(living, player)) {
                result.add(living);
            }
        }
        return result;
    }

    public LivingEntity nearestFallen(Player player) {
        LivingEntity nearest = null;
        double distance = Double.MAX_VALUE;
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 8, 8, 8)) {
            if (entity instanceof LivingEntity living && living.getPersistentDataContainer().has(fallenKey, PersistentDataType.INTEGER)) {
                double current = living.getLocation().distanceSquared(player.getLocation());
                if (current < distance) {
                    nearest = living;
                    distance = current;
                }
            }
        }
        return nearest;
    }

    public boolean recruit(Player player) {
        LivingEntity target = nearestFallen(player);
        if (target == null || target.isDead()) {
            return false;
        }
        target.getPersistentDataContainer().remove(fallenKey);
        target.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
        target.setRemoveWhenFarAway(false);
        target.setPersistent(true);
        target.setAI(true);
        player.sendMessage(ComponentSupport.message("[Gravebound] The " + target.getType().name().toLowerCase().replace('_', ' ') + " rises to serve you."));
        return true;
    }

    public void setArmyAI(Player player, boolean enabled) {
        for (LivingEntity entity : army(player)) {
            entity.setAI(enabled);
            if (enabled) {
                entity.setTarget(null);
            }
        }
    }

    public void commandAttack(Player player, LivingEntity target) {
        for (LivingEntity entity : army(player)) {
            if (entity instanceof Monster monster && target != null && target != entity) {
                monster.setTarget(target);
            }
        }
    }

    public int release(Player player) {
        int released = 0;
        for (LivingEntity entity : army(player)) {
            entity.getPersistentDataContainer().remove(ownerKey);
            entity.setRemoveWhenFarAway(true);
            released++;
        }
        return released;
    }

    private void maintainArmy() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (LivingEntity entity : army(player)) {
                if (entity.getLocation().distanceSquared(player.getLocation()) > 2304) {
                    entity.teleport(player.getLocation().clone().add(1, 0, 1));
                }
            }
        }
    }
}
