package com.dormhelios.view;

import com.dormhelios.model.entity.Tenant;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import java.util.logging.Logger;

public class SendReminderDialog extends javax.swing.JDialog {
    
    private static final Logger LOGGER = Logger.getLogger(SendReminderDialog.class.getName());
    private boolean saved = false;

    /**
     * Creates new form SendReminderView
     */
    public SendReminderDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setupComponents();
    }
    
    private void setupComponents() {
        setLocationRelativeTo(getParent()); // Center relative to parent
        setTitle("Send Payment Reminder");
    }
    
    /**
     * Adds an ActionListener to the tenant ComboBox
     * @param listener ActionListener to be notified when tenant selection changes
     */
    public void addTenantSelectionListener(ActionListener listener) {
        tenantComboBox.addActionListener(listener);
    }
    
    /**
     * Sets the model for the Tenant ComboBox with tenant data.
     * @param tenants List of tenants to populate the dropdown
     */
    public void setTenantComboBoxModel(List<Tenant> tenants) {
        Vector<ComboBoxItem<Integer>> model = new Vector<>();
        model.add(new ComboBoxItem<>(null, "Select Tenant...")); // Add prompt item with null ID
        
        if (tenants != null) {
            for (Tenant tenant : tenants) {
                String displayName = tenant.getLastName() + ", " + tenant.getFirstName();
                if (tenant.getRoomId() != null) {
                    displayName += " (Room: " + tenant.getRoomId() + ")"; // Add room info if available
                }
                model.add(new ComboBoxItem<>(tenant.getTenantId(), displayName));
            }
        }
        
        tenantComboBox.setModel(new DefaultComboBoxModel<>(model));
    }
    
    /**
     * Helper method to update the contact number field when a tenant is selected
     */
    private void updateContactNumber() {
        ComboBoxItem<Integer> selectedItem = (ComboBoxItem<Integer>) tenantComboBox.getSelectedItem();
        if (selectedItem != null && selectedItem.getId() != null) {
            // In a real implementation, this would fetch the tenant's phone number
            // For now, we'll just clear the field as it should be populated by the controller
            contactNumberComboBox.setText("");
        } else {
            contactNumberComboBox.setText("");
        }
    }
    
    /**
     * Sets the contact number in the text field
     * @param phoneNumber The phone number to display
     */
    public void setContactNumber(String phoneNumber) {
        contactNumberComboBox.setText(phoneNumber);
    }
    
    /**
     * Get the selected tenant ID
     * @return The selected tenant ID or null if none selected
     */
    public Integer getSelectedTenantId() {
        ComboBoxItem<Integer> selectedItem = (ComboBoxItem<Integer>) tenantComboBox.getSelectedItem();
        return selectedItem != null ? selectedItem.getId() : null;
    }
    
    /**
     * Get the message to send
     * @return The message text
     */
    public String getMessage() {
        return jTextArea1.getText();
    }
    
    /**
     * Adds an ActionListener to the Send button.
     * @param listener ActionListener provided by the controller.
     */
    public void addSendButtonListener(ActionListener listener) {
        sendButton1.addActionListener(listener);
    }

    /**
     * Adds an ActionListener to the Cancel button.
     * @param listener ActionListener provided by the controller.
     */
    public void addCancelButtonListener(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }
    
    /**
     * Pre-fills the message text area with a template message
     * @param template The template message to add
     */
    public void setMessageTemplate(String template) {
        jTextArea1.setText(template);
    }
    
    /**
     * Makes the dialog visible. Resets the saved flag.
     */
    public void showDialog() {
        this.saved = false;
        setVisible(true);
    }

    /**
     * Hides and disposes of the dialog window.
     */
    public void closeDialog() {
        dispose();
    }

    /**
     * Sets the flag indicating the send button was clicked.
     */
    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    /**
     * Checks if the Send button was clicked before the dialog was closed.
     * @return true if Send was clicked, false otherwise.
     */
    public boolean isSaved() {
        return saved;
    }
    
    /**
     * Display an error message to the user
     * @param message The error message to display
     */
    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Display a success message to the user
     * @param message The success message to display
     */
    public void displaySuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Disables the send button to prevent multiple submissions
     */
    public void disableSendButton() {
        sendButton1.setEnabled(false);
    }
    
    /**
     * Enables the send button
     */
    public void enableSendButton() {
        sendButton1.setEnabled(true);
    }
    
    /**
     * Simulates sending an SMS by displaying a success message dialog.
     * This is a placeholder for actual SMS API implementation.
     * 
     * @param tenantName The name of the tenant
     * @param phoneNumber The phone number being sent to
     * @param message The message content
     * @return Always returns true (simulating successful send)
     */
    public boolean simulateSendSMS(String tenantName, String phoneNumber, String message) {
        String confirmationMessage = String.format(
            "SMS Reminder Sent (Simulation)\n\nTo: %s (%s)\nMessage: %s", 
            tenantName, 
            phoneNumber,
            message
        );
        
        JOptionPane.showMessageDialog(
            this, 
            confirmationMessage,
            "Reminder Sent", 
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // Log the simulated SMS
        LOGGER.info("Simulated SMS sent to " + tenantName + " at " + phoneNumber);
        
        // Always return success in simulation mode
        return true;
    }
    
    // --- Helper Class for ComboBox Items ---
    private static class ComboBoxItem<T> {
        private final T id;
        private final String displayValue;

        public ComboBoxItem(T id, String displayValue) {
            this.id = id;
            this.displayValue = displayValue;
        }

        public T getId() {
            return id;
        }

        @Override
        public String toString() {
            return displayValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ComboBoxItem<?> that = (ComboBoxItem<?>) o;
            return java.util.Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(id);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        tenantComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        contactNumberComboBox = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        cancelButton = new javax.swing.JButton();
        sendButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Send Reminder");

        jLabel2.setText("Tenant");

        tenantComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel3.setText("Contact Number");

        contactNumberComboBox.setEnabled(false);
        contactNumberComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contactNumberComboBoxActionPerformed(evt);
            }
        });

        jLabel4.setText("Message");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        sendButton1.setText("Send");
        sendButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(contactNumberComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(tenantComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(81, 81, 81)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sendButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tenantComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contactNumberComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void contactNumberComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contactNumberComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_contactNumberComboBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void sendButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sendButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(SendReminderDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SendReminderDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SendReminderDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SendReminderDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SendReminderDialog dialog = new SendReminderDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField contactNumberComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton sendButton1;
    private javax.swing.JComboBox tenantComboBox;
    // End of variables declaration//GEN-END:variables
}
