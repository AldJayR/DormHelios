package com.dormhelios.model.dao;

import com.dormhelios.model.entity.SystemLog;
import java.util.List;

/**
 * DAO interface for managing system_logs table
 */
public interface SystemLogDAO {
    /**
     * Retrieves all system log entries, ordered by name (timestamp)
     * @return list of SystemLog
     */
    List<SystemLog> findAll();

    /**
     * Inserts a new system log entry
     * @param log the SystemLog entity to insert
     * @return true if insert succeeded, false otherwise
     */
    boolean insert(SystemLog log);
}