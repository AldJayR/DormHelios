/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.dormhelios.view;

import com.dormhelios.model.entity.Payment;
import com.dormhelios.model.entity.Room; // Still potentially needed for Room Number
import com.dormhelios.model.entity.Tenant; // Still needed for Tenant Name
import com.dormhelios.util.QRCodeGenerator; // Assuming this utility exists

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage; // For QR code
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.net.URI;

public class ReceiptDialog extends javax.swing.JDialog {

    /**
     * Creates new form ReceiptDialog
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    private static final Logger LOGGER = Logger.getLogger(ReceiptDialog.class.getName());

    public ReceiptDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    private void setupComponents() {
        setLocationRelativeTo(getParent()); // Center relative to parent
        qrCodeLabel.setText(""); // Ensure label is empty initially
        qrCodeLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the QR code image
    }

    public void displayReceiptDetails(Payment payment, Tenant tenant, Room room) {
        if (payment == null) {
            clearView();
            setTitle("Payment Receipt - Error");
            // Display error state appropriately
            tenantValueLabel.setText("Error: No Payment Data");
            return;
        }

        setTitle("Payment Receipt"); // Simple title

        // Populate Tenant/Room Info
        tenantValueLabel.setText(tenant != null ? tenant.getLastName() + ", " + tenant.getFirstName() : "N/A");
        roomNumberValueLabel.setText(room != null ? room.getRoomNumber() : "N/A");

        // Populate Payment Details
        paymentDateValueLabel.setText(payment.getPaymentDate() != null ? payment.getPaymentDate().format(DATE_FORMATTER) : "N/A");
        amountPaidValueLabel.setText(CURRENCY_FORMATTER.format(payment.getAmount() != null ? payment.getAmount() : 0));
        periodCoveredValueLabel.setText(formatPeriodCovered(payment.getPeriodCoveredStart(), payment.getPeriodCoveredEnd()));

        // Generate and Display QR Code
        if (payment.getQrCodeData() != null && !payment.getQrCodeData().isEmpty()) {
            try {
                BufferedImage qrImage = QRCodeGenerator.generateQRCodeImage(payment.getQrCodeData(), 150, 150); // Adjust size
                if (qrImage != null) {
                    qrCodeLabel.setIcon(new ImageIcon(qrImage));
                    qrCodeLabel.setText("");
                } else {
                    displayQrError();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error generating QR code image for payment ID: " + payment.getPaymentId(), e);
                displayQrError();
            }
        } else {
            LOGGER.warning("No QR code data found for payment ID: " + payment.getPaymentId());
            displayQrError();
        }
    }

    private void displayQrError() {
        qrCodeLabel.setIcon(null);
        qrCodeLabel.setText("[QR Code Unavailable]");
        qrCodeLabel.setForeground(java.awt.Color.RED);
        qrCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Helper method to format the period covered display.
     */
    private String formatPeriodCovered(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return "N/A";
        }
        if (start.getDayOfMonth() == 1 && end.equals(start.withDayOfMonth(start.lengthOfMonth()))) {
            return start.format(MONTH_YEAR_FORMATTER);
        }
        // Fallback if not a full standard month
        return start.format(DATE_FORMATTER) + " - " + end.format(DATE_FORMATTER);
    }

    /**
     * Clears all displayed data.
     */
    private void clearView() {
        setTitle("Payment Receipt");
        tenantValueLabel.setText("");
        roomNumberValueLabel.setText("");
        paymentDateValueLabel.setText("");
        amountPaidValueLabel.setText("");
        periodCoveredValueLabel.setText("");
        qrCodeLabel.setIcon(null);
        qrCodeLabel.setText("");
    }

    public void addPrintButtonListener(ActionListener listener) {
        printButton.addActionListener(listener);
    }

    /**
     * Adds an ActionListener to the Download button.
     *
     * @param listener ActionListener provided by the controller.
     */
    public void addDownloadButtonListener(ActionListener listener) {
        downloadButton.addActionListener(listener);
    }

    /**
     * Adds an ActionListener to the Done/Close button.
     *
     * @param listener ActionListener provided by the controller.
     */
    public void addDoneButtonListener(ActionListener listener) {
        doneButton.addActionListener(listener);
    }

    /**
     * Makes the dialog visible.
     */
    public void showDialog() {
        setVisible(true);
    }

    /**
     * Hides and disposes of the dialog window.
     */
    public void closeDialog() {
        dispose();
    }

    // Method to get the component to print (optional, for controller)
    public JPanel getPrintableComponent() {
        return receiptContentPanel; // Name the main content panel in Matisse
    }

    /**
     * Saves the receipt as a PDF file.
     * Uses Java Print API to "print" to a PDF file.
     */
    public void saveReceiptAsPDF() {
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Receipt as PDF");
        
        // Set default filename based on tenant and date if available
        String defaultFilename = "Receipt";
        if (!tenantValueLabel.getText().isEmpty() && !paymentDateValueLabel.getText().isEmpty()) {
            String tenant = tenantValueLabel.getText().replaceAll("[^a-zA-Z0-9]", "_");
            String date = paymentDateValueLabel.getText();
            defaultFilename = "Receipt_" + tenant + "_" + date;
        }
        
        // Set up file filter and default name
        fileChooser.setSelectedFile(new File(defaultFilename + ".pdf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        
        // Show save dialog
        int returnVal = fileChooser.showSaveDialog(this);
        
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            LOGGER.info("PDF save operation cancelled by user");
            return;
        }
        
        File selectedFile = fileChooser.getSelectedFile();
        // Add .pdf extension if not present
        if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".pdf");
        }
        
        // Check if file exists and confirm overwrite
        if (selectedFile.exists()) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "File " + selectedFile.getName() + " already exists. Overwrite?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION);
            
            if (confirm != JOptionPane.YES_OPTION) {
                LOGGER.info("PDF overwrite cancelled by user");
                return;
            }
        }
        
        try {
            // Setup printer job
            PrinterJob job = PrinterJob.getPrinterJob();
            
            // Set up the printable component (the receipt panel)
            final JPanel contentPanel = receiptContentPanel;
            
            // Create a Printable object
            java.awt.print.Printable printable = new java.awt.print.Printable() {
                @Override
                public int print(java.awt.Graphics graphics, PageFormat pageFormat, int pageIndex) 
                        throws PrinterException {
                    if (pageIndex > 0) {
                        return java.awt.print.Printable.NO_SUCH_PAGE;
                    }
                    
                    // Calculate scale to fit panel to page
                    double scaleX = pageFormat.getImageableWidth() / contentPanel.getWidth();
                    double scaleY = pageFormat.getImageableHeight() / contentPanel.getHeight();
                    double scale = Math.min(scaleX, scaleY);
                    
                    // Create a scaled graphics context
                    Graphics2D g2d = (Graphics2D)graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    g2d.scale(scale, scale);
                    
                    // Print the panel
                    contentPanel.print(g2d);
                    
                    return java.awt.print.Printable.PAGE_EXISTS;
                }
            };
            
            // Set up page format
            PageFormat pageFormat = job.defaultPage();
            Paper paper = pageFormat.getPaper();
            
            // Set margins (0.5 inch on all sides)
            double margin = 36; // 0.5 inch in points (72 points = 1 inch)
            paper.setImageableArea(margin, margin, paper.getWidth() - margin * 2, 
                    paper.getHeight() - margin * 2);
            pageFormat.setPaper(paper);
            
            job.setPrintable(printable, pageFormat);
            
            // Set up attributes to save as PDF
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            attributes.add(new Destination(selectedFile.toURI()));
            
            // "Print" to PDF file
            job.print(attributes);
            
            JOptionPane.showMessageDialog(this, 
                    "Receipt saved as PDF: " + selectedFile.getName(), 
                    "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            
            LOGGER.info("Receipt saved as PDF to " + selectedFile.getAbsolutePath());
            
        } catch (PrinterException e) {
            LOGGER.log(Level.SEVERE, "Error saving receipt as PDF", e);
            JOptionPane.showMessageDialog(this,
                    "Could not save receipt: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error saving receipt as PDF", e);
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        receiptContentPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        tenantValueLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        roomNumberValueLabel = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        paymentDateValueLabel = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        amountPaidValueLabel = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel10 = new javax.swing.JLabel();
        periodCoveredValueLabel = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel12 = new javax.swing.JLabel();
        qrCodeLabel = new javax.swing.JLabel();
        printButton = new javax.swing.JButton();
        downloadButton = new javax.swing.JButton();
        doneButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Payment Details");
        setResizable(false);

        receiptContentPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel1.setText("Payment Details");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Tenant");

        tenantValueLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        tenantValueLabel.setText("Maria Santos");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Room Number");

        roomNumberValueLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        roomNumberValueLabel.setText("104");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Payment Date");

        paymentDateValueLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        paymentDateValueLabel.setText("2025-10-25");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Amount Paid");

        amountPaidValueLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        amountPaidValueLabel.setForeground(new java.awt.Color(153, 255, 0));
        amountPaidValueLabel.setText("P5,000.00");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Period Covered");

        periodCoveredValueLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        periodCoveredValueLabel.setText("April 2025");

        jLabel12.setText("Scan for Payment Details");

        qrCodeLabel.setText("qr  code goes here");

        printButton.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12)); // NOI18N
        printButton.setText("Print");
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        downloadButton.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12)); // NOI18N
        downloadButton.setText("Download");

        doneButton.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12)); // NOI18N
        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout receiptContentPanelLayout = new javax.swing.GroupLayout(receiptContentPanel);
        receiptContentPanel.setLayout(receiptContentPanelLayout);
        receiptContentPanelLayout.setHorizontalGroup(
            receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(receiptContentPanelLayout.createSequentialGroup()
                .addGroup(receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(receiptContentPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(receiptContentPanelLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(tenantValueLabel)
                                .addGap(75, 75, 75))
                            .addGroup(receiptContentPanelLayout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addGap(94, 94, 94)
                                .addComponent(periodCoveredValueLabel))
                            .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(receiptContentPanelLayout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(107, 107, 107)
                                .addComponent(amountPaidValueLabel))
                            .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(receiptContentPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addGap(94, 94, 94)
                                        .addComponent(roomNumberValueLabel))
                                    .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel1)
                                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                                    .addComponent(jSeparator2)))
                            .addGroup(receiptContentPanelLayout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(100, 100, 100)
                                .addComponent(paymentDateValueLabel))))
                    .addGroup(receiptContentPanelLayout.createSequentialGroup()
                        .addGap(118, 118, 118)
                        .addComponent(jLabel12))
                    .addGroup(receiptContentPanelLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(downloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(printButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(doneButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(receiptContentPanelLayout.createSequentialGroup()
                        .addGap(110, 110, 110)
                        .addComponent(qrCodeLabel)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        receiptContentPanelLayout.setVerticalGroup(
            receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(receiptContentPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addGroup(receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tenantValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(roomNumberValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(paymentDateValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(amountPaidValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(periodCoveredValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(qrCodeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 144, Short.MAX_VALUE)
                .addGroup(receiptContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(downloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(printButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(doneButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(receiptContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(receiptContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_doneButtonActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_printButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ReceiptDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ReceiptDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ReceiptDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ReceiptDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ReceiptDialog dialog = new ReceiptDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel amountPaidValueLabel;
    private javax.swing.JButton doneButton;
    private javax.swing.JButton downloadButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JLabel paymentDateValueLabel;
    private javax.swing.JLabel periodCoveredValueLabel;
    private javax.swing.JButton printButton;
    private javax.swing.JLabel qrCodeLabel;
    private javax.swing.JPanel receiptContentPanel;
    private javax.swing.JLabel roomNumberValueLabel;
    private javax.swing.JLabel tenantValueLabel;
    // End of variables declaration//GEN-END:variables
}
