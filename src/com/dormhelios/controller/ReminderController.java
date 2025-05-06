package com.dormhelios.controller;

import com.dormhelios.model.dao.TenantDAO;
import com.dormhelios.model.entity.Tenant;
import com.dormhelios.util.SMSService;
import com.dormhelios.view.MainDashboardView;
import com.dormhelios.view.SendReminderDialog;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Controller class for managing the Send Reminder functionality.
 * Handles the interaction between the SendReminderDialog and the tenant data.
 */
public class ReminderController {
    
    private static final Logger LOGGER = Logger.getLogger(ReminderController.class.getName());
    
    private final SendReminderDialog reminderDialog;
    private final TenantDAO tenantDAO;
    private final MainDashboardView mainView;
    private final SMSService smsService;
    
    private Tenant selectedTenant;
    
    public ReminderController(SendReminderDialog reminderDialog, TenantDAO tenantDAO, MainDashboardView mainView) {
        this.reminderDialog = reminderDialog;
        this.tenantDAO = tenantDAO;
        this.mainView = mainView;
        this.smsService = SMSService.fromConfig();
        
        initializeListeners();
        loadTenants();
    }
    
    private void initializeListeners() {
        // Add action listener for the send button
        reminderDialog.addSendButtonListener(this::handleSendButtonClick);
        
        // Add action listener for the cancel button
        reminderDialog.addCancelButtonListener(e -> reminderDialog.closeDialog());
        
        // Add action listener for the tenant selection change
        reminderDialog.addTenantSelectionListener(e -> handleTenantSelectionChange());
    }
    
    /**
     * Loads tenant data into the dialog
     */
    private void loadTenants() {
        try {
            List<Tenant> tenants = tenantDAO.findAll();
            reminderDialog.setTenantComboBoxModel(tenants);
            
            // Set default message template
            String defaultTemplate = "Dear tenant,\n\nThis is a friendly reminder that your rent payment "
                    + "for " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")) 
                    + " is due. Please settle your account as soon as possible.\n\n"
                    + "Thank you,\nDorm Management";
            
            reminderDialog.setMessageTemplate(defaultTemplate);
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error loading tenants for reminder", ex);
            JOptionPane.showMessageDialog(mainView, 
                    "Error loading tenant data: " + ex.getMessage(), 
                    "Data Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handles the tenant selection change event
     */
    public void handleTenantSelectionChange() {
        Integer selectedTenantId = reminderDialog.getSelectedTenantId();
        if (selectedTenantId != null) {
            try {
                Optional<Tenant> optionalTenant = tenantDAO.findById(selectedTenantId);
                if (optionalTenant.isPresent()) {
                    selectedTenant = optionalTenant.get();
                    if (selectedTenant.getPhoneNumber() != null) {
                        reminderDialog.setContactNumber(selectedTenant.getPhoneNumber());
                    } else {
                        reminderDialog.setContactNumber("No phone number available");
                    }
                } else {
                    reminderDialog.setContactNumber("Tenant not found");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error loading tenant details", ex);
                reminderDialog.setContactNumber("Error loading contact info");
            }
        } else {
            selectedTenant = null;
            reminderDialog.setContactNumber("");
        }
    }
    
    /**
     * Handles the send button click event
     * @param e ActionEvent from the button click
     */
    private void handleSendButtonClick(ActionEvent e) {
        // Get the selected tenant ID
        Integer selectedTenantId = reminderDialog.getSelectedTenantId();
        if (selectedTenantId == null) {
            reminderDialog.displayErrorMessage("Please select a tenant");
            return;
        }
        
        // Get the message text
        String message = reminderDialog.getMessage();
        if (message == null || message.trim().isEmpty()) {
            reminderDialog.displayErrorMessage("Please enter a message to send");
            return;
        }
        
        // Check if we have a valid tenant with phone number
        if (selectedTenant == null || selectedTenant.getPhoneNumber() == null || 
                selectedTenant.getPhoneNumber().trim().isEmpty()) {
            reminderDialog.displayErrorMessage("The selected tenant has no valid phone number");
            return;
        }
        
        // Disable send button to prevent multiple clicks
        reminderDialog.disableSendButton();
        
        try {
            // Get tenant info for the simulation message
            String tenantName = selectedTenant.getFirstName() + " " + selectedTenant.getLastName();
            String phoneNumber = selectedTenant.getPhoneNumber();
            
            // Use our simulated SMS sender instead of the actual SMS service
            boolean result = reminderDialog.simulateSendSMS(tenantName, phoneNumber, message);
            
            // Always succeeds in simulation mode
            if (result) {
                LOGGER.info("Simulated SMS reminder sent to " + tenantName + " (" + phoneNumber + ")");
                
                // Set the dialog as saved and close it
                reminderDialog.setSaved(true);
                reminderDialog.closeDialog();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error in SMS reminder simulation", ex);
            reminderDialog.displayErrorMessage("Error: " + ex.getMessage());
        } finally {
            // Always re-enable the send button
            reminderDialog.enableSendButton();
        }
    }
    
    /**
     * Shows the send reminder dialog
     */
    public void showSendReminderDialog() {
        reminderDialog.showDialog();
    }
}