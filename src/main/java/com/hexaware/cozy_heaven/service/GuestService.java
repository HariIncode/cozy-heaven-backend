package com.hexaware.cozy_heaven.service;

import java.util.List;

import com.hexaware.cozy_heaven.dto.GuestDTO;
import com.hexaware.cozy_heaven.entity.Guest;

public interface GuestService {

	Guest addGuest(int bookingId ,GuestDTO guestDto);

	Guest updateGuest(int guestId, GuestDTO guestDTO);

	Guest getGuestById(int guestId);

	List<Guest> getAllGuest();

	boolean deleteGuest(int guestId);

	List<Guest> getGuestByBooking(int bookingId);
}
