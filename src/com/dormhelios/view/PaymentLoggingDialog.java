package com.dormhelios.view;

import com.dormhelios.model.entity.Payment; // For return type and method enum
import com.dormhelios.model.entity.Tenant; // For populating dropdown
import com.dormhelios.model.entity.Room; // For room dropdown
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class PaymentLoggingDialog extends javax.swing.JDialog {

    /**
     * Creates new form PaymentLoggingDialog
     */
    private boolean saved = false; // Flag if save was clicked
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    public PaymentLoggingDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setupComponents();
    }

    private void setupComponents() {
        setLocationRelativeTo(getParent());
        // Populate fixed dropdowns
        paymentMethodComboBox.setModel(new DefaultComboBoxModel<>(Payment.PaymentMethod.values()));
        populateMonthCoveredComboBox(); // Populate month/year dropdown
    }

    /**
     * Populates the "Covered Month" combo box with recent/upcoming months.
     */
    private void populateMonthCoveredComboBox() {
        Vector<YearMonth> months = new Vector<>();
        YearMonth current = YearMonth.now();
        // Add previous few months, current month, and next few months
        for (int i = -3; i <= 3; i++) {
            months.add(current.plusMonths(i));
        }
        // Use a custom renderer or just rely on YearMonth.toString()
        // For better display, use a ComboBoxItem or custom renderer
        // Using toString() for simplicity here
        DefaultComboBoxModel<YearMonth> model = new DefaultComboBoxModel<>(months);
        coveredMonthComboBox.setModel(model);
        coveredMonthComboBox.setSelectedItem(current); // Default to current month

        // Optional: Custom renderer for better formatting
        /*
         coveredMonthComboBox.setRenderer(new javax.swing.ListCellRenderer<YearMonth>() {
             private final javax.swing.JLabel label = new javax.swing.JLabel();
             @Override
             public java.awt.Component getListCellRendererComponent(javax.swing.JList<? extends YearMonth> list, YearMonth value, int index, boolean isSelected, boolean cellHasFocus) {
                 if (value != null) {
                     label.setText(value.format(MONTH_YEAR_FORMATTER));
                 } else {
                     label.setText("Select Month...");
                 }
                 // Handle selection background/foreground if needed
                 return label;
             }
         });
         */
    }

    public Payment getPaymentData() {
        // Validation

        if (paymentAmountField.getText().trim().isEmpty()) {
            displayErrorMessage("Payment Amount is required.");
            return null;
        }
        if (paymentDateField.getText().trim().isEmpty() || paymentDateField.getText().trim().equals("YYYY-MM-DD")) {
            displayErrorMessage("Payment Date is required.");
            return null;
        }
        if (coveredMonthComboBox.getSelectedItem() == null) {
            displayErrorMessage("Please select the Covered Month/Period.");
            return null;
        }
        if (paymentMethodComboBox.getSelectedItem() == null) {
            displayErrorMessage("Please select a Payment Method.");
            return null;
        }
        
        // Check tenant selection
        ComboBoxItem<Integer> selectedTenantItem = (ComboBoxItem<Integer>) tenantComboBox.getSelectedItem();
        if (selectedTenantItem == null || selectedTenantItem.getId() == null) {
            displayErrorMessage("Please select a Tenant.");
            return null;
        }

        Payment payment = new Payment();

        // Get Tenant ID from combo box
        payment.setTenantId(selectedTenantItem.getId());

        // Parse Amount
        try {
            BigDecimal amount = new BigDecimal(paymentAmountField.getText().trim().replace(",", ""));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                displayErrorMessage("Payment Amount must be positive.");
                return null;
            }
            payment.setAmount(amount);
        } catch (NumberFormatException e) {
            displayErrorMessage("Invalid Payment Amount. Please enter a valid number.");
            return null;
        }

        // Parse Payment Date
        try {
            payment.setPaymentDate(LocalDate.parse(paymentDateField.getText().trim(), DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            displayErrorMessage("Invalid Payment Date format. Please use YYYY-MM-DD.");
            return null;
        }

        // Determine Period Covered (Assuming full month based on selection)
        YearMonth selectedMonth = (YearMonth) coveredMonthComboBox.getSelectedItem();
        payment.setPeriodCoveredStart(selectedMonth.atDay(1));
        payment.setPeriodCoveredEnd(selectedMonth.atEndOfMonth());

        payment.setPaymentMethod((Payment.PaymentMethod) paymentMethodComboBox.getSelectedItem());
        payment.setReceiptReference(receiptReferenceField.getText().trim().isEmpty() ? null : receiptReferenceField.getText().trim());

        // User ID and QR Code Data will be set by the Controller before saving
        return payment;
    }

    /**
     * Clears all form fields to their default state.
     */
    public void clearForm() {
        // tenantComboBox.setSelectedIndex(0); // Select prompt
        paymentAmountField.setText("");
        paymentDateField.setText(""); // Clear or set to current date?
        // paymentDateField.setValue(LocalDate.now()); // If using JDatePicker or similar
        coveredMonthComboBox.setSelectedItem(YearMonth.now()); // Default to current month
        paymentMethodComboBox.setSelectedIndex(0); // Select first method (e.g., CASH)
        receiptReferenceField.setText("");
        tenantComboBox.requestFocusInWindow();
    }

    /**
     * Adds an ActionListener to the Save button.
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

    // Add methods for OCR button listeners if implementing OCR
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
     * Sets the flag indicating the save button was clicked.
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
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    // --- Helper Class for ComboBox Items (Copied from TenantFormDialog for consistency) ---
    // Allows storing both an ID (Integer) and a display String in the ComboBox
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
     * Sets the model for the Room ComboBox with room data.
     * @param rooms List of rooms to populate the dropdown
     */
    public void setRoomComboBoxModel(List<Room> rooms) {
        Vector<ComboBoxItem<Integer>> model = new Vector<>();
        model.add(new ComboBoxItem<>(null, "Select Room...")); // Add prompt item with null ID
        
        if (rooms != null) {
            for (Room room : rooms) {
                String displayText = room.getRoomNumber();
                // Add additional room info like status or monthly rate if needed
                if (room.getStatus() != null) {
                    displayText += " (" + room.getStatus().name() + ")";
                }
                if (room.getMonthlyRate() != null) {
                    displayText += " - ₱" + room.getMonthlyRate();
                }
                model.add(new ComboBoxItem<>(room.getRoomId(), displayText));
            }
        }
        
        roomComboBox.setModel(new DefaultComboBoxModel<>(model));
    }

    /**
     * Gets the selected room ID from the room combo box.
     * @return The selected room ID, or null if no room is selected
     */
    public Integer getSelectedRoomId() {
        ComboBoxItem<Integer> selectedRoomItem = (ComboBoxItem<Integer>) roomComboBox.getSelectedItem();
        return selectedRoomItem != null ? selectedRoomItem.getId() : null;
    }

    /**
     * Selects a room in the combo box by its ID.
     * @param roomId The ID of the room to select
     */
    public void selectRoomById(Integer roomId) {
        if (roomId == null) {
            roomComboBox.setSelectedIndex(0);
            return;
        }
        
        for (int i = 0; i < roomComboBox.getItemCount(); i++) {
            Object item = roomComboBox.getItemAt(i);
            if (item instanceof ComboBoxItem) {
                ComboBoxItem<Integer> comboItem = (ComboBoxItem<Integer>) item;
                if (roomId.equals(comboItem.getId())) {
                    roomComboBox.setSelectedIndex(i);
                    return;
                }
            }
        }
        roomComboBox.setSelectedIndex(0);
    }

    /**
     * Adds a listener to the room combo box.
     * @param listener ActionListener to be added
     */
    public void addRoomComboBoxListener(ActionListener listener) {
        roomComboBox.addActionListener(listener);
    }

    /**
     * Sets up the dialog for adding a new payment.
     */
    public void setupForAdd() {
        setTitle("New Payment");
        saveButton.setText("Save Payment");
        paymentHeaderLabel.setText("Log New Payment");
        clearForm();
    }
    
    /**
     * Sets up the dialog for editing an existing payment.
     * 
     * @param payment The Payment object to edit
     */
    public void setupForEdit(Payment payment) {
        setTitle("Edit Payment");
        saveButton.setText("Update Payment");
        paymentHeaderLabel.setText("Edit Payment");
        populateForm(payment);
    }
    
    /**
     * Populates the form with data from an existing Payment
     * 
     * @param payment The Payment object containing data to display
     */
    private void populateForm(Payment payment) {
        if (payment == null) {
            clearForm();
            return;
        }
        
        // Select the tenant in the combo box
        selectTenantById(payment.getTenantId());
        
        // Set payment amount
        if (payment.getAmount() != null) {
            paymentAmountField.setText(payment.getAmount().toPlainString());
        }
        
        // Set payment date
        if (payment.getPaymentDate() != null) {
            paymentDateField.setText(payment.getPaymentDate().format(DATE_FORMATTER));
        }
        
        // Select the covered month
        if (payment.getPeriodCoveredStart() != null) {
            YearMonth coveredMonth = YearMonth.from(payment.getPeriodCoveredStart());
            for (int i = 0; i < coveredMonthComboBox.getItemCount(); i++) {
                YearMonth item = (YearMonth) coveredMonthComboBox.getItemAt(i);
                if (item.equals(coveredMonth)) {
                    coveredMonthComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        // Set payment method
        paymentMethodComboBox.setSelectedItem(payment.getPaymentMethod());
        
        // Set receipt reference
        receiptReferenceField.setText(payment.getReceiptReference());
    }
    
    /**
     * Helper method to select a tenant in the combobox by ID
     */
    private void selectTenantById(Integer tenantId) {
        if (tenantId == null) {
            tenantComboBox.setSelectedIndex(0);
            return;
        }
        
        for (int i = 0; i < tenantComboBox.getItemCount(); i++) {
            Object item = tenantComboBox.getItemAt(i);
            if (item instanceof ComboBoxItem) {
                ComboBoxItem<Integer> comboItem = (ComboBoxItem<Integer>) item;
                if (tenantId.equals(comboItem.getId())) {
                    tenantComboBox.setSelectedIndex(i);
                    return;
                }
            }
        }
        tenantComboBox.setSelectedIndex(0);
    }

    /**
     * Adds a listener to sync the room and tenant selection.
     * When a room is selected, this will filter the tenant combo box to show only
     * tenants assigned to that room.
     * 
     * @param tenants List of all tenants to filter
     */
    public void setupRoomTenantLinking(List<Tenant> tenants) {
        // Store all tenants for filtering
        final List<Tenant> allTenants = new ArrayList<>(tenants);
        
        // Flag to prevent recursive triggering of listeners
        final boolean[] isUpdatingSelection = {false};
        
        // Add listener to room combo box
        roomComboBox.addActionListener(e -> {
            // Skip if we're already in the middle of updating selections
            if (isUpdatingSelection[0]) {
                return;
            }
            
            isUpdatingSelection[0] = true;
            try {
                // Get selected room ID
                Integer selectedRoomId = getSelectedRoomId();
                
                // Remember current tenant selection
                ComboBoxItem<Integer> currentTenantSelection = null;
                if (tenantComboBox.getSelectedItem() instanceof ComboBoxItem) {
                    currentTenantSelection = (ComboBoxItem<Integer>) tenantComboBox.getSelectedItem();
                }
                
                if (selectedRoomId == null) {
                    // If no room selected, show all tenants
                    setTenantComboBoxModel(allTenants);
                } else {
                    // Filter tenants by selected room
                    List<Tenant> filteredTenants = allTenants.stream()
                        .filter(tenant -> selectedRoomId.equals(tenant.getRoomId()))
                        .collect(java.util.stream.Collectors.toList());
                    
                    // Set filtered model
                    setTenantComboBoxModel(filteredTenants);
                }
                
                // Try to restore previous tenant selection if it's in the new model
                if (currentTenantSelection != null && currentTenantSelection.getId() != null) {
                    selectTenantById(currentTenantSelection.getId());
                }
            } finally {
                isUpdatingSelection[0] = false;
            }
        });
        
        // Add listener to tenant combo box
        tenantComboBox.addActionListener(e -> {
            // Skip if we're already in the middle of updating selections
            if (isUpdatingSelection[0]) {
                return;
            }
            
            isUpdatingSelection[0] = true;
            try {
                Object selected = tenantComboBox.getSelectedItem();
                if (selected instanceof ComboBoxItem) {
                    @SuppressWarnings("unchecked")
                    ComboBoxItem<Integer> selectedTenantItem = (ComboBoxItem<Integer>) selected;
                    
                    Integer tenantId = selectedTenantItem.getId();
                    if (tenantId != null) {
                        // Find the tenant by ID
                        Optional<Tenant> selectedTenant = allTenants.stream()
                            .filter(t -> tenantId.equals(t.getTenantId()))
                            .findFirst();
                        
                        // If tenant has a room, select it in the room combo box
                        selectedTenant.ifPresent(tenant -> {
                            if (tenant.getRoomId() != null) {
                                selectRoomById(tenant.getRoomId());
                            }
                        });
                    }
                }
            } finally {
                isUpdatingSelection[0] = false;
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        paymentHeaderLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        roomComboBox = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        paymentAmountField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        paymentDateField = new javax.swing.JFormattedTextField();
        jLabel7 = new javax.swing.JLabel();
        coveredMonthComboBox = new javax.swing.JComboBox();
        tenantComboBox = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        receiptReferenceField = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        paymentMethodComboBox = new javax.swing.JComboBox();
        cancelButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        paymentHeaderLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        paymentHeaderLabel.setText("Log New Payment");

        jLabel2.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12)); // NOI18N
        jLabel2.setText("Room Information");

        jLabel3.setText("Select Room");

        roomComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(roomComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 403, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(roomComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jLabel4.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12)); // NOI18N
        jLabel4.setText("Payment Details");

        jLabel5.setText("Covered Month");

        jLabel6.setText("Payment Date");

        jLabel7.setText("Payment Amount");

        coveredMonthComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        tenantComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel9.setText("Tenant");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(paymentAmountField, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(coveredMonthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(33, 33, 33)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(paymentDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(tenantComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel4))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tenantComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(paymentDateField)
                            .addComponent(paymentAmountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(coveredMonthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, 16))
        );

        jLabel8.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12)); // NOI18N
        jLabel8.setText("Other");

        jLabel10.setText("Receipt Reference");

        jLabel11.setText("Payment Method");

        paymentMethodComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        paymentMethodComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paymentMethodComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(paymentMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(35, 35, 35)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(receiptReferenceField, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel8))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(receiptReferenceField, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                    .addComponent(paymentMethodComboBox))
                .addGap(18, 18, 18))
        );

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

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
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(paymentHeaderLabel)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(23, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(paymentHeaderLabel)
                .addGap(7, 7, 7)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saveButtonActionPerformed

    private void paymentMethodComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentMethodComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_paymentMethodComboBoxActionPerformed

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
            java.util.logging.Logger.getLogger(PaymentLoggingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PaymentLoggingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PaymentLoggingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PaymentLoggingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PaymentLoggingDialog dialog = new PaymentLoggingDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox coveredMonthComboBox;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField paymentAmountField;
    private javax.swing.JFormattedTextField paymentDateField;
    private javax.swing.JLabel paymentHeaderLabel;
    private javax.swing.JComboBox paymentMethodComboBox;
    private javax.swing.JFormattedTextField receiptReferenceField;
    private javax.swing.JComboBox roomComboBox;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox tenantComboBox;
    // End of variables declaration//GEN-END:variables
}
