package com.dormhelios.service;

import com.dormhelios.model.dao.SystemLogDAO;
import com.dormhelios.model.dao.SystemLogDAOImpl;
import com.dormhelios.model.entity.SystemLog;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service layer for system logs, wrapping DAO operations
 */
public class SystemLogService {
    private static final SystemLogDAO dao = new SystemLogDAOImpl();
    private static final DateTimeFormatter NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Retrieves all system log entries
     */
    public static List<SystemLog> getAllLogs() {
        return dao.findAll();
    }

    /**
     * Writes a log entry with a timestamped key
     * @param source identifier of the component logging the message
     * @param message log message
     */
    public static void log(String source, String message) {
        String timestamp = LocalDateTime.now().format(NAME_FORMATTER);
        String key = timestamp + ":" + source;
        SystemLog entry = new SystemLog(key, message);
        dao.insert(entry);
    }
}