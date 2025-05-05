package com.dormhelios.view;

import com.dormhelios.model.entity.Room; // Import Room entity
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat; // For parsing/displaying currency
import java.text.ParseException;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.text.NumberFormatter;
import javax.swing.SpinnerNumberModel;

public class RoomFormDialog extends javax.swing.JDialog {

    /**
     * Creates new form RoomFormDialog
     */
    private Room currentRoom; // Store the room being edited (null if adding)
    private boolean saved = false;

    
    public RoomFormDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setupComponents();
    }

    private void setupComponents() {
        setLocationRelativeTo(getParent()); // Center relative to parent
        // Populate fixed dropdowns like Status
        statusComboBox.setModel(new DefaultComboBoxModel<>(Room.RoomStatus.values()));

        SpinnerNumberModel capacityModel = new SpinnerNumberModel(1, 1, 10, 1); // Initial, Min, Max, Step
        capacitySpinner.setModel(capacityModel);
        
        // Add a change listener to automatically set slots_available equal to capacity
        capacitySpinner.addChangeListener(e -> {
            // When capacity changes, we'll update slots_available in getRoomData()
        });
    }

    public void setupForAdd() {
        setTitle("Add New Room");
        saveButton.setText("Save Room");
        jLabel1.setText("Add New Room");
        this.currentRoom = null;
        clearForm();
    }

    /**
     * Sets the dialog title and button text for editing an existing room, and
     * populates the form with the room's data.
     *
     * @param room The Room object to edit.
     */
    public void setupForEdit(Room room) {
        setTitle("Edit Room: " + room.getRoomNumber());
        saveButton.setText("Update Room");
        jLabel1.setText("Edit Room");
        currentRoom = room;
        populateForm(room);
    }

    /**
     * Populates the form fields with data from a Room object.
     *
     * @param room The room whose data should be displayed.
     */
    private void populateForm(Room room) {
        if (room == null) {
            clearForm();
            return;
        }
        roomNumberField.setText(room.getRoomNumber());
        capacitySpinner.setValue(room.getCapacity());
        monthlyRateField.setText(room.getMonthlyRate() != null ? room.getMonthlyRate().toPlainString() : "0.00");
        statusComboBox.setSelectedItem(room.getStatus());
    }

    /**
     * Collects data from form fields and returns a Room object. Performs basic
     * validation. Returns null if validation fails.
     *
     * @return A Room object populated with form data, or null if invalid.
     */
    public Room getRoomData() {
        // Basic Validation
        if (roomNumberField.getText().trim().isEmpty()
                || (int) capacitySpinner.getValue() == 0
                || monthlyRateField.getText().trim().isEmpty()) {
            displayErrorMessage("Room Number, Capacity, and Monthly Rate are required.");
            return null;
        }

        Room room = new Room(); // Create a new Room object
        
        // If editing an existing room, preserve the ID
        if (currentRoom != null) {
            room.setRoomId(currentRoom.getRoomId());
        }

        room.setRoomNumber(roomNumberField.getText().trim());

        // Parse capacity
        try {
            int capacity = (Integer) capacitySpinner.getValue();
            if (capacity <= 0) {
                throw new NumberFormatException("Capacity must be positive.");
            }
            room.setCapacity(capacity);
            
            // Set slots_available equal to capacity for new rooms or when increasing capacity
            if (currentRoom == null || currentRoom.getCapacity() != capacity) {
                // For new rooms, always set slots_available = capacity
                // For existing rooms, only update slots if capacity increased
                if (currentRoom == null || capacity > currentRoom.getCapacity()) {
                    room.setSlotsAvailable(capacity);
                } else {
                    // When decreasing capacity, we need to keep track of occupied slots
                    int occupiedSlots = currentRoom.getCapacity() - currentRoom.getSlotsAvailable();
                    int newSlotsAvailable = Math.max(0, capacity - occupiedSlots);
                    room.setSlotsAvailable(newSlotsAvailable);
                }
            } else {
                // If capacity hasn't changed, keep the current slots_available value
                room.setSlotsAvailable(currentRoom.getSlotsAvailable());
            }
        } catch (NumberFormatException e) {
            displayErrorMessage("Invalid Capacity. Please enter a positive whole number.");
            return null;
        }

        // Parse monthly rate
        try {
            // Consider using NumberFormat for locale-specific parsing if needed
            BigDecimal rate = new BigDecimal(monthlyRateField.getText().trim().replace(",", "")); // Allow comma
            if (rate.compareTo(BigDecimal.ZERO) < 0) {
                displayErrorMessage("Monthly Rate cannot be negative.");
                return null;
            }
            room.setMonthlyRate(rate);
        } catch (NumberFormatException e) {
            displayErrorMessage("Invalid Monthly Rate. Please enter a valid number.");
            return null;
        }

        room.setStatus((Room.RoomStatus) statusComboBox.getSelectedItem());
        
        // Preserve other fields from the existing room if editing
        if (currentRoom != null && currentRoom.getDescription() != null) {
            room.setDescription(currentRoom.getDescription());
        }

        return room;
    }

    /**
     * Clears all form fields to their default state.
     */
    public void clearForm() {
        roomNumberField.setText("");
        capacitySpinner.setValue(1); // Default capacity
        // Or if using JSpinner: capacitySpinner.setValue(1);
        monthlyRateField.setText("0.00");
        statusComboBox.setSelectedItem(Room.RoomStatus.VACANT); // Default status
        roomNumberField.requestFocusInWindow(); // Set focus
    }

    /**
     * Adds an ActionListener to the Save/Update button.
     *
     * @param listener ActionListener provided by the controller.
     */
    public void addSaveButtonListener(ActionListener listener) {
        saveButton.addActionListener(listener);
    }

    /**
     * Adds an ActionListener to the Cancel button.
     *
     * @param listener ActionListener provided by the controller.
     */
    public void addCancelButtonListener(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }

    /**
     * Makes the dialog visible. Resets the saved flag.
     */
    public void showDialog() {
        this.saved = false; // Reset save flag each time dialog is shown
        setVisible(true);
    }

    /**
     * Hides and disposes of the dialog window.
     */
    public void closeDialog() {
        dispose();
    }

    /**
     * Sets the flag indicating the save button was clicked. Typically called by
     * the controller after successful save.
     */
    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    /**
     * Checks if the Save button was clicked before the dialog was closed.
     *
     * @return true if Save was clicked, false otherwise.
     */
    public boolean isSaved() {
        return saved;
    }

    // --- Utility Methods ---
    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollBar1 = new javax.swing.JScrollBar();
        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        roomNumberField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        capacitySpinner = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        monthlyRateField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        statusComboBox = new javax.swing.JComboBox();
        cancelButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Room");
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        jLabel1.setText("Add New Room");

        roomNumberField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                roomNumberFieldActionPerformed(evt);
            }
        });

        jLabel3.setText("Room Number");

        jLabel4.setText("Capacity");

        jLabel5.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel5.setText("Basic Information");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(roomNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(capacitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(capacitySpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                    .addComponent(roomNumberField))
                .addGap(24, 24, 24))
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel2.setText("Financial Details");

        jLabel6.setText("Monthly Rate");

        monthlyRateField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monthlyRateFieldActionPerformed(evt);
            }
        });

        jLabel7.setText("Status");

        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Available", "Maintenance", "Occupied" }));
        statusComboBox.setMinimumSize(new java.awt.Dimension(103, 30));
        statusComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(monthlyRateField, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel2))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(statusComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(88, 88, 88))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(monthlyRateField, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        cancelButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        saveButton.setBackground(new java.awt.Color(0, 204, 255));
        saveButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        saveButton.setForeground(new java.awt.Color(255, 255, 255));
        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void roomNumberFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_roomNumberFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_roomNumberFieldActionPerformed

    private void monthlyRateFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthlyRateFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_monthlyRateFieldActionPerformed

    private void statusComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_statusComboBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saveButtonActionPerformed

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
            java.util.logging.Logger.getLogger(RoomFormDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RoomFormDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RoomFormDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RoomFormDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                RoomFormDialog dialog = new RoomFormDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JSpinner capacitySpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollBar jScrollBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField monthlyRateField;
    private javax.swing.JTextField roomNumberField;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox statusComboBox;
    // End of variables declaration//GEN-END:variables
}
