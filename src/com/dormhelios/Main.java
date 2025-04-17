package com.dormhelios;

import com.dormhelios.controller.LoginController;
import com.dormhelios.controller.MainDashboardController;
import com.dormhelios.controller.RegisterController;
import com.dormhelios.model.dao.*; // Import all DAO interfaces & impls
import com.dormhelios.model.entity.User; // Needed for passing user
import com.dormhelios.view.LoginView;
import com.dormhelios.view.MainDashboardView;
import com.dormhelios.view.RegisterView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Optional;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    // --- DAO Instances (Create them once) ---
    // In a real app, use a Dependency Injection framework (like Guice or Spring)
    // or a Service Locator pattern. For simplicity, we instantiate them here.
    private static final UserDAO userDAO = new UserDAOImpl();
    private static final TenantDAO tenantDAO = new TenantDAOImpl();
    private static final RoomDAO roomDAO = new RoomDAOImpl();
    private static final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private static final GuardianDAO guardianDAO = new GuardianDAOImpl(); // If needed
    private static final EmergencyContactDAO emergencyContactDAO = new EmergencyContactDAOImpl(); // If needed

    // --- View Instances (Managed by navigation logic) ---
    private static LoginView loginView;
    private static RegisterView registerView;
    private static MainDashboardView mainDashboardView;

    // --- Controller Instances ---
    private static LoginController loginController;
    private static RegisterController registerController;
    private static MainDashboardController mainDashboardController;

    // --- Store logged-in user ---
    private static User currentLoggedInUser;

    public static void main(String[] args) {
        setupLookAndFeel();

        // Ensure GUI operations happen on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(Main::startApplication);
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to set system Look and Feel.", e);
        }
    }

    /**
     * Initializes and shows the initial Login View.
     */
    private static void startApplication() {
        showLoginScreen();
    }

    /**
     * Creates and displays the Login Screen and its controller. Sets up the
     * navigation actions.
     */
    public static void showLoginScreen() {
        // Close other main views if they exist
        if (registerView != null) {
            registerView.dispose();
        }
        if (mainDashboardView != null) {
            mainDashboardView.dispose();
        }

        loginView = new LoginView();
        loginController = new LoginController(loginView, userDAO);

        // Define what happens on successful login
        loginController.setOnLoginSuccessAction(() -> {
            // Find the user again (or pass it directly if LoginController stored it)
            // This is slightly redundant but safer if LoginController doesn't hold state.
            Optional<User> userOpt = userDAO.findByUsername(loginView.getEmailInput()); // Assuming email is username here
            if (userOpt.isPresent()) {
                currentLoggedInUser = userOpt.get();
                showMainDashboard();
            } else {
                // Should not happen if login logic was correct, but handle defensively
                LOGGER.log(Level.SEVERE, "User data lost after successful login check!");
                loginView.displayErrorMessage("Internal error after login. Please restart.");
            }
        });

        // Define what happens when register is requested
        loginController.setOnRegisterRequestAction(() -> {
            loginView.closeView(); // Close login before showing register
            showRegisterScreen();
        });

        loginController.showLoginView(); // Make the login view visible
    }

    /**
     * Creates and displays the Register Screen and its controller.
     */
    public static void showRegisterScreen() {
        registerView = new RegisterView();
        registerController = new RegisterController(registerView, userDAO); // Pass DAO

        // Define what happens after successful registration or clicking "Already have account"
        registerController.setOnRegistrationCompleteListener(() -> {
            registerView.closeView(); // Close register view
            showLoginScreen();      // Go back to login
        });
        registerController.setOnBackToLoginListener(() -> {
            registerView.closeView(); // Close register view
            showLoginScreen();      // Go back to login
        });

        registerController.showRegisterView(); // Make the register view visible
    }

    /**
     * Creates and displays the Main Dashboard Screen and its controller.
     * Requires the logged-in user information.
     */
    public static void showMainDashboard() {
        if (currentLoggedInUser == null) {
            LOGGER.log(Level.SEVERE, "Attempted to show main dashboard without a logged-in user!");
            showLoginScreen(); // Go back to login if no user
            return;
        }

        mainDashboardView = new MainDashboardView();
        mainDashboardController = new MainDashboardController(
                mainDashboardView,
                currentLoggedInUser, // Pass the user object
                // Pass all necessary DAOs
                userDAO,
                tenantDAO,
                roomDAO,
                paymentDAO,
                guardianDAO,
                emergencyContactDAO
        );

        // Define what happens on logout
        mainDashboardController.setOnLogoutListener(() -> {
            currentLoggedInUser = null; // Clear logged-in user state
            mainDashboardView.closeView(); // Close dashboard
            showLoginScreen(); // Show login screen again
        });

        mainDashboardController.initializeDashboard(); // Setup and show the dashboard
    }
}
