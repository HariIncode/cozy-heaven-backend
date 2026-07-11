package com.hexaware.cozy_heaven.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.cozy_heaven.entity.Refund;

public interface RefundRepository extends JpaRepository<Refund, Integer> {
	public Refund findByBookingBookingId(int bookingId);
}
