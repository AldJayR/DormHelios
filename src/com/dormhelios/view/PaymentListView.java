package com.dormhelios.view;

import com.dormhelios.model.entity.Payment;
import com.dormhelios.util.TableRenderers;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

public class PaymentListView extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

    public PaymentListView() {
        initComponents();
        setupTable();
        setupSearchFieldPlaceholder();
        setupTableAppearance();
        applyCustomStyling();
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
     * Configures the payment table with appropriate renderers and column sizes.
     */
    private void setupTableAppearance() {
        // Set column widths
        TableColumnModel columnModel = paymentTable.getColumnModel();
        
        // Apply appropriate renderers for each column
        // Payment ID - Left-aligned default
        columnModel.getColumn(0).setPreferredWidth(80);
        columnModel.getColumn(0).setCellRenderer(new TableRenderers.BaseRenderer());
        
        // Tenant Name - Left-aligned default
        columnModel.getColumn(1).setPreferredWidth(180);
        columnModel.getColumn(1).setCellRenderer(new TableRenderers.BaseRenderer());
        
        // Room Number - Left-aligned default
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(2).setCellRenderer(new TableRenderers.BaseRenderer());
        
        // Amount - Right-aligned currency
        columnModel.getColumn(3).setPreferredWidth(120);
        columnModel.getColumn(3).setCellRenderer(new TableRenderers.CurrencyRenderer());
        
        // Payment Date - Formatted date
        columnModel.getColumn(4).setPreferredWidth(120);
        columnModel.getColumn(4).setCellRenderer(new TableRenderers.DateRenderer());
        
        // Period Covered - Formatted text
        columnModel.getColumn(5).setPreferredWidth(140);
        columnModel.getColumn(5).setCellRenderer(new TableRenderers.BaseRenderer());
        
        // Payment Method - Status-styled renderer
        columnModel.getColumn(6).setPreferredWidth(120);
        columnModel.getColumn(6).setCellRenderer(new TableRenderers.StatusRenderer());
        
        // Actions column - if present
        if (columnModel.getColumnCount() > 7) {
            columnModel.getColumn(7).setPreferredWidth(100);
            // We'll set cell editor elsewhere as it requires action handling
        }
        
        // Adjust row height for better readability
        paymentTable.setRowHeight(32);
        
        // Improve table header appearance
        paymentTable.getTableHeader().setFont(paymentTable.getTableHeader().getFont().deriveFont(java.awt.Font.BOLD));
        paymentTable.getTableHeader().setOpaque(false);
        
        // Make the table selection more visible
        paymentTable.setSelectionBackground(new java.awt.Color(30, 115, 190, 80));
        paymentTable.setSelectionForeground(java.awt.Color.BLACK);
        
        // Remove grid lines for a cleaner look
        paymentTable.setShowVerticalLines(false);
        paymentTable.setShowHorizontalLines(true);
        paymentTable.setGridColor(new java.awt.Color(230, 230, 230));
        
        // Enable row sorting
        paymentTable.setAutoCreateRowSorter(true);
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

    /**
     * Updates the view with payment data including related tenant and room details.
     * 
     * @param payments List of payments to display
     * @param tenantNames Map of tenant IDs to names
     * @param roomNumbers Map of tenant IDs to room numbers
     */
    public void displayPayments(List<Payment> payments, java.util.Map<Integer, String> tenantNames, java.util.Map<Integer, String> roomNumbers) {
        tableModel.setRowCount(0); // Clear existing rows
        if (payments == null) {
            System.out.println("WARNING: Payment list is null in displayPayments");
            return;
        }

        System.out.println("Displaying " + payments.size() + " payments in PaymentListView");
        
        for (Payment payment : payments) {
            Object[] row = new Object[]{
                payment.getPaymentId(), // Hidden ID
                payment.getPaymentDate().format(DATE_FORMATTER),
                tenantNames.getOrDefault(payment.getTenantId(), "Unknown"), // Get tenant name from map
                roomNumbers.getOrDefault(payment.getTenantId(), "N/A"), // Get room number from map
                CURRENCY_FORMATTER.format(payment.getAmount()),
                formatPeriodCovered(payment.getPeriodCoveredStart(), payment.getPeriodCoveredEnd()),
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "N/A",
                "View" // Action text for the receipt column
            };
            System.out.println("Adding payment row: ID=" + payment.getPaymentId() + 
                               ", Tenant=" + tenantNames.getOrDefault(payment.getTenantId(), "Unknown") +
                               ", Amount=" + payment.getAmount());
            tableModel.addRow(row);
        }
        System.out.println("Table now has " + tableModel.getRowCount() + " rows");
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

    /**
     * Filters the table based on the text in the search field.
     * This method is called by the controller when the search field text changes.
     */
    public void filterTableBySearch() {
        String searchText = getSearchText();
        
        // Don't filter if the search field contains the placeholder text "Search"
        if (searchText.equals("Search")) {
            sorter.setRowFilter(null); // Clear any existing filter
            return;
        }
        
        // If empty after trim, clear filter
        if (searchText.isEmpty()) {
            sorter.setRowFilter(null); // Clear any existing filter
            return;
        }
        
        try {
            // Filter based on multiple columns:
            // Date (col 1), Tenant Name (col 2), Room No (col 3), Amount (col 4), Method (col 6)
            RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + searchText, 1, 2, 3, 4, 6);
            sorter.setRowFilter(rf);
        } catch (java.util.regex.PatternSyntaxException e) {
            // If the regex pattern is invalid, don't apply any filter
            sorter.setRowFilter(null);
            System.err.println("Invalid search regex: " + e.getMessage());
        }
    }

    /**
     * Filters the table based on both the search text and combo box selection.
     * This provides a more comprehensive filtering solution than filterTableBySearch().
     */
    public void filterTable() {
        String searchText = getSearchText();
        String filterSelection = getSelectedFilter();
        
        // Don't filter if the search field contains the placeholder text "Search"
        if (searchText.equals("Search")) {
            searchText = "";
        }
        
        final String finalSearchText = searchText;
        final String finalFilterSelection = filterSelection;
        
        // Create a custom row filter that works correctly with renderers
        RowFilter<DefaultTableModel, Object> compositeFilter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // First check if we should include based on search text
                if (!finalSearchText.isEmpty()) {
                    boolean matches = false;
                    // Check tenant name (column 2), room number (column 3), amount (column 4), method (column 6)
                    String tenant = entry.getStringValue(2).toLowerCase();
                    String roomNo = entry.getStringValue(3).toLowerCase();
                    String amount = entry.getStringValue(4).toLowerCase();
                    String method = entry.getStringValue(6).toLowerCase();
                    
                    if (tenant.contains(finalSearchText.toLowerCase()) || 
                        roomNo.contains(finalSearchText.toLowerCase()) ||
                        amount.contains(finalSearchText.toLowerCase()) ||
                        method.contains(finalSearchText.toLowerCase())) {
                        matches = true;
                    }
                    
                    if (!matches) {
                        return false; // No need to check filter if search doesn't match
                    }
                }
                
                // Then check if we should include based on filter combo box
                if (filterSelection != null && !filterSelection.equals("All Payments") && !filterSelection.trim().isEmpty()) {
                    if (filterSelection.equals("Most Recent First")) {
                        // Date is in column 1
                        String dateStr = entry.getStringValue(1);
                        if (dateStr.equals("N/A")) {
                            return false;
                        }
                        try {
                            LocalDate paymentDate = LocalDate.parse(dateStr);
                            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
                            return paymentDate.isAfter(thirtyDaysAgo);
                        } catch (Exception e) {
                            // If date can't be parsed, include the row
                            return true;
                        }
                    } else if (filterSelection.equals("Oldest First")) {
                        // Date is in column 1
                        String dateStr = entry.getStringValue(1);
                        if (dateStr.equals("N/A")) {
                            return false;
                        }
                        try {
                            LocalDate paymentDate = LocalDate.parse(dateStr);
                            LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
                            return paymentDate.isBefore(ninetyDaysAgo);
                        } catch (Exception e) {
                            // If date can't be parsed, include the row
                            return true;
                        }
                    }
                }
                
                // If we reach here, include the row
                return true;
            }
        };
        
        // Apply our custom filter
        sorter.setRowFilter(compositeFilter);
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
     * Exposes the searchField for external access.
     * 
     * @return The searchField component
     */
    public javax.swing.JTextField getSearchField() {
        return searchField;
    }

    /**
     * Applies Tailwind-inspired styling to the PaymentListView components.
     * Call this method after initComponents() in the constructor.
     */
    private void applyCustomStyling() {
        // Tailwind color palette
        Color primary = new Color(59, 130, 246);     // blue-500
        Color primaryLight = new Color(96, 165, 250); // blue-400
        Color success = new Color(34, 197, 94);      // green-500 
        Color danger = new Color(239, 68, 68);       // red-500
        Color warning = new Color(245, 158, 11);     // amber-500
        Color emerald = new Color(16, 185, 129);     // emerald-500
        Color bgLight = new Color(243, 244, 246);    // gray-100
        Color slate100 = new Color(241, 245, 249);   // slate-100
        Color slate200 = new Color(226, 232, 240);   // slate-200
        Color slate700 = new Color(51, 65, 85);      // slate-700
        Color slate800 = new Color(30, 41, 59);      // slate-800
        
        // Background styling
        this.setBackground(bgLight);
        
        // Style title 
        jLabel1.setForeground(slate800);
        
        // Style the Add Payment button
        newPaymentButton.setBackground(emerald);
        newPaymentButton.setForeground(Color.WHITE);
        newPaymentButton.setFont(newPaymentButton.getFont().deriveFont(Font.BOLD));
        newPaymentButton.setBorderPainted(false);
        newPaymentButton.setFocusPainted(false);
        newPaymentButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Style action buttons with different colors
        if (viewButton != null) {
            viewButton.setBackground(primary);
            viewButton.setForeground(Color.WHITE);
            viewButton.setFont(viewButton.getFont().deriveFont(Font.BOLD));
            viewButton.setBorderPainted(false);
            viewButton.setFocusPainted(false);
            viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        if (editButton != null) {
            editButton.setBackground(success);
            editButton.setForeground(Color.WHITE);
            editButton.setFont(editButton.getFont().deriveFont(Font.BOLD));
            editButton.setBorderPainted(false);
            editButton.setFocusPainted(false);
            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        if (deleteButton != null) {
            deleteButton.setBackground(danger);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFont(deleteButton.getFont().deriveFont(Font.BOLD));
            deleteButton.setBorderPainted(false);
            deleteButton.setFocusPainted(false);
            deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        // Style the search field
        searchField.setBackground(slate100);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(slate200, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Style filter combo box
        filterComboBox.setBackground(Color.WHITE);
        filterComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(slate200, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // Style the table
        paymentTable.setRowHeight(40);
        paymentTable.setIntercellSpacing(new Dimension(10, 0));
        paymentTable.setShowGrid(false);
        paymentTable.setShowHorizontalLines(true);
        paymentTable.setGridColor(slate200);
        
        // Table header styling
        paymentTable.getTableHeader().setBackground(bgLight);
        paymentTable.getTableHeader().setForeground(slate700);
        paymentTable.getTableHeader().setFont(paymentTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        paymentTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, slate200));
        
        // Table selection styling
        paymentTable.setSelectionBackground(new Color(primaryLight.getRed(), primaryLight.getGreen(), primaryLight.getBlue(), 100));
        paymentTable.setSelectionForeground(slate800);
        
        // Money column styling (special color for the amount)
        if (paymentTable.getColumnCount() > 4) { // Assuming amount is column 4
            TableColumn amountColumn = paymentTable.getColumnModel().getColumn(4);
            amountColumn.setCellRenderer(new DefaultTableCellRenderer() {
                {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
                
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (!isSelected) {
                        c.setForeground(emerald);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    }
                    return c;
                }
            });
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        newPaymentButton = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        filterComboBox = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        paymentTable = new javax.swing.JTable();
        viewButton = new javax.swing.JButton();

        editButton.setText("Edit");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(viewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
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
                .addComponent(viewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
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
