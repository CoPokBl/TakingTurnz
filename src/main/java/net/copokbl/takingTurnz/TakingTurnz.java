package net.copokbl.takingTurnz;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TakingTurnz extends JavaPlugin implements Listener {
    private static TakingTurnz instance;
    private final List<Game> games = new ArrayList<>();
    private World limbo;

    @Override
    public void onEnable() {
        instance = this;

        Objects.requireNonNull(getCommand("takingturnz")).setExecutor(new TakeTurnzCommand());

        getServer().getPluginManager().registerEvents(this, this);

        // make limbo world
        WorldCreator wc = new WorldCreator("taking_turnz_limbo");
        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.seed("michael is bad".hashCode());
        this.limbo = wc.createWorld();

        getLogger().info("TakingTurnz plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        for (Game game : games) {
            if (game.contains(e.getPlayer())) {
                game.playerDisconnect(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Game game = getGame(e.getPlayer());
        if (game == null) {
            return;
        }

        // they were in game, go to limbo
        e.getPlayer().spigot().respawn();
        e.getPlayer().teleport(limbo.getSpawnLocation());
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent e) {
        if (e.getPlayer().getWorld().getName().equals("taking_turnz_limbo")) {
            e.setCancelled(true);
        }
    }

    public @Nullable Game getGame(Player player) {
        for (Game game : games) {
            if (game.contains(player)) {
                return game;
            }
        }
        return null;
    }

    public List<Game> getGames() {
        return games;
    }

    public World getLimbo() {
        return limbo;
    }

    public static TakingTurnz getInstance() {
        return instance;
    }
}
