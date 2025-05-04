package com.dormhelios.controller;

import com.dormhelios.model.dao.UserDAO;
import com.dormhelios.model.entity.User;
import com.dormhelios.view.SetupView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Controller responsible for handling the logic of the SetupView.
 * Completes the user profile setup after initial registration.
 */
public class SetupController {

    private static final Logger LOGGER = Logger.getLogger(SetupController.class.getName());

    private final SetupView setupView;
    private final UserDAO userDAO;
    private final User currentUser;

    // --- Navigation Callbacks ---
    private Runnable onSetupComplete; // Action after successful setup (go to dashboard)

    public SetupController(SetupView setupView, UserDAO userDAO, User currentUser) {
        this.setupView = setupView;
        this.userDAO = userDAO;
        this.currentUser = currentUser;

        attachListeners();
    }

    /**
     * Sets the action to be performed when setup is complete.
     * Typically navigates to the Main Dashboard.
     * 
     * @param onSetupComplete A Runnable containing the logic.
     */
    public void setOnSetupCompleteListener(Runnable onSetupComplete) {
        this.onSetupComplete = onSetupComplete;
    }

    private void attachListeners() {
        setupView.addSaveButtonListener(new SaveButtonListener());
    }

    /**
     * Makes the SetupView visible.
     */
    public void showSetupView() {
        SwingUtilities.invokeLater(() -> setupView.setVisible(true));
    }

    // --- Action Listener Classes ---

    class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String firstName = setupView.getFirstName();
            String lastName = setupView.getLastName();
            String address = setupView.getAddress();
            String contactNumber = setupView.getContactNumber();
            String role = setupView.getRole();

            // --- Input Validation ---
            if (firstName.isEmpty() || lastName.isEmpty()) {
                setupView.displayErrorMessage("First name and Last name are required.");
                return;
            }

            if (contactNumber.isEmpty()) {
                setupView.displayErrorMessage("Contact number is required.");
                return;
            }

            // Disable save button to prevent multiple submissions
            setupView.setSaveEnabled(false);

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                private String errorMessage;

                @Override
                protected Boolean doInBackground() {
                    try {
                        // Update the current user with the additional information
                        currentUser.setFirstName(firstName);
                        currentUser.setSurname(lastName);
                        currentUser.setPhoneNumber(contactNumber);
                        
                        // Set the role based on selection
                        switch (role) {
                            case "Landlord":
                                currentUser.setRole(User.Role.LANDLORD);
                                break;
                            default:
                                currentUser.setRole(User.Role.TENANT);
                                break;
                        }
                        
                        // Additional fields if needed
                        // currentUser.setAddress(address);
                        
                        // Update the user in the database
                        return userDAO.updateUser(currentUser);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error during setup process", ex);
                        errorMessage = "An unexpected error occurred during setup.";
                        return false;
                    }
                }

                @Override
                protected void done() {
                    // Re-enable button
                    setupView.setSaveEnabled(true);
                    try {
                        boolean success = get();
                        if (success) {
                            setupView.displayInfoMessage("Setup successful!");
                            LOGGER.log(Level.INFO, "User setup completed for ID: {0}", currentUser.getUserId());
                            
                            // Close the setup view and navigate to the main dashboard
                            if (onSetupComplete != null) {
                                setupView.closeView();
                                onSetupComplete.run();
                            } else {
                                LOGGER.log(Level.SEVERE, "onSetupComplete action was not set in SetupController!");
                                setupView.closeView();
                            }
                        } else {
                            setupView.displayErrorMessage(errorMessage != null ? 
                                errorMessage : "Failed to save user information.");
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error finalizing setup", ex);
                        setupView.displayErrorMessage("An unexpected error occurred.");
                    }
                }
            };
            worker.execute();
        }
    }
}