package com.hexaware.cozy_heaven.service;

import java.util.List;

import com.hexaware.cozy_heaven.dto.UpdatePaymentStatusDTO;
import com.hexaware.cozy_heaven.entity.Payment;

public interface PaymentService {

	Payment updatePaymentStatus(UpdatePaymentStatusDTO dto);

	Payment getPaymentById(int paymentId);

	List<Payment> getAllPayment();

	boolean deletePayment(int paymentId);

	Payment getPaymentByBooking(int bookingId);
}
