package net.copokbl.takingTurnz;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TakingTurnz extends JavaPlugin {
    private static TakingTurnz instance;

    @Override
    public void onEnable() {
        instance = this;

        Objects.requireNonNull(getCommand("takingturnz")).setExecutor(new TakeTurnzCommand());

        getLogger().info("TakingTurnz plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static TakingTurnz getInstance() {
        return instance;
    }
}
