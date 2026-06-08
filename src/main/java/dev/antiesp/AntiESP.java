package dev.antiesp;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiESP extends JavaPlugin {

    private static AntiESP instance;
    private ProtocolManager protocolManager;
    private EntityPacketListener packetListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        protocolManager = ProtocolLibrary.getProtocolManager();
        packetListener = new EntityPacketListener(this, protocolManager);
        packetListener.register();

        getCommand("antiesp").setExecutor(new AntiESPCommand(this));
        getLogger().info("AntiESP enabled. Hiding entities below Y=" + getHideY());
    }

    @Override
    public void onDisable() {
        if (packetListener != null) packetListener.unregister();
        getLogger().info("AntiESP disabled.");
    }

    public int getHideY() {
        return getConfig().getInt("hide-below-y", 16);
    }

    public boolean allowBypass() {
        return getConfig().getBoolean("allow-bypass", true);
    }

    public static AntiESP getInstance() { return instance; }
}
