package com.dormhelios.view;

import java.awt.CardLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Color;

public class MainDashboardView extends javax.swing.JFrame {

    /**
     * Creates new form MainDashboardView
     */
    public static final String DASHBOARD_PANEL = "DASHBOARD";
    public static final String TENANTS_PANEL = "TENANTS";
    public static final String ROOMS_PANEL = "ROOMS";
    public static final String PAYMENTS_PANEL = "PAYMENTS";
    public static final String SETTINGS_PANEL = "SETTINGS";
    private CardLayout cardLayout;

    public MainDashboardView() {
        initComponents();
        setLocationRelativeTo(null);
        setupComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")

    private void setupComponents() {
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sidebarPanel = new javax.swing.JPanel();
        userInfoLabel = new javax.swing.JLabel();
        tenantsButton = new javax.swing.JButton();
        settingsButton = new javax.swing.JButton();
        dashboardButton = new javax.swing.JButton();
        dormsButton = new javax.swing.JButton();
        paymentsButton = new javax.swing.JButton();
        logoutButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        contentPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Dashboard | DormHelios");

        sidebarPanel.setBackground(new java.awt.Color(102, 204, 255));
        sidebarPanel.setPreferredSize(new java.awt.Dimension(200, 704));

        userInfoLabel.setFont(new java.awt.Font("Segoe UI Semibold", 1, 24)); // NOI18N
        userInfoLabel.setText("Username");

        tenantsButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tenantsButton.setText("Tenants");
        tenantsButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tenantsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tenantsButtonActionPerformed(evt);
            }
        });

        settingsButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        settingsButton.setText("Settings");
        settingsButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsButtonActionPerformed(evt);
            }
        });

        dashboardButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        dashboardButton.setText("Dashboard");
        dashboardButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        dashboardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dashboardButtonActionPerformed(evt);
            }
        });

        dormsButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        dormsButton.setText("Dorms");
        dormsButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        dormsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dormsButtonActionPerformed(evt);
            }
        });

        paymentsButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        paymentsButton.setText("Payments");
        paymentsButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        paymentsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paymentsButtonActionPerformed(evt);
            }
        });

        logoutButton.setForeground(new java.awt.Color(51, 153, 255));
        logoutButton.setText("<html>\n<u>Logout</u>\n</html>");
        logoutButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sidebarPanelLayout = new javax.swing.GroupLayout(sidebarPanel);
        sidebarPanel.setLayout(sidebarPanelLayout);
        sidebarPanelLayout.setHorizontalGroup(
            sidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tenantsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(settingsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(dashboardButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(dormsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(paymentsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sidebarPanelLayout.createSequentialGroup()
                .addContainerGap(48, Short.MAX_VALUE)
                .addComponent(userInfoLabel)
                .addGap(42, 42, 42))
            .addGroup(sidebarPanelLayout.createSequentialGroup()
                .addComponent(logoutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        sidebarPanelLayout.setVerticalGroup(
            sidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebarPanelLayout.createSequentialGroup()
                .addGap(145, 145, 145)
                .addComponent(userInfoLabel)
                .addGap(28, 28, 28)
                .addComponent(dashboardButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(tenantsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(dormsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(paymentsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(settingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logoutButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(sidebarPanel, java.awt.BorderLayout.LINE_START);

        contentPanel.setBackground(new java.awt.Color(204, 204, 255));
        contentPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        contentPanel.setPreferredSize(new java.awt.Dimension(1066, 800));

        javax.swing.GroupLayout contentPanelLayout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1064, Short.MAX_VALUE)
        );
        contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 798, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(contentPanel);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void setUserDisplayName(String userName) {
        // Assuming userInfoLabel is the name of the JLabel in the sidebar
        userInfoLabel.setText(userName);
    }

    
    
    public JButton getDashboardButton() {
        return dashboardButton;
    }

    public JButton getDormsButton() {
        return dormsButton;
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

    public JButton getPaymentsButton() {
        return paymentsButton;
    }

    public JButton getSettingsButton() {
        return settingsButton;
    }

    /**
     * Adds an ActionListener for the Dashboard navigation button.
     *
     * @param listener ActionListener provided by the controller.
     */
    public JButton getTenantsButton() {
        return tenantsButton;
    }

    public void addDashboardButtonListener(ActionListener listener) {
        dashboardButton.addActionListener(listener);
    }

    /**
     * Adds an ActionListener for the Tenants navigation button.
     *
     * @param listener ActionListener provided by the controller.
     */
    public void addTenantsButtonListener(ActionListener listener) {
        tenantsButton.addActionListener(listener);
    }

    /**
     * Adds an ActionListener for the Dorms/Rooms navigation button.
     *
     * @param listener ActionListener provided by the controller.
     */
    public void addDormsButtonListener(ActionListener listener) {
        dormsButton.addActionListener(listener);
    }

    /**
     * Adds an ActionListener for the Payments navigation button.
     *
     * @param listener ActionListener provided by the controller.
     */
    public void addPaymentsButtonListener(ActionListener listener) {
        paymentsButton.addActionListener(listener);
    }

    /**
     * Adds an ActionListener for the Settings navigation button.
     *
     * @param listener ActionListener provided by the controller.
     */
    public void addSettingsButtonListener(ActionListener listener) {
        settingsButton.addActionListener(listener);
    }

    public void addLogoutButtonListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
    }

    public void addContentPanel(JPanel panel, String panelName) {
        contentPanel.add(panel, panelName);
    }

    /**
     * Displays the specified content panel in the main content area. Also
     * updates the highlighting on the sidebar buttons.
     *
     * @param panelName The unique String identifier of the panel to show.
     * @param selectedButton The sidebar JButton corresponding to the panel
     * being shown.
     */
    public void displayPanel(String panelName, JButton selectedButton) {
        cardLayout.show(contentPanel, panelName);
        highlightSidebarButton(selectedButton); // Update sidebar highlight
    }

    private void highlightSidebarButton(JButton selectedButton) {
        // Reset all sidebar buttons to default style
        JButton[] sidebarButtons = {dashboardButton, tenantsButton, dormsButton, paymentsButton, settingsButton};
        for (JButton btn : sidebarButtons) {
            if (btn != null) { // Check if button exists
                btn.setBackground(Color.WHITE); // Default background
                btn.setForeground(new Color(108, 117, 125)); // Default text color (#6C757D)
                btn.setOpaque(true); // Ensure background color is visible
                btn.setContentAreaFilled(true);
                // Reset font weight if needed (optional)
                // btn.setFont(btn.getFont().deriveFont(Font.PLAIN));
            }
        }

        // Apply highlighted style to the selected button
        if (selectedButton != null) {
            selectedButton.setBackground(new Color(233, 236, 239)); // Highlight background (#E9ECEF)
            selectedButton.setForeground(new Color(13, 110, 253)); // Highlight text color (#0D6EFD)
            // Optionally make text bold
            // selectedButton.setFont(selectedButton.getFont().deriveFont(Font.BOLD));
        }
    }

    /**
     * Closes the main application window.
     */
    public void closeView() {
        this.dispose();
    }
    private void tenantsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tenantsButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tenantsButtonActionPerformed

    private void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_settingsButtonActionPerformed

    private void dashboardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dashboardButtonActionPerformed

    private void dormsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dormsButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dormsButtonActionPerformed

    private void paymentsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentsButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_paymentsButtonActionPerformed

    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_logoutButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainDashboardView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainDashboardView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainDashboardView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainDashboardView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainDashboardView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JButton dashboardButton;
    private javax.swing.JButton dormsButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton logoutButton;
    private javax.swing.JButton paymentsButton;
    private javax.swing.JButton settingsButton;
    private javax.swing.JPanel sidebarPanel;
    private javax.swing.JButton tenantsButton;
    private javax.swing.JLabel userInfoLabel;
    // End of variables declaration//GEN-END:variables
}
