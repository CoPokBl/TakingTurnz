package net.copokbl.takingTurnz;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class Game {
    private final int turnTime;
    private final List<UUID> players;
    private int heroIndex = 0;
    private long nextTurnTime = 0L;

    private Player getHero() {
        return Bukkit.getPlayer(players.get(heroIndex));
    }

    public Game(int turnTime, List<Player> players) {
        this.turnTime = turnTime;
        this.players = players.stream().map(Player::getUniqueId).toList();

        // send all but the hero player to limbo
        players.stream().skip(1).forEach(this::sendToLimbo);
    }

    public void start() {
        msg(ChatColor.GREEN + "Game started! Each player has " + turnTime + " seconds per turn.");

        nextTurnTime = System.currentTimeMillis() + (turnTime * 1000L);

        Bukkit.getScheduler().runTaskTimer(TakingTurnz.getInstance(), () -> {
            // inform players of remaining time
            long timeLeft = (nextTurnTime - System.currentTimeMillis()) / 1000L;
            int nextIndex = nextHeroIndex(false);

            for (int i = 0; i < players.size(); i++) {
                String message = ChatColor.AQUA + "Time left in " + getHero().getName() + "'s turn: " +
                        ChatColor.YELLOW + timeLeft + " seconds";

                if (i == nextIndex) {
                    message = ChatColor.AQUA + "Time left until your turn: " +
                            ChatColor.YELLOW + timeLeft + " seconds";
                }

                Player p = Bukkit.getPlayer(players.get(i));
                if (p != null) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
                }
            }

            // make pitch lower as time runs out
            if (timeLeft <= 5 && timeLeft > 0) {
                float pitch = 1.0f + (5 - timeLeft) * 0.2f;
                for (int index : List.of(heroIndex, nextIndex)) {
                    Player player = Bukkit.getPlayer(players.get(index));
                    if (player != null) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, pitch);
                    }
                }
            }

            if (System.currentTimeMillis() >= nextTurnTime) {
                Bukkit.getScheduler().runTask(TakingTurnz.getInstance(), () -> nextTurn());
            }
        }, 0L, 20L);
    }

    private void nextTurn() {
        nextTurn(null);
    }

    // oldHero exists for the case where the current hero leaves the game
    // we need to be able to force the object
    private void nextTurn(@Nullable Player oldHero) {
        if (oldHero == null) {
            oldHero = getHero();
        }
        nextHeroIndex();
        Player newHero = getHero();

        if (newHero.isDead()) {
            newHero.spigot().respawn();
        }

        if (oldHero.getUniqueId().equals(newHero.getUniqueId())) {
            // same hero
            msg(ChatColor.YELLOW + "There's no one else to take a turn! " + oldHero.getName() + " will continue their turn.");
            nextTurnTime = System.currentTimeMillis() + (turnTime * 1000L);
            return;
        }

        // replicate oldhero stats into newhero
        newHero.getActivePotionEffects().forEach(e -> newHero.removePotionEffect(e.getType()));
        newHero.setHealth(oldHero.getHealth());
        newHero.setFoodLevel(oldHero.getFoodLevel());
        newHero.setSaturation(oldHero.getSaturation());
        newHero.setGameMode(GameMode.SURVIVAL);
        oldHero.getActivePotionEffects().forEach(newHero::addPotionEffect);
        newHero.setLevel(oldHero.getLevel());
        newHero.setExp(oldHero.getExp());
        newHero.setFallDistance(oldHero.getFallDistance());
        newHero.setRemainingAir(oldHero.getRemainingAir());
        newHero.setFireTicks(oldHero.getFireTicks());
        newHero.setFreezeTicks(oldHero.getFreezeTicks());
        newHero.getInventory().setHeldItemSlot(oldHero.getInventory().getHeldItemSlot());

        Entity vehicle = oldHero.getVehicle();
        if (vehicle != null) {
            vehicle.removePassenger(oldHero);
        }

        newHero.teleport(oldHero.getLocation());
        sendToLimbo(oldHero);

        if (vehicle != null) {
            vehicle.addPassenger(newHero);
        }

        // after teleport bc things like chests
        newHero.getInventory().clear();
        newHero.getInventory().setContents(oldHero.getInventory().getContents());

        Location newSpawn = getBedFromSpawn(oldHero);
        newHero.setRespawnLocation(newSpawn);

        msg(ChatColor.YELLOW + oldHero.getName() + "'s turn is over. It's now " + newHero.getName() + "'s turn!");
        nextTurnTime = System.currentTimeMillis() + (turnTime * 1000L);
    }

    private void sendToLimbo(Player player) {
        Location spawnLocation = TakingTurnz.getInstance().getLimbo().getSpawnLocation();

        if (player.isDead()) {
            player.spigot().respawn();
        }

        if (player.isSleeping()) {
            player.setGameMode(GameMode.SPECTATOR);
            Bukkit.getScheduler().runTaskLater(TakingTurnz.getInstance(), () -> {
                player.setGameMode(GameMode.CREATIVE);
                player.teleport(spawnLocation);
            }, 5L);
        } else {
            player.setGameMode(GameMode.CREATIVE);
            player.teleport(spawnLocation);
        }
    }

    public void msg(String msg) {
        for (UUID id : players) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                p.sendMessage(msg);
            }
        }
    }

    private int nextHeroIndex() {
        return nextHeroIndex(true);
    }

    private int nextHeroIndex(boolean increment) {
        int ind = heroIndex;
        while (true) {
            ind = (ind + 1) % players.size();

            Player p = Bukkit.getPlayer(players.get(ind));
            if (p != null) {
                if (increment) {
                    heroIndex = ind;
                }
                return ind;
            }
        }
    }

    private Location getBedFromSpawn(Player player) {
        Location spawn = player.getRespawnLocation();
        if (spawn == null) {
            return null;
        }

        World world = spawn.getWorld();

        // search nearby for a bed
        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = spawn.clone().add(x, y, z);
                    Material mat = loc.getBlock().getType();
                    if (mat.name().endsWith("_BED")) {
                        return loc;
                    }
                }
            }
        }

        return null;
    }

    public void playerDisconnect(Player player) {
        if (player.equals(getHero())) {
            msg(ChatColor.RED + player.getName() + " has disconnected during their turn. Moving to next player.");
            nextTurn(player);
        }
    }

    public boolean contains(Player player) {
        return players.contains(player.getUniqueId());
    }
}
