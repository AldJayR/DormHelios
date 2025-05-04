package com.dormhelios.view;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import com.dormhelios.model.entity.Tenant;
import com.dormhelios.model.entity.TenantWithRoom;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.DocumentListener;
import java.time.LocalDate;

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

        // Set up filter combo box with improved options
        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(
            new String[] {"All Tenants", "Active Tenants", "Recent Tenants", "To Leave"}
        ));
        
        // Set up search field placeholder text behavior
        setupSearchFieldPlaceholder();

        // Adjust column widths (optional)
        tenantTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        tenantTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        tenantTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Room No.
        tenantTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Check-in Date
        tenantTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Status
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

    /*
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
    */

    /**
     * Displays tenants with their room numbers in the table.
     * This uses the TenantWithRoom class which contains the room number information.
     *
     * @param tenants List of TenantWithRoom objects to display
     */
    public void displayTenantsWithRooms(List<TenantWithRoom> tenants) {
        // Clear existing rows
        tableModel.setRowCount(0);
        if (tenants == null) {
            return; // Handle null list gracefully
        }
        
        // Populate table
        for (TenantWithRoom tenant : tenants) {
            Object[] row = new Object[]{
                tenant.getTenantId(),
                tenant.getLastName() + ", " + tenant.getFirstName(), // Combine name
                tenant.getRoomNumber() != null ? tenant.getRoomNumber() : "N/A", // Use the actual room number 
                tenant.getLeaseStartDate() != null ? tenant.getLeaseStartDate().toString() : "N/A", // Format date as needed
                getTenantStatus(tenant) // Determine status based on logic
            };
            tableModel.addRow(row);
        }
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
     * Applies filtering to the table based on the search text and filter combo box.
     * Typically called by a listener attached by the controller.
     */
    public void filterTable() {
        String searchText = getSearchText();
        String filterSelection = getSelectedFilter();
        
        // Create a list to hold multiple filters if needed
        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
        
        // Add search text filter if not empty
        if (!searchText.isEmpty() && !searchText.equals("Search")) {
            try {
                // Filter based on Name column (index 1) - case insensitive
                RowFilter<DefaultTableModel, Object> searchFilter = 
                    RowFilter.regexFilter("(?i)" + searchText, 1);
                filters.add(searchFilter);
            } catch (java.util.regex.PatternSyntaxException e) {
                // If regex is invalid, ignore this filter
            }
        }
        
        // Add combo box selection filter if not "All Tenants"
        if (filterSelection != null && !filterSelection.equals("All Tenants") && !filterSelection.trim().isEmpty()) {
            if (filterSelection.equals("Recent Tenants")) {
                // Filter for tenants who moved in within last 30 days
                LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
                RowFilter<DefaultTableModel, Object> dateFilter = new RowFilter<DefaultTableModel, Object>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                        // Check-in Date is in column 3
                        Object checkInDateObj = entry.getValue(3);
                        if (checkInDateObj != null && !checkInDateObj.equals("N/A")) {
                            try {
                                LocalDate checkInDate = LocalDate.parse(checkInDateObj.toString());
                                return checkInDate.isAfter(thirtyDaysAgo);
                            } catch (Exception e) {
                                // If date can't be parsed, include the row
                                return true;
                            }
                        }
                        return false;
                    }
                };
                filters.add(dateFilter);
            } else if (filterSelection.equals("To Leave")) {
                // Filter for tenants with "To Leave" status
                RowFilter<DefaultTableModel, Object> statusFilter = 
                    RowFilter.regexFilter("^To Leave$", 4); // Status is in column 4
                filters.add(statusFilter);
            } else if (filterSelection.equals("Active Tenants")) {
                // Filter for tenants with "Active" status
                RowFilter<DefaultTableModel, Object> activeFilter = 
                    RowFilter.regexFilter("^Active$", 4); // Status is in column 4
                filters.add(activeFilter);
            }
        }
        
        if (filters.isEmpty()) {
            // No filters, show all rows
            sorter.setRowFilter(null);
        } else if (filters.size() == 1) {
            // Only one filter
            sorter.setRowFilter(filters.get(0));
        } else {
            // Combine multiple filters with AND logic
            RowFilter<DefaultTableModel, Object> andFilter = RowFilter.andFilter(filters);
            sorter.setRowFilter(andFilter);
        }
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

        setBackground(new java.awt.Color(250, 250, 250));
        setPreferredSize(new java.awt.Dimension(1023, 500));

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
                .addContainerGap())
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
