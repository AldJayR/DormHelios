package com.dormhelios.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Collection of custom table cell renderers to enhance the appearance of tables
 * throughout the application.
 */
public class TableRenderers {

    // Date formatter for consistent date formatting
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a");
    private static final NumberFormat CURRENCY_FORMATTER = new DecimalFormat("₱#,##0.00");
    
    /**
     * Base renderer with common styling for all cells
     */
    public static class BaseRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Apply padding for better readability
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                label.setBorder(new EmptyBorder(4, 8, 4, 8));
                
                // Set vertical alignment
                label.setVerticalAlignment(JLabel.CENTER);
            }
            
            // If the row is selected, we'll let the L&F handle the colors
            if (!isSelected) {
                // Apply zebra striping if enabled
                if (row % 2 == 0) {
                    c.setBackground(table.getBackground());
                } else {
                    // Use the alternating color from UIManager or fallback
                    Color alternateColor = UIManager.getColor("Table.alternateRowColor");
                    c.setBackground(alternateColor != null ? alternateColor : new Color(248, 250, 252));
                }
                
                // Normal foreground color
                c.setForeground(table.getForeground());
            }
            
            return c;
        }
    }
    
    /**
     * Renderer for numeric values with right alignment
     */
    public static class NumberRenderer extends BaseRenderer {
        private final DecimalFormat format;
        
        public NumberRenderer() {
            this("#,##0.##"); // Default number format
        }
        
        public NumberRenderer(String pattern) {
            format = new DecimalFormat(pattern);
            setHorizontalAlignment(JLabel.RIGHT);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value instanceof Number) {
                value = format.format(value);
            }
            
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    /**
     * Renderer for currency values
     */
    public static class CurrencyRenderer extends BaseRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value instanceof BigDecimal || value instanceof Number) {
                value = CURRENCY_FORMATTER.format(value);
            }
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);
            
            // Optional: add a subtle green tint to positive values, red for negative
            if (!isSelected && value != null && value instanceof String) {
                String strValue = (String)value;
                if (strValue.startsWith("-")) {
                    c.setForeground(new Color(220, 53, 69)); // Bootstrap danger red
                } else if (!strValue.equals("₱0.00")) {
                    c.setForeground(new Color(40, 167, 69)); // Bootstrap success green
                }
            }
            
            return c;
        }
    }
    
    /**
     * Renderer for date values
     */
    public static class DateRenderer extends BaseRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value instanceof LocalDate) {
                value = ((LocalDate) value).format(DATE_FORMATTER);
            } else if (value instanceof LocalDateTime) {
                value = ((LocalDateTime) value).format(DATE_TIME_FORMATTER);
            } else if (value instanceof Date) {
                // For java.util.Date (legacy code)
                DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
                value = df.format((Date) value);
            }
            
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    /**
     * Renderer for status values with colored indicators
     */
    public static class StatusRenderer extends BaseRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected && value != null) {
                String status = value.toString().toUpperCase();
                
                // Set a different color based on status
                switch (status) {
                    case "ACTIVE":
                    case "PAID":
                    case "COMPLETED":
                    case "APPROVED":
                    case "AVAILABLE":
                    case "VACANT":
                        c.setForeground(new Color(40, 167, 69)); // Green
                        break;
                    case "PENDING":
                    case "PROCESSING":
                    case "IN PROGRESS":
                        c.setForeground(new Color(255, 193, 7)); // Yellow/Orange
                        break;
                    case "INACTIVE":
                    case "OVERDUE":
                    case "EXPIRED":
                    case "REJECTED":
                    case "OCCUPIED":
                        c.setForeground(new Color(220, 53, 69)); // Red
                        break;
                    case "UNDER_MAINTENANCE":
                    case "MAINTENANCE":
                        c.setForeground(new Color(108, 117, 125)); // Gray
                        break;
                    default:
                        c.setForeground(table.getForeground()); // Default
                        break;
                }
                
                // Make status text bold for emphasis
                Font currentFont = c.getFont();
                c.setFont(currentFont.deriveFont(Font.BOLD));
            }
            
            return c;
        }
    }
    
    /**
     * Renderer for buttons in tables
     */
    public static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        private String actionName;
        
        public ButtonRenderer(String actionName) {
            this.actionName = actionName;
            setOpaque(true);
            setFocusPainted(false);
            setBorderPainted(true);
            setText(actionName);
            
            // Set button styling
            setFont(getFont().deriveFont(12f));
            setMargin(new Insets(2, 8, 2, 8));
            
            // Style based on action type
            if (actionName.equalsIgnoreCase("Edit") || actionName.equalsIgnoreCase("View")) {
                setBackground(new Color(13, 110, 253)); // Bootstrap primary blue
                setForeground(Color.WHITE);
            } else if (actionName.equalsIgnoreCase("Delete") || actionName.equalsIgnoreCase("Remove")) {
                setBackground(new Color(220, 53, 69)); // Bootstrap danger red
                setForeground(Color.WHITE);
            } else {
                setBackground(new Color(108, 117, 125)); // Bootstrap secondary gray
                setForeground(Color.WHITE);
            }
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            // Use value if provided, otherwise use the action name
            setText(value instanceof String ? (String)value : actionName);
            
            if (isSelected) {
                // If the row is selected, adjust button style slightly
                setBackground(getBackground().darker());
            }
            
            return this;
        }
    }
}