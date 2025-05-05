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
import java.util.ArrayList;
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
    private PaymentListView paymentListView;
    private SendReminderDialog reminderDialog;
    private ReminderController reminderController;
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
        mainView.addPaymentsButtonListener(e -> showPaymentListPanel());
        // mainView.addSettingsButtonListener(e -> showSettingsPanel());
        mainView.addLogoutButtonListener(e -> logout());
    }

    /**
     * Attaches listeners to the Quick Action buttons on the DashboardPanel.
     */
    private void attachDashboardActionListeners() {
        if (dashboardPanel == null) return; // Should not happen if called after init

        // Connect Add Tenant button to the TenantController's add functionality
        dashboardPanel.addAddTenantButtonListener(e -> {
            LOGGER.info("Dashboard 'Add Tenant' button clicked - Opening add tenant form");
            // Show tenant list panel first
            showTenantListPanel();
            // Use the existing tenant controller to show the add tenant form
            if (tenantController != null) {
                tenantController.showAddTenantForm();
            }
        });

        // Connect New Payment button to the Payment functionality
        dashboardPanel.addNewPaymentButtonListener(e -> {
            LOGGER.info("Dashboard 'New Payment' button clicked - Opening payment logging dialog");
            // Show payment list panel first
            showPaymentListPanel();
            // Open the payment logging dialog
            if (paymentListView != null) {
                // Since paymentListView is shown, the PaymentController should be initialized
                // Use the "Add Payment" button in the payment list view to trigger the dialog
                ActionEvent simulatedEvent = new ActionEvent(paymentListView, ActionEvent.ACTION_PERFORMED, "AddPayment");
                paymentListView.getNewPaymentButton().getActionListeners()[0].actionPerformed(simulatedEvent);
            }
        });

        // Connect Add Room button to the Room functionality
        dashboardPanel.addAddRoomButtonListener(e -> {
            LOGGER.info("Dashboard 'Add Room' button clicked - Opening add room form");
            // Show room list panel first
            showRoomListPanel();
            // Use the existing room controller to show the add room form
            if (roomListView != null) {
                // Trigger the "Add Room" button in the room list view
                ActionEvent simulatedEvent = new ActionEvent(roomListView, ActionEvent.ACTION_PERFORMED, "AddRoom");
                roomListView.getAddRoomsButton().getActionListeners()[0].actionPerformed(simulatedEvent);
            }
        });

        dashboardPanel.addSendReminderButtonListener(e -> {
            LOGGER.info("Dashboard 'Send Reminder' clicked - Opening reminder dialog");
            showSendReminderDialog();
        });
    }

    // --- Panel Display Methods ---

    private void showDashboardPanel() {
        if (dashboardPanel == null) { // Should exist, but check defensively
            dashboardPanel = new DashboardPanel();
            mainView.addContentPanel(dashboardPanel, MainDashboardView.DASHBOARD_PANEL);
            attachDashboardActionListeners(); // Re-attach if newly created
        }
        // Enable scrolling for dashboard panel
        mainView.getJScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // Reset scroll position to top
        SwingUtilities.invokeLater(() -> mainView.getJScrollPane().getVerticalScrollBar().setValue(0));
        
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
        // Disable scrolling for tenant list view since it has its own scrolling
        mainView.getJScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        // Reset scroll position to top
        SwingUtilities.invokeLater(() -> {
            // Reset main scroll pane
            mainView.getJScrollPane().getVerticalScrollBar().setValue(0);
            // Also reset tenant list's table scroll pane if it exists
            if (tenantListView != null && tenantListView.getTenantTable() != null && 
                tenantListView.getTenantTable().getParent() != null && 
                tenantListView.getTenantTable().getParent().getParent() instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) tenantListView.getTenantTable().getParent().getParent();
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });
        
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
        // Enable scrolling for room list panel
        mainView.getJScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // Reset scroll position to top
        SwingUtilities.invokeLater(() -> {
            // Reset main scroll pane
            mainView.getJScrollPane().getVerticalScrollBar().setValue(0);
            // Reset room list's internal scroll pane
            if (roomListView != null && roomListView.getRoomTable() != null && 
                roomListView.getRoomTable().getParent() != null && 
                roomListView.getRoomTable().getParent().getParent() instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) roomListView.getRoomTable().getParent().getParent();
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });
        
        mainView.displayPanel(MainDashboardView.ROOMS_PANEL, mainView.getDormsButton());
    }

    private void showPaymentListPanel() {
        if (paymentListView == null) {
            paymentListView = new PaymentListView();
            // Instantiate PaymentController with all required dependencies
            PaymentController paymentController = new PaymentController(
                paymentListView, 
                paymentDAO, 
                tenantDAO, 
                roomDAO, 
                userDAO, 
                loggedInUser, 
                mainView
            );
            // Add payment panel to main view
            mainView.addContentPanel(paymentListView, MainDashboardView.PAYMENTS_PANEL);
            LOGGER.info("Payment List Panel created and wired to PaymentController.");
            // Load payment data through the controller
            paymentController.loadPaymentData();
        }
        // Disable scrolling for payment list view since it has its own scrolling
        mainView.getJScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        // Reset scroll position to top
        SwingUtilities.invokeLater(() -> {
            // Reset main scroll pane
            mainView.getJScrollPane().getVerticalScrollBar().setValue(0);
            // Also reset payment list's table scroll pane if it exists
            if (paymentListView != null && paymentListView.getPaymentTable() != null && 
                paymentListView.getPaymentTable().getParent() != null && 
                paymentListView.getPaymentTable().getParent().getParent() instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) paymentListView.getPaymentTable().getParent().getParent();
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });
        
        mainView.displayPanel(MainDashboardView.PAYMENTS_PANEL, mainView.getPaymentsButton());
    }

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
                
                // Calculate tenants added this month
                YearMonth currentMonth = YearMonth.now();
                LocalDate startOfMonth = currentMonth.atDay(1);
                LocalDate endOfMonth = currentMonth.atEndOfMonth();
                
                // Get tenant count change for current month
                int newTenantsThisMonth = tenantDAO.countNewTenantsByDateRange(startOfMonth, endOfMonth);
                
                // Get room statistics
                int totalRooms = roomDAO.countAll(); // Efficient count of active rooms
                int occupiedCount = roomDAO.countByStatus(Room.RoomStatus.OCCUPIED); // Efficient count

                // Get revenue for current month
                BigDecimal revenue = paymentDAO.sumAmountByDateRange(startOfMonth, endOfMonth); // Efficient sum

                // Get overdue payments - could be implemented in PaymentDAO
                List<Payment> overduePayments = paymentDAO.findOverduePayments();
                List<String> reminders = new ArrayList<>();
                
                // Transform overdue payments into readable reminders
                for (Payment payment : overduePayments) {
                    String tenantName = payment.getTenant() != null ? 
                            payment.getTenant().getFirstName() + " " + payment.getTenant().getLastName() : 
                            "Unknown";
                    String roomNumber = payment.getRoom() != null ? 
                            payment.getRoom().getRoomNumber() : 
                            "Unknown";
                    
                    reminders.add("Payment overdue: " + tenantName + " (Room " + roomNumber + ")");
                }
                
                // If no overdue payments found, add a placeholder message
                if (reminders.isEmpty()) {
                    reminders.add("No overdue payments");
                }
                
                // Get recent activities - ideally from an audit log
                // This could be enhanced with an actual AuditLogDAO
                List<String> activities = new ArrayList<>();
                List<Payment> recentPayments = paymentDAO.findRecentPayments(5); // Get 5 most recent payments
                
                // Transform recent payments into readable activities
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
                for (Payment payment : recentPayments) {
                    String date = payment.getPaymentDate().format(formatter);
                    String tenantName = payment.getTenant() != null ? 
                            payment.getTenant().getFirstName() + " " + payment.getTenant().getLastName() : 
                            "Unknown";
                    
                    activities.add(date + ": Payment received from " + tenantName + 
                            " (" + NumberFormat.getCurrencyInstance(new Locale("en", "PH")).format(payment.getAmount()) + ")");
                }
                
                // If no recent payments found, add a placeholder message
                if (activities.isEmpty()) {
                    activities.add("No recent payment activities");
                }

                return new DashboardData(tenantCount, occupiedCount, totalRooms, revenue, reminders, activities, newTenantsThisMonth);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get(); // Get results from doInBackground
                    // --- Update UI on the EDT ---
                    String tenantChangeText = "+" + data.newTenantsThisMonth + " this month";
                    dashboardPanel.setTotalTenants(data.tenantCount, tenantChangeText);

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
        final int newTenantsThisMonth;

        DashboardData(int tenantCount, long occupiedCount, int totalRooms, BigDecimal monthlyRevenue, List<String> reminders, List<String> activities, int newTenantsThisMonth) {
            this.tenantCount = tenantCount;
            this.occupiedCount = occupiedCount;
            this.totalRooms = totalRooms;
            this.monthlyRevenue = monthlyRevenue;
            this.reminders = reminders;
            this.activities = activities;
            this.newTenantsThisMonth = newTenantsThisMonth;
        }
    }

    /**
     * Shows the send reminder dialog and initializes the controller if needed
     */
    private void showSendReminderDialog() {
        // Create the dialog and controller if they don't exist
        if (reminderDialog == null) {
            LOGGER.info("Creating new SendReminderDialog and ReminderController");
            reminderDialog = new SendReminderDialog(mainView, true);
            reminderController = new ReminderController(reminderDialog, tenantDAO, mainView);
        }
        
        // Show the dialog
        reminderController.showSendReminderDialog();
    }
}