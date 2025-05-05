package com.dormhelios.view;

import com.dormhelios.model.entity.Room; // Import the Room entity
import java.awt.event.ActionListener;
import java.text.NumberFormat; // For currency formatting
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Logger; // For logging
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.DocumentListener; // For live search
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class RoomListView extends javax.swing.JPanel {

    /**
     * Creates new form RoomListView
     */
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter; // For sorting and filtering
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    private static final Logger LOGGER = Logger.getLogger(RoomListView.class.getName()); // Logger for debugging

    public RoomListView() {
        initComponents();
        setupTable();
    }

    private void setupTable() {
        // Define table columns - Match your wireframe/needs
        String[] columnNames = {"ID", "Room No.", "Capacity", "Available Slots", "Monthly Rate", "Status"}; // Added Available Slots column
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Make table cells non-editable by default
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomTable.setModel(tableModel);
        roomTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        roomTable.setAutoCreateRowSorter(true); // Enable basic column sorting
        sorter = new TableRowSorter<>(tableModel);
        roomTable.setRowSorter(sorter);

        // Set up filter combo box with improved options
        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(
            new String[] {"All Rooms", "Vacant", "Occupied", "Maintenance"}
        ));
        
        // Set up search field placeholder text behavior
        setupSearchFieldPlaceholder();

        // Hide the ID column visually but keep it in the model for retrieval
        roomTable.getColumnModel().getColumn(0).setMinWidth(0);
        roomTable.getColumnModel().getColumn(0).setMaxWidth(0);
        roomTable.getColumnModel().getColumn(0).setWidth(0);

        // Adjust other column widths (optional)
        roomTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Room No.
        roomTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Capacity
        roomTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Available Slots
        roomTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Rate
        roomTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Status
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

    public void updateSummaryCards(int total, int vacant, int occupied, int maintenance) {
        totalRoomsValueLabel.setText(String.valueOf(total));
        vacantValueLabel.setText(String.valueOf(vacant));
        occupiedValueLabel.setText(String.valueOf(occupied));
        maintenanceValueLabel.setText(String.valueOf(maintenance));
    }

    /**
     * Populates the room table with data.
     *
     * @param rooms List of Room objects to display.
     */
    public void displayRooms(List<Room> rooms) {
        // Clear existing rows
        tableModel.setRowCount(0);
        if (rooms == null) {
            return; // Handle null list gracefully
        }
        // Populate table
        for (Room room : rooms) {
            Object[] row = new Object[]{
                room.getRoomId(), // Include ID in the model
                room.getRoomNumber(),
                room.getCapacity(),
                room.getSlotsAvailable(), // Fixed method name
                CURRENCY_FORMATTER.format(room.getMonthlyRate() != null ? room.getMonthlyRate() : 0), // Format currency
                room.getStatus() != null ? room.getStatus().name() : "N/A" // Display status name
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Gets the currently selected room's ID from the table.
     *
     * @return The room ID, or -1 if no row is selected.
     */
    public int getSelectedRoomId() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Convert view row index to model row index in case of sorting/filtering
            int modelRow = roomTable.convertRowIndexToModel(selectedRow);
            // Get ID from the hidden first column (index 0)
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
     * @return The selected filter criteria (e.g., "All Rooms", "Vacant",
     * "Occupied").
     */
    public String getSelectedFilter() {
        // Assuming the ComboBox model contains String representations or Room.RoomStatus enums
        Object selected = filterComboBox.getSelectedItem();
        return selected != null ? selected.toString() : "All Rooms"; // Handle null selection
    }

    /**
     * Applies filtering to the table based on the search text and status
     * filter. Typically called by listeners attached by the controller.
     */
    public void filterTable() {
        String searchText = getSearchText();
        String statusFilter = getSelectedFilter();
        
        // If search field contains placeholder text, treat as empty
        if (searchText.equals("Search")) {
            searchText = "";
        }

        RowFilter<DefaultTableModel, Object> combinedFilter = null;
        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        // Search filter (Column 1 is Room Number) - case insensitive
        if (!searchText.isEmpty()) {
            try {
                filters.add(RowFilter.regexFilter("(?i)" + searchText, 1));
            } catch (java.util.regex.PatternSyntaxException e) {
                // Ignore invalid regex
                LOGGER.warning("Invalid search regex: " + e.getMessage());
            }
        }

        // Status filter (Column 5 is Status) - case insensitive
        if (!statusFilter.equals("All Rooms")) {
            try {
                // Map the filter combo box values to potential status values
                String statusRegex;
                if (statusFilter.equalsIgnoreCase("Maintenance")) {
                    // Special case for "Maintenance" to match "UNDER_MAINTENANCE"
                    statusRegex = "(?i)UNDER_MAINTENANCE|Maintenance";
                } else {
                    // For "Vacant" and "Occupied" - make case insensitive
                    statusRegex = "(?i)" + statusFilter;
                }
                
                // Apply the case-insensitive filter to status column
                filters.add(RowFilter.regexFilter(statusRegex, 5));
            } catch (java.util.regex.PatternSyntaxException e) {
                // Ignore invalid regex
                LOGGER.warning("Invalid status filter regex: " + e.getMessage());
            }
        }

        // Combine filters if multiple exist
        if (!filters.isEmpty()) {
            combinedFilter = RowFilter.andFilter(filters);
        }

        // Apply the filter
        sorter.setRowFilter(combinedFilter); // null clears filter
        
        // Log the filtering action for debugging
        LOGGER.fine("Table filtered - Search: '" + searchText + "', Status: '" + statusFilter + 
                    "', Filters applied: " + filters.size());
    }

    // --- Methods to Add Listeners ---
    public void addAddRoomButtonListener(ActionListener listener) {
        addRoomsButton.addActionListener(listener);
    }
    
    public void addEditRoomButtonListener(ActionListener listener) {
        editButton.addActionListener(listener);
    }



    public void addDeleteButtonListener(ActionListener listener) {
        deleteButton.addActionListener(listener);
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

    // --- Utility Methods ---
    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public int displayConfirmDialog(String message, String title) {
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    }
    
    public void displayInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public JTable getRoomTable() {
        return roomTable;
    }
    
    /**
     * Gets the "Add Room" button for external access.
     * 
     * @return The Add Rooms button component
     */
    public javax.swing.JButton getAddRoomsButton() {
        return addRoomsButton;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jLabel1 = new javax.swing.JLabel();
        addRoomsButton = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        filterComboBox = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        roomTable = new javax.swing.JTable();
        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        totalRoomsValueLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        vacantValueLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        occupiedValueLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        maintenanceValueLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(250, 250, 250));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 1, 36)); // NOI18N
        jLabel1.setText("Room Management");

        addRoomsButton.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        addRoomsButton.setText("Add Room");

        searchField.setText("Search");

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Rooms" }));

        roomTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Room Number", "Capacity", "Monthy Rate", "Status"
            }
        ));
        jScrollPane1.setViewportView(roomTable);

        editButton.setText("Edit");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");

        jLabel2.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel2.setText("Total Rooms");

        totalRoomsValueLabel.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        totalRoomsValueLabel.setText("15");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(totalRoomsValueLabel)
                    .addComponent(jLabel2))
                .addContainerGap(128, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalRoomsValueLabel)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel2.setPreferredSize(new java.awt.Dimension(221, 119));

        jLabel4.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel4.setText("Vacant");

        vacantValueLabel.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        vacantValueLabel.setText("5");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(vacantValueLabel)
                    .addComponent(jLabel4))
                .addContainerGap(164, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vacantValueLabel)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel3.setPreferredSize(new java.awt.Dimension(221, 119));

        occupiedValueLabel.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        occupiedValueLabel.setText("12");

        jLabel7.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel7.setText("Occupied");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(occupiedValueLabel)
                    .addComponent(jLabel7))
                .addContainerGap(146, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(occupiedValueLabel)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel4.setPreferredSize(new java.awt.Dimension(221, 119));

        jLabel9.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel9.setText("Maintenance");

        maintenanceValueLabel.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        maintenanceValueLabel.setText("1");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maintenanceValueLabel)
                    .addComponent(jLabel9))
                .addContainerGap(125, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maintenanceValueLabel)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 884, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(filterComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addRoomsButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(29, 29, 29)
                                .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jScrollPane1)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(10, 10, 10)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(55, 55, 55)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(44, 44, 44)
                                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(39, 39, 39)
                                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addRoomsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(26, 26, 26)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 472, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_editButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRoomsButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JComboBox<String> filterComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel maintenanceValueLabel;
    private javax.swing.JLabel occupiedValueLabel;
    private javax.swing.JTable roomTable;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel totalRoomsValueLabel;
    private javax.swing.JLabel vacantValueLabel;
    // End of variables declaration//GEN-END:variables
}
