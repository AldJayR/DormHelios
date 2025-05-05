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
        listView.addSearchFieldListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                listView.filterTable();
            }

            public void removeUpdate(DocumentEvent e) {
                listView.filterTable();
            }

            public void changedUpdate(DocumentEvent e) {
                listView.filterTable();
            }
        });
        listView.addFilterComboBoxListener(e -> listView.filterTable());
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
        createTenant(data);
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
                
                if (roomChanged) {
                    // If the tenant was previously assigned to a room, increment its available slots
                    if (oldRoomId != null && oldRoomId > 0) {
                        roomDAO.incrementSlotsAvailable(oldRoomId);
                    }
                    
                    // If the tenant is being assigned to a new room, decrement its available slots
                    if (newRoomId != null && newRoomId > 0) {
                        roomDAO.decrementSlotsAvailable(newRoomId);
                    }
                }
                
                // Update the tenant record
                return tenantDAO.updateTenant(data);
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

    public void createTenant(Tenant tenant) {
        TenantDAO tenantDAO = new TenantDAOImpl();
        int tenantId = tenantDAO.addTenant(tenant);

        // If tenant was successfully added and assigned to a room
        if (tenantId > 0 && tenant.getRoomId() != null && tenant.getRoomId() > 0) {
            // Update the room assignment which will decrement slots_available
            tenantDAO.assignTenantToRoom(tenantId, tenant.getRoomId());
        }

        refreshTenantList();
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
