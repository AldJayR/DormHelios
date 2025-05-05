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
import javax.swing.table.TableColumnModel;

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
        setupSearchFieldPlaceholder();
        setupTableAppearance(); // Apply table appearance settings
        applyCustomStyling(); // Apply Tailwind-inspired styling
        setupScrollablePanel(); // Ensure correct scrolling behavior
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
                new String[]{"All Rooms", "Vacant", "Occupied", "Maintenance"}
        ));

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
     * Configures the room table with appropriate renderers and column sizes for
     * a professional look.
     */
    private void setupTableAppearance() {
        // Set column widths and renderers
        TableColumnModel columnModel = roomTable.getColumnModel();

        // Get the actual number of columns in the table to prevent ArrayIndexOutOfBoundsException
        int columnCount = columnModel.getColumnCount();

        // Apply appropriate renderers for each column - only if they exist
        // ID column - Base renderer (hidden column)
        if (columnCount > 0) {
            columnModel.getColumn(0).setPreferredWidth(40);
            columnModel.getColumn(0).setCellRenderer(new com.dormhelios.util.TableRenderers.BaseRenderer());
        }

        // Room Number column - Base renderer
        if (columnCount > 1) {
            columnModel.getColumn(1).setPreferredWidth(120);
            columnModel.getColumn(1).setCellRenderer(new com.dormhelios.util.TableRenderers.BaseRenderer());
        }

        // Capacity column - Base renderer
        if (columnCount > 2) {
            columnModel.getColumn(2).setPreferredWidth(100);
            columnModel.getColumn(2).setCellRenderer(new com.dormhelios.util.TableRenderers.BaseRenderer());
        }

        // Available Slots column - Base renderer
        if (columnCount > 3) {
            columnModel.getColumn(3).setPreferredWidth(120);
            columnModel.getColumn(3).setCellRenderer(new com.dormhelios.util.TableRenderers.BaseRenderer());
        }

        // Monthly Rate column - Currency renderer
        if (columnCount > 4) {
            columnModel.getColumn(4).setPreferredWidth(140);
            columnModel.getColumn(4).setCellRenderer(new com.dormhelios.util.TableRenderers.CurrencyRenderer());
        }

        // Status column - Status renderer
        if (columnCount > 5) {
            columnModel.getColumn(5).setPreferredWidth(100);
            columnModel.getColumn(5).setCellRenderer(new com.dormhelios.util.TableRenderers.StatusRenderer());
        }

        // Adjust row height for better readability
        roomTable.setRowHeight(32);

        // Improve table header appearance
        roomTable.getTableHeader().setFont(roomTable.getTableHeader().getFont().deriveFont(java.awt.Font.BOLD));
        roomTable.getTableHeader().setOpaque(false);

        // Make the table selection more visible
        roomTable.setSelectionBackground(new java.awt.Color(30, 115, 190, 80));
        roomTable.setSelectionForeground(java.awt.Color.BLACK);

        // Remove grid lines for a cleaner look
        roomTable.setShowVerticalLines(false);
        roomTable.setShowHorizontalLines(true);
        roomTable.setGridColor(new java.awt.Color(230, 230, 230));

        // Enable row sorting
        roomTable.setAutoCreateRowSorter(true);
    }

    /**
     * Sets up the search field with placeholder text behavior. The placeholder
     * text "Search" will disappear when the field gains focus and reappear when
     * the field loses focus if it's empty.
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

        // Don't filter if the search field contains the placeholder text "Search"
        if (searchText.equals("Search")) {
            searchText = "";
        }

        final String finalSearchText = searchText;
        final String finalFilterSelection = statusFilter;

        // Create a row filter that handles both search and status filtering
        RowFilter<DefaultTableModel, Object> compositeFilter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // First check if we should include based on search text
                if (!finalSearchText.isEmpty()) {
                    boolean matches = false;
                    // Check room number (column 1)
                    String roomNumber = entry.getStringValue(1).toLowerCase();
                    if (roomNumber.contains(finalSearchText.toLowerCase())) {
                        matches = true;
                    }

                    if (!matches) {
                        return false; // No need to check status if search text doesn't match
                    }
                }

                // Then check if we should include based on status filter
                if (!statusFilter.equals("All Rooms")) {
                    // Get the status column value (column 5)
                    String status = entry.getStringValue(5).toUpperCase();

                    switch (statusFilter) {
                        case "Vacant":
                            return status.contains("VACANT");
                        case "Occupied":
                            return status.contains("OCCUPIED");
                        case "Maintenance":
                            return status.contains("MAINTENANCE") || status.contains("UNDER_MAINTENANCE");
                        default:
                            return true;
                    }
                }

                // If we reach here, include the row
                return true;
            }
        };

        // Apply our custom filter
        sorter.setRowFilter(compositeFilter);

        LOGGER.fine("Table filtered - Search: '" + searchText + "', Status: '" + statusFilter + "'");
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
     * Applies Tailwind-inspired styling to the RoomListView components. Call
     * this method after initComponents() in the constructor.
     */
    private void applyCustomStyling() {
        // Tailwind color palette
        java.awt.Color primary = new java.awt.Color(59, 130, 246);     // blue-500
        java.awt.Color primaryLight = new java.awt.Color(96, 165, 250); // blue-400
        java.awt.Color success = new java.awt.Color(34, 197, 94);      // green-500 
        java.awt.Color danger = new java.awt.Color(239, 68, 68);       // red-500
        java.awt.Color warning = new java.awt.Color(245, 158, 11);     // amber-500
        java.awt.Color cyan = new java.awt.Color(8, 145, 178);         // cyan-600
        java.awt.Color emerald = new java.awt.Color(16, 185, 129);     // emerald-500
        java.awt.Color orange = new java.awt.Color(249, 115, 22);      // orange-500
        java.awt.Color purple = new java.awt.Color(147, 51, 234);      // purple-600
        java.awt.Color bgLight = new java.awt.Color(243, 244, 246);    // gray-100
        java.awt.Color slate100 = new java.awt.Color(241, 245, 249);   // slate-100
        java.awt.Color slate200 = new java.awt.Color(226, 232, 240);   // slate-200
        java.awt.Color slate700 = new java.awt.Color(51, 65, 85);      // slate-700
        java.awt.Color slate800 = new java.awt.Color(30, 41, 59);      // slate-800

        // Background styling
        this.setBackground(bgLight);

        // Style title 
        jLabel1.setForeground(slate800);

        // Style the Add Room button
        addRoomsButton.setBackground(primary);
        addRoomsButton.setForeground(java.awt.Color.WHITE);
        addRoomsButton.setFont(addRoomsButton.getFont().deriveFont(java.awt.Font.BOLD));
        addRoomsButton.setBorderPainted(false);
        addRoomsButton.setFocusPainted(false);
        addRoomsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Style action buttons
        editButton.setBackground(warning);
        editButton.setForeground(java.awt.Color.WHITE);
        editButton.setFont(editButton.getFont().deriveFont(java.awt.Font.BOLD));
        editButton.setBorderPainted(false);
        editButton.setFocusPainted(false);
        editButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        deleteButton.setBackground(danger);
        deleteButton.setForeground(java.awt.Color.WHITE);
        deleteButton.setFont(deleteButton.getFont().deriveFont(java.awt.Font.BOLD));
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Style the search field
        searchField.setBackground(slate100);
        searchField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(slate200, 1, true),
                javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Style filter combo box
        filterComboBox.setBackground(java.awt.Color.WHITE);
        filterComboBox.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(slate200, 1, true),
                javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Style summary cards
        styleCard(jPanel1, primary, "Total Rooms");
        styleCard(jPanel2, emerald, "Vacant");
        styleCard(jPanel3, orange, "Occupied");
        styleCard(jPanel4, purple, "Maintenance");

        // Style the table
        roomTable.setRowHeight(40);
        roomTable.setIntercellSpacing(new java.awt.Dimension(10, 0));
        roomTable.setShowGrid(false);
        roomTable.setShowHorizontalLines(true);
        roomTable.setGridColor(slate200);

        // Table header styling
        roomTable.getTableHeader().setBackground(bgLight);
        roomTable.getTableHeader().setForeground(slate700);
        roomTable.getTableHeader().setFont(roomTable.getTableHeader().getFont().deriveFont(java.awt.Font.BOLD));
        roomTable.getTableHeader().setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, slate200));

        // Table selection styling
        roomTable.setSelectionBackground(new java.awt.Color(primaryLight.getRed(), primaryLight.getGreen(), primaryLight.getBlue(), 100));
        roomTable.setSelectionForeground(slate800);
    }

    /**
     * Helper method to style a summary card panel with Tailwind-inspired
     * colors.
     *
     * @param panel The panel to style
     * @param color The main color for the card
     * @param title The title text (for identifying which labels to style)
     */
    private void styleCard(javax.swing.JPanel panel, java.awt.Color color, String title) {
        // Apply a light version of the color as background
        panel.setBackground(new java.awt.Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                35)); // Very light opacity

        // Add rounded corners and border
        panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), 100), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Find the title label and the value label in the panel
        for (java.awt.Component comp : panel.getComponents()) {
            if (comp instanceof javax.swing.JLabel) {
                javax.swing.JLabel label = (javax.swing.JLabel) comp;
                if (label.getText().contains(title) || title.contains(label.getText())) {
                    // This is the title label
                    label.setForeground(new java.awt.Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            220)); // Slightly transparent
                } else {
                    // This must be the value label
                    label.setForeground(color); // Use the full color for the value
                }
            }
        }
    }

    /**
     * Initializes the components and properly configures the layout to ensure
     * correct scrolling behavior.
     */
    private void setupScrollablePanel() {
        // Ensure the main panel can properly accommodate scrolling
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Make sure the table takes all the available space
        roomTable.setFillsViewportHeight(true);

        // Ensure the scroll pane doesn't get extra space
        jScrollPane1.setPreferredSize(new java.awt.Dimension(jScrollPane1.getPreferredSize().width, 500));

        // Ensure the viewport is properly setup
        jScrollPane1.getViewport().setBackground(this.getBackground());
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
        setPreferredSize(new java.awt.Dimension(1200, 900));

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
