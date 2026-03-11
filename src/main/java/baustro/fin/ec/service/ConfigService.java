package baustro.fin.ec.service;

import baustro.fin.ec.util.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigService {

    private static ConfigService instance;

    private ConfigService() {}

    public static ConfigService getInstance() {
        if (instance == null) instance = new ConfigService();
        return instance;
    }

    public String get(String key) {
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement("SELECT value FROM config WHERE key = ?")) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("value") : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void set(String key, String value) {
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(
                    "INSERT INTO config(key,value) VALUES(?,?) " +
                    "ON CONFLICT(key) DO UPDATE SET value=excluded.value")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isSetupDone() {
        return "true".equals(get("setup_done"));
    }

    public boolean hasMasterPassword() {
        String hash = get("master_password_hash");
        return hash != null && !hash.isEmpty();
    }
}
