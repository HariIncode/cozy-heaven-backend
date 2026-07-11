package com.hexaware.cozy_heaven.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.hexaware.cozy_heaven.dto.HotelDTO;
import com.hexaware.cozy_heaven.dto.SearchHotelDTO;
import com.hexaware.cozy_heaven.dto.response.HotelResponseDTO;
import com.hexaware.cozy_heaven.entity.Hotel;
import com.hexaware.cozy_heaven.entity.Review;
import com.hexaware.cozy_heaven.entity.User;
import com.hexaware.cozy_heaven.exception.HotelException;
import com.hexaware.cozy_heaven.model.Role;
import com.hexaware.cozy_heaven.repository.HotelRepository;
import com.hexaware.cozy_heaven.repository.ReviewRepository;
import com.hexaware.cozy_heaven.repository.RoomRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class HotelServiceImpl implements HotelService {

	final HotelRepository hotelRepo;

	final UserService userService;

	final RoomRepository roomRepo;

	final ReviewRepository reviewRepo;

	HotelServiceImpl(HotelRepository hotelRepo, UserService userService, RoomRepository roomRepo, ReviewRepository reviewRepo) {
		this.hotelRepo = hotelRepo;
		this.userService = userService;
		this.roomRepo = roomRepo;
		this.reviewRepo = reviewRepo;
	}
	
	String exceptionMessage = "Hotel With this Id dosen't Exist. : ";
	
	private HotelResponseDTO toResponse(Hotel hotel) {
		HotelResponseDTO dto = new HotelResponseDTO();
		
		dto.setHotelId(hotel.getHotelId());
		dto.setName(hotel.getName());
		dto.setLocation(hotel.getLocation());
		dto.setDescription(hotel.getDescription());
		dto.setImageUrl(hotel.getImageUrl());
		
		dto.setOwnerId(hotel.getOwner().getUserId());
		dto.setOwnerName(hotel.getOwner().getName());
		dto.setOwnerEmail(hotel.getOwner().getEmail());
		dto.setOwnerContact(hotel.getOwner().getContactNumber());
		
		dto.setDining(hotel.isDining());
		dto.setParking(hotel.isParking());
		dto.setWifi(hotel.isWifi());
		dto.setRoomService(hotel.isRoomService());
		dto.setPool(hotel.isPool());
		dto.setGym(hotel.isGym());
		
		dto.setAverageRating(hotel.getAverageRating());
		dto.setTotalReviews(hotel.getTotalReviews());
	
		dto.setCreatedAt(hotel.getCreatedAt());
		
		return dto;
	}

	@Override
	public HotelResponseDTO addHotel(HotelDTO dto) {

		User user = userService.getUserById(dto.getOwnerId());

		if (user.getRole() != Role.HOTEL_OWNER) {
			log.error("The User is Not a Hotel Owner.");
			throw new HotelException(
					"Cannot Create Hotel for user with userId" + dto.getOwnerId() + " This User is not a Owner.");
		}

		Hotel hotel = new Hotel();

		log.info("Creating New Hotel for OwnerId :{}", dto.getOwnerId());

		hotel.setName(dto.getName());
		hotel.setLocation(dto.getLocation());
		hotel.setDescription(dto.getDescription());
		hotel.setImageUrl(dto.getImageUrl());
		User owner = userService.getUserById(dto.getOwnerId());
		hotel.setOwner(owner);
		hotel.setDining(dto.isDining());
		hotel.setGym(dto.isGym());
		hotel.setParking(dto.isParking());
		hotel.setPool(dto.isPool());
		hotel.setWifi(dto.isWifi());
		hotel.setRoomService(dto.isRoomService());

		Hotel savedHotel = hotelRepo.save(hotel);

		log.info("Hotel Created Successfully.");

		return toResponse(savedHotel);
	}

	@Override
	public HotelResponseDTO updateHotel(int hotelId, HotelDTO dto) {
		
		User user = userService.getUserById(dto.getOwnerId());
		
		if (user.getRole() != Role.HOTEL_OWNER) {
			log.error("The User is Not a Hotel Owner.");
			throw new HotelException(
					"Cannot Create Hotel for user with userId " + dto.getOwnerId() + " This User is not a Owner.");
		}
		
		Hotel hotel = hotelRepo.findById(hotelId).orElseThrow(() -> {
			log.error("Hotel with this Id doesn't Exist. :", hotelId);
			return new HotelException(exceptionMessage + hotelId);
		});
		
		if(!Objects.equals(hotel.getOwner().getUserId(), user.getUserId())) {
			log.error("You Cannot edit others Hotel.");
			throw new HotelException(
					"You Can only Edit Your Own Hotels");
		}
		

		hotel.setName(dto.getName());
		hotel.setLocation(dto.getLocation());
		hotel.setDescription(dto.getDescription());
		hotel.setImageUrl(dto.getImageUrl());
		hotel.setDining(dto.isDining());
		hotel.setGym(dto.isGym());
		hotel.setParking(dto.isParking());
		hotel.setPool(dto.isPool());
		hotel.setWifi(dto.isWifi());
		hotel.setRoomService(dto.isRoomService());

		Hotel updatedHotel = hotelRepo.save(hotel);

		log.info("Hotel Details Updated for HotelId: {}", hotelId);

		return toResponse(updatedHotel);
	}

	@Override
	public HotelResponseDTO getHotelById(int hotelId) {
		Hotel hotel = hotelRepo.findById(hotelId).orElseThrow(() -> {
			log.error("Hotel with this Id doesn't Exist. :", hotelId);
			return new HotelException(exceptionMessage + hotelId);
		});

		log.info("Getting Hotel For HotelId: {}", hotelId);

		return toResponse(hotel);
	}

	@Override
	public List<HotelResponseDTO> getAllHotel() {
		log.info("Getting All Hotels.");
		return hotelRepo.findAll().stream().map(this::toResponse).toList();
	}

	@Override
	public boolean deleteHotel(int hotelId) {

		log.info("Inside Hotel Deletion method");
		Hotel hotel = hotelRepo.findById(hotelId).orElseThrow(() -> {
			log.error("Hotel with this Id doesn't Exist. :", hotelId);
			return new HotelException(exceptionMessage + hotelId);
		});
		hotelRepo.delete(hotel);

		log.info("Hotel with HotelID {} Deleted SuccessFully.", hotelId);

		return true;
	}

	@Override
	public List<HotelResponseDTO> getHotelsByOwner(int ownerId) {
		List<HotelResponseDTO> hotels = hotelRepo.findByOwnerUserId(ownerId).stream().map(this::toResponse).toList();
		log.info("Getting All the Hotels this Owner Holds: OwnerId {}", ownerId);
		return hotels;
	}

	@Override
	public List<HotelResponseDTO> searchHotel(SearchHotelDTO dto) {

	    String location = dto.getLocation();
	    LocalDate checkIn = dto.getCheckIn();
	    LocalDate checkOut = dto.getCheckOut();
	    int guests = dto.getGuest();

	    List<Hotel> hotels = hotelRepo.findByLocation(location);

	    if (hotels.isEmpty()) {
	        log.error("No Hotels Found in the given Location: {}", location);
	        throw new HotelException("No Hotels Found in the given Location: " + location);
	    }

	    List<HotelResponseDTO> availableHotels = hotels.stream()
	            .filter(hotel -> hotel.getRooms().stream()
	                    .anyMatch(room ->
	                            room.isAvailable()
	                            && room.getCapacity() >= guests
	                            && roomRepo.isRoomAvailableForDates(
	                                    room.getRoomId(),
	                                    checkIn,
	                                    checkOut)))
	            .map(this::toResponse)
	            .toList();

	    if (availableHotels.isEmpty()) {
	        throw new HotelException("No Hotels with available rooms found in: " + location);
	    }
	    
	    log.info("Getting Hotels for the Given Location: {}", location);
	    
	    return availableHotels;
	}

	@Override
	public void updateAverageRating(int hotelId) {
		List<Review> reviews = reviewRepo.findByHotelHotelId(hotelId);
		Hotel hotel = hotelRepo.findById(hotelId).orElseThrow(() -> {
			log.error("Hotel with this Id doesn't Exist. :", hotelId);
			return new HotelException(exceptionMessage + hotelId);
		});

		if (reviews.isEmpty()) {
			hotel.setAverageRating(0.0);
			hotel.setTotalReviews(0);
		} else {
			double average = reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);
			hotel.setAverageRating(average);
			hotel.setTotalReviews(reviews.size());
		}

		hotelRepo.save(hotel);
	}

}
