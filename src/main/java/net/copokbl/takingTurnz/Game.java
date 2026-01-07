package net.copokbl.takingTurnz;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.List;

public class Game {
    private final int turnTime;
    private final List<Player> players;
    private int heroIndex = 0;
    private long nextTurnTime = 0L;
    private final World limbo;

    private Player getHero() {
        return players.get(heroIndex);
    }

    public Game(int turnTime, List<Player> players) {
        this.turnTime = turnTime;
        this.players = players;

        // make limbo world
        WorldCreator wc = new WorldCreator("limbo_world_" + System.currentTimeMillis());
        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.seed("michael is bad".hashCode());
        this.limbo = wc.createWorld();

        // send all but the hero player to limbo
        players.stream().skip(1).forEach(this::sendToLimbo);
    }

    public void start() {
        msg(ChatColor.GREEN + "Game started! Each player has " + turnTime + " seconds per turn.");

        nextTurnTime = System.currentTimeMillis() + (turnTime * 1000L);

        Bukkit.getScheduler().runTaskTimer(TakingTurnz.getInstance(), () -> {
            // inform players of remaining time
            long timeLeft = (nextTurnTime - System.currentTimeMillis()) / 1000L;
            int nextIndex = (heroIndex + 1) % players.size();

            for (int i = 0; i < players.size(); i++) {
                String message = ChatColor.AQUA + "Time left in " + getHero().getName() + "'s turn: " +
                        ChatColor.YELLOW + timeLeft + " seconds";

                if (i == nextIndex) {
                    message = ChatColor.AQUA + "Time left until your turn: " +
                            ChatColor.YELLOW + timeLeft + " seconds";
                }

                players.get(i).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
            }

            // make pitch lower as time runs out
            if (timeLeft <= 5 && timeLeft > 0) {
                float pitch = 1.0f + (5 - timeLeft) * 0.2f;
                for (Player player : List.of(getHero(), players.get(nextIndex))) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, pitch);
                }
            }

            if (System.currentTimeMillis() >= nextTurnTime) {
                Bukkit.getScheduler().runTask(TakingTurnz.getInstance(), this::nextTurn);
            }
        }, 0L, 20L);
    }

    private void nextTurn() {
        Player oldHero = getHero();
        heroIndex = (heroIndex + 1) % players.size();
        Player newHero = getHero();

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

        newHero.teleport(oldHero.getLocation());
        sendToLimbo(oldHero);

        // after teleport bc things like chests
        newHero.getInventory().clear();
        newHero.getInventory().setContents(oldHero.getInventory().getContents());

        Location newSpawn = getBedFromSpawn(oldHero);
        newHero.setRespawnLocation(newSpawn);

        msg(ChatColor.YELLOW + oldHero.getName() + "'s turn is over. It's now " + newHero.getName() + "'s turn!");
        nextTurnTime = System.currentTimeMillis() + (turnTime * 1000L);
    }

    private void sendToLimbo(Player player) {
        Location spawnLocation = limbo.getSpawnLocation();

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
        for (Player p : players) {
            p.sendMessage(msg);
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
}
