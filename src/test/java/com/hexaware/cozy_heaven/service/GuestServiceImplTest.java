package com.hexaware.cozy_heaven.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.hexaware.cozy_heaven.dto.BookingDTO;
import com.hexaware.cozy_heaven.dto.GuestDTO;
import com.hexaware.cozy_heaven.dto.HotelDTO;
import com.hexaware.cozy_heaven.dto.RoomDTO;
import com.hexaware.cozy_heaven.dto.UpdatePaymentStatusDTO;
import com.hexaware.cozy_heaven.dto.UserDTO;
import com.hexaware.cozy_heaven.dto.response.BookingResponseDTO;
import com.hexaware.cozy_heaven.dto.response.HotelResponseDTO;
import com.hexaware.cozy_heaven.entity.Guest;
import com.hexaware.cozy_heaven.entity.Payment;
import com.hexaware.cozy_heaven.exception.GuestException;
import com.hexaware.cozy_heaven.model.Bed;
import com.hexaware.cozy_heaven.model.Gender;
import com.hexaware.cozy_heaven.model.GuestType;
import com.hexaware.cozy_heaven.model.PaymentStatus;
import com.hexaware.cozy_heaven.model.Role;
import com.hexaware.cozy_heaven.model.RoomType;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class GuestServiceImplTest {

	@Autowired
	GuestService guestService;

	@Autowired
	BookingService bookingService;

	@Autowired
	PaymentService paymentService;

	@Autowired
	UserService userService;

	@Autowired
	HotelService hotelService;

	@Autowired
	RoomService roomService;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		log.info(null);
 }	

	private int createGuest(String email) {
		UserDTO dto = new UserDTO();
		dto.setName("Guest User");
		dto.setEmail(email);
		dto.setPassword("Guest@1234");
		dto.setGender(Gender.MALE);
		dto.setContactNumber("9888888888");
		dto.setAddress("Guest Test Road, Jaipur");
		dto.setRole(Role.GUEST);
		return userService.addUser(dto).getUserId();
	}

	/** Capacity = 3 so we can add multiple guests in tests */
	private int createHotelAndRoom(String ownerEmail, int roomNumber, String hotelName) {
		UserDTO ownerDTO = new UserDTO();
		ownerDTO.setName("Guest Owner");
		ownerDTO.setEmail(ownerEmail);
		ownerDTO.setPassword("Owner@1234");
		ownerDTO.setGender(Gender.FEMALE);
		ownerDTO.setContactNumber("9999999999");
		ownerDTO.setAddress("Owner Address, Lucknow");
		ownerDTO.setRole(Role.HOTEL_OWNER);
		int ownerId = userService.addUser(ownerDTO).getUserId();

		HotelDTO hotelDTO = new HotelDTO();
		hotelDTO.setOwnerId(ownerId);
		hotelDTO.setName(hotelName);
		hotelDTO.setLocation("Jaipur");
		hotelDTO.setDescription("Guest test hotel");
		hotelDTO.setImageUrl("https://example.com/gs.jpg");
		hotelDTO.setWifi(true);
		hotelDTO.setParking(false);
		hotelDTO.setDining(false);
		hotelDTO.setGym(false);
		hotelDTO.setPool(false);
		hotelDTO.setRoomService(false);
		HotelResponseDTO hotel = hotelService.addHotel(hotelDTO);

		RoomDTO roomDTO = new RoomDTO();
		roomDTO.setHotelId(hotel.getHotelId());
		roomDTO.setRoomNumber(roomNumber);
		roomDTO.setSizeSQM(35f);
		roomDTO.setBedSize(Bed.KING);
		roomDTO.setCapacity(3); // capacity = 3 for multi-guest tests
		roomDTO.setFare(5000);
		roomDTO.setRoomType(RoomType.SUITE);
		roomDTO.setAc(true);
		roomDTO.setAvailable(true);
		return roomService.addRoom(hotel.getHotelId(), roomDTO).getRoomId();
	}

	private int createConfirmedBooking(int userId, int roomId, int dayOffset) {
		BookingDTO bookingDTO = new BookingDTO();
		bookingDTO.setUserId(userId);
		bookingDTO.setRoomId(roomId);
		bookingDTO.setCheckIn(LocalDate.now().plusDays(dayOffset));
		bookingDTO.setCheckOut(LocalDate.now().plusDays(dayOffset + 2));
		bookingDTO.setAdults(1);
		bookingDTO.setChildrens(0);
		BookingResponseDTO booking = bookingService.createBooking(bookingDTO);

		Payment payment = paymentService.getPaymentByBooking(booking.getBookingId());
		UpdatePaymentStatusDTO statusDTO = new UpdatePaymentStatusDTO();
		statusDTO.setPaymentId(Math.toIntExact(payment.getPaymentId()));
		statusDTO.setStatus(PaymentStatus.SUCCESS);
		paymentService.updatePaymentStatus(statusDTO);

		return booking.getBookingId();
	}

	private GuestDTO buildGuestDTO(String name, int age, GuestType type) {
		GuestDTO dto = new GuestDTO();
		dto.setName(name);
		dto.setAge(age);
		dto.setGuestType(type);
		return dto;
	}

	@Test
	@Order(1)
	void testAddGuest_Success() {
		int userId = createGuest("gs_user1@cozy.com");
		int roomId = createHotelAndRoom("gs_owner1@cozy.com", 101, "Guest Hotel 1");
		int bookingId = createConfirmedBooking(userId, roomId, 30);

		GuestDTO dto = buildGuestDTO("Alice", 28, GuestType.ADULT);
		Guest saved = guestService.addGuest(bookingId, dto);

		assertNotNull(saved);
		assertNotNull(saved.getGuestId());
		assertEquals("Alice", saved.getName());
		assertEquals(GuestType.ADULT, saved.getGuestType());
	}

	@Test
	@Order(2)
	void testAddGuest_BookingNotFound_ThrowsGuestException() {
		GuestDTO dto = buildGuestDTO("Bob", 10, GuestType.CHILD);
		assertThrows(GuestException.class, () -> guestService.addGuest(999999, dto));
	}

	@Test
	@Order(3)
	void testAddGuest_BookingNotConfirmed_ThrowsGuestException() {
		int userId = createGuest("gs_user2@cozy.com");
		int roomId = createHotelAndRoom("gs_owner2@cozy.com", 201, "Guest Hotel 2");

		// Create booking but do NOT confirm (status stays PENDING)
		BookingDTO bookingDTO = new BookingDTO();
		bookingDTO.setUserId(userId);
		bookingDTO.setRoomId(roomId);
		bookingDTO.setCheckIn(LocalDate.now().plusDays(40));
		bookingDTO.setCheckOut(LocalDate.now().plusDays(42));
		bookingDTO.setAdults(1);
		bookingDTO.setChildrens(0);
		BookingResponseDTO booking = bookingService.createBooking(bookingDTO);

		GuestDTO dto = buildGuestDTO("Charlie", 32, GuestType.ADULT);
		assertThrows(GuestException.class, () -> guestService.addGuest(booking.getBookingId(), dto));
	}

	@Test
	@Order(4)
	void testAddGuest_CapacityExceeded_ThrowsGuestException() {
		int userId = createGuest("gs_user3@cozy.com");
		int roomId = createHotelAndRoom("gs_owner3@cozy.com", 301, "Guest Hotel 3");
		int bookingId = createConfirmedBooking(userId, roomId, 50);

		// Room capacity = 3 — fill it up
		guestService.addGuest(bookingId, buildGuestDTO("G1", 25, GuestType.ADULT));
		guestService.addGuest(bookingId, buildGuestDTO("G2", 22, GuestType.ADULT));
		guestService.addGuest(bookingId, buildGuestDTO("G3", 8, GuestType.CHILD));

		// 4th guest must exceed capacity
		GuestDTO excess = buildGuestDTO("G4", 5, GuestType.CHILD);
		assertThrows(GuestException.class, () -> guestService.addGuest(bookingId, excess));
	}

	@Test
	@Order(5)
	void testGetGuestById_Success() {
		int userId = createGuest("gs_user4@cozy.com");
		int roomId = createHotelAndRoom("gs_owner4@cozy.com", 401, "Guest Hotel 4");
		int bookingId = createConfirmedBooking(userId, roomId, 60);

		Guest created = guestService.addGuest(bookingId, buildGuestDTO("Diana", 35, GuestType.ADULT));
		Guest fetched = guestService.getGuestById(created.getGuestId());

		assertNotNull(fetched);
		assertEquals(created.getGuestId(), fetched.getGuestId());
		assertEquals("Diana", fetched.getName());
	}

	@Test
	@Order(6)
	void testGetGuestById_NotFound_ThrowsGuestException() {
		assertThrows(GuestException.class, () -> guestService.getGuestById(999999));
	}

	@Test
	@Order(7)
	void testGetAllGuest_ReturnsList() {
		List<Guest> guests = guestService.getAllGuest();
		assertNotNull(guests);
		assertTrue(guests.size() > 0);
	}

	@Test
	@Order(8)
	void testGetGuestByBooking_Success() {
		int userId = createGuest("gs_user5@cozy.com");
		int roomId = createHotelAndRoom("gs_owner5@cozy.com", 501, "Guest Hotel 5");
		int bookingId = createConfirmedBooking(userId, roomId, 70);

		guestService.addGuest(bookingId, buildGuestDTO("Eve", 29, GuestType.ADULT));
		guestService.addGuest(bookingId, buildGuestDTO("Frank", 6, GuestType.CHILD));

		List<Guest> guests = guestService.getGuestByBooking(bookingId);

		assertNotNull(guests);
		assertEquals(2, guests.size());
	}

	@Test
	@Order(9)
	void testUpdateGuest_Success() {
		int userId = createGuest("gs_user6@cozy.com");
		int roomId = createHotelAndRoom("gs_owner6@cozy.com", 601, "Guest Hotel 6");
		int bookingId = createConfirmedBooking(userId, roomId, 80);

		Guest created = guestService.addGuest(bookingId, buildGuestDTO("George", 40, GuestType.ADULT));

		GuestDTO updateDTO = buildGuestDTO("George Updated", 41, GuestType.ADULT);
		Guest updated = guestService.updateGuest(created.getGuestId(), updateDTO);

		assertNotNull(updated);
		assertEquals("George Updated", updated.getName());
		assertEquals(41, updated.getAge());
	}

	@Test
	@Order(10)
	void testUpdateGuest_NotFound_ThrowsGuestException() {
		GuestDTO dto = buildGuestDTO("Nobody", 20, GuestType.ADULT);
		assertThrows(GuestException.class, () -> guestService.updateGuest(999999, dto));
	}

	@Test
	@Order(11)
	void testDeleteGuest_Success() {
		int userId = createGuest("gs_user7@cozy.com");
		int roomId = createHotelAndRoom("gs_owner7@cozy.com", 701, "Guest Hotel 7");
		int bookingId = createConfirmedBooking(userId, roomId, 90);

		Guest created = guestService.addGuest(bookingId, buildGuestDTO("Hank", 45, GuestType.ADULT));

		boolean result = guestService.deleteGuest(created.getGuestId());

		assertTrue(result);
		assertThrows(GuestException.class, () -> guestService.getGuestById(created.getGuestId()));
	}

	@Test
	@Order(12)
	void testDeleteGuest_NotFound_ThrowsGuestException() {
		assertThrows(GuestException.class, () -> guestService.deleteGuest(999999));
	}

}
