package fr.justop.hycraftQuestsAddons;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {

    private final HycraftQuestsAddons plugin;
    private Connection connection;

    public DBManager(HycraftQuestsAddons plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            File dbFile = new File(plugin.getDataFolder(), "inventories.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            connection = DriverManager.getConnection(url);

            plugin.getLogger().info("SQLite connecté !");
        } catch (SQLException e) {
            plugin.getLogger().severe("Impossible de connecter SQLite");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        String sql = """
        CREATE TABLE IF NOT EXISTS player_inventory (
            uuid TEXT PRIMARY KEY,
            inventory BLOB NOT NULL,
            armor BLOB NOT NULL,
            offhand BLOB NOT NULL,
            xp REAL,
            level INTEGER,
            health REAL,
            food INTEGER,
            saved_at INTEGER
        );
    """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
