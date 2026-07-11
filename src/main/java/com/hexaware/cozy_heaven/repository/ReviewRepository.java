package com.hexaware.cozy_heaven.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.cozy_heaven.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
	
	boolean existsByBookingBookingId(int bookingId);
	List<Review> findByHotelHotelId(int hotelId);
	List<Review> findByUserUserId(int userId);
}
