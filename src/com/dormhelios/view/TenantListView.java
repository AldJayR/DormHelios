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
import javax.swing.table.TableColumnModel;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.JButton;

public class TenantListView extends javax.swing.JPanel {

    /**
     * Creates new form TenantListView
     */
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    public TenantListView() {
        initComponents();
        setupTable();
        setupSearchFieldPlaceholder();
        setupTableAppearance();
        applyCustomStyling();
    }

    private void setupTable() {
        // Clear any placeholder text so search starts empty
        searchField.setText("");
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
        
        // Don't filter if the search field contains the placeholder text "Search"
        if (searchText.equals("Search")) {
            searchText = "";
        }
        
        // Create final copies of the variables for use in the inner class
        final String finalSearchText = searchText;
        final String finalFilterSelection = filterSelection;
        
        // Create a custom row filter that works with our renderers
        RowFilter<DefaultTableModel, Object> compositeFilter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // First check if we should include based on search text
                if (!finalSearchText.isEmpty()) {
                    boolean matches = false;
                    // Search in Name (column 1) and Room No (column 2)
                    String name = entry.getStringValue(1).toLowerCase();
                    String roomNo = entry.getStringValue(2).toLowerCase();
                    
                    if (name.contains(finalSearchText.toLowerCase()) || 
                        roomNo.contains(finalSearchText.toLowerCase())) {
                        matches = true;
                    }
                    
                    if (!matches) {
                        return false; // No need to check combo box filter if search doesn't match
                    }
                }
                
                // Then check if we should include based on filter combo box
                if (finalFilterSelection != null && !finalFilterSelection.equals("All Tenants") && !finalFilterSelection.trim().isEmpty()) {
                    if (finalFilterSelection.equals("Recent Tenants")) {
                        // Check-in Date is in column 3
                        String checkInDateStr = entry.getStringValue(3);
                        if (checkInDateStr.equals("N/A")) {
                            return false;
                        }
                        try {
                            LocalDate checkInDate = LocalDate.parse(checkInDateStr);
                            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
                            return checkInDate.isAfter(thirtyDaysAgo);
                        } catch (Exception e) {
                            // If date can't be parsed, include the row
                            return true;
                        }
                    } else if (finalFilterSelection.equals("To Leave")) {
                        // Status is in column 4
                        String status = entry.getStringValue(4).toUpperCase();
                        return status.contains("LEAVE");
                    } else if (finalFilterSelection.equals("Active Tenants")) {
                        // Status is in column 4
                        String status = entry.getStringValue(4).toUpperCase();
                        return status.contains("ACTIVE");
                    }
                }
                
                // If we reach here, include the row
                return true;
            }
        };
        
        // Apply our custom filter
        sorter.setRowFilter(compositeFilter);
    }

    /**
     * Configures the tenant table with appropriate renderers and column sizes for a professional look.
     */
    private void setupTableAppearance() {
        // Set column widths and renderers
        TableColumnModel columnModel = tenantTable.getColumnModel();
        
        // Get the actual number of columns in the table to prevent ArrayIndexOutOfBoundsException
        int columnCount = columnModel.getColumnCount();
        
        // Apply appropriate renderers for each column - only if they exist
        // ID column - Base renderer (hidden column)
        if (columnCount > 0) {
            columnModel.getColumn(0).setPreferredWidth(40);
            columnModel.getColumn(0).setCellRenderer(new com.dormhelios.util.TableRenderers.BaseRenderer());
        }
        
        // Name column - Base renderer
        if (columnCount > 1) {
            columnModel.getColumn(1).setPreferredWidth(180);
            columnModel.getColumn(1).setCellRenderer(new com.dormhelios.util.TableRenderers.BaseRenderer());
        }
        
        // Room Number column - Base renderer
        if (columnCount > 2) {
            columnModel.getColumn(2).setPreferredWidth(80);
            columnModel.getColumn(2).setCellRenderer(new com.dormhelios.util.TableRenderers.BaseRenderer());
        }
        
        // Check-in Date column - Date renderer
        if (columnCount > 3) {
            columnModel.getColumn(3).setPreferredWidth(120);
            columnModel.getColumn(3).setCellRenderer(new com.dormhelios.util.TableRenderers.DateRenderer());
        }
        
        // Status column - Status renderer
        if (columnCount > 4) {
            columnModel.getColumn(4).setPreferredWidth(100);
            columnModel.getColumn(4).setCellRenderer(new com.dormhelios.util.TableRenderers.StatusRenderer());
        }
        
        // Adjust row height for better readability
        tenantTable.setRowHeight(32);
        
        // Improve table header appearance
        tenantTable.getTableHeader().setFont(tenantTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        tenantTable.getTableHeader().setOpaque(false);
        
        // Make the table selection more visible
        tenantTable.setSelectionBackground(new Color(30, 115, 190, 80));
        tenantTable.setSelectionForeground(Color.BLACK);
        
        // Remove grid lines for a cleaner look
        tenantTable.setShowVerticalLines(false);
        tenantTable.setShowHorizontalLines(true);
        tenantTable.setGridColor(new Color(230, 230, 230));
        
        // Enable row sorting
        tenantTable.setAutoCreateRowSorter(true);
        
        // Make search field look nicer
        searchField.setBorder(BorderFactory.createCompoundBorder(
                searchField.getBorder(),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
    }

    /**
     * Applies Tailwind-inspired styling to the TenantListView components.
     * Call this method after initComponents() in the constructor.
     */
    private void applyCustomStyling() {
        // Tailwind color palette
        Color primary = new Color(59, 130, 246);     // blue-500
        Color primaryLight = new Color(96, 165, 250); // blue-400
        Color success = new Color(34, 197, 94);      // green-500 
        Color danger = new Color(239, 68, 68);       // red-500
        Color warning = new Color(245, 158, 11);     // amber-500
        Color bgLight = new Color(243, 244, 246);    // gray-100
        Color bgDark = new Color(31, 41, 55);        // gray-800
        Color slate100 = new Color(241, 245, 249);   // slate-100
        Color slate200 = new Color(226, 232, 240);   // slate-200
        Color slate700 = new Color(51, 65, 85);      // slate-700
        Color slate800 = new Color(30, 41, 59);      // slate-800
        
        // Background styling
        this.setBackground(bgLight);
        
        // Style title 
        jLabel1.setForeground(slate800);
        
        // Style the Add Tenant button
        addTenantButton.setBackground(primary);
        addTenantButton.setForeground(Color.WHITE);
        addTenantButton.setFont(addTenantButton.getFont().deriveFont(Font.BOLD));
        addTenantButton.setBorderPainted(false);
        addTenantButton.setFocusPainted(false);
        addTenantButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Style action buttons with different colors
        JButton[] actionButtons = {viewTenantButton, editTenantButton, deleteTenantButton};
        Color[] buttonColors = {success, warning, danger};
        
        for (int i = 0; i < actionButtons.length; i++) {
            JButton button = actionButtons[i];
            button.setBackground(buttonColors[i]);
            button.setForeground(Color.WHITE);
            button.setFont(button.getFont().deriveFont(Font.BOLD));
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        tenantTable.setRowHeight(40);
        tenantTable.setIntercellSpacing(new Dimension(10, 0));
        tenantTable.setShowGrid(false);
        tenantTable.setShowHorizontalLines(true);
        tenantTable.setGridColor(slate200);
        
        // Table header styling
        tenantTable.getTableHeader().setBackground(bgLight);
        tenantTable.getTableHeader().setForeground(slate700);
        tenantTable.getTableHeader().setFont(tenantTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        tenantTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, slate200));
        
        // Table selection styling
        tenantTable.setSelectionBackground(new Color(primaryLight.getRed(), primaryLight.getGreen(), primaryLight.getBlue(), 100));
        tenantTable.setSelectionForeground(slate800);
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

    public javax.swing.JTextField getSearchField() {
        return searchField;
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
