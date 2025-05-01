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
        SwingWorker<List<Tenant>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Tenant> doInBackground() {
                return tenantDAO.findAll();
            }
            @Override
            protected void done() {
                try {
                    List<Tenant> tenants = get();
                    listView.displayTenants(tenants);
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
            public void insertUpdate(DocumentEvent e) { listView.filterTable(); }
            public void removeUpdate(DocumentEvent e) { listView.filterTable(); }
            public void changedUpdate(DocumentEvent e) { listView.filterTable(); }
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
        if (formDialog.isSaved()) loadInitialData();
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
                        if (formDialog.isSaved()) loadInitialData();
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
        if (data == null) return;
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return tenantDAO.addTenant(data);
            }
            @Override
            protected void done() {
                try {
                    int newId = get();
                    if (newId > 0) {
                        formDialog.setSaved(true);
                        formDialog.closeDialog();
                    } else {
                        formDialog.displayErrorMessage("Failed to save tenant.");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Error saving tenant", e.getCause());
                    formDialog.displayErrorMessage("Error: " + e.getCause().getMessage());
                }
            }
        };
        worker.execute();
    }

    private void saveUpdatedTenant() {
        Tenant data = formDialog.getTenantData();
        if (data == null) return;
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return tenantDAO.updateTenant(data);
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        formDialog.setSaved(true);
                        formDialog.closeDialog();
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

    private void deactivateTenant() {
        int id = listView.getSelectedTenantId();
        if (id < 0) {
            listView.displayErrorMessage("Select a tenant to deactivate.");
            return;
        }
        int confirm = listView.displayConfirmDialog(
                "Deactivate tenant ID " + id + "?", "Confirm Deactivation");
        if (confirm != JOptionPane.YES_OPTION) return;
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
            Tenant tenant; Room room; Guardian guardian; EmergencyContact contact; List<Payment> payments;
            @Override
            protected Void doInBackground() {
                tenant = tenantDAO.findById(id).orElse(null);
                room = tenant != null && tenant.getRoomId()!=null ? roomDAO.findById(tenant.getRoomId()).orElse(null) : null;
                guardian = tenant!=null && tenant.getGuardianId()!=null ? guardianDAO.findById(tenant.getGuardianId()).orElse(null):null;
                contact = tenant!=null && tenant.getEmergencyContactId()!=null ? contactDAO.findById(tenant.getEmergencyContactId()).orElse(null):null;
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
}
