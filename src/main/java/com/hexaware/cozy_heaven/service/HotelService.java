package com.hexaware.cozy_heaven.service;

import java.util.List;

import com.hexaware.cozy_heaven.dto.HotelDTO;
import com.hexaware.cozy_heaven.dto.SearchHotelDTO;
import com.hexaware.cozy_heaven.dto.response.HotelResponseDTO;

public interface HotelService {

	HotelResponseDTO addHotel(HotelDTO dto);

	HotelResponseDTO updateHotel(int hotelId, HotelDTO dto);

	HotelResponseDTO getHotelById(int hotelId);

	List<HotelResponseDTO> getAllHotel();

	boolean deleteHotel(int hotelId);

	List<HotelResponseDTO> getHotelsByOwner(int ownerId);

	List<HotelResponseDTO> searchHotel(SearchHotelDTO dto);
	
	void updateAverageRating(int hotelId);
	
}
