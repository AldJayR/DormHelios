package com.dormhelios.view;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import com.dormhelios.model.entity.Tenant;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.DocumentListener;

public class TenantListView extends javax.swing.JPanel {

    /**
     * Creates new form TenantListView
     */
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    public TenantListView() {
        initComponents();
        setupTable();
    }

    private void setupTable() {
        // Define table columns - Match your wireframe/needs
        String[] columnNames = {"ID", "Name", "Room No.", "Check-in Date", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Make table cells non-editable by default
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tenantTable.setModel(tableModel);
        tenantTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION); // Allow only one row selection
        tenantTable.setAutoCreateRowSorter(true); // Enable basic column sorting
        sorter = new TableRowSorter<>(tableModel);
        tenantTable.setRowSorter(sorter);

        // Adjust column widths (optional)
        tenantTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        tenantTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        tenantTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Room No.
        tenantTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Check-in Date
        tenantTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Status
    }

    public void displayTenants(List<Tenant> tenants) {
        // Clear existing rows
        tableModel.setRowCount(0);
        if (tenants == null) {
            return; // Handle null list gracefully
        }
        // Populate table
        for (Tenant tenant : tenants) {
            Object[] row = new Object[]{
                tenant.getTenantId(),
                tenant.getLastName() + ", " + tenant.getFirstName(), // Combine name
                tenant.getRoomId() != null ? getRoomNumberFromId(tenant.getRoomId()) : "N/A", // Need logic to get Room#
                tenant.getLeaseStartDate() != null ? tenant.getLeaseStartDate().toString() : "N/A", // Format date as needed
                getTenantStatus(tenant) // Determine status based on logic
            // Add other relevant columns if needed
            };
            tableModel.addRow(row);
        }
        // Optionally update row count label if you add one
        // rowCountLabel.setText("Total Tenants: " + tableModel.getRowCount());
    }

    private String getRoomNumberFromId(int roomId) {
        // TODO: Implement logic to fetch Room Number based on roomId
        // This might involve calling RoomDAO or having data pre-joined/cached
        return "Room" + roomId; // Placeholder
    }

    private String getTenantStatus(Tenant tenant) {
        if (tenant.getLeaseEndDate() != null && tenant.getLeaseEndDate().isBefore(java.time.LocalDate.now().plusDays(7))) {
            return "To Leave";
        }
        // Add logic for Pending, Active etc.
        return "Active"; // Placeholder
    }

    public int getSelectedTenantId() {
        int selectedRow = tenantTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Convert view row index to model row index in case of sorting/filtering
            int modelRow = tenantTable.convertRowIndexToModel(selectedRow);
            // Assuming ID is in the first column (index 0)
            return (Integer) tableModel.getValueAt(modelRow, 0);
        }
        return -1; // Indicate no selection
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
     * Gets the selected item from the filter combo box.
     *
     * @return The selected filter criteria (e.g., "All Tenants", "Active", "To
     * Leave").
     */
    public String getSelectedFilter() {
        return (String) filterComboBox.getSelectedItem();
    }

    /**
     * Applies filtering to the table based on the search text. Typically called
     * by a listener attached by the controller.
     */
    public void filterTable() {
        String searchText = getSearchText();
        RowFilter<DefaultTableModel, Object> rf = null;
        try {
            // Filter based on Name column (index 1) - case insensitive
            // Add more columns to search if needed (e.g., using regex OR)
            rf = RowFilter.regexFilter("(?i)" + searchText, 1);
        } catch (java.util.regex.PatternSyntaxException e) {
            // If regex is invalid, don't filter
            return;
        }
        sorter.setRowFilter(rf);
    }

    // --- Methods to Add Listeners ---
    public void addAddTenantButtonListener(ActionListener listener) {
        addTenantButton.addActionListener(listener);
    }

    public void addEditButtonListener(ActionListener listener) {
        editTenantButton.addActionListener(listener);
    }

    public void addViewButtonListener(ActionListener listener) {
        viewTenantButton.addActionListener(listener);
    }

    public void addDeleteButtonListener(ActionListener listener) {
        deleteTenantButton.addActionListener(listener);
    }

    /**
     * Adds a listener to the search field (e.g., to trigger filtering on key
     * release).
     *
     * @param listener DocumentListener provided by the controller.
     */
    public void addSearchFieldListener(DocumentListener listener) {
        searchField.getDocument().addDocumentListener(listener);
    }

    /**
     * Adds a listener to the filter combo box.
     *
     * @param listener ActionListener provided by the controller.
     */
    public void addFilterComboBoxListener(ActionListener listener) {
        filterComboBox.addActionListener(listener);
    }

    /**
     * Adds a mouse listener to the table (e.g., for double-click action).
     *
     * @param listener MouseAdapter provided by the controller.
     */
    public void addTableMouseListener(MouseAdapter listener) {
        tenantTable.addMouseListener(listener);
    }

    // --- Utility Methods ---
    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public int displayConfirmDialog(String message, String title) {
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    public JTable getTenantTable() {
        return tenantTable;
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
        addTenantButton = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        filterComboBox = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tenantTable = new javax.swing.JTable();
        deleteTenantButton = new javax.swing.JButton();
        viewTenantButton = new javax.swing.JButton();
        editTenantButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 36)); // NOI18N
        jLabel1.setText("Tenants");

        addTenantButton.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        addTenantButton.setText("Add Tenant");

        searchField.setText("Search");

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Tenants", "Recent Tenants", " " }));

        tenantTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Name", "Room", "Check-in Date", "Status"
            }
        ));
        jScrollPane1.setViewportView(tenantTable);

        deleteTenantButton.setText("Delete");

        viewTenantButton.setText("View");

        editTenantButton.setText("Edit");
        editTenantButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editTenantButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(viewTenantButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(editTenantButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteTenantButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(filterComboBox, 0, 159, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(addTenantButton, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(26, 26, 26))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(addTenantButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 472, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(deleteTenantButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(viewTenantButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(editTenantButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(13, 13, 13))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void editTenantButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editTenantButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_editTenantButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addTenantButton;
    private javax.swing.JButton deleteTenantButton;
    private javax.swing.JButton editTenantButton;
    private javax.swing.JComboBox<String> filterComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField searchField;
    private javax.swing.JTable tenantTable;
    private javax.swing.JButton viewTenantButton;
    // End of variables declaration//GEN-END:variables
}
