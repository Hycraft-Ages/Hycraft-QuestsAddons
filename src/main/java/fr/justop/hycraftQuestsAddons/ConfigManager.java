package fr.justop.hycraftQuestsAddons;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {

    private final HycraftQuestsAddons plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(HycraftQuestsAddons plugin) {
        this.plugin = plugin;
        setup();
    }

    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "whitelisted_cmds.yml");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(configFile);

                config.set("messages.denied", "§eVous ne pouvez pas executer de commande tant que la quête est en cours! Utilisez §6/q interrupt");
                config.set("allowed-commands", List.of("/q interrupt", "/q rejoin", "/feed", "/heal"));

                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getWhitelistConfig() {
        return config;
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}
