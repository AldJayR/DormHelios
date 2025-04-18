package com.dormhelios.controller;

import com.dormhelios.model.dao.*; // Import all DAO interfaces
import com.dormhelios.model.entity.User;
import com.dormhelios.util.PasswordUtils;
import com.dormhelios.view.LoginView;
import com.dormhelios.view.MainDashboardView;
import com.dormhelios.view.RegisterView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Controller responsible for handling the logic of the LoginView.
 * Dependencies (DAOs, other controllers/views) are injected.
 */
public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    private final LoginView loginView;
    private final UserDAO userDAO; // Depends on the interface

    // --- Dependencies needed for navigation ---
    // These would ideally be provided by a factory or dependency injection framework
    // For now, we'll assume they are set after construction or passed in.
    private Runnable onLoginSuccess; // Action to run when login is successful
    private Runnable onRegisterRequest; // Action to run when register is requested

    public LoginController(LoginView loginView, UserDAO userDAO) {
        this.loginView = loginView;
        // Inject the concrete DAO implementation via the constructor (or a setter)
        this.userDAO = userDAO;

        attachListeners();
    }

    // --- Setters for Navigation Actions (Simple Dependency Injection) ---

    /**
     * Sets the action to be performed when login is successful.
     * This action typically involves creating and showing the MainDashboard.
     * @param onLoginSuccess A Runnable containing the logic to execute on success.
     */
    public void setOnLoginSuccessAction(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    /**
     * Sets the action to be performed when the user requests registration.
     * This action typically involves creating and showing the RegisterView.
     * @param onRegisterRequest A Runnable containing the logic to execute on request.
     */
    public void setOnRegisterRequestAction(Runnable onRegisterRequest) {
        this.onRegisterRequest = onRegisterRequest;
    }


    private void attachListeners() {
        loginView.addLoginButtonListener(new LoginButtonListener());
        loginView.addRegisterButtonListener(new RegisterButtonListener());
    }

    /**
     * Makes the LoginView visible. Call this to start the login process.
     */
    public void showLoginView() {
         SwingUtilities.invokeLater(() -> loginView.setVisible(true));
    }
    

    // --- Action Listener Classes ---

    class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = loginView.getEmailInput();
            char[] password = loginView.getPasswordInput();

            if (email.isEmpty() || password.length == 0) {
                loginView.displayErrorMessage("Username/Email and Password cannot be empty.");
                PasswordUtils.clearPasswordArray(password);
                return;
            }

            // Disable login button to prevent multiple clicks
            loginView.setLoginEnabled(false);

            SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
                private String errorMessage;

                @Override
                protected User doInBackground() {
                    try {
                        Optional<User> userOptional = userDAO.findByEmail(email);
                        if (userOptional.isPresent()) {
                            User user = userOptional.get();
                            if (!user.isActive()) {
                                errorMessage = "Your account is inactive. Please contact an administrator.";
                            } else if (PasswordUtils.checkPassword(new String(password), user.getPasswordHash())) {
                                return user;
                            } else {
                                errorMessage = "Invalid username or password.";
                            }
                        } else {
                            errorMessage = "Invalid username or password.";
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error during login process", ex);
                        errorMessage = "An unexpected error occurred. Please try again.";
                    }
                    return null;
                }

                @Override
                protected void done() {
                    PasswordUtils.clearPasswordArray(password);
                    loginView.setLoginEnabled(true);
                    try {
                        User user = get();
                        if (user != null) {
                            LOGGER.log(Level.INFO, "User logged in: {0}", user.getUsername());
                            loginView.closeView();
                            if (onLoginSuccess != null) onLoginSuccess.run();
                        } else {
                            loginView.displayErrorMessage(errorMessage);
                            LOGGER.log(Level.WARNING, "Login failed: {0}", email);
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error finalizing login", ex);
                        loginView.displayErrorMessage("An unexpected error occurred.");
                    }
                }
            };
            worker.execute();
        }
    }

    class RegisterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            LOGGER.log(Level.INFO, "'Register' button clicked.");
            // Execute the register request action (which should open the register view)
            if (onRegisterRequest != null) {
                onRegisterRequest.run();
                 // Optionally close the login view *after* the register view is ready
                 // loginView.closeView();
            } else {
                 LOGGER.log(Level.SEVERE, "onRegisterRequest action was not set in LoginController!");
                 loginView.displayErrorMessage("Internal configuration error. Cannot open registration.");
            }
        }
    }
}