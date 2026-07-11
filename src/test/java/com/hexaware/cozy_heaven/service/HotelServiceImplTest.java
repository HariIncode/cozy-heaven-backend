package com.hexaware.cozy_heaven.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.hexaware.cozy_heaven.dto.HotelDTO;
import com.hexaware.cozy_heaven.dto.UserDTO;
import com.hexaware.cozy_heaven.dto.response.HotelResponseDTO;
import com.hexaware.cozy_heaven.exception.HotelException;
import com.hexaware.cozy_heaven.model.Gender;
import com.hexaware.cozy_heaven.model.Role;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HotelServiceImplTest {

	@Autowired
	HotelService hotelService;

	@Autowired
	UserService userService;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
   // TODO document why this method is empty
 }

	private int createOwner(String email) {
		UserDTO dto = new UserDTO();
		dto.setName("Hotel Owner");
		dto.setEmail(email);
		dto.setPassword("Owner@1234");
		dto.setGender(Gender.FEMALE);
		dto.setContactNumber("9000000001");
		dto.setAddress("Owner Street, Bengaluru");
		dto.setRole(Role.HOTEL_OWNER);
		return userService.addUser(dto).getUserId();
	}

	private int createGuest(String email) {
		UserDTO dto = new UserDTO();
		dto.setName("Regular Guest");
		dto.setEmail(email);
		dto.setPassword("Guest@1234");
		dto.setGender(Gender.MALE);
		dto.setContactNumber("9000000002");
		dto.setAddress("Guest Lane, Chennai");
		dto.setRole(Role.GUEST);
		return userService.addUser(dto).getUserId();
	}

	private HotelDTO buildHotelDTO(int ownerId, String name, String location) {
		HotelDTO dto = new HotelDTO();
		dto.setOwnerId(ownerId);
		dto.setName(name);
		dto.setLocation(location);
		dto.setDescription("A comfortable hotel for all travellers.");
		dto.setImageUrl("https://example.com/hotel.jpg");
		dto.setDining(true);
		dto.setParking(true);
		dto.setWifi(true);
		dto.setRoomService(false);
		dto.setPool(false);
		dto.setGym(true);
		return dto;
	}

	@Test
	@Order(1)
	void testAddHotel_Success() {
		int ownerId = createOwner("hotel_owner1@cozy.com");
		HotelDTO dto = buildHotelDTO(ownerId, "CozyStay Inn", "Chennai");

		HotelResponseDTO saved = hotelService.addHotel(dto);

		assertNotNull(saved);
		assertNotNull(saved.getHotelId());
		assertEquals("CozyStay Inn", saved.getName());
		assertEquals("Chennai", saved.getLocation());
		assertEquals(ownerId, saved.getOwnerId());
		assertTrue(saved.isDining());
	}

	@Test
	@Order(2)
	void testAddHotel_ByNonOwner_ThrowsHotelException() {
		int guestId = createGuest("non_owner_guest@cozy.com");
		HotelDTO dto = buildHotelDTO(guestId, "Invalid Hotel", "Delhi");

		assertThrows(HotelException.class, () -> hotelService.addHotel(dto));
	}

	@Test
	@Order(3)
	void testAddHotel_OwnerNotFound_ThrowsException() {
		HotelDTO dto = buildHotelDTO(999999, "Ghost Hotel", "Nowhere");
		assertThrows(Exception.class, () -> hotelService.addHotel(dto));
	}

	@Test
	@Order(4)
	void testGetHotelById_Success() {
		int ownerId = createOwner("hotel_owner2@cozy.com");
		HotelDTO dto = buildHotelDTO(ownerId, "GetById Hotel", "Mumbai");
		HotelResponseDTO created = hotelService.addHotel(dto);

		HotelResponseDTO fetched = hotelService.getHotelById(created.getHotelId());

		assertNotNull(fetched);
		assertEquals(created.getHotelId(), fetched.getHotelId());
		assertEquals("GetById Hotel", fetched.getName());
		assertEquals(ownerId, fetched.getOwnerId());
	}

	@Test
	@Order(5)
	void testGetHotelById_NotFound_ThrowsHotelException() {
		assertThrows(HotelException.class, () -> hotelService.getHotelById(999999));
	}

	@Test
	@Order(6)
	void testGetAllHotel_ReturnsList() {
		List<HotelResponseDTO> hotels = hotelService.getAllHotel();
		assertNotNull(hotels);
		assertTrue(hotels.size() > 0);
	}

	@Test
	@Order(7)
	void testGetHotelsByOwner_Success() {
		int ownerId = createOwner("hotel_owner3@cozy.com");
		hotelService.addHotel(buildHotelDTO(ownerId, "Owner First Hotel", "Hyderabad"));
		hotelService.addHotel(buildHotelDTO(ownerId, "Owner Second Hotel", "Hyderabad"));

		List<HotelResponseDTO> hotels = hotelService.getHotelsByOwner(ownerId);

		assertNotNull(hotels);
		assertTrue(hotels.size() >= 2);
		assertEquals(ownerId, hotels.get(0).getOwnerId());
	}

	@Test
	@Order(8)
	void testGetHotelsByOwner_NoHotels_ReturnsEmptyList() {
		int ownerId = createOwner("owner_no_hotels@cozy.com");
		List<HotelResponseDTO> hotels = hotelService.getHotelsByOwner(ownerId);
		assertNotNull(hotels);
		assertTrue(hotels.isEmpty());
	}

	@Test
	@Order(9)
	void testUpdateHotel_Success() {
		int ownerId = createOwner("hotel_owner4@cozy.com");
		HotelResponseDTO created = hotelService.addHotel(buildHotelDTO(ownerId, "Old Name", "Pune"));

		HotelDTO updateDTO = buildHotelDTO(ownerId, "New Name", "Pune");
		updateDTO.setGym(false);
		updateDTO.setPool(true);

		HotelResponseDTO updated = hotelService.updateHotel(created.getHotelId(), updateDTO);

		assertNotNull(updated);
		assertEquals("New Name", updated.getName());
		assertTrue(updated.isPool());
	}

	@Test
	@Order(10)
	void testUpdateHotel_ByNonOwnerRole_ThrowsHotelException() {
		int ownerId = createOwner("hotel_owner5@cozy.com");
		int guestId = createGuest("hotel_guest5@cozy.com");

		HotelResponseDTO created = hotelService.addHotel(buildHotelDTO(ownerId, "Owner Hotel 5", "Kochi"));

		// Guest (not HOTEL_OWNER role) tries to update
		HotelDTO updateDTO = buildHotelDTO(guestId, "Hacked Name", "Kochi");
		assertThrows(HotelException.class, () -> hotelService.updateHotel(created.getHotelId(), updateDTO));
	}

	@Test
	@Order(11)
	void testUpdateHotel_ByDifferentOwner_ThrowsHotelException() {
		int ownerA = createOwner("hotel_ownerA@cozy.com");
		int ownerB = createOwner("hotel_ownerB@cozy.com");

		HotelResponseDTO created = hotelService.addHotel(buildHotelDTO(ownerA, "Owner A Hotel", "Agra"));

		// ownerB tries to update ownerA's hotel
		HotelDTO updateDTO = buildHotelDTO(ownerB, "Stolen Name", "Agra");
		assertThrows(HotelException.class, () -> hotelService.updateHotel(created.getHotelId(), updateDTO));
	}

	@Test
	@Order(12)
	void testUpdateHotel_NotFound_ThrowsHotelException() {
		int ownerId = createOwner("hotel_owner6@cozy.com");
		HotelDTO dto = buildHotelDTO(ownerId, "Ghost", "Nowhere");
		assertThrows(HotelException.class, () -> hotelService.updateHotel(999999, dto));
	}

	@Test
	@Order(13)
	void testUpdateAverageRating_NoReviews_SetsZero() {
		int ownerId = createOwner("hotel_owner7@cozy.com");
		HotelResponseDTO hotel = hotelService.addHotel(buildHotelDTO(ownerId, "Rating Hotel", "Kolkata"));

		hotelService.updateAverageRating(hotel.getHotelId());

		HotelResponseDTO updated = hotelService.getHotelById(hotel.getHotelId());
		assertEquals(0.0, updated.getAverageRating());
		assertEquals(0, updated.getTotalReviews());
	}

	@Test
	@Order(14)
	void testDeleteHotel_Success() {
		int ownerId = createOwner("hotel_owner8@cozy.com");
		HotelResponseDTO created = hotelService.addHotel(buildHotelDTO(ownerId, "Delete Me Hotel", "Jaipur"));

		boolean result = hotelService.deleteHotel(created.getHotelId());

		assertTrue(result);
		assertThrows(HotelException.class, () -> hotelService.getHotelById(created.getHotelId()));
	}

	@Test
	@Order(15)
	void testDeleteHotel_NotFound_ThrowsHotelException() {
		assertThrows(HotelException.class, () -> hotelService.deleteHotel(999998));
	}

}
