package com.dormhelios.view;

import com.dormhelios.model.entity.Payment;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class PaymentListView extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

    public PaymentListView() {
        initComponents();
        setupTable();
    }

    private void setupTable() {
        String[] columnNames = {"ID", "Date", "Tenant", "Room No.", "Amount", "Period", "Method", "Receipt"}; // Added hidden ID
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make Receipt column potentially clickable later, others not editable
                return column == 7; // Only allow interaction on the last column conceptually
            }
        };
        paymentTable.setModel(tableModel);
        paymentTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        paymentTable.setAutoCreateRowSorter(true); // Enable basic column sorting
        sorter = new TableRowSorter<>(tableModel);
        paymentTable.setRowSorter(sorter);

        // Set up search field placeholder text behavior
        setupSearchFieldPlaceholder();

        // Hide the ID column
        paymentTable.getColumnModel().getColumn(0).setMinWidth(0);
        paymentTable.getColumnModel().getColumn(0).setMaxWidth(0);
        paymentTable.getColumnModel().getColumn(0).setWidth(0);

        // Adjust other column widths (optional)
        paymentTable.getColumnModel().getColumn(1).setPreferredWidth(90);  // Date
        paymentTable.getColumnModel().getColumn(2).setPreferredWidth(180); // Tenant Name
        paymentTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Room No.
        paymentTable.getColumnModel().getColumn(4).setPreferredWidth(90);  // Amount
        paymentTable.getColumnModel().getColumn(5).setPreferredWidth(90);  // Period
        paymentTable.getColumnModel().getColumn(6).setPreferredWidth(90);  // Method
        paymentTable.getColumnModel().getColumn(7).setPreferredWidth(70);  // Receipt Action

        // Right-align amount column (optional but good practice)
        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
        paymentTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
    }
    
    /**
     * Sets up the search field with placeholder text behavior.
     * The placeholder text "Search" will disappear when the field gains focus
     * and reappear when the field loses focus if it's empty.
     */
    private void setupSearchFieldPlaceholder() {
        // Add placeholder text behavior to search field
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                // When field gains focus and contains the default "Search" text, clear it
                if (searchField.getText().equals("Search")) {
                    searchField.setText("");
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                // When field loses focus and is empty, restore the "Search" placeholder
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search");
                }
            }
        });
    }

    public void displayPayments(List<Payment> payments /* Potentially List<PaymentDTO> */) {
        tableModel.setRowCount(0); // Clear existing rows
        if (payments == null) {
            return;
        }

        for (Payment payment : payments) {
            Object[] row = new Object[]{
                payment.getPaymentId(), // Hidden ID
                payment.getPaymentDate().format(DATE_FORMATTER),
                getTenantNameFromId(payment.getTenantId()), // Placeholder - Controller needs to provide this
                getRoomNumberFromTenantId(payment.getTenantId()), // Placeholder - Controller needs to provide this
                CURRENCY_FORMATTER.format(payment.getAmount()),
                formatPeriodCovered(payment.getPeriodCoveredStart(), payment.getPeriodCoveredEnd()),
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "N/A",
                "View" // Action text for the receipt column
            };
            tableModel.addRow(row);
        }
        // Update pagination info if implemented
        // updatePaginationInfo();
    }

    // --- Placeholder Helper Methods (Controller/Service should provide real data) ---
    private String getTenantNameFromId(int tenantId) {
        // TODO: Fetch Tenant Name via TenantDAO or use pre-joined data
        return "Tenant " + tenantId; // Placeholder
    }

    private String getRoomNumberFromTenantId(int tenantId) {
        // TODO: Fetch Tenant -> Room -> Room Number or use pre-joined data
        return "Room ?"; // Placeholder
    }

    private String formatPeriodCovered(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return "N/A";
        }
        if (start.getDayOfMonth() == 1 && end.equals(start.withDayOfMonth(start.lengthOfMonth()))) {
            return start.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));
        }
        return start.format(DATE_FORMATTER) + " - " + end.format(DATE_FORMATTER);
    }
    // --- End Placeholder Helper Methods ---

    /**
     * Gets the currently selected payment's ID from the table.
     *
     * @return The payment ID, or -1 if no row is selected.
     */
    public int getSelectedPaymentId() {
        int selectedRow = paymentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = paymentTable.convertRowIndexToModel(selectedRow);
            return (Integer) tableModel.getValueAt(modelRow, 0); // Get ID from hidden column
        }
        return -1;
    }

    /**
     * Gets the text entered in the search field.
     *
     * @return The search text.
     */
    public String getSearchText() {
        return searchField.getText().trim();
    }

   
    public void filterTableBySearch() {
        String searchText = getSearchText();
        RowFilter<DefaultTableModel, Object> rf = null;
        try {
            // Filter based on Tenant Name (col 2) or Room No (col 3) or Method (col 6) - case insensitive
            rf = RowFilter.regexFilter("(?i)" + searchText, 2, 3, 6);
        } catch (java.util.regex.PatternSyntaxException e) {
            return; // Ignore invalid regex
        }
        sorter.setRowFilter(rf);
    }

    // --- Methods to Add Listeners ---
    public void addNewPaymentButtonListener(ActionListener listener) {
        newPaymentButton.addActionListener(listener);
    }

    public void addEditButtonListener(ActionListener listener) {
        editButton.addActionListener(listener);
    }

    public void addViewButtonListener(ActionListener listener) {
        viewButton.addActionListener(listener);
    }

    public void addDeleteButtonListener(ActionListener listener) {
        deleteButton.addActionListener(listener);
    }

    public void addSearchFieldListener(DocumentListener listener) {
        searchField.getDocument().addDocumentListener(listener);
    }

    public void addTableMouseListener(MouseAdapter listener) {
        paymentTable.addMouseListener(listener);
    }
    
    public void addFilterComboBoxListener(ActionListener listener) {
        filterComboBox.addActionListener(listener);
    }
    
    public String getSelectedFilter() {
        return (String) filterComboBox.getSelectedItem();
    }

    // --- Utility Methods ---
    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public int displayConfirmDialog(String message, String title) {
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    public JTable getPaymentTable() {
        return paymentTable;
    }
    
    /**
     * Gets the "Add Payment" button for external access.
     * 
     * @return The New Payment button component
     */
    public javax.swing.JButton getNewPaymentButton() {
        return newPaymentButton;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        newPaymentButton = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        filterComboBox = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        paymentTable = new javax.swing.JTable();
        viewButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 1, 36)); // NOI18N
        jLabel1.setText("Payment List Records");

        newPaymentButton.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        newPaymentButton.setText("Add Payment");
        newPaymentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newPaymentButtonActionPerformed(evt);
            }
        });

        searchField.setText("Search");

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Payments", "Most Recent", "Old" }));

        paymentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Date", "Tenant", "Room Number", "Amount", "Period", "Method", "Receipt"
            }
        ));
        jScrollPane1.setViewportView(paymentTable);

        viewButton.setText("View");

        editButton.setText("Edit");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(viewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 895, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(filterComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(newPaymentButton, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE))))))
                .addGap(22, 22, 22))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(newPaymentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 472, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(viewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(27, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newPaymentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newPaymentButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_newPaymentButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_editButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JComboBox<String> filterComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton newPaymentButton;
    private javax.swing.JTable paymentTable;
    private javax.swing.JTextField searchField;
    private javax.swing.JButton viewButton;
    // End of variables declaration//GEN-END:variables
}
