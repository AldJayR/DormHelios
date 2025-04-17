package com.dormhelios.model.dao;

import com.dormhelios.model.entity.Payment;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public interface PaymentDAO {

    Optional<Payment> findById(int paymentId);

    List<Payment> findAll();

    List<Payment> findByTenantId(int tenantId); // Get all payments for a specific tenant

    List<Payment> findByDateRange(LocalDate startDate, LocalDate endDate); // Find payments within a date range

    List<Payment> findByUserId(int userId); // Find payments logged by a specific user

    int addPayment(Payment payment); // Return generated ID

    boolean updatePayment(Payment payment); // Usually less common to update payments

    boolean deletePayment(int paymentId); // Use with extreme caution!

    BigDecimal sumAmountByDateRange(LocalDate startDate, LocalDate endDate); // New method for summing
}
