package com.dormhelios.controller;

import com.dormhelios.model.dao.UserDAO;
import com.dormhelios.model.entity.User;
import com.dormhelios.util.PasswordUtils; // Use the same Password Utility
import com.dormhelios.view.RegisterView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Controller responsible for handling the logic of the RegisterView.
 */
public class RegisterController {

    private static final Logger LOGGER = Logger.getLogger(RegisterController.class.getName());

    private final RegisterView registerView;
    private final UserDAO userDAO; // Depends on the interface

    // --- Navigation Callbacks ---
    private Runnable onRegistrationComplete; // Action after successful registration or cancellation
    private Runnable onBackToLogin;          // Action when "Already have account" is clicked

    public RegisterController(RegisterView registerView, UserDAO userDAO) {
        this.registerView = registerView;
        this.userDAO = userDAO;

        attachListeners();
    }

    // --- Setters for Navigation Actions ---

    /**
     * Sets the action to be performed when registration is complete (success or cancel).
     * Typically navigates back to the Login screen.
     * @param onRegistrationComplete A Runnable containing the logic.
     */
    public void setOnRegistrationCompleteListener(Runnable onRegistrationComplete) {
        this.onRegistrationComplete = onRegistrationComplete;
    }

    /**
     * Sets the action to be performed when the user clicks the link/button
     * to go back to the login screen.
     * @param onBackToLogin A Runnable containing the logic.
     */
    public void setOnBackToLoginListener(Runnable onBackToLogin) {
        this.onBackToLogin = onBackToLogin;
    }


    private void attachListeners() {
        registerView.addRegisterButtonListener(new RegisterButtonListener());
        registerView.addLoginLinkButtonListener(new BackToLoginButtonListener());
    }

    /**
     * Makes the RegisterView visible.
     */
    public void showRegisterView() {
        SwingUtilities.invokeLater(() -> registerView.setVisible(true));
    }

    // --- Action Listener Classes ---

    class RegisterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = registerView.getEmailInput();
            char[] password = registerView.getPasswordInput();
            char[] confirmPassword = registerView.getConfirmPasswordInput();

            // --- Input Validation ---
            if (email.isEmpty() || password.length == 0 || confirmPassword.length == 0) {
                registerView.displayErrorMessage("Email, Password, and Confirm Password cannot be empty.");
                clearPasswordArrays(password, confirmPassword);
                return;
            }

            if (!isValidEmail(email)) {
                 registerView.displayErrorMessage("Please enter a valid email address.");
                 clearPasswordArrays(password, confirmPassword);
                 return;
            }

            if (password.length < 8) { // Example minimum length
                 registerView.displayErrorMessage("Password must be at least 8 characters long.");
                 clearPasswordArrays(password, confirmPassword);
                 return;
            }

            if (!Arrays.equals(password, confirmPassword)) {
                registerView.displayErrorMessage("Passwords do not match.");
                clearPasswordArrays(password, confirmPassword);
                return;
            }

            // Disable register button to prevent multiple submissions
            registerView.setRegisterEnabled(false);

            SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                private String errorMessage;

                @Override
                protected Integer doInBackground() {
                    try {
                        if (userDAO.findByEmail(email).isPresent()) {
                            errorMessage = "An account with this email already exists.";
                            return -1;
                        }
                        String hashed = PasswordUtils.hashPassword(new String(password));
                        if (hashed == null) {
                            errorMessage = "Could not secure password. Registration failed.";
                            return -1;
                        }
                        User newUser = new User("test", hashed, "test", "Test", User.Role.TENANT, email, null);
                        newUser.setActive(true);
                        return userDAO.addUser(newUser);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error during registration process", ex);
                        errorMessage = "An unexpected error occurred during registration.";
                        return -1;
                    }
                }

                @Override
                protected void done() {
                    // Always clear passwords and re-enable button
                    clearPasswordArrays(password, confirmPassword);
                    registerView.setRegisterEnabled(true);
                    try {
                        int newId = get();
                        if (newId > 0) {
                            registerView.displayInfoMessage("Registration successful! You can now log in.");
                            LOGGER.log(Level.INFO, "New user registered with ID: {0}", newId);
                            if (onRegistrationComplete != null) onRegistrationComplete.run();
                            else registerView.closeView();
                        } else {
                            registerView.displayErrorMessage(errorMessage);
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error finalizing registration", ex);
                        registerView.displayErrorMessage("An unexpected error occurred.");
                    }
                }
            };
            worker.execute();
        }
    }

    class BackToLoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            LOGGER.log(Level.INFO, "'Already have account' clicked.");
            // Trigger back action
            if (onBackToLogin != null) {
                onBackToLogin.run();
            } else {
                 LOGGER.log(Level.SEVERE, "onBackToLogin action was not set in RegisterController!");
                 // Close view anyway
                 registerView.closeView();
            }
        }
    }

    // --- Helper Methods ---

    private void clearPasswordArrays(char[]... arrays) {
        for (char[] array : arrays) {
            PasswordUtils.clearPasswordArray(array);
        }
    }

    // Basic email validation (Consider using a more robust library like Apache Commons Validator)
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        // Simple regex: checks for something@something.something
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return email.matches(emailRegex);
    }
}