package com.dormhelios.model.dao;

import com.dormhelios.model.entity.SystemLog;
import com.dormhelios.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemLogDAOImpl implements SystemLogDAO {
    private static final Logger LOGGER = Logger.getLogger(SystemLogDAOImpl.class.getName());

    private static final String FIND_ALL_SQL = "SELECT name, value FROM system_logs ORDER BY name";
    private static final String INSERT_SQL = "INSERT INTO system_logs (name, value) VALUES (?, ?)";

    @Override
    public List<SystemLog> findAll() {
        List<SystemLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                SystemLog log = new SystemLog();
                log.setName(rs.getString("name"));
                log.setValue(rs.getString("value"));
                logs.add(log);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving system logs", e);
        }
        return logs;
    }

    @Override
    public boolean insert(SystemLog log) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            stmt.setString(1, log.getName());
            stmt.setString(2, log.getValue());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting system log: " + log.getName(), e);
            return false;
        }
    }
}