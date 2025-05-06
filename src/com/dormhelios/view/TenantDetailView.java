package com.dormhelios.view;

import com.dormhelios.model.entity.EmergencyContact;
import com.dormhelios.model.entity.Guardian;
import com.dormhelios.model.entity.Payment; // Needed for payment history
import com.dormhelios.model.entity.Room;
import com.dormhelios.model.entity.Tenant;
import java.awt.event.ActionListener;
import java.text.NumberFormat; // For currency formatting
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import javax.swing.table.DefaultTableModel;

public class TenantDetailView extends javax.swing.JDialog {

    private DefaultTableModel paymentHistoryTableModel;
    private Tenant currentTenant; // Store the tenant being viewed

    // Define a formatter for date fields (adjust pattern as needed)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    // Define a currency formatter
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

    public TenantDetailView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setupComponents();
    }

    private void setupComponents() {
        setLocationRelativeTo(getParent()); // Center relative to parent
        setupPaymentHistoryTable();
    }

    private void setupPaymentHistoryTable() {
        // Define table columns
        String[] columnNames = {"Date Paid", "Amount", "Month Covered", "Method", "Receipt"}; // Match wireframe
        paymentHistoryTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        paymentHistoryTable.setModel(paymentHistoryTableModel);
        paymentHistoryTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        // Adjust column widths (optional)
        paymentHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(90);  // Date Paid
        paymentHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(90);  // Amount
        paymentHistoryTable.getColumnModel().getColumn(2).setPreferredWidth(90);  // Month Covered
        paymentHistoryTable.getColumnModel().getColumn(3).setPreferredWidth(90);  // Method
        paymentHistoryTable.getColumnModel().getColumn(4).setPreferredWidth(70);  // Receipt Action
    }

    public void displayTenantDetails(Tenant tenant, Room room, List<Payment> payments) {
        if (tenant == null) {
            // Handle error - cannot display details for null tenant
            clearView();
            setTitle("Tenant Details - Error");
            // Optionally show an error message inside the dialog
            return;
        }
        this.currentTenant = tenant;

        // --- Populate Basic Info ---
        String fullName = tenant.getFirstName() + " " + tenant.getLastName();
        setTitle("Tenant Details - " + fullName); // Set dynamic title
        titleNameLabel.setText(fullName); // Also set label inside

        fullNameValueLabel.setText(fullName);
        studentNumberValueLabel.setText(tenant.getStudentIdNumber() != null ? tenant.getStudentIdNumber() : "N/A");
        emailValueLabel.setText(tenant.getEmail());
        phoneValueLabel.setText(tenant.getPhoneNumber());
        addressValueLabel.setText(tenant.getPermanentAddress() != null ? tenant.getPermanentAddress() : "N/A");

        // --- Populate Guardian/Emergency Information directly from Tenant ---
        guardianContactValueLabel.setText(tenant.getGuardianName() != null ? tenant.getGuardianName() : "N/A");
        emergencyContactValueLabel.setText(tenant.getEmergencyContactNumber() != null ? tenant.getEmergencyContactNumber() : "N/A");

        // --- Populate Lease/Room ---
        roomNumberValueLabel.setText(room != null ? room.getRoomNumber() : "Unassigned");
        leaseStartValueLabel.setText(tenant.getLeaseStartDate() != null ? tenant.getLeaseStartDate().format(DATE_FORMATTER) : "N/A");
        leaseEndValueLabel.setText(tenant.getLeaseEndDate() != null ? tenant.getLeaseEndDate().format(DATE_FORMATTER) : "N/A");
        depositAmountValueLabel.setText(CURRENCY_FORMATTER.format(tenant.getSecurityDepositAmount() != null ? tenant.getSecurityDepositAmount() : 0));
        depositStatusValueLabel.setText(tenant.getSecurityDepositStatus() != null ? tenant.getSecurityDepositStatus().name() : "N/A"); // Display status name

        // --- Populate Notes ---
        notesTextArea.setText(tenant.getNotes() != null ? tenant.getNotes() : "");
        notesTextArea.setCaretPosition(0); // Scroll to top

        // --- Populate Payment History Table ---
        paymentHistoryTableModel.setRowCount(0); // Clear previous data
        if (payments != null) {
            for (Payment payment : payments) {
                Object[] row = new Object[]{
                    payment.getPaymentDate().format(DATE_FORMATTER),
                    CURRENCY_FORMATTER.format(payment.getAmount()),
                    formatPeriodCovered(payment.getPeriodCoveredStart(), payment.getPeriodCoveredEnd()), // Helper to format period
                    payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "N/A",
                    "Show QR" // Placeholder text for the action column
                };
                paymentHistoryTableModel.addRow(row);
            }
        }
    }

    /**
     * Helper method to format the period covered display.
     */
    private String formatPeriodCovered(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return "N/A";
        }
        // Example: Format as "MMM YYYY" if it covers a full month
        if (start.getDayOfMonth() == 1 && end.equals(start.withDayOfMonth(start.lengthOfMonth()))) {
            return start.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));
        }
        // Otherwise, show range
        return start.format(DATE_FORMATTER) + " - " + end.format(DATE_FORMATTER);
    }

    /**
     * Clears all displayed data.
     */
    private void clearView() {
        setTitle("Tenant Details");
        titleNameLabel.setText("[Tenant Name]");
        fullNameValueLabel.setText("");
        studentNumberValueLabel.setText("");
        emailValueLabel.setText("");
        phoneValueLabel.setText("");
        addressValueLabel.setText("");
        guardianContactValueLabel.setText("");
        guardianContactValueLabel.setText("");
        emergencyContactValueLabel.setText("");
        roomNumberValueLabel.setText("");
        leaseStartValueLabel.setText("");
        leaseEndValueLabel.setText("");
        depositAmountValueLabel.setText("");
        depositStatusValueLabel.setText("");
        notesTextArea.setText("");
        paymentHistoryTableModel.setRowCount(0);
    }

    /**
     * Adds an ActionListener to the Edit button.
     *
     * @param listener ActionListener provided by the controller.
     */

    /**
     * Adds an ActionListener to the Close button.
     *
     * @param listener ActionListener provided by the controller.
     */
    public void addCloseButtonListener(ActionListener listener) {
        closeButton.addActionListener(listener);
    }

    /**
     * Adds a MouseListener to the Payment History table (e.g., for clicking
     * "Show QR").
     *
     * @param listener MouseAdapter provided by the controller.
     */
    public void addPaymentHistoryTableMouseListener(java.awt.event.MouseAdapter listener) {
        paymentHistoryTable.addMouseListener(listener);
    }

    /**
     * Gets the Payment ID from the selected row in the payment history table.
     * Note: This requires storing the Payment ID, perhaps in a hidden column or
     * fetching the corresponding Payment object based on the row index. For
     * simplicity, let's assume the controller handles fetching based on row
     * index.
     *
     * @return The selected row index, or -1 if none selected.
     */
    public int getSelectedPaymentHistoryRow() {
        return paymentHistoryTable.getSelectedRow();
    }

    /**
     * Makes the dialog visible.
     */
    public void showDialog() {
        setVisible(true);
    }

    /**
     * Hides and disposes of the dialog window.
     */
    public void closeDialog() {
        dispose();
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
        titleNameLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        personalContactPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        fullNameLabel = new javax.swing.JLabel();
        studentNumberLabel = new javax.swing.JLabel();
        emailLabel = new javax.swing.JLabel();
        phoneLabel = new javax.swing.JLabel();
        addressLabel = new javax.swing.JLabel();
        fullNameValueLabel = new javax.swing.JLabel();
        studentNumberValueLabel = new javax.swing.JLabel();
        emailValueLabel = new javax.swing.JLabel();
        addressValueLabel = new javax.swing.JLabel();
        phoneValueLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        fullNameLabel6 = new javax.swing.JLabel();
        guardianContactValueLabel = new javax.swing.JLabel();
        fullNameLabel10 = new javax.swing.JLabel();
        emergencyContactValueLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        fullNameLabel12 = new javax.swing.JLabel();
        emailLabel1 = new javax.swing.JLabel();
        depositAmountValueLabel = new javax.swing.JLabel();
        studentNumberLabel1 = new javax.swing.JLabel();
        leaseEndValueLabel = new javax.swing.JLabel();
        depositStatusValueLabel = new javax.swing.JLabel();
        phoneLabel1 = new javax.swing.JLabel();
        roomNumberValueLabel = new javax.swing.JLabel();
        addressLabel1 = new javax.swing.JLabel();
        leaseStartValueLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        notesTextArea = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        paymentHistoryTable = new javax.swing.JTable();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(579, 700));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        titleNameLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        titleNameLabel.setText("John Doe");
        jPanel1.add(titleNameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 16, -1, -1));
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 54, 530, 10));

        personalContactPanel.setPreferredSize(new java.awt.Dimension(247, 292));

        jLabel2.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel2.setText("Personal & Contact Information");

        fullNameLabel.setText("Full Name: ");

        studentNumberLabel.setText("Student Number: ");

        emailLabel.setText("Email:");

        phoneLabel.setText("Phone:");

        addressLabel.setText("Address:");

        fullNameValueLabel.setText("placeholder");

        studentNumberValueLabel.setText("placeholder");

        emailValueLabel.setText("placeholder");

        addressValueLabel.setText("placeholder");

        phoneValueLabel.setText("placeholder");

        jLabel3.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel3.setText("Guardian & Emergency Contact");

        fullNameLabel6.setText("Guardian:");

        guardianContactValueLabel.setText("placeholder");

        fullNameLabel10.setText("Emergency Contact No.:");

        emergencyContactValueLabel.setText("placeholder");

        javax.swing.GroupLayout personalContactPanelLayout = new javax.swing.GroupLayout(personalContactPanel);
        personalContactPanel.setLayout(personalContactPanelLayout);
        personalContactPanelLayout.setHorizontalGroup(
            personalContactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(personalContactPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, personalContactPanelLayout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(personalContactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(personalContactPanelLayout.createSequentialGroup()
                        .addComponent(fullNameLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(guardianContactValueLabel))
                    .addGroup(personalContactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(personalContactPanelLayout.createSequentialGroup()
                            .addGap(1, 1, 1)
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(personalContactPanelLayout.createSequentialGroup()
                        .addComponent(addressLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addressValueLabel))
                    .addGroup(personalContactPanelLayout.createSequentialGroup()
                        .addComponent(phoneLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phoneValueLabel))
                    .addGroup(personalContactPanelLayout.createSequentialGroup()
                        .addComponent(emailLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(emailValueLabel))
                    .addGroup(personalContactPanelLayout.createSequentialGroup()
                        .addComponent(studentNumberLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(studentNumberValueLabel))
                    .addGroup(personalContactPanelLayout.createSequentialGroup()
                        .addComponent(fullNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fullNameValueLabel))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(personalContactPanelLayout.createSequentialGroup()
                        .addComponent(fullNameLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(emergencyContactValueLabel)))
                .addGap(17, 17, 17))
        );
        personalContactPanelLayout.setVerticalGroup(
            personalContactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(personalContactPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(personalContactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fullNameLabel)
                    .addComponent(fullNameValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(personalContactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(studentNumberLabel)
                    .addComponent(studentNumberValueLabel))
                .addGap(12, 12, 12)
                .addGroup(personalContactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(emailLabel)
                    .addComponent(emailValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(personalContactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(phoneLabel)
                    .addComponent(phoneValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(personalContactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addressLabel)
                    .addComponent(addressValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(personalContactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fullNameLabel6)
                    .addComponent(guardianContactValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(personalContactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fullNameLabel10)
                    .addComponent(emergencyContactValueLabel))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jPanel1.add(personalContactPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 70, -1, -1));

        jPanel3.setPreferredSize(new java.awt.Dimension(247, 292));

        jLabel4.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel4.setText("Lease & Room Details");

        fullNameLabel12.setText("Room Number: ");

        emailLabel1.setText("Lease End: ");

        depositAmountValueLabel.setText("placeholder");

        studentNumberLabel1.setText("Lease Start:");

        leaseEndValueLabel.setText("placeholder");

        depositStatusValueLabel.setText("placeholder");

        phoneLabel1.setText("Deposit Amount: ");

        roomNumberValueLabel.setText("placeholder");

        addressLabel1.setText("Deposit Status: ");

        leaseStartValueLabel.setText("placeholder");

        jLabel5.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel5.setText("Notes");

        notesTextArea.setEditable(false);
        notesTextArea.setColumns(20);
        notesTextArea.setRows(5);
        jScrollPane1.setViewportView(notesTextArea);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jSeparator5)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGap(1, 1, 1)
                            .addComponent(jLabel5))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(addressLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(depositStatusValueLabel))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(phoneLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(depositAmountValueLabel))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(emailLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(leaseEndValueLabel))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(studentNumberLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(leaseStartValueLabel))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(fullNameLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(roomNumberValueLabel))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel4)))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fullNameLabel12)
                    .addComponent(roomNumberValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(studentNumberLabel1)
                    .addComponent(leaseStartValueLabel))
                .addGap(12, 12, 12)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(emailLabel1)
                    .addComponent(leaseEndValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(phoneLabel1)
                    .addComponent(depositAmountValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addressLabel1)
                    .addComponent(depositStatusValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 70, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel6.setText("Payment History");

        paymentHistoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Date Paid", "Amount", "Month Covered", "Method", "Receipt"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(paymentHistoryTable);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 390, 530, 210));

        closeButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        closeButton.setText("Close");
        jPanel1.add(closeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(445, 610, 100, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 651, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(TenantDetailView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TenantDetailView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TenantDetailView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TenantDetailView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                TenantDetailView dialog = new TenantDetailView(new javax.swing.JFrame(), true);
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
    private javax.swing.JLabel addressLabel;
    private javax.swing.JLabel addressLabel1;
    private javax.swing.JLabel addressValueLabel;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel depositAmountValueLabel;
    private javax.swing.JLabel depositStatusValueLabel;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JLabel emailLabel1;
    private javax.swing.JLabel emailValueLabel;
    private javax.swing.JLabel emergencyContactValueLabel;
    private javax.swing.JLabel fullNameLabel;
    private javax.swing.JLabel fullNameLabel10;
    private javax.swing.JLabel fullNameLabel12;
    private javax.swing.JLabel fullNameLabel6;
    private javax.swing.JLabel fullNameValueLabel;
    private javax.swing.JLabel guardianContactValueLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JLabel leaseEndValueLabel;
    private javax.swing.JLabel leaseStartValueLabel;
    private javax.swing.JTextArea notesTextArea;
    private javax.swing.JTable paymentHistoryTable;
    private javax.swing.JPanel personalContactPanel;
    private javax.swing.JLabel phoneLabel;
    private javax.swing.JLabel phoneLabel1;
    private javax.swing.JLabel phoneValueLabel;
    private javax.swing.JLabel roomNumberValueLabel;
    private javax.swing.JLabel studentNumberLabel;
    private javax.swing.JLabel studentNumberLabel1;
    private javax.swing.JLabel studentNumberValueLabel;
    private javax.swing.JLabel titleNameLabel;
    // End of variables declaration//GEN-END:variables
}
