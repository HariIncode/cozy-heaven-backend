package com.hexaware.cozy_heaven.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.cozy_heaven.entity.Hotel;
import com.hexaware.cozy_heaven.entity.User;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {
	List<Hotel> findByOwner(User user);
	
	List<Hotel> findByOwnerUserId(int ownerId);
	
	List<Hotel> findByLocation(String location);
}
