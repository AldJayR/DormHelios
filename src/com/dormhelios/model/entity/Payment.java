package com.dormhelios.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a payment record logged in the system.
 */
public class Payment {

    public enum PaymentMethod {
        CASH, BANK_TRANSFER, GCASH, MAYA, OTHER
    }

    private int paymentId;
    private int tenantId; // FK to Tenants (who the payment is for)
    private int userId;   // FK to Users (who logged the payment)
    private LocalDate paymentDate; // Date payment was received/logged
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private LocalDate periodCoveredStart; // Start of rental period covered
    private LocalDate periodCoveredEnd;   // End of rental period covered
    private String receiptReference; // Nullable (e.g., external transaction ID)
    private String qrCodeData;       // Nullable (Data embedded in generated QR code)
    private String notes;            // Nullable
    private LocalDateTime createdAt; // Timestamp when logged

    public Payment() {
        this.amount = BigDecimal.ZERO;
    }

    // --- Getters and Setters ---
    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = Objects.requireNonNullElse(amount, BigDecimal.ZERO);
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDate getPeriodCoveredStart() {
        return periodCoveredStart;
    }

    public void setPeriodCoveredStart(LocalDate periodCoveredStart) {
        this.periodCoveredStart = periodCoveredStart;
    }

    public LocalDate getPeriodCoveredEnd() {
        return periodCoveredEnd;
    }

    public void setPeriodCoveredEnd(LocalDate periodCoveredEnd) {
        this.periodCoveredEnd = periodCoveredEnd;
    }

    public String getReceiptReference() {
        return receiptReference;
    }

    public void setReceiptReference(String receiptReference) {
        this.receiptReference = receiptReference;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- equals, hashCode, toString ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Payment payment = (Payment) o;
        // Base on primary key if set
        return paymentId != 0 && paymentId == payment.paymentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId); // Use primary key
    }

    @Override
    public String toString() {
        return "Payment{"
                + "paymentId=" + paymentId
                + ", tenantId=" + tenantId
                + ", paymentDate=" + paymentDate
                + ", amount=" + amount
                + ", method=" + paymentMethod
                + '}';
    }
}
