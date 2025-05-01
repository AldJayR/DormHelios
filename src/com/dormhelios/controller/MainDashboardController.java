package com.dormhelios.controller;

import com.dormhelios.model.dao.*; // Import relevant DAOs
import com.dormhelios.model.dao.GuardianDAO;
import com.dormhelios.model.dao.EmergencyContactDAO;
import com.dormhelios.model.entity.User;
import com.dormhelios.model.entity.Payment;
import com.dormhelios.model.entity.Room;
import com.dormhelios.view.*; // Import relevant Views/Panels

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal; // For potential revenue calculation
import java.text.NumberFormat; // For currency formatting
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the MainDashboardView. Handles navigation,
 * populating the dashboard panel, and managing content panels.
 */
public class MainDashboardController {

    private static final Logger LOGGER = Logger.getLogger(MainDashboardController.class.getName());

    private final MainDashboardView mainView;
    private final User loggedInUser;
    private final UserDAO userDAO;
    private final TenantDAO tenantDAO;
    private final RoomDAO roomDAO;
    private final PaymentDAO paymentDAO;
    private final GuardianDAO guardianDAO;
    private final EmergencyContactDAO emergencyContactDAO;
    // Add other DAOs as needed (GuardianDAO, etc.)

    private Runnable onLogoutListener; // Callback for logout action

    // Content Panels (created lazily)
    private DashboardPanel dashboardPanel;
    private TenantListView tenantListView;
    private TenantFormDialog tenantFormDialog;   // Dialog for add/edit
    private TenantDetailView tenantDetailView;   // Dialog for details
    private TenantController tenantController;   // Controller to manage tenant views
    private RoomListView roomListView;
    ///private PaymentListView paymentListView;
    //private SettingsView settingsView;
    // Add UserManagementView etc. if implementing admin features

    public MainDashboardController(MainDashboardView mainView, User loggedInUser,
                                   UserDAO userDAO, TenantDAO tenantDAO, RoomDAO roomDAO, PaymentDAO paymentDAO,
                                   GuardianDAO guardianDAO, EmergencyContactDAO emergencyContactDAO) {
        this.mainView = mainView;
        this.loggedInUser = loggedInUser;
        this.userDAO = userDAO;
        this.tenantDAO = tenantDAO;
        this.roomDAO = roomDAO;
        this.paymentDAO = paymentDAO;
        this.guardianDAO = guardianDAO;
        this.emergencyContactDAO = emergencyContactDAO;
        // Assign other DAOs

        attachNavigationListeners();
    }

    /**
     * Sets the action to perform on logout.
     * @param onLogoutListener Runnable action.
     */
    public void setOnLogoutListener(Runnable onLogoutListener) {
        this.onLogoutListener = onLogoutListener;
    }

    /**
     * Initializes the main dashboard view:
     * - Sets user info in sidebar.
     * - Creates and adds the initial DashboardPanel.
     * - Loads data for the DashboardPanel asynchronously.
     * - Makes the main view visible.
     */
    public void initializeDashboard() {
        mainView.setUserDisplayName(loggedInUser.getFirstName() != null ? loggedInUser.getFirstName() : loggedInUser.getUsername());

        // Create and add the initial dashboard panel
        dashboardPanel = new DashboardPanel();
        mainView.addContentPanel(dashboardPanel, MainDashboardView.DASHBOARD_PANEL);

        // Attach listeners for dashboard quick actions
        attachDashboardActionListeners();

        // Display the dashboard panel initially
        mainView.displayPanel(MainDashboardView.DASHBOARD_PANEL, mainView.getDashboardButton()); // Assuming getter exists in view

        // Load dashboard data asynchronously
        loadDashboardData();

        // Make the main window visible
        SwingUtilities.invokeLater(() -> mainView.setVisible(true));
    }

    /**
     * Attaches listeners to the main sidebar navigation buttons.
     */
    private void attachNavigationListeners() {
        mainView.addDashboardButtonListener(e -> showDashboardPanel());
        mainView.addTenantsButtonListener(e -> showTenantListPanel());
        mainView.addDormsButtonListener(e -> showRoomListPanel());
        //mainView.addPaymentsButtonListener(e -> showPaymentListPanel());
        // mainView.addSettingsButtonListener(e -> showSettingsPanel());
        mainView.addLogoutButtonListener(e -> logout());
    }

    /**
     * Attaches listeners to the Quick Action buttons on the DashboardPanel.
     */
    private void attachDashboardActionListeners() {
        if (dashboardPanel == null) return; // Should not happen if called after init

        // Example: Add Tenant button opens the Tenant Form
        dashboardPanel.addAddTenantButtonListener(e -> {
            // Logic to open the Tenant Add/Edit Form (potentially involves TenantController)
            LOGGER.info("Dashboard 'Add Tenant' clicked - Placeholder Action");
            // Example: new TenantController(...).showAddTenantForm(); // Needs proper setup
             JOptionPane.showMessageDialog(mainView, "Add Tenant action not fully implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        // Example: New Payment button opens the Payment Logging Dialog
        dashboardPanel.addNewPaymentButtonListener(e -> {
             // Logic to open the Payment Logging Dialog (potentially involves PaymentController)
             LOGGER.info("Dashboard 'New Payment' clicked - Placeholder Action");
             // Example: new PaymentController(...).showLogPaymentDialog(); // Needs proper setup
              JOptionPane.showMessageDialog(mainView, "New Payment action not fully implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        dashboardPanel.addAddRoomButtonListener(e -> {
             // Logic to open the Room Add/Edit Form (potentially involves RoomController)
             LOGGER.info("Dashboard 'Add Room' clicked - Placeholder Action");
             // Example: new RoomController(...).showAddRoomForm(); // Needs proper setup
              JOptionPane.showMessageDialog(mainView, "Add Room action not fully implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        dashboardPanel.addSendReminderButtonListener(e -> {
             // Logic for sending reminders (complex, likely out of initial scope)
             LOGGER.info("Dashboard 'Send Reminder' clicked - Placeholder Action");
              JOptionPane.showMessageDialog(mainView, "Send Reminder action not implemented.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });
    }


    // --- Panel Display Methods ---

    private void showDashboardPanel() {
        if (dashboardPanel == null) { // Should exist, but check defensively
            dashboardPanel = new DashboardPanel();
            mainView.addContentPanel(dashboardPanel, MainDashboardView.DASHBOARD_PANEL);
            attachDashboardActionListeners(); // Re-attach if newly created
        }
        mainView.displayPanel(MainDashboardView.DASHBOARD_PANEL, mainView.getDashboardButton());
        loadDashboardData(); // Refresh data when navigating back
    }

    private void showTenantListPanel() {
        if (tenantListView == null) {
            // Initialize tenant views and controller
            tenantListView = new TenantListView();
            tenantFormDialog = new TenantFormDialog(mainView, true);
            tenantDetailView = new TenantDetailView(mainView, true);
            // Use injected DAOs
            tenantController = new TenantController(
                tenantListView,
                tenantFormDialog,
                tenantDetailView,
                tenantDAO,
                roomDAO,
                guardianDAO,
                emergencyContactDAO,
                paymentDAO,
                userDAO,      // Pass UserDAO
                mainView
            );
            tenantController.loadInitialData();
            mainView.addContentPanel(tenantListView, MainDashboardView.TENANTS_PANEL);
            LOGGER.info("Tenant List Panel created and wired to TenantController.");
        }
        mainView.displayPanel(MainDashboardView.TENANTS_PANEL, mainView.getTenantsButton());
    }

    private void showRoomListPanel() {
         if (roomListView == null) {
            roomListView = new RoomListView();
            RoomController roomController = new RoomController(roomListView, roomDAO, mainView);
            roomController.loadInitialData();
            mainView.addContentPanel(roomListView, MainDashboardView.ROOMS_PANEL);
             LOGGER.info("Room List Panel created (Controller/Data loading needed).");
        }
        mainView.displayPanel(MainDashboardView.ROOMS_PANEL, mainView.getDormsButton());
    }

    /*
     private void showPaymentListPanel() {
         if (paymentListView == null) {
            paymentListView = new PaymentListView();
            // Instantiate PaymentController
            // PaymentController paymentController = new PaymentController(paymentListView, paymentDAO, ...);
            // paymentController.initializePaymentList();
            mainView.addContentPanel(paymentListView, MainDashboardView.PAYMENTS_PANEL);
             LOGGER.info("Payment List Panel created (Controller/Data loading needed).");
             paymentListView.add(new JLabel("Payment List View - Full implementation pending."));
        }
        mainView.displayPanel(MainDashboardView.PAYMENTS_PANEL, mainView.getPaymentsButton());
         // paymentController.loadPaymentData();
    }
     */

     /*
     private void showSettingsPanel() {
         if (settingsView == null) {
            settingsView = new SettingsView();
            // Instantiate SettingsController
            // SettingsController settingsController = new SettingsController(settingsView, userDAO, loggedInUser);
            // settingsController.initializeSettings();
            mainView.addContentPanel(settingsView, MainDashboardView.SETTINGS_PANEL);
             LOGGER.info("Settings Panel created (Controller/Data loading needed).");
             settingsView.add(new JLabel("Settings View - Full implementation pending."));
        }
        mainView.displayPanel(MainDashboardView.SETTINGS_PANEL, mainView.getSettingsButton());
         // settingsController.loadUserSettings();
    }
    */

    // --- Data Loading (Asynchronous) ---

    private void loadDashboardData() {
        if (dashboardPanel == null) return;

        SwingWorker<DashboardData, Void> worker = new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                LOGGER.info("Loading dashboard data in background...");
                // Fetch data using efficient DAO methods
                int tenantCount = tenantDAO.countAll(); // Efficient count of active tenants
                int totalRooms = roomDAO.countAll(); // Efficient count of active rooms
                int occupiedCount = roomDAO.countByStatus(Room.RoomStatus.OCCUPIED); // Efficient count

                YearMonth currentMonth = YearMonth.now();
                LocalDate startOfMonth = currentMonth.atDay(1);
                LocalDate endOfMonth = currentMonth.atEndOfMonth();
                BigDecimal revenue = paymentDAO.sumAmountByDateRange(startOfMonth, endOfMonth); // Efficient sum

                // Fetch reminders/alerts (requires custom logic/queries)
                List<String> reminders = List.of("Placeholder: Payment Overdue R101", "Placeholder: Incoming Payment R203");
                // Fetch recent activities (requires an audit log table/mechanism)
                List<String> activities = List.of("Placeholder: Added Tenant X", "Placeholder: Logged Payment Y");

                return new DashboardData(tenantCount, occupiedCount, totalRooms, revenue, reminders, activities);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get(); // Get results from doInBackground
                    // --- Update UI on the EDT ---
                    dashboardPanel.setTotalTenants(data.tenantCount, "+? this month"); // Need logic for change text

                    String occupancyRateStr = "N/A";
                    String occupancyDetailStr = String.format("%d out of %d rooms", data.occupiedCount, data.totalRooms);
                    if (data.totalRooms > 0) {
                        double rate = (double) data.occupiedCount / data.totalRooms * 100;
                        occupancyRateStr = String.format("%.0f%%", rate);
                    }
                    dashboardPanel.setOccupancyRate(occupancyRateStr, occupancyDetailStr);

                    // Format currency
                    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH")); // PHP Locale
                    dashboardPanel.setMonthlyRevenue(currencyFormatter.format(data.monthlyRevenue));

                    // Update lists
                    DefaultListModel<String> reminderModel = dashboardPanel.getRemindersListModel();
                    reminderModel.clear();
                    data.reminders.forEach(reminderModel::addElement);

                    DefaultListModel<String> activityModel = dashboardPanel.getRecentActivitiesListModel();
                    activityModel.clear();
                    data.activities.forEach(activityModel::addElement);

                    // Update chart placeholder (actual chart update would happen here)
                    LOGGER.info("Dashboard data loaded and UI updated.");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    LOGGER.log(Level.WARNING, "Dashboard data loading interrupted", e);
                } catch (ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Error loading dashboard data", e.getCause());
                    // Show error message on dashboard panel or dialog
                     JOptionPane.showMessageDialog(mainView, "Error loading dashboard data: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Re-enable controls if they were disabled
                    // dashboardPanel.setLoadingState(false);
                }
            }
        };

        worker.execute(); // Start the SwingWorker
    }

    // --- Logout ---

    private void logout() {
        LOGGER.info("Logout action initiated by user: " + loggedInUser.getUsername());
        int confirm = JOptionPane.showConfirmDialog(
                mainView,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (onLogoutListener != null) {
                onLogoutListener.run(); // Trigger the action defined in MainApp
            } else {
                LOGGER.log(Level.SEVERE, "onLogoutListener is not set!");
                // Fallback: just close the window, but MainApp won't know
                mainView.closeView();
            }
        }
    }

    // --- Helper Data Class for SwingWorker ---
    private static class DashboardData {
        final int tenantCount;
        final long occupiedCount;
        final int totalRooms;
        final BigDecimal monthlyRevenue;
        final List<String> reminders;
        final List<String> activities;

        DashboardData(int tenantCount, long occupiedCount, int totalRooms, BigDecimal monthlyRevenue, List<String> reminders, List<String> activities) {
            this.tenantCount = tenantCount;
            this.occupiedCount = occupiedCount;
            this.totalRooms = totalRooms;
            this.monthlyRevenue = monthlyRevenue;
            this.reminders = reminders;
            this.activities = activities;
        }
    }
}