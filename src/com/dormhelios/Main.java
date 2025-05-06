package com.dormhelios;

import com.dormhelios.controller.LoginController;
import com.dormhelios.controller.MainDashboardController;
import com.dormhelios.controller.RegisterController;
import com.dormhelios.controller.TenantDashboardController;
import com.dormhelios.model.dao.*; // Import all DAO interfaces & impls
import com.dormhelios.model.entity.User; // Needed for passing user
import com.dormhelios.view.LoginView;
import com.dormhelios.view.MainDashboardView;
import com.dormhelios.view.RegisterView;
import com.dormhelios.view.TenantDashboardView;
import com.dormhelios.view.AdminDashboardView;
import com.formdev.flatlaf.FlatLightLaf; // Import FlatLaf

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Optional;
import java.awt.Color; // Import Color
import java.awt.Dimension; // Import Dimension
import javax.swing.BorderFactory;

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
            // Set up FlatLaf with a custom theme
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 8);
            
            // Apply FlatLaf Light theme
            FlatLightLaf.setup();
            
            // --- Color Scheme & Accent Colors ---
            Color primaryColor = new Color(30, 115, 190); // Primary blue color
            Color secondaryColor = new Color(72, 166, 242); // Lighter blue for secondary elements
            Color accentColor = new Color(239, 108, 0); // Orange accent for important actions
            Color subtleColor = new Color(240, 240, 245); // Very light gray-blue for backgrounds
            
            // Main accent color settings
            UIManager.put("Component.accentColor", primaryColor);
            UIManager.put("hyperlink.foreground", secondaryColor);
            
            // --- General Component Styling ---
            // Rounded corners
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("Component.arrowType", "chevron");
            UIManager.put("Button.margin", new Insets(6, 14, 6, 14));
            
            // Focus indicators
            UIManager.put("Component.focusColor", secondaryColor);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("TextField.focusedBackground", new Color(250, 250, 255));
            
            // Borders and outlines
            UIManager.put("Component.borderWidth", 1);
            
            // --- Table Customizations ---
            // Table Row Design
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false); // Modern tables often omit vertical lines
            UIManager.put("Table.rowHeight", 30); // Increased row height for better readability
            UIManager.put("Table.cellMargins", new Insets(4, 8, 4, 8)); // Add padding inside cells
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1)); // Minimal spacing between cells
            
            // Table Colors
            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("Table.alternateRowColor", new Color(248, 250, 252)); // Very subtle alternating color
            UIManager.put("Table.selectionBackground", primaryColor.brighter());
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.focusCellBackground", new Color(232, 242, 254)); // Light blue for focused cell
            UIManager.put("Table.gridColor", new Color(230, 230, 230)); // Light gray grid lines
            
            // Table Header Styling
            UIManager.put("TableHeader.background", subtleColor);
            UIManager.put("TableHeader.foreground", new Color(60, 60, 60)); // Dark gray text
            UIManager.put("TableHeader.font", UIManager.getFont("TableHeader.font").deriveFont(java.awt.Font.BOLD));
            UIManager.put("TableHeader.cellBorder", BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)));
            
            // --- Scroll Bar Styling ---
            UIManager.put("ScrollBar.thumbArc", 999); // Rounded thumb
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.width", 10); // Thinner scrollbars
            UIManager.put("ScrollBar.track", subtleColor);
            UIManager.put("ScrollBar.thumb", new Color(180, 180, 180));
            UIManager.put("ScrollBar.thumbDarkShadow", new Color(180, 180, 180));
            UIManager.put("ScrollBar.thumbHighlight", new Color(180, 180, 180));
            UIManager.put("ScrollBar.thumbShadow", new Color(180, 180, 180));
            
            // --- Panel & Container Styling ---
            UIManager.put("Panel.background", Color.WHITE);
            UIManager.put("SplitPane.background", Color.WHITE);
            UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
            
            // --- Button Styling ---
            // Primary action button (e.g. Save, Submit)
            UIManager.put("Button.default.background", accentColor);
            UIManager.put("Button.default.foreground", Color.WHITE);
            UIManager.put("Button.default.focusedBackground", accentColor.darker());
            
            // Normal buttons
            UIManager.put("Button.hoverBackground", new Color(235, 235, 240));
            UIManager.put("Button.pressedBackground", new Color(225, 225, 230));
            
            // --- Form Field Styling ---
            // Text fields
            UIManager.put("TextField.margin", new Insets(4, 8, 4, 8));
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("TextComponent.placeholderForeground", new Color(160, 160, 160));
            
            // Combo boxes
            UIManager.put("ComboBox.padding", new Insets(4, 8, 4, 8));
            UIManager.put("ComboBox.selectionBackground", primaryColor);
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);
            
            // --- Tabs Styling ---
            UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
            UIManager.put("TabbedPane.underlineColor", primaryColor);
            UIManager.put("TabbedPane.showTabSeparators", false);
            UIManager.put("TabbedPane.tabSeparatorColor", new Color(230, 230, 230));
            UIManager.put("TabbedPane.tabHeight", 36);
            UIManager.put("TabbedPane.contentAreaInsets", new Insets(12, 12, 12, 12));
            
            // --- ToolTip Styling ---
            UIManager.put("ToolTip.background", new Color(50, 50, 50, 230));
            UIManager.put("ToolTip.foreground", Color.WHITE);
            UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(6, 10, 6, 10));
            UIManager.put("ToolTip.smallFont", UIManager.getFont("ToolTip.font").deriveFont(11f));
            
            // --- Dialog & Modal Styling ---
            UIManager.put("OptionPane.messageFont", UIManager.getFont("Label.font").deriveFont(java.awt.Font.PLAIN, 14));
            UIManager.put("OptionPane.buttonFont", UIManager.getFont("Button.font").deriveFont(java.awt.Font.BOLD));
            UIManager.put("OptionPane.messageAreaBorder", BorderFactory.createEmptyBorder(16, 16, 16, 16));
            UIManager.put("OptionPane.buttonAreaBorder", BorderFactory.createEmptyBorder(0, 16, 16, 16));
            
            // --- Typography Enhancements ---
            // Improve font rendering for all components
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            
            // Apply the changes immediately
            FlatLightLaf.updateUI();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize or customize FlatLaf Look and Feel.", e);
            // Optionally fall back to system L&F or default if FlatLaf fails
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to set system Look and Feel as fallback.", ex);
            }
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
            Optional<User> userOpt = userDAO.findByEmail(loginView.getEmailInput()); // Assuming email is username here
            if (userOpt.isPresent()) {
                currentLoggedInUser = userOpt.get();
                
                // Add detailed debugging about the user role
                LOGGER.log(Level.WARNING, "DEBUG - User login successful");
                LOGGER.log(Level.WARNING, "DEBUG - User email: " + currentLoggedInUser.getEmail());
                LOGGER.log(Level.WARNING, "DEBUG - User role: " + currentLoggedInUser.getRole());
                LOGGER.log(Level.WARNING, "DEBUG - User role class: " + currentLoggedInUser.getRole().getClass().getName());
                LOGGER.log(Level.WARNING, "DEBUG - Role equality test - Is LANDLORD? " + 
                        (currentLoggedInUser.getRole() == User.Role.LANDLORD));
                LOGGER.log(Level.WARNING, "DEBUG - Role equality test - Is TENANT? " + 
                        (currentLoggedInUser.getRole() == User.Role.TENANT));
                LOGGER.log(Level.WARNING, "DEBUG - Role equality test - Is ADMIN? " + 
                        (currentLoggedInUser.getRole() == User.Role.ADMIN));
                
                // Determine which dashboard to show based on user role
                switch (currentLoggedInUser.getRole()) {
                    case TENANT:
                        // If user is a tenant, show tenant dashboard
                        LOGGER.log(Level.WARNING, "DEBUG - Routing to TENANT dashboard");
                        showTenantDashboard();
                        break;
                        
                    case LANDLORD:
                        // If user is a landlord, show the main dashboard
                        LOGGER.log(Level.WARNING, "DEBUG - Routing to LANDLORD dashboard (MainDashboardView)");
                        showMainDashboard();
                        break;
                        
                    case ADMIN:
                        // Admin user - route to Admin Dashboard
                        LOGGER.log(Level.WARNING, "DEBUG - Routing to ADMIN dashboard (AdminDashboardView)");
                        showAdminDashboard();
                        break;
                        
                    default:
                        // Handle unexpected role (shouldn't happen but being defensive)
                        LOGGER.log(Level.WARNING, "DEBUG - Unknown role: " + currentLoggedInUser.getRole());
                        loginView.displayErrorMessage("Unknown user role. Please contact administrator.");
                        break;
                }
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

    /**
     * Creates and displays the Tenant Dashboard Screen.
     */
    public static void showTenantDashboard() {
        if (currentLoggedInUser == null) {
            LOGGER.log(Level.SEVERE, "Attempted to show tenant dashboard without a logged-in user!");
            showLoginScreen(); // Go back to login if no user
            return;
        }

        LOGGER.log(Level.INFO, "Showing tenant dashboard for user: " + currentLoggedInUser.getEmail());
        
        // Create the tenant dashboard view
        TenantDashboardView tenantDashboardView = new TenantDashboardView();
        
        // Create and initialize the tenant dashboard controller with all necessary dependencies
        TenantDashboardController tenantDashboardController = new TenantDashboardController(
                tenantDashboardView,
                tenantDAO,
                paymentDAO,
                roomDAO,
                userDAO,
                currentLoggedInUser
        );
        
        // Set user display name in sidebar
        tenantDashboardView.setUserDisplayName(currentLoggedInUser.getFirstName() != null ? 
                currentLoggedInUser.getFirstName() : currentLoggedInUser.getUsername());
        
        // Set logout action
        tenantDashboardView.setLogoutActionListener(e -> {
            currentLoggedInUser = null; // Clear logged-in user state
            tenantDashboardView.closeView(); // Close dashboard
            showLoginScreen(); // Show login screen again
        });
        
        // Show the tenant dashboard view
        tenantDashboardView.setVisible(true);
    }
    
    /**
     * Creates and displays the Admin Dashboard Screen.
     */
    public static void showAdminDashboard() {
        if (currentLoggedInUser == null) {
            LOGGER.log(Level.SEVERE, "Attempted to show admin dashboard without a logged-in user!");
            showLoginScreen();
            return;
        }
        // Initialize and show Admin Dashboard View
        AdminDashboardView adminView = new AdminDashboardView();
        adminView.setUserDisplayName(
            currentLoggedInUser.getFirstName() != null ? currentLoggedInUser.getFirstName() : currentLoggedInUser.getUsername()
        );
        // Logout action
        adminView.setLogoutActionListener(e -> {
            currentLoggedInUser = null;
            adminView.closeView();
            showLoginScreen();
        });
        adminView.setVisible(true);
    }
}
