package com.hexaware.cozy_heaven.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.cozy_heaven.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
	public Payment findByBookingBookingId(int bookingId);
}
