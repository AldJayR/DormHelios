package com.dormhelios.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * System-wide logger utility for DormHelios application
 * Logs messages to a file and keeps them in memory for display
 */
public class Logger {
    private static final Logger instance = new Logger();
    private static final String LOG_FOLDER = "logs";
    private static final String LOG_FILE_PREFIX = "dormhelios_";
    private static final String LOG_FILE_EXT = ".log";
    private static final int MAX_MEMORY_LOGS = 1000;
    
    private final List<LogEntry> logEntries;
    private final SimpleDateFormat dateFormat;
    private String currentLogFile;
    
    private Logger() {
        this.logEntries = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        initLogDirectory();
        setupCurrentLogFile();
    }
    
    public static Logger getInstance() {
        return instance;
    }
    
    /**
     * Logs an info message
     * @param source The source of the log (class/component name)
     * @param message The log message
     */
    public void info(String source, String message) {
        log(LogLevel.INFO, source, message);
    }
    
    /**
     * Logs a warning message
     * @param source The source of the log (class/component name)
     * @param message The log message
     */
    public void warning(String source, String message) {
        log(LogLevel.WARNING, source, message);
    }
    
    /**
     * Logs an error message
     * @param source The source of the log (class/component name)
     * @param message The log message
     */
    public void error(String source, String message) {
        log(LogLevel.ERROR, source, message);
    }
    
    /**
     * Logs an error message with exception details
     * @param source The source of the log (class/component name)
     * @param message The log message
     * @param e The exception
     */
    public void error(String source, String message, Exception e) {
        log(LogLevel.ERROR, source, message + " - Exception: " + e.getMessage());
    }
    
    /**
     * Logs a system message
     * @param source The source of the log (class/component name)
     * @param message The log message
     */
    public void system(String source, String message) {
        log(LogLevel.SYSTEM, source, message);
    }
    
    /**
     * Returns all log entries currently in memory
     * @return List of LogEntry objects
     */
    public List<LogEntry> getLogEntries() {
        return new ArrayList<>(logEntries);
    }
    
    /**
     * Returns a formatted string with all logs for display
     * @return Formatted string containing all logs
     */
    public String getFormattedLogs() {
        StringBuilder sb = new StringBuilder();
        for (LogEntry entry : logEntries) {
            sb.append(entry.toString()).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Returns the path to the current log file
     * @return Current log file path
     */
    public String getCurrentLogFile() {
        return currentLogFile;
    }
    
    private void log(LogLevel level, String source, String message) {
        LogEntry entry = new LogEntry(level, source, message, new Date());
        
        // Add to memory cache
        logEntries.add(entry);
        if (logEntries.size() > MAX_MEMORY_LOGS) {
            logEntries.remove(0); // Remove oldest entry if we exceed max
        }
        
        // Write to file
        writeToLogFile(entry);
    }
    
    private void initLogDirectory() {
        File logDir = new File(LOG_FOLDER);
        if (!logDir.exists()) {
            logDir.mkdir();
        }
    }
    
    private void setupCurrentLogFile() {
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        currentLogFile = LOG_FOLDER + File.separator + 
                         LOG_FILE_PREFIX + 
                         fileDateFormat.format(new Date()) + 
                         LOG_FILE_EXT;
    }
    
    private void writeToLogFile(LogEntry entry) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentLogFile, true))) {
            writer.write(entry.toString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    
    /**
     * LogEntry class to store individual log messages
     */
    public static class LogEntry {
        private final LogLevel level;
        private final String source;
        private final String message;
        private final Date timestamp;
        
        public LogEntry(LogLevel level, String source, String message, Date timestamp) {
            this.level = level;
            this.source = source;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public LogLevel getLevel() {
            return level;
        }
        
        public String getSource() {
            return source;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Date getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("[%s] %s (%s): %s", 
                    sdf.format(timestamp), 
                    level.name(), 
                    source, 
                    message);
        }
    }
    
    /**
     * Log levels for the system
     */
    public enum LogLevel {
        INFO,
        WARNING,
        ERROR,
        SYSTEM
    }
}