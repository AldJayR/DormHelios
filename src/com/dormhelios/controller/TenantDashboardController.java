package com.dormhelios.controller;

import com.dormhelios.model.dao.PaymentDAO;
import com.dormhelios.model.dao.RoomDAO;
import com.dormhelios.model.dao.TenantDAO;
import com.dormhelios.model.dao.UserDAO;
import com.dormhelios.model.entity.Payment;
import com.dormhelios.model.entity.Room;
import com.dormhelios.model.entity.Tenant;
import com.dormhelios.model.entity.User;
import com.dormhelios.view.TenantDashboardView;
import com.dormhelios.util.QRCodeGenerator; // Import QR code generator utility

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/**
 * Controller class for the tenant dashboard.
 * Manages interaction between the tenant dashboard view and model.
 */
public class TenantDashboardController {
    private static final Logger LOGGER = Logger.getLogger(TenantDashboardController.class.getName());
    
    private final TenantDashboardView view;
    private final TenantDAO tenantDAO;
    private final PaymentDAO paymentDAO;
    private final RoomDAO roomDAO;
    private final UserDAO userDAO;
    private final User currentUser;
    
    // Tenant and room information
    private Tenant tenant;
    private Room room;
    
    // Standard rules for all tenants - could be moved to a configuration file
    private static final String DEFAULT_RULES = "<html>"
            + "1. Keep noise to a minimum after 10 PM<br/>"
            + "2. Maintain cleanliness in common areas<br/>"
            + "3. Dispose of trash properly<br/>"
            + "4. Report maintenance issues promptly<br/>"
            + "5. Pay rent on time (before the 5th of each month)<br/>"
            + "6. No unauthorized guests for extended periods<br/>"
            + "</html>";
    
    /**
     * Constructs a TenantDashboardController.
     * 
     * @param view The tenant dashboard view
     * @param tenantDAO Data access object for tenant information
     * @param paymentDAO Data access object for payment information
     * @param roomDAO Data access object for room information
     * @param userDAO Data access object for user information
     * @param currentUser The currently logged-in user
     */
    public TenantDashboardController(TenantDashboardView view, TenantDAO tenantDAO, 
            PaymentDAO paymentDAO, RoomDAO roomDAO, UserDAO userDAO, User currentUser) {
        this.view = view;
        this.tenantDAO = tenantDAO;
        this.paymentDAO = paymentDAO;
        this.roomDAO = roomDAO;
        this.userDAO = userDAO;
        this.currentUser = currentUser;
        
        // Initialize the dashboard
        initializeDashboard();
    }
    
    /**
     * Initializes the dashboard with tenant data.
     * Sets up all UI components with data from the DAOs.
     */
    private void initializeDashboard() {
        // Find tenant by user ID
        findTenantByUserId();
        
        if (tenant != null) {
            // Set tenant name
            view.getTenantDashboardPanel().setTenantName(tenant.getFirstName() + " " + tenant.getLastName());
            
            // Load room information if assigned
            if (tenant.getRoomId() != null) {
                loadRoomInformation();
            } else {
                displayNoRoomAssigned();
            }
            
            // Load payment history
            loadPaymentHistory();
            
            // Generate and set announcements
            generateAnnouncements();
        } else {
            handleTenantNotFound();
        }
    }
    
    /**
     * Finds the tenant associated with the current user.
     */
    private void findTenantByUserId() {
        try {
            // Tenant might have a user account that matches the logged-in user
            List<Tenant> allTenants = tenantDAO.findAll();
            for (Tenant t : allTenants) {
                if (t.getUserId() != null && t.getUserId().equals(currentUser.getUserId())) {
                    tenant = t;
                    LOGGER.log(Level.INFO, "Found tenant with ID: {0} for user: {1}", 
                            new Object[]{tenant.getTenantId(), currentUser.getUserId()});
                    return;
                }
            }
            LOGGER.log(Level.WARNING, "No tenant found for user ID: {0}", currentUser.getUserId());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding tenant for user: " + currentUser.getUserId(), e);
        }
    }
    
    /**
     * Loads room information for the tenant.
     */
    private void loadRoomInformation() {
        try {
            Optional<Room> roomOpt = roomDAO.findById(tenant.getRoomId());
            if (roomOpt.isPresent()) {
                room = roomOpt.get();
                
                // Count how many tenants are in this room
                int tenantCount = countTenantsInRoom(room.getRoomId());
                
                // Calculate per-tenant rate by dividing the monthly rate by tenant count
                BigDecimal perTenantRate = room.getMonthlyRate();
                if (tenantCount > 0) {
                    perTenantRate = room.getMonthlyRate().divide(new BigDecimal(tenantCount), 2, BigDecimal.ROUND_HALF_UP);
                }
                
                // Format the monthly rate as currency
                NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
                String formattedRate = currencyFormatter.format(perTenantRate);
                
                // Set room information in the view
                view.getTenantDashboardPanel().setRoomInformation(
                        room.getRoomNumber(),
                        formattedRate,
                        String.valueOf(room.getSlotsAvailable()),
                        DEFAULT_RULES
                );
                
                // Set landlord information (using admin user info as landlord)
                setLandlordInformation();
            } else {
                LOGGER.log(Level.WARNING, "Room not found for ID: {0}", tenant.getRoomId());
                displayNoRoomAssigned();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading room information for tenant: " + tenant.getTenantId(), e);
            displayNoRoomAssigned();
        }
    }
    
    /**
     * Counts the number of tenants in a given room.
     * 
     * @param roomId The ID of the room
     * @return The number of tenants in the room
     */
    private int countTenantsInRoom(Integer roomId) {
        try {
            List<Tenant> tenants = tenantDAO.findAll();
            int count = 0;
            for (Tenant t : tenants) {
                if (roomId != null && roomId.equals(t.getRoomId())) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting tenants in room ID: " + roomId, e);
            return 0;
        }
    }
    
    /**
     * Sets landlord information in the view.
     * This implementation pulls landlord information from users with the LANDLORD role.
     */
    private void setLandlordInformation() {
        try {
            // Find a landlord user
            List<User> users = userDAO.findAll();
            User landlord = null;
            
            for (User user : users) {
                if (user.getRole() == User.Role.LANDLORD) {
                    landlord = user;
                    break;
                }
            }
            
            if (landlord != null) {
                // Format the landlord name
                String landlordName = "";
                if (landlord.getFirstName() != null && !landlord.getFirstName().isEmpty()) {
                    landlordName = landlord.getFirstName();
                    if (landlord.getSurname() != null && !landlord.getSurname().isEmpty()) {
                        landlordName += " " + landlord.getSurname();
                    }
                } else {
                    landlordName = landlord.getUsername();
                }
                
                view.getTenantDashboardPanel().setLandlordInformation(
                        landlordName,
                        landlord.getPhoneNumber() != null && !landlord.getPhoneNumber().isEmpty() 
                            ? landlord.getPhoneNumber() 
                            : "No contact number available"
                );
                LOGGER.log(Level.INFO, "Landlord information set: {0}", landlordName);
            } else {
                // If no landlord is found, look for an admin user as fallback
                for (User user : users) {
                    if (user.getRole() == User.Role.ADMIN) {
                        landlord = user;
                        break;
                    }
                }
                
                if (landlord != null) {
                    String landlordName = "";
                    if (landlord.getFirstName() != null && !landlord.getFirstName().isEmpty()) {
                        landlordName = landlord.getFirstName();
                        if (landlord.getSurname() != null && !landlord.getSurname().isEmpty()) {
                            landlordName += " " + landlord.getSurname();
                        }
                    } else {
                        landlordName = landlord.getUsername();
                    }
                    
                    view.getTenantDashboardPanel().setLandlordInformation(
                            landlordName + " (Administrator)",
                            landlord.getPhoneNumber() != null && !landlord.getPhoneNumber().isEmpty() 
                                ? landlord.getPhoneNumber() 
                                : "No contact number available"
                    );
                    LOGGER.log(Level.INFO, "Administrator information set as fallback: {0}", landlordName);
                } else {
                    view.getTenantDashboardPanel().setLandlordInformation(
                            "Property Manager",
                            "Contact the front desk"
                    );
                    LOGGER.log(Level.WARNING, "No landlord or admin user found");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting landlord information", e);
            view.getTenantDashboardPanel().setLandlordInformation(
                    "Error retrieving landlord info",
                    "Please contact administration"
            );
        }
    }
    
    /**
     * Displays a message when no room is assigned to the tenant.
     */
    private void displayNoRoomAssigned() {
        view.getTenantDashboardPanel().setRoomInformation(
                "Not assigned",
                "N/A",
                "N/A",
                DEFAULT_RULES
        );
        view.getTenantDashboardPanel().setLandlordInformation(
                "Not available",
                "Not available"
        );
    }
    
    /**
     * Loads payment history for the tenant.
     */
    private void loadPaymentHistory() {
        try {
            // Get payments for the tenant
            List<Payment> payments = paymentDAO.findByTenantId(tenant.getTenantId());
            
            // Create table model for payments
            String[] columnNames = {"Date Paid", "Amount", "Month Covered", "Method", "Show QR"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make table read-only
                }
                
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    // Make the last column ("Show QR") display as a button-like cell
                    if (columnIndex == 4) {
                        return String.class;
                    }
                    return super.getColumnClass(columnIndex);
                }
            };
            
            // Date formatter
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            
            // Add payment data to the table model
            for (Payment payment : payments) {
                String datePaid = payment.getPaymentDate().format(dateFormatter);
                String amount = currencyFormatter.format(payment.getAmount());
                
                String periodCovered = "N/A";
                if (payment.getPeriodCoveredStart() != null) {
                    periodCovered = payment.getPeriodCoveredStart().format(DateTimeFormatter.ofPattern("MMM yyyy"));
                    if (payment.getPeriodCoveredEnd() != null) {
                        periodCovered += " - " + payment.getPeriodCoveredEnd().format(DateTimeFormatter.ofPattern("MMM yyyy"));
                    }
                }
                
                String method = payment.getPaymentMethod().toString();
                String viewQRButton = payment.getQrCodeData() != null ? "View QR" : "N/A";
                
                Object[] rowData = {datePaid, amount, periodCovered, method, viewQRButton};
                tableModel.addRow(rowData);
            }
            
            // Set the table model
            view.getTenantDashboardPanel().setPaymentHistoryTableModel(tableModel);
            
            // Add mouse listener to handle "View QR" button clicks
            // This is the correct way to add the MouseAdapter - specify the column index (4)
            view.getTenantDashboardPanel().addTableColumnActionListener(4, new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JTable table = view.getTenantDashboardPanel().getPaymentHistoryTable();
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    
                    // Check if the click is on the "View QR" column and the cell contains "View QR" (not "N/A")
                    if (col == 4 && row >= 0 && "View QR".equals(table.getValueAt(row, col))) {
                        // Get the payment for this row
                        Payment payment = null;
                        if (row < payments.size()) {
                            payment = payments.get(row);
                        }
                        
                        if (payment != null && payment.getQrCodeData() != null) {
                            // Display QR code in a dialog
                            displayQRCodeDialog(payment);
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading payment history for tenant: " + tenant.getTenantId(), e);
            
            // Create an empty table model
            String[] columnNames = {"Date Paid", "Amount", "Month Covered", "Method", "Show QR"};
            DefaultTableModel emptyModel = new DefaultTableModel(columnNames, 0);
            view.getTenantDashboardPanel().setPaymentHistoryTableModel(emptyModel);
        }
    }
    
    /**
     * Displays a dialog with the QR code for the payment
     * 
     * @param payment The payment containing QR code data
     */
    private void displayQRCodeDialog(Payment payment) {
        try {
            // Generate QR code image from payment data
            java.awt.image.BufferedImage qrImage = QRCodeGenerator.generateQRCodeImage(payment.getQrCodeData(), 250, 250);
            
            // Create an ImageIcon and scale it to fit dialog
            ImageIcon qrIcon = new ImageIcon(qrImage);
            
            // Format payment details for display
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            String formattedAmount = currencyFormatter.format(payment.getAmount());
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            String formattedDate = payment.getPaymentDate().format(dateFormatter);
            
            // Create a panel to hold both the QR image and payment details
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            
            // Add payment details
            JLabel titleLabel = new JLabel("Payment QR Code", JLabel.CENTER);
            titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
            titleLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            
            JLabel amountLabel = new JLabel("Amount: " + formattedAmount, JLabel.CENTER);
            amountLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            
            JLabel dateLabel = new JLabel("Paid on: " + formattedDate, JLabel.CENTER);
            dateLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            
            JLabel imageLabel = new JLabel(qrIcon);
            imageLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            
            panel.add(Box.createVerticalStrut(10));
            panel.add(titleLabel);
            panel.add(Box.createVerticalStrut(10));
            panel.add(amountLabel);
            panel.add(dateLabel);
            panel.add(Box.createVerticalStrut(15));
            panel.add(imageLabel);
            panel.add(Box.createVerticalStrut(10));
            
            // Show dialog with QR code
            JOptionPane.showMessageDialog(view, panel, "Payment QR Code", JOptionPane.PLAIN_MESSAGE);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error displaying QR code", e);
            JOptionPane.showMessageDialog(view, 
                "Could not display QR code. The data may be corrupted.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generates announcements for the tenant based on payment history and lease information.
     * This replaces the need for a separate AnnouncementDAO.
     */
    private void generateAnnouncements() {
        StringBuilder announcements = new StringBuilder();
        LocalDate today = LocalDate.now();
        
        try {
            // Check for rent due
            boolean rentDue = isRentDue();
            if (rentDue) {
                announcements.append("📢 RENT PAYMENT DUE\n");
                announcements.append("Your rent payment for this month is due. Please make your payment before the 5th to avoid late fees.\n\n");
            }
            
            // Check lease expiration
            if (tenant.getLeaseEndDate() != null) {
                Period period = Period.between(today, tenant.getLeaseEndDate());
                int daysUntilLeaseExpiration = period.getDays() + (period.getMonths() * 30) + (period.getYears() * 365);
                
                if (daysUntilLeaseExpiration <= 30 && daysUntilLeaseExpiration > 0) {
                    announcements.append("📢 LEASE EXPIRING SOON\n");
                    announcements.append("Your lease agreement will expire in " + daysUntilLeaseExpiration + " days. ");
                    announcements.append("Please contact the landlord to discuss renewal options.\n\n");
                } else if (daysUntilLeaseExpiration <= 0) {
                    announcements.append("📢 LEASE EXPIRED\n");
                    announcements.append("Your lease agreement has expired. ");
                    announcements.append("Please contact the landlord immediately to discuss your options.\n\n");
                }
            }
            
            // Check if there are any recent maintenance updates
            // This would normally come from a maintenance request system
            // For now, we'll just add a generic message
            announcements.append("📢 MAINTENANCE UPDATE\n");
            announcements.append("Regular cleaning of common areas is scheduled for every Saturday morning. ");
            announcements.append("Please keep your personal belongings tidy.\n\n");
            
            // Add upcoming events or general information
            announcements.append("📢 REMINDERS\n");
            announcements.append("• Keep noise to a minimum after 10 PM\n");
            announcements.append("• Report any maintenance issues promptly\n");
            announcements.append("• Garbage collection is on Monday, Wednesday, and Friday\n");
            
            // Set the announcements in the view
            view.getTenantDashboardPanel().setAnnouncements(announcements.toString());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating announcements for tenant: " + tenant.getTenantId(), e);
            view.getTenantDashboardPanel().setAnnouncements("Error loading announcements. Please refresh the dashboard.");
        }
    }
    
    /**
     * Checks if rent is due based on payment history.
     * 
     * @return true if rent is due, false otherwise
     */
    private boolean isRentDue() {
        try {
            if (tenant == null) {
                return false;
            }
            
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfCurrentMonth = today.withDayOfMonth(1);
            
            // Get payments for current month
            List<Payment> payments = paymentDAO.findByTenantId(tenant.getTenantId());
            for (Payment payment : payments) {
                // Check if payment covers current month
                if (payment.getPeriodCoveredStart() != null) {
                    LocalDate startPeriod = payment.getPeriodCoveredStart();
                    LocalDate endPeriod = payment.getPeriodCoveredEnd() != null ? 
                            payment.getPeriodCoveredEnd() : startPeriod.plusMonths(1).minusDays(1);
                    
                    // If payment period covers current month, rent is not due
                    if ((startPeriod.isBefore(firstDayOfCurrentMonth) || startPeriod.isEqual(firstDayOfCurrentMonth)) && 
                            (endPeriod.isAfter(today) || endPeriod.isEqual(today))) {
                        return false;
                    }
                }
                
                // If payment was made in current month, rent is not due
                if (payment.getPaymentDate().getMonth() == today.getMonth() && 
                        payment.getPaymentDate().getYear() == today.getYear()) {
                    return false;
                }
            }
            
            // If no payments cover current month, rent is due
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if rent is due for tenant: " + tenant.getTenantId(), e);
            return false;
        }
    }
    
    /**
     * Handles the case when no tenant is found for the current user.
     */
    private void handleTenantNotFound() {
        LOGGER.log(Level.WARNING, "No tenant found for user ID: {0}", currentUser.getUserId());
        
        // Set default values in the view
        view.getTenantDashboardPanel().setTenantName(currentUser.getFirstName() != null ? 
                currentUser.getFirstName() : currentUser.getUsername());
        
        view.getTenantDashboardPanel().setRoomInformation(
                "Not assigned",
                "N/A",
                "N/A",
                DEFAULT_RULES
        );
        
        view.getTenantDashboardPanel().setLandlordInformation(
                "Not available",
                "Not available"
        );
        
        // Create an empty table model
        String[] columnNames = {"Date Paid", "Amount", "Month Covered", "Method", "Show QR"};
        DefaultTableModel emptyModel = new DefaultTableModel(columnNames, 0);
        view.getTenantDashboardPanel().setPaymentHistoryTableModel(emptyModel);
        
        // Set error message in announcements
        view.getTenantDashboardPanel().setAnnouncements(
                "⚠️ ERROR: Your tenant information could not be loaded.\n\n" +
                "This could be because your user account is not yet linked to a tenant record. " +
                "Please contact the administrator to resolve this issue."
        );
    }
}