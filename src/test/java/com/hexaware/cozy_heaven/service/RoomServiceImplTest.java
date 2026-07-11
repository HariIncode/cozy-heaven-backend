package com.hexaware.cozy_heaven.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.hexaware.cozy_heaven.dto.HotelDTO;
import com.hexaware.cozy_heaven.dto.RoomDTO;
import com.hexaware.cozy_heaven.dto.UserDTO;
import com.hexaware.cozy_heaven.dto.response.HotelResponseDTO;
import com.hexaware.cozy_heaven.entity.Room;
import com.hexaware.cozy_heaven.entity.User;
import com.hexaware.cozy_heaven.exception.RoomException;
import com.hexaware.cozy_heaven.model.Bed;
import com.hexaware.cozy_heaven.model.Gender;
import com.hexaware.cozy_heaven.model.Role;
import com.hexaware.cozy_heaven.model.RoomType;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoomServiceImplTest {

	@Autowired
	RoomService roomService;

	@Autowired
	HotelService hotelService;

	@Autowired
	UserService userService;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	private int createOwnerAndHotel(String email, String hotelName) {
		UserDTO userDTO = new UserDTO();
		userDTO.setName("Room Owner");
		userDTO.setEmail(email);
		userDTO.setPassword("Owner@1234");
		userDTO.setGender(Gender.MALE);
		userDTO.setContactNumber("9111111111");
		userDTO.setAddress("Owner Address, Bengaluru");
		userDTO.setRole(Role.HOTEL_OWNER);
		User owner = userService.addUser(userDTO);

		HotelDTO hotelDTO = new HotelDTO();
		hotelDTO.setOwnerId(owner.getUserId());
		hotelDTO.setName(hotelName);
		hotelDTO.setLocation("Bengaluru");
		hotelDTO.setDescription("Test hotel for room tests.");
		hotelDTO.setImageUrl("https://example.com/img.jpg");
		hotelDTO.setWifi(true);
		hotelDTO.setParking(true);
		hotelDTO.setDining(false);
		hotelDTO.setGym(false);
		hotelDTO.setPool(false);
		hotelDTO.setRoomService(false);
		HotelResponseDTO hotel = hotelService.addHotel(hotelDTO);
		return hotel.getHotelId();
	}

	private RoomDTO buildRoomDTO(int hotelId, int roomNumber, int fare, int capacity) {
		RoomDTO dto = new RoomDTO();
		dto.setHotelId(hotelId);
		dto.setRoomNumber(roomNumber);
		dto.setSizeSQM(25.5f);
		dto.setBedSize(Bed.DOUBLE);
		dto.setCapacity(capacity);
		dto.setFare(fare);
		dto.setRoomType(RoomType.DELUXE);
		dto.setAc(true);
		dto.setAvailable(true);
		return dto;
	}

	@Test
	@Order(1)
	void testAddRoom_Success() {
		int hotelId = createOwnerAndHotel("room_owner1@cozy.com", "Room Test Hotel 1");
		RoomDTO dto = buildRoomDTO(hotelId, 101, 2500, 2);

		Room saved = roomService.addRoom(hotelId, dto);

		assertNotNull(saved);
		assertNotNull(saved.getRoomId());
		assertEquals(101, saved.getRoomNumber());
		assertEquals(2500, saved.getFare());
		assertTrue(saved.isAvailable());
	}

	@Test
	@Order(2)
	void testAddRoom_DuplicateRoomNumber_ThrowsRoomException() {
		int hotelId = createOwnerAndHotel("room_owner2@cozy.com", "Room Test Hotel 2");
		roomService.addRoom(hotelId, buildRoomDTO(hotelId, 201, 3000, 2));

		assertThrows(RoomException.class, () -> roomService.addRoom(hotelId, buildRoomDTO(hotelId, 201, 3000, 2)));
	}

	@Test
	@Order(3)
	void testAddRoom_HotelNotFound_ThrowsException() {
		RoomDTO dto = buildRoomDTO(999999, 301, 1500, 1);
		assertThrows(Exception.class, () -> roomService.addRoom(999999, dto));
	}

	@Test
	@Order(4)
	void testGetRoomById_Success() {
		int hotelId = createOwnerAndHotel("room_owner3@cozy.com", "Room Test Hotel 3");
		Room created = roomService.addRoom(hotelId, buildRoomDTO(hotelId, 401, 1800, 2));

		Room fetched = roomService.getRoomById(created.getRoomId());

		assertNotNull(fetched);
		assertEquals(created.getRoomId(), fetched.getRoomId());
		assertEquals(401, fetched.getRoomNumber());
	}

	@Test
	@Order(5)
	void testGetRoomById_NotFound_ThrowsRoomException() {
		assertThrows(RoomException.class, () -> roomService.getRoomById(999999));
	}

	@Test
	@Order(6)
	void testGetAllRooms_ReturnsList() {
		List<Room> rooms = roomService.getAllRooms();
		assertNotNull(rooms);
		assertTrue(rooms.size() > 0);
	}

	@Test
	@Order(7)
	void testGetRoomsByHotel_Success() {
		int hotelId = createOwnerAndHotel("room_owner4@cozy.com", "Room Test Hotel 4");
		roomService.addRoom(hotelId, buildRoomDTO(hotelId, 501, 2000, 2));
		roomService.addRoom(hotelId, buildRoomDTO(hotelId, 502, 3000, 3));

		List<Room> rooms = roomService.getRoomsByHotel(hotelId);

		assertNotNull(rooms);
		assertEquals(2, rooms.size());
	}

	@Test
	@Order(8)
	void testUpdateRoom_Success() {
		int hotelId = createOwnerAndHotel("room_owner5@cozy.com", "Room Test Hotel 5");
		Room created = roomService.addRoom(hotelId, buildRoomDTO(hotelId, 601, 2000, 2));

		RoomDTO updateDTO = buildRoomDTO(hotelId, 601, 5000, 4);
		updateDTO.setRoomType(RoomType.SUITE);

		Room updated = roomService.updateRoom(created.getRoomId(), updateDTO);

		assertNotNull(updated);
		assertEquals(5000, updated.getFare());
		assertEquals(RoomType.SUITE, updated.getRoomType());
		assertEquals(4, updated.getCapacity());
	}

	@Test
	@Order(9)
	void testUpdateRoom_NotFound_ThrowsRoomException() {
		RoomDTO dto = buildRoomDTO(1, 701, 2500, 2);
		assertThrows(RoomException.class, () -> roomService.updateRoom(999999, dto));
	}

	@Test
	@Order(10)
	void testBlockRoom_Success() {
		int hotelId = createOwnerAndHotel("room_owner6@cozy.com", "Room Test Hotel 6");
		Room created = roomService.addRoom(hotelId, buildRoomDTO(hotelId, 801, 2500, 2));

		roomService.blockRoom(created.getRoomId());

		Room blocked = roomService.getRoomById(created.getRoomId());
		assertFalse(blocked.isAvailable());
	}

	@Test
	@Order(11)
	void testUnblockRoom_Success() {
		int hotelId = createOwnerAndHotel("room_owner7@cozy.com", "Room Test Hotel 7");
		Room created = roomService.addRoom(hotelId, buildRoomDTO(hotelId, 901, 2500, 2));

		roomService.blockRoom(created.getRoomId());
		roomService.unblockRoom(created.getRoomId());

		Room unblocked = roomService.getRoomById(created.getRoomId());
		assertTrue(unblocked.isAvailable());
	}

	@Test
	@Order(12)
	void testGetAvailableRooms_ReturnsListForDates() {
		int hotelId = createOwnerAndHotel("room_owner8@cozy.com", "Room Test Hotel 8");
		roomService.addRoom(hotelId, buildRoomDTO(hotelId, 1001, 2000, 2));

		LocalDate checkIn = LocalDate.now().plusDays(5);
		LocalDate checkOut = LocalDate.now().plusDays(8);

		List<Room> available = roomService.getAvailableRooms(hotelId, checkIn, checkOut);

		assertNotNull(available);
		assertTrue(available.size() > 0);
	}

	@Test
	@Order(13)
	void testDeleteRoom_Success() {
		int hotelId = createOwnerAndHotel("room_owner9@cozy.com", "Room Test Hotel 9");
		Room created = roomService.addRoom(hotelId, buildRoomDTO(hotelId, 1101, 1800, 1));

		boolean result = roomService.deleteRoom(created.getRoomId());

		assertTrue(result);
		assertThrows(RoomException.class, () -> roomService.getRoomById(created.getRoomId()));
	}

	@Test
	@Order(14)
	void testDeleteRoom_NotFound_ThrowsRoomException() {
		assertThrows(RoomException.class, () -> roomService.deleteRoom(999997));
	}

}
