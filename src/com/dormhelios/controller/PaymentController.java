package com.dormhelios.controller;

import com.dormhelios.model.dao.*;
import com.dormhelios.model.entity.*;
import com.dormhelios.util.QRCodeGenerator; // Assuming this utility exists
import com.dormhelios.view.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException; // For printing
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList; // Import ArrayList
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale; // Add import for Locale
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for managing the PaymentListView, PaymentLoggingDialog, and
 * ReceiptDialog. Handles displaying payments, logging new ones, viewing
 * receipts, searching, and sorting.
 */
public class PaymentController {

    private static final Logger LOGGER = Logger.getLogger(PaymentController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

    private final PaymentListView paymentListView;
    private final PaymentDAO paymentDAO;
    private final TenantDAO tenantDAO;
    private final RoomDAO roomDAO;
    private final UserDAO userDAO; // Needed for receipt's "Logged By"
    private final User loggedInUser;
    private final MainDashboardView mainView;

    private PaymentLoggingDialog paymentLoggingDialog;
    private ReceiptDialog receiptDialog;

    // Store the currently loaded list AND associated data for display/filtering
    // Using a simple inner class or record for DTO (Data Transfer Object)
    private record PaymentDisplayData(Payment payment, String tenantName, String roomNumber) {

    }
    private List<PaymentDisplayData> currentPaymentDisplayList = Collections.emptyList();

    public PaymentController(PaymentListView paymentListView, PaymentDAO paymentDAO,
            TenantDAO tenantDAO, RoomDAO roomDAO, UserDAO userDAO, User loggedInUser,
            MainDashboardView mainView) {
        this.paymentListView = paymentListView;
        this.paymentDAO = paymentDAO;
        this.tenantDAO = tenantDAO;
        this.roomDAO = roomDAO;
        this.userDAO = userDAO; // Store UserDAO
        this.loggedInUser = loggedInUser;
        this.mainView = mainView;

        attachListeners();
        
        // Load payment data immediately when controller is initialized
        loadPaymentData();
    }

    /**
     * Attaches listeners to the components within PaymentListView.
     */
    private void attachListeners() {
        paymentListView.addNewPaymentButtonListener(e -> openLogPaymentDialog());
        paymentListView.addViewButtonListener(e -> viewSelectedPaymentReceipt());
        paymentListView.addEditButtonListener(e -> editSelectedPayment());
        paymentListView.addDeleteButtonListener(e -> deleteSelectedPayment());

        // Listener for live search
        paymentListView.addSearchFieldListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterDisplayedPayments();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterDisplayedPayments();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterDisplayedPayments();
            }
        });

        // Listener for sorting/filter changes (triggers data reload)
        paymentListView.addFilterComboBoxListener(e -> loadPaymentData()); // Reload data based on filter

        // Listener for table clicks (e.g., clicking the "View" link in the Receipt column)
        paymentListView.addTableMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable table = paymentListView.getPaymentTable();
                int column = table.getColumnModel().getColumnIndex("Receipt");
                int row = table.rowAtPoint(e.getPoint());

                if (row >= 0 && table.columnAtPoint(e.getPoint()) == column) {
                    LOGGER.info("Receipt 'View' clicked at view row: " + row);
                    viewPaymentReceiptFromTable(row); // Pass view row index
                }
            }
        });
    }

    /**
     * Loads payment data asynchronously based on the selected filter/sort order
     * and updates the view. Fetches related data for display.
     */
    public void loadPaymentData() {
        String filter = paymentListView.getSelectedFilter(); // "All Payments", "Most Recent First", "Oldest First"
        LOGGER.log(Level.INFO, "Loading payment data with filter: " + filter);

        // Show loading state (optional)
        // paymentListView.showLoadingState(true);
        SwingWorker<List<PaymentDisplayData>, Void> worker = new SwingWorker<List<PaymentDisplayData>, Void>() {
            @Override
            protected List<PaymentDisplayData> doInBackground() throws Exception {
                // Fetch raw payments based on desired DB order
                List<Payment> payments;
                switch (filter) {
                    case "Oldest First":
                        // TODO: Add findAndSortByDate(ASC) to PaymentDAO or sort here
                        payments = paymentDAO.findAll(); // Fetch all
                        LOGGER.log(Level.INFO, "Retrieved {0} payments from DAO findAll() method", payments.size());
                        payments.sort(Comparator.comparing(Payment::getPaymentDate)); // Sort ascending
                        break;
                    case "Most Recent First":
                    case "All Payments": // Assuming default DAO fetch is recent first
                    default:
                        // TODO: Add findAndSortByDate(DESC) to PaymentDAO or ensure default is DESC
                        payments = paymentDAO.findAll(); // Fetch all (assuming default sort is recent first)
                        LOGGER.log(Level.INFO, "Retrieved {0} payments from DAO findAll() method", payments.size());
                        break;
                }

                // Enrich data for display (fetch related names/numbers)
                // This is N+1 query problem if done naively. Better ways exist (Map lookup, JOIN in DAO).
                // Simple Map lookup example:
                // Map<Integer, Tenant> tenantMap = tenantDAO.findAll().stream().collect(Collectors.toMap(Tenant::getTenantId, t -> t));
                // Map<Integer, Room> roomMap = roomDAO.findAll().stream().collect(Collectors.toMap(Room::getRoomId, r -> r));
                List<PaymentDisplayData> displayData = new ArrayList<>();
                for (Payment p : payments) {
                    // Inefficient N+1 lookups - replace with better strategy
                    String tName = tenantDAO.findById(p.getTenantId())
                            .map(t -> t.getLastName() + ", " + t.getFirstName())
                            .orElse("Unknown Tenant");
                    String rNum = tenantDAO.findById(p.getTenantId())
                            .flatMap(t -> t.getRoomId() != null ? roomDAO.findById(t.getRoomId()) : Optional.empty())
                            .map(Room::getRoomNumber)
                            .orElse("N/A");
                    displayData.add(new PaymentDisplayData(p, tName, rNum));
                }
                LOGGER.log(Level.INFO, "Enriched {0} payment records with tenant and room data", displayData.size());
                return displayData;
            }

            @Override
            protected void done() {
                try {
                    currentPaymentDisplayList = get(); // Store the loaded & enriched list
                    LOGGER.log(Level.INFO, "Worker completed. Retrieved {0} enriched payment records", currentPaymentDisplayList.size());

                    // Create maps for tenant names and room numbers
                    java.util.Map<Integer, String> tenantNames = new java.util.HashMap<>();
                    java.util.Map<Integer, String> roomNumbers = new java.util.HashMap<>();
                    
                    // Extract Payment objects and prepare maps
                    List<Payment> payments = new ArrayList<>();
                    for (PaymentDisplayData data : currentPaymentDisplayList) {
                        payments.add(data.payment());
                        tenantNames.put(data.payment().getTenantId(), data.tenantName());
                        roomNumbers.put(data.payment().getTenantId(), data.roomNumber());
                    }

                    LOGGER.log(Level.INFO, "Preparing to update view with {0} payments", payments.size());
                    // Pass all information to the view
                    paymentListView.displayPayments(payments, tenantNames, roomNumbers);
                    LOGGER.log(Level.INFO, "View update method called with {0} payments", payments.size());
                    
                    filterDisplayedPayments(); // Apply search filter to newly loaded data
                    LOGGER.log(Level.INFO, "Payment data loaded and view updated.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING, "Payment data loading interrupted", e);
                } catch (ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Error loading payment data", e.getCause());
                    paymentListView.displayErrorMessage("Error loading payment data: " + e.getCause().getMessage());
                } finally {
                    // paymentListView.showLoadingState(false); // Hide loading state
                }
            }
        };
        worker.execute();
    }

    /**
     * Applies the text search filter to the currently displayed data in the
     * table.
     */
    private void filterDisplayedPayments() {
        paymentListView.filterTableBySearch();
    }

    /**
     * Opens the PaymentLoggingDialog for adding a new payment.
     */
    private void openLogPaymentDialog() {
        LOGGER.info("Opening Log New Payment dialog.");
        if (paymentLoggingDialog == null) {
            paymentLoggingDialog = new PaymentLoggingDialog(mainView, true);
            paymentLoggingDialog.addSaveButtonListener(e -> saveNewPayment());
            paymentLoggingDialog.addCancelButtonListener(e -> paymentLoggingDialog.closeDialog());
            // Add OCR listener here if it were implemented
        }

        // Fetch tenants and rooms for populating combo boxes
        try {
            // Simple synchronous fetch for now
            List<Tenant> tenants = tenantDAO.findAll(); // Fetch active tenants ideally
            List<Room> rooms = roomDAO.findAll();
            
            // Populate the combo boxes
            paymentLoggingDialog.setTenantComboBoxModel(tenants);
            paymentLoggingDialog.setRoomComboBoxModel(rooms);
            
            // Setup the room-tenant linking for interactive filtering
            paymentLoggingDialog.setupRoomTenantLinking(tenants);
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load data for payment dialog", ex);
            paymentLoggingDialog.displayErrorMessage("Error loading room and tenant data.");
        }

        paymentLoggingDialog.showDialog();

        if (paymentLoggingDialog.isSaved()) {
            loadPaymentData(); // Reload data to show the new payment
        }
    }

    /**
     * Handles saving a new payment from the PaymentLoggingDialog.
     */
    private void saveNewPayment() {
        Payment paymentData = paymentLoggingDialog.getPaymentData();
        if (paymentData != null) {
            paymentData.setUserId(loggedInUser.getUserId());

            // Generate QR Code Data String (Needs related data)
            String qrData = generateQrCodeString(paymentData); // Pass payment object
            paymentData.setQrCodeData(qrData);

            SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return paymentDAO.addPayment(paymentData);
                }

                @Override
                protected void done() {
                    try {
                        int newId = get();
                        if (newId > 0) {
                            LOGGER.info("New payment saved successfully with ID: " + newId);
                            paymentLoggingDialog.setSaved(true);
                            paymentLoggingDialog.closeDialog();
                            paymentData.setPaymentId(newId); // Set ID for receipt view
                            viewReceipt(paymentData); // Show receipt immediately
                        } else {
                            LOGGER.warning("Failed to save new payment (DAO returned <= 0).");
                            paymentLoggingDialog.displayErrorMessage("Failed to save payment.");
                        }
                    } catch (Exception e) {
                        handleWorkerException("saving new payment", e);
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Overloaded helper method that loads tenant and room data before showing receipt.
     * Used when we only have a Payment object but need related data for the receipt.
     */
    private void viewReceipt(Payment payment) {
        // Fetch related data for the payment
        Optional<Tenant> tenantOpt = tenantDAO.findById(payment.getTenantId());
        Optional<Room> roomOpt = tenantOpt.flatMap(t -> t.getRoomId() != null ? 
                roomDAO.findById(t.getRoomId()) : Optional.empty());
        
        // Call the main viewReceipt method with the fetched data
        viewReceipt(payment, tenantOpt.orElse(null), roomOpt.orElse(null));
    }

    /**
     * Generates the string data to be encoded in the QR code. Fetches necessary
     * related data.
     *
     * @param payment The Payment object (ID might be 0 if called before
     * saving).
     * @return A string containing key payment details.
     */
    private String generateQrCodeString(Payment payment) {
        // Fetch related data needed for QR content
        String tenantName = tenantDAO.findById(payment.getTenantId())
                .map(t -> t.getFirstName() + " " + t.getLastName())
                .orElse("Unknown");
        String roomNumber = tenantDAO.findById(payment.getTenantId())
                .flatMap(t -> t.getRoomId() != null ? roomDAO.findById(t.getRoomId()) : Optional.empty())
                .map(Room::getRoomNumber)
                .orElse("N/A");

        // Format: Key-value pairs or simple delimited string
        // Using simplified format matching the simplified receipt
        return String.format(
                "Tenant:%s\nRoom:%s\nDate:%s\nAmount:%.2f\nPeriod:%s",
                tenantName,
                roomNumber,
                payment.getPaymentDate().format(DATE_FORMATTER),
                payment.getAmount(),
                formatPeriodCovered(payment.getPeriodCoveredStart(), payment.getPeriodCoveredEnd()) // Use helper
        );
    }

    /**
     * Helper method to format the period covered display.
     */
    private String formatPeriodCovered(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return "N/A";
        }
        if (start.getDayOfMonth() == 1 && end.equals(start.withDayOfMonth(start.lengthOfMonth()))) {
            return start.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));
        }
        return start.format(DATE_FORMATTER) + " - " + end.format(DATE_FORMATTER);
    }

    /**
     * Opens the ReceiptDialog for the payment corresponding to the selected
     * table row.
     */
    private void viewSelectedPaymentReceipt() {
        int selectedPaymentId = paymentListView.getSelectedPaymentId();
        if (selectedPaymentId < 0) {
            paymentListView.displayErrorMessage("Please select a payment record to view.");
            return;
        }
        viewPaymentById(selectedPaymentId);
    }

    /**
     * Opens the ReceiptDialog for the payment at a specific VIEW row index.
     *
     * @param viewRowIndex The row index in the JTable's current view.
     */
    private void viewPaymentReceiptFromTable(int viewRowIndex) {
        if (viewRowIndex >= 0) {
            int modelRow = paymentListView.getPaymentTable().convertRowIndexToModel(viewRowIndex);
            int paymentId = (Integer) paymentListView.getPaymentTable().getModel().getValueAt(modelRow, 0);
            viewPaymentById(paymentId);
        }
    }

    /**
     * Fetches payment details by ID and displays the ReceiptDialog.
     *
     * @param paymentId The ID of the payment to view.
     */
    private void viewPaymentById(int paymentId) {
        LOGGER.info("Viewing receipt for Payment ID: " + paymentId);
        // Use SwingWorker if fetching related data might be slow
        SwingWorker<ReceiptData, Void> worker = new SwingWorker<ReceiptData, Void>() {
            @Override
            protected ReceiptData doInBackground() throws Exception {
                Optional<Payment> paymentOpt = paymentDAO.findById(paymentId);
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    Optional<Tenant> tenantOpt = tenantDAO.findById(payment.getTenantId());
                    Optional<Room> roomOpt = tenantOpt.flatMap(t -> t.getRoomId() != null ? roomDAO.findById(t.getRoomId()) : Optional.empty());
                    // Optional: Fetch user who logged it if needed on receipt
                    // Optional<User> loggerOpt = userDAO.findById(payment.getUserId());
                    return new ReceiptData(payment, tenantOpt.orElse(null), roomOpt.orElse(null));
                }
                return null; // Payment not found
            }

            @Override
            protected void done() {
                try {
                    ReceiptData data = get();
                    if (data != null) {
                        viewReceipt(data.payment, data.tenant, data.room); // Call helper to show dialog
                    } else {
                        LOGGER.log(Level.WARNING, "Payment ID {0} not found for viewing receipt.", paymentId);
                        paymentListView.displayErrorMessage("Could not find details for the selected payment.");
                    }
                } catch (Exception e) {
                    handleWorkerException("viewing receipt", e);
                }
            }
        };
        worker.execute();
    }

    /**
     * Helper method to actually create and show the ReceiptDialog.
     */
    private void viewReceipt(Payment payment, Tenant tenant, Room room) {
        if (receiptDialog == null) {
            receiptDialog = new ReceiptDialog(mainView, true);
            receiptDialog.addDoneButtonListener(e -> receiptDialog.closeDialog());
            receiptDialog.addPrintButtonListener(e -> printReceipt());
            // Add download listener if implemented
        }
        receiptDialog.displayReceiptDetails(payment, tenant, room); // Use simplified method
        receiptDialog.showDialog();
    }

    /**
     * Placeholder for editing a payment.
     */
    private void editSelectedPayment() {
        // ... (Implementation remains the same - likely disabled or complex) ...
        paymentListView.displayErrorMessage("Editing payment records is not currently supported.");
    }

    /**
     * Placeholder for deleting a payment.
     */
    private void deleteSelectedPayment() {
        // ... (Implementation remains the same - likely disabled or complex) ...
        paymentListView.displayErrorMessage("Deleting payment records is not currently enabled.");
    }

    /**
     * Handles printing the receipt content.
     */
    private void printReceipt() {
        if (receiptDialog == null || !receiptDialog.isShowing()) {
            LOGGER.warning("Attempted to print receipt, but dialog is not available.");
            return;
        }
        
        PrinterJob job = PrinterJob.getPrinterJob();
        
        // Get the panel to print from the dialog
        JPanel contentPanel = receiptDialog.getPrintableComponent();
        
        // Create a Printable object directly from the panel
        java.awt.print.Printable printable = new java.awt.print.Printable() {
            @Override
            public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) 
                    throws PrinterException {
                if (pageIndex > 0) {
                    return java.awt.print.Printable.NO_SUCH_PAGE;
                }
                
                // Calculate scale to fit the panel to the page
                double scale = Math.min(
                    pageFormat.getImageableWidth() / contentPanel.getWidth(),
                    pageFormat.getImageableHeight() / contentPanel.getHeight()
                );
                
                // Create a scaled graphics context
                java.awt.Graphics2D g2d = (java.awt.Graphics2D)graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2d.scale(scale, scale);
                
                // Print the panel
                contentPanel.print(g2d);
                
                return java.awt.print.Printable.PAGE_EXISTS;
            }
        };
        
        job.setPrintable(printable);

        if (job.printDialog()) {
            try {
                LOGGER.info("Sending receipt to printer...");
                job.print();
                LOGGER.info("Print job sent.");
            } catch (PrinterException ex) {
                LOGGER.log(Level.SEVERE, "Error printing receipt", ex);
                JOptionPane.showMessageDialog(receiptDialog, "Could not print receipt: " + ex.getMessage(), 
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            LOGGER.info("Print job cancelled by user.");
        }
    }

    // --- Helper for Handling SwingWorker Exceptions ---
    private void handleWorkerException(String action, Exception e) {
        Throwable cause = (e instanceof ExecutionException) ? e.getCause() : e;
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Action '" + action + "' interrupted", cause);
        } else {
            LOGGER.log(Level.SEVERE, "Error during action '" + action + "'", cause);
            // Show generic error to user
            JOptionPane.showMessageDialog(mainView, "An unexpected error occurred while " + action + ".\nPlease check logs or try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Helper Data Class for SwingWorker (Receipt Data) ---
    private static record ReceiptData(Payment payment, Tenant tenant, Room room) {

    }

}
