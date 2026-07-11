package com.hexaware.cozy_heaven.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.cozy_heaven.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
	public List<Booking> findByUserUserId(int userId);
	public List<Booking> findByRoomHotelHotelId(int hotelId);
}
