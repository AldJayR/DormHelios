package com.dormhelios.controller;

import com.dormhelios.model.dao.*;
import com.dormhelios.model.entity.*;
import com.dormhelios.view.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TenantController {

    private static final Logger LOGGER = Logger.getLogger(TenantController.class.getName());

    private final TenantListView listView;
    private final TenantFormDialog formDialog;
    private final TenantDetailView detailView;
    private final TenantDAO tenantDAO;
    private final RoomDAO roomDAO;
    private final GuardianDAO guardianDAO;
    private final EmergencyContactDAO contactDAO;
    private final PaymentDAO paymentDAO;
    private final UserDAO userDAO; // Add UserDAO dependency
    private final JFrame parentFrame;

    public TenantController(TenantListView listView,
            TenantFormDialog formDialog,
            TenantDetailView detailView,
            TenantDAO tenantDAO,
            RoomDAO roomDAO,
            GuardianDAO guardianDAO,
            EmergencyContactDAO contactDAO,
            PaymentDAO paymentDAO,
            UserDAO userDAO, // Add UserDAO to constructor
            JFrame parentFrame) {
        this.listView = listView;
        this.formDialog = formDialog;
        this.detailView = detailView;
        this.tenantDAO = tenantDAO;
        this.roomDAO = roomDAO;
        this.guardianDAO = guardianDAO;
        this.contactDAO = contactDAO;
        this.paymentDAO = paymentDAO;
        this.userDAO = userDAO; // Assign UserDAO
        this.parentFrame = parentFrame;
        attachListeners();
    }

    public void loadInitialData() {
        SwingWorker<List<TenantWithRoom>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TenantWithRoom> doInBackground() {
                return tenantDAO.findAllWithRoomNumbers();
            }

            @Override
            protected void done() {
                try {
                    List<TenantWithRoom> tenants = get();
                    listView.displayTenantsWithRooms(tenants);
                    // Apply current filters/search after loading new data
                    listView.filterTable();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Error loading tenants", e.getCause());
                    listView.displayErrorMessage("Error loading tenants: " + e.getCause().getMessage());
                }
            }
        };
        worker.execute();
    }

    private void attachListeners() {
        listView.addAddTenantButtonListener(e -> openAddDialog());
        listView.addEditButtonListener(e -> openEditDialog());
        listView.addDeleteButtonListener(e -> deactivateTenant());
        listView.addViewButtonListener(e -> openDetailDialog());
        
        // Fix the search field listener implementation
        listView.addSearchFieldListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> listView.filterTable());
                System.out.println("Searching!");
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> listView.filterTable());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> listView.filterTable());
            }
        });
        
        listView.addFilterComboBoxListener(e -> {
            // Use invokeLater for consistency
            SwingUtilities.invokeLater(() -> listView.filterTable());
        });

        // Fallback: listen to key releases directly on the search field
        listView.getSearchField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                SwingUtilities.invokeLater(() -> listView.filterTable());
            }
        });
    }

    private void openAddDialog() {
        formDialog.setupForAdd();
        // Load combo models
        formDialog.setRoomComboBoxModel(roomDAO.findAll());
        formDialog.setGuardianComboBoxModel(guardianDAO.findAll());
        formDialog.setEmergencyContactComboBoxModel(contactDAO.findAll());
        formDialog.setUserComboBoxModel(userDAO.findAll()); // Load users
        formDialog.addSaveButtonListener(e -> saveNewTenant());
        formDialog.addCancelButtonListener(e -> formDialog.closeDialog());
        formDialog.showDialog();
        if (formDialog.isSaved()) {
            loadInitialData();
        }
    }

    private void openEditDialog() {
        int id = listView.getSelectedTenantId();
        if (id < 0) {
            listView.displayErrorMessage("Select a tenant to edit.");
            return;
        }
        SwingWorker<Optional<Tenant>, Void> worker = new SwingWorker<>() {
            @Override
            protected Optional<Tenant> doInBackground() {
                return tenantDAO.findById(id);
            }

            @Override
            protected void done() {
                try {
                    Optional<Tenant> opt = get();
                    if (opt.isPresent()) {
                        Tenant t = opt.get();
                        formDialog.setupForEdit(t);
                        // Load combo models
                        formDialog.setRoomComboBoxModel(roomDAO.findAll());
                        formDialog.setGuardianComboBoxModel(guardianDAO.findAll());
                        formDialog.setEmergencyContactComboBoxModel(contactDAO.findAll());
                        formDialog.setUserComboBoxModel(userDAO.findAll()); // Load users
                        formDialog.addSaveButtonListener(e -> saveUpdatedTenant());
                        formDialog.addCancelButtonListener(e -> formDialog.closeDialog());
                        formDialog.showDialog();
                        if (formDialog.isSaved()) {
                            loadInitialData();
                        }
                    } else {
                        listView.displayErrorMessage("Tenant not found.");
                        loadInitialData();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Error fetching tenant for edit", e.getCause());
                    listView.displayErrorMessage("Error: " + e.getCause().getMessage());
                }
            }
        };
        worker.execute();
    }

    private void saveNewTenant() {
        Tenant data = formDialog.getTenantData();
        if (data == null) {
            return;
        }
        
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return tenantDAO.addTenant(data);
            }

            @Override
            protected void done() {
                try {
                    int tenantId = get();
                    if (tenantId > 0) {
                        formDialog.setSaved(true);
                        formDialog.closeDialog();
                        // Refresh the tenant list
                        loadInitialData();
                        // Notify RoomController to refresh its data
                        notifyRoomListUpdate();
                    } else {
                        formDialog.displayErrorMessage("Failed to add tenant.");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Error adding tenant", e.getCause());
                    formDialog.displayErrorMessage("Error: " + e.getCause().getMessage());
                }
            }
        };
        worker.execute();
    }

    private void saveUpdatedTenant() {
        Tenant data = formDialog.getTenantData();
        if (data == null) {
            return;
        }
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                // Get the original tenant data to compare room changes
                Optional<Tenant> originalTenantOpt = tenantDAO.findById(data.getTenantId());
                if (!originalTenantOpt.isPresent()) {
                    return false;
                }
                
                Tenant originalTenant = originalTenantOpt.get();
                Integer oldRoomId = originalTenant.getRoomId();
                Integer newRoomId = data.getRoomId();
                
                // Check if the room assignment has changed
                boolean roomChanged = (oldRoomId == null && newRoomId != null) || 
                                     (oldRoomId != null && !oldRoomId.equals(newRoomId));
                
                // If room has changed, use assignTenantToRoom which handles slots properly
                if (roomChanged) {
                    // Store the room ID temporarily
                    Integer tempRoomId = data.getRoomId();
                    
                    // Set room to null for the main update to avoid double-counting
                    data.setRoomId(null);
                    
                    // First update the tenant data without room change
                    boolean updateSuccess = tenantDAO.updateTenant(data);
                    if (!updateSuccess) {
                        return false;
                    }
                    
                    // Then use the transactional assignTenantToRoom method to handle the room change
                    // This method internally manages incrementing old room and decrementing new room
                    boolean assignSuccess = tenantDAO.assignTenantToRoom(data.getTenantId(), tempRoomId);
                    return assignSuccess;
                } else {
                    // No room change, just update the tenant normally
                    return tenantDAO.updateTenant(data);
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        formDialog.setSaved(true);
                        formDialog.closeDialog();
                        // Refresh the tenant list
                        loadInitialData();
                        // Notify RoomController to refresh its data if a room change occurred
                        notifyRoomListUpdate();
                    } else {
                        formDialog.displayErrorMessage("Failed to update tenant.");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Error updating tenant", e.getCause());
                    formDialog.displayErrorMessage("Error: " + e.getCause().getMessage());
                }
            }
        };
        worker.execute();
    }
    
    // Add a method to notify room list to refresh
    private void notifyRoomListUpdate() {
        // Use SwingUtilities.invokeLater to ensure this runs on the EDT
        SwingUtilities.invokeLater(() -> {
            // Fire a property change event that can be listened to by RoomController
            if (parentFrame != null && parentFrame instanceof MainDashboardView) {
                // Cast to MainDashboardView to access the custom firePropertyChange method
                ((MainDashboardView) parentFrame).firePropertyChange("ROOM_DATA_CHANGED", false, true);
                LOGGER.log(Level.INFO, "Notified room list to refresh after tenant room change");
            }
        });
    }

    private void deactivateTenant() {
        int id = listView.getSelectedTenantId();
        if (id < 0) {
            listView.displayErrorMessage("Select a tenant to deactivate.");
            return;
        }
        int confirm = listView.displayConfirmDialog(
                "Deactivate tenant ID " + id + "?", "Confirm Deactivation");
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return tenantDAO.setActiveStatus(id, false);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        listView.displayErrorMessage("Tenant deactivated.");
                        loadInitialData();
                    } else {
                        listView.displayErrorMessage("Could not deactivate tenant.");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Error deactivating tenant", e.getCause());
                    listView.displayErrorMessage("Error: " + e.getCause().getMessage());
                }
            }
        };
        worker.execute();
    }

    private void openDetailDialog() {
        int id = listView.getSelectedTenantId();
        if (id < 0) {
            listView.displayErrorMessage("Select a tenant to view.");
            return;
        }
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Tenant tenant;
            Room room;
            Guardian guardian;
            EmergencyContact contact;
            List<Payment> payments;

            @Override
            protected Void doInBackground() {
                tenant = tenantDAO.findById(id).orElse(null);
                room = tenant != null && tenant.getRoomId() != null ? roomDAO.findById(tenant.getRoomId()).orElse(null) : null;
                guardian = tenant != null && tenant.getGuardianId() != null ? guardianDAO.findById(tenant.getGuardianId()).orElse(null) : null;
                contact = tenant != null && tenant.getEmergencyContactId() != null ? contactDAO.findById(tenant.getEmergencyContactId()).orElse(null) : null;
                payments = paymentDAO.findByTenantId(id);
                return null;
            }

            @Override
            protected void done() {
                detailView.displayTenantDetails(tenant, room, guardian, contact, payments);
                detailView.addCloseButtonListener(e -> detailView.closeDialog());
                detailView.showDialog();
            }
        };
        worker.execute();
    }

    /**
     * Public method to create a tenant, which can be called by external controllers.
     * Uses the instance tenantDAO rather than creating a new instance.
     * 
     * @param tenant The tenant data to save
     * @return The ID of the newly created tenant, or -1 if failed
     */
    public int createTenant(Tenant tenant) {
        // Use the instance tenantDAO instead of creating a new one
        int tenantId = tenantDAO.addTenant(tenant);
        
        if (tenantId > 0) {
            // Refresh the tenant list if the tenant was added successfully
            refreshTenantList();
        }
        
        return tenantId;
    }

    private void refreshTenantList() {
        loadInitialData();
    }

    // --- Methods for External Access ---
    /**
     * Public method to open the add tenant dialog. Can be called from external
     * controllers.
     */
    public void showAddTenantForm() {
        openAddDialog();
    }
}
