package com.hexaware.cozy_heaven.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.cozy_heaven.entity.Guest;

public interface GuestRepository extends JpaRepository<Guest, Integer> {
	public List<Guest> findByBookingBookingId(int bookingId);
}
