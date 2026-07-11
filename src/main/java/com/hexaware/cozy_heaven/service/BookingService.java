package com.hexaware.cozy_heaven.service;

import java.util.List;

import com.hexaware.cozy_heaven.dto.BookingDTO;
import com.hexaware.cozy_heaven.dto.CancelBookingDTO;
import com.hexaware.cozy_heaven.dto.response.BookingResponseDTO;

public interface BookingService {

	BookingResponseDTO createBooking( BookingDTO dto);
	
	BookingResponseDTO cancelBooking(CancelBookingDTO dto);
	
	BookingResponseDTO completeBooking(int bookingId);
	
	BookingResponseDTO getBookingById(int bookingId);
	
	BookingResponseDTO markNoShow(int bookingId);
	
	List<BookingResponseDTO> getAllBooking();
	
	boolean deleteBooking(int bookingId);
	
	List<BookingResponseDTO> getBookingsByUser(int userId);
	
	List<BookingResponseDTO> getBookingsByHotel(int hotelId);
}
