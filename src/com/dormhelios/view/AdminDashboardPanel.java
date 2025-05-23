package com.dormhelios.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Cursor;
import java.awt.Component;
import com.dormhelios.model.entity.SystemLog;
import com.dormhelios.service.SystemLogService;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.dormhelios.view.SystemLogsDialog;
import com.dormhelios.view.AdminCreateAccountDialog;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.SQLException;
import com.dormhelios.util.DatabaseConnection;
import com.dormhelios.model.dao.UserDAO;
import com.dormhelios.model.dao.UserDAOImpl;
import com.dormhelios.model.entity.User;

public class AdminDashboardPanel extends javax.swing.JPanel {

    /**
     * Creates new form DashboardPanel
     */
    public AdminDashboardPanel() {
        initComponents();
        applyCustomStyling(); // Apply our custom styling after initialization
        initSystemLogsFunctionality();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        jPanel1 = new javax.swing.JPanel();
        tenantDashboardHeaderLabel = new javax.swing.JLabel();
        systemStatusCard = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lastBackupDateValueLabel = new javax.swing.JLabel();
        databaseStatusValueLabel = new javax.swing.JLabel();
        userAccountsCArd = new javax.swing.JPanel();
        revenueTitleLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        totalAccountsValueLabel = new javax.swing.JLabel();
        activeAccountsValueLabel = new javax.swing.JLabel();
        systemLogsCard = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        systemLogsTextArea = new javax.swing.JTextArea();
        quickActionsCard = new javax.swing.JPanel();
        revenueTitleLabel2 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        backupButton = new javax.swing.JButton();
        viewSystemLogsButton = new javax.swing.JButton();
        createUserAccountButton = new javax.swing.JButton();
        printLogsButton = new javax.swing.JButton();

        jFormattedTextField1.setText("jFormattedTextField1");

        setBackground(new java.awt.Color(250, 250, 250));
        setPreferredSize(new java.awt.Dimension(1023, 787));

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        tenantDashboardHeaderLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 36)); // NOI18N
        tenantDashboardHeaderLabel.setText("System Dashboard");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(tenantDashboardHeaderLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tenantDashboardHeaderLabel)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        systemStatusCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        jLabel2.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel2.setText("System Status");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Database Status");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel9.setText("Last Backup:");

        lastBackupDateValueLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lastBackupDateValueLabel.setText("Mar 02, 2025");

        databaseStatusValueLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        databaseStatusValueLabel.setText("Online");

        javax.swing.GroupLayout systemStatusCardLayout = new javax.swing.GroupLayout(systemStatusCard);
        systemStatusCard.setLayout(systemStatusCardLayout);
        systemStatusCardLayout.setHorizontalGroup(
            systemStatusCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemStatusCardLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(systemStatusCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(systemStatusCardLayout.createSequentialGroup()
                        .addGroup(systemStatusCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(systemStatusCardLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(systemStatusCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel1))))
                        .addGap(22, 119, Short.MAX_VALUE)
                        .addGroup(systemStatusCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(databaseStatusValueLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lastBackupDateValueLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(15, 15, 15)))
                .addContainerGap())
        );
        systemStatusCardLayout.setVerticalGroup(
            systemStatusCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemStatusCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(systemStatusCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(databaseStatusValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(systemStatusCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(lastBackupDateValueLabel))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        revenueTitleLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        revenueTitleLabel.setText("User Accounts");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel10.setText("Total Accounts: ");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel11.setText("Active Accounts");

        totalAccountsValueLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        totalAccountsValueLabel.setText("42");

        activeAccountsValueLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        activeAccountsValueLabel.setText("25");

        javax.swing.GroupLayout userAccountsCArdLayout = new javax.swing.GroupLayout(userAccountsCArd);
        userAccountsCArd.setLayout(userAccountsCArdLayout);
        userAccountsCArdLayout.setHorizontalGroup(
            userAccountsCArdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(userAccountsCArdLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(userAccountsCArdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(revenueTitleLabel)
                    .addGroup(userAccountsCArdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(userAccountsCArdLayout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addGroup(userAccountsCArdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(userAccountsCArdLayout.createSequentialGroup()
                                    .addComponent(jLabel11)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(activeAccountsValueLabel))
                                .addGroup(userAccountsCArdLayout.createSequentialGroup()
                                    .addComponent(jLabel10)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(totalAccountsValueLabel))))
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        userAccountsCArdLayout.setVerticalGroup(
            userAccountsCArdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(userAccountsCArdLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(revenueTitleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(userAccountsCArdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(totalAccountsValueLabel))
                .addGap(18, 18, 18)
                .addGroup(userAccountsCArdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(activeAccountsValueLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        systemLogsCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        jLabel3.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel3.setText("Recent System Logs");

        systemLogsTextArea.setEditable(false);
        systemLogsTextArea.setColumns(20);
        systemLogsTextArea.setRows(5);
        jScrollPane2.setViewportView(systemLogsTextArea);

        javax.swing.GroupLayout systemLogsCardLayout = new javax.swing.GroupLayout(systemLogsCard);
        systemLogsCard.setLayout(systemLogsCardLayout);
        systemLogsCardLayout.setHorizontalGroup(
            systemLogsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemLogsCardLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(systemLogsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 844, Short.MAX_VALUE)
                    .addComponent(jLabel3)
                    .addComponent(jSeparator3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        systemLogsCardLayout.setVerticalGroup(
            systemLogsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemLogsCardLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        revenueTitleLabel2.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        revenueTitleLabel2.setText("Quick Actions");

        backupButton.setText("Run Backup");

        viewSystemLogsButton.setText("View System Logs");

        createUserAccountButton.setText("Create User Account");

        printLogsButton.setText("Print Logs");

        javax.swing.GroupLayout quickActionsCardLayout = new javax.swing.GroupLayout(quickActionsCard);
        quickActionsCard.setLayout(quickActionsCardLayout);
        quickActionsCardLayout.setHorizontalGroup(
            quickActionsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quickActionsCardLayout.createSequentialGroup()
                .addGroup(quickActionsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(quickActionsCardLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(quickActionsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(revenueTitleLabel2)
                            .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 838, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(quickActionsCardLayout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addComponent(backupButton, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(83, 83, 83)
                        .addComponent(viewSystemLogsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(createUserAccountButton)
                        .addGap(56, 56, 56)
                        .addComponent(printLogsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        quickActionsCardLayout.setVerticalGroup(
            quickActionsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quickActionsCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(revenueTitleLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(quickActionsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(quickActionsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(backupButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(viewSystemLogsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                        .addComponent(printLogsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                    .addComponent(createUserAccountButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(systemStatusCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48)
                        .addComponent(userAccountsCArd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(systemLogsCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(quickActionsCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(123, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(systemStatusCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(userAccountsCArd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(30, 30, 30)
                .addComponent(systemLogsCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(quickActionsCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(93, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Sets the tenant's name in the dashboard header
     * 
     * @param name The tenant's name to display
     */
    public void setTenantName(String name) {
        tenantDashboardHeaderLabel.setText("Welcome, " + name);
    }
    
 
    
    /**
     * Applies custom Tailwind-inspired styling to the dashboard panel components.
     * Call this method after initComponents() in the constructor.
     */
    private void applyCustomStyling() {
        // Tailwind color palette
        Color white = new Color(255, 255, 255);       // white
        Color primary = new Color(59, 130, 246);      // blue-500 
        Color primaryLight = new Color(191, 219, 254); // blue-200
        Color primaryDark = new Color(37, 99, 235);   // blue-600
        Color secondary = new Color(79, 70, 229);     // indigo-600
        Color secondaryLight = new Color(224, 231, 255); // indigo-100
        Color success = new Color(34, 197, 94);       // green-500
        Color successLight = new Color(187, 247, 208); // green-100
        Color danger = new Color(239, 68, 68);        // red-500
        Color warning = new Color(245, 158, 11);      // amber-500
        Color warningLight = new Color(254, 240, 138); // amber-100
        Color bgLight = new Color(248, 250, 252);     // slate-50
        Color bgGray = new Color(243, 244, 246);      // gray-100
        Color textDark = new Color(15, 23, 42);       // slate-900
        Color textGray = new Color(71, 85, 105);      // slate-600
        Color textLight = new Color(148, 163, 184);   // slate-400
        Color borderColor = new Color(226, 232, 240); // slate-200
        Color cardHoverBg = new Color(241, 245, 249); // slate-100

        // Set background color for the entire panel
        this.setBackground(bgLight);
        
        // Style welcome panel with gradient-like effect
        jPanel1.setBackground(white);
        jPanel1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        tenantDashboardHeaderLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        tenantDashboardHeaderLabel.setForeground(textDark);
        
        // Style announcement card
        styleCard(systemStatusCard, primary, primaryLight, "Announcements", jLabel2);
      
        
        // Style payment history panel
        styleCard(systemLogsCard, success, successLight, "Payment History", jLabel3);
        // Style table
    
        
        // Style landlord information card
        styleCard(quickActionsCard, warning, warningLight, "Landlord Information", revenueTitleLabel2);

        
        // Apply consistent styling to separators
        jSeparator1.setForeground(borderColor);
        jSeparator2.setForeground(borderColor);
        jSeparator3.setForeground(borderColor);
        jSeparator4.setForeground(borderColor);
    }

    /**
     * Helper method to style a card with Tailwind-inspired design
     * 
     * @param card The panel to style
     * @param accentColor The accent color for the card
     * @param lightColor The light background color
     * @param title The title of the card
     * @param titleLabel The title label component
     */
    private void styleCard(JPanel card, Color accentColor, Color lightColor, String title, JLabel titleLabel) {
        // Set card background and border
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 90)),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 14, 16)
            )
        ));
        
        // Add a subtle rounded corner effect
        card.putClientProperty("JComponent.roundRect", true);
        
        // Style the title
        if (titleLabel != null) {
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(accentColor);
        }
        
        // Add a subtle hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, accentColor),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                        BorderFactory.createEmptyBorder(12, 16, 14, 16)
                    )
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 90)),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                        BorderFactory.createEmptyBorder(12, 16, 14, 16)
                    )
                ));
            }
        });
    }

    /**
     * Helper method to style a label consistently
     * 
     * @param label The label to style
     * @param color The text color
     * @param size The font size
     * @param style The font style
     */
    private void styleLabel(JLabel label, Color color, int size, int style) {
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
    }

    /**
     * Initializes system log loading, and button actions for view, print, and backup
     */
    private void initSystemLogsFunctionality() {
        // Load initial logs and statistics
        refreshSystemLogs();
        loadStatistics();

        viewSystemLogsButton.addActionListener(e -> {
            SystemLogsDialog dialog = new SystemLogsDialog((java.awt.Frame) this.getTopLevelAncestor(), true);
            dialog.setLogsText(systemLogsTextArea.getText());
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            SystemLogService.log("AdminDashboardPanel", "Viewed system logs");
        });

        printLogsButton.addActionListener(e -> {
            try {
                systemLogsTextArea.print();
                SystemLogService.log("AdminDashboardPanel", "Printed system logs");
            } catch (PrinterException ex) {
                SystemLogService.log("AdminDashboardPanel", "Failed to print logs: " + ex.getMessage());
            }
        });

        backupButton.addActionListener(e -> {
            // Perform mysqldump backup with user-selected directory
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Backup Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return; // user canceled
            }
            File backupDir = chooser.getSelectedFile();
            try {
                // Load DB properties
                Properties props = new Properties();
                try (InputStream in = com.dormhelios.util.DatabaseConnection.class
                        .getResourceAsStream("/config/database.properties")) {
                    if (in != null) props.load(in);
                }
                String url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/dormhelios_db");
                String user = props.getProperty("db.user", "root");
                String pass = props.getProperty("db.password", "");
                String dbName = url.substring(url.lastIndexOf('/') + 1).split("\\?")[0];
                // Prepare backup file in chosen directory
                String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                File backupFile = new File(backupDir, "backup_" + timestamp + ".sql");

                // Hardcode mysqldump path
                String dumpCmd = "C:\\laragon\\bin\\mysql\\mysql-8.0.30-winx64\\bin\\mysqldump.exe";
                ArrayList<String> cmd = new ArrayList<>();
                cmd.add(dumpCmd);
                cmd.add("-u"); cmd.add(user);
                if (!pass.isEmpty()) cmd.add("-p" + pass);
                cmd.add(dbName);
                cmd.add("-r"); cmd.add(backupFile.getAbsolutePath());

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                int exitCode = p.waitFor();
                if (exitCode == 0) {
                    SystemLogService.log("AdminDashboardPanel", "Database backup saved to " + backupFile.getAbsolutePath());
                    String today = new SimpleDateFormat("MMM dd, yyyy").format(new Date());
                    lastBackupDateValueLabel.setText(today);
                    JOptionPane.showMessageDialog(this, "Backup saved to " + backupFile.getAbsolutePath(), "Backup Successful", JOptionPane.INFORMATION_MESSAGE);
                    refreshSystemLogs();
                } else {
                    SystemLogService.log("AdminDashboardPanel", "Backup failed with code: " + exitCode);
                    JOptionPane.showMessageDialog(this, "Backup failed with exit code: " + exitCode, "Backup Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | InterruptedException ex) {
                SystemLogService.log("AdminDashboardPanel", "Backup exception: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage(), "Backup Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        createUserAccountButton.addActionListener(e -> {
            AdminCreateAccountDialog dialog = new AdminCreateAccountDialog((java.awt.Frame) this.getTopLevelAncestor(), true);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            SystemLogService.log("AdminDashboardPanel", "Opened Create User Account");
        });
    }

    /**
     * Loads database status and user account statistics from the database.
     */
    private void loadStatistics() {
        // Database connection status
        try (Connection conn = DatabaseConnection.getConnection()) {
            databaseStatusValueLabel.setText("Online");
        } catch (SQLException ex) {
            databaseStatusValueLabel.setText("Offline");
        }
        // Total and active user counts
        UserDAO userDAO = new UserDAOImpl();
        List<User> users = userDAO.findAll();
        int total = users.size();
        long activeCount = users.stream().filter(User::isActive).count();
        totalAccountsValueLabel.setText(String.valueOf(total));
        activeAccountsValueLabel.setText(String.valueOf(activeCount));
    }

    /**
     * Refreshes the systemLogsTextArea with all logs from the database
     */
    private void refreshSystemLogs() {
        List<SystemLog> logs = SystemLogService.getAllLogs();
        StringBuilder sb = new StringBuilder();
        for (SystemLog log : logs) {
            sb.append("[").append(log.getName()).append("] ")
              .append(log.getValue()).append("\n");
        }
        systemLogsTextArea.setText(sb.toString());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activeAccountsValueLabel;
    private javax.swing.JButton backupButton;
    private javax.swing.JButton createUserAccountButton;
    private javax.swing.JLabel databaseStatusValueLabel;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel lastBackupDateValueLabel;
    private javax.swing.JButton printLogsButton;
    private javax.swing.JPanel quickActionsCard;
    private javax.swing.JLabel revenueTitleLabel;
    private javax.swing.JLabel revenueTitleLabel2;
    private javax.swing.JPanel systemLogsCard;
    private javax.swing.JTextArea systemLogsTextArea;
    private javax.swing.JPanel systemStatusCard;
    private javax.swing.JLabel tenantDashboardHeaderLabel;
    private javax.swing.JLabel totalAccountsValueLabel;
    private javax.swing.JPanel userAccountsCArd;
    private javax.swing.JButton viewSystemLogsButton;
    // End of variables declaration//GEN-END:variables
}
