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
import com.hexaware.cozy_heaven.dto.HotelDTO;
import com.hexaware.cozy_heaven.dto.RoomDTO;
import com.hexaware.cozy_heaven.dto.UpdatePaymentStatusDTO;
import com.hexaware.cozy_heaven.dto.UserDTO;
import com.hexaware.cozy_heaven.dto.response.BookingResponseDTO;
import com.hexaware.cozy_heaven.dto.response.HotelResponseDTO;
import com.hexaware.cozy_heaven.entity.Payment;
import com.hexaware.cozy_heaven.entity.Room;
import com.hexaware.cozy_heaven.exception.PaymentException;
import com.hexaware.cozy_heaven.model.Bed;
import com.hexaware.cozy_heaven.model.BookingStatus;
import com.hexaware.cozy_heaven.model.Gender;
import com.hexaware.cozy_heaven.model.PaymentStatus;
import com.hexaware.cozy_heaven.model.Role;
import com.hexaware.cozy_heaven.model.RoomType;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentServiceImplTest {

	@Autowired
	PaymentService paymentService;

	@Autowired
	BookingService bookingService;

	@Autowired
	UserService userService;

	@Autowired
	HotelService hotelService;

	@Autowired
	RoomService roomService;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	private int createGuest(String email) {
		UserDTO dto = new UserDTO();
		dto.setName("Payment Guest");
		dto.setEmail(email);
		dto.setPassword("Guest@1234");
		dto.setGender(Gender.MALE);
		dto.setContactNumber("9444444444");
		dto.setAddress("Payment Test Street, Delhi");
		dto.setRole(Role.GUEST);
		return userService.addUser(dto).getUserId();
	}

	private int createHotelAndRoom(String ownerEmail, int roomNumber, String hotelName) {
		UserDTO ownerDTO = new UserDTO();
		ownerDTO.setName("Payment Owner");
		ownerDTO.setEmail(ownerEmail);
		ownerDTO.setPassword("Owner@1234");
		ownerDTO.setGender(Gender.FEMALE);
		ownerDTO.setContactNumber("9555555555");
		ownerDTO.setAddress("Owner Address, Pune");
		ownerDTO.setRole(Role.HOTEL_OWNER);
		int ownerId = userService.addUser(ownerDTO).getUserId();

		HotelDTO hotelDTO = new HotelDTO();
		hotelDTO.setOwnerId(ownerId);
		hotelDTO.setName(hotelName);
		hotelDTO.setLocation("Delhi");
		hotelDTO.setDescription("Payment test hotel");
		hotelDTO.setImageUrl("https://example.com/pay.jpg");
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
		roomDTO.setSizeSQM(20f);
		roomDTO.setBedSize(Bed.SINGLE);
		roomDTO.setCapacity(1);
		roomDTO.setFare(2000);
		roomDTO.setRoomType(RoomType.STANDARD);
		roomDTO.setAc(false);
		roomDTO.setAvailable(true);
		Room room = roomService.addRoom(hotel.getHotelId(), roomDTO);
		return room.getRoomId();
	}

	private int createBookingAndGetPaymentId(String guestEmail, String ownerEmail, int roomNumber, String hotelName) {
		int userId = createGuest(guestEmail);
		int roomId = createHotelAndRoom(ownerEmail, roomNumber, hotelName);

		BookingDTO bookingDTO = new BookingDTO();
		bookingDTO.setUserId(userId);
		bookingDTO.setRoomId(roomId);
		bookingDTO.setCheckIn(LocalDate.now().plusDays(30));
		bookingDTO.setCheckOut(LocalDate.now().plusDays(32));
		bookingDTO.setAdults(1);
		bookingDTO.setChildrens(0);

		BookingResponseDTO booking = bookingService.createBooking(bookingDTO);
		Payment payment = paymentService.getPaymentByBooking(booking.getBookingId());
		return Math.toIntExact(payment.getPaymentId());
	}

	@Test
	@Order(1)
	void testGetPaymentById_Success() {
		int paymentId = createBookingAndGetPaymentId("pay_guest1@cozy.com", "pay_owner1@cozy.com", 101, "Pay Hotel 1");

		Payment payment = paymentService.getPaymentById(paymentId);

		assertNotNull(payment);
		assertEquals(paymentId, payment.getPaymentId());
		assertEquals(PaymentStatus.PENDING, payment.getStatus());
	}

	@Test
	@Order(2)
	void testGetPaymentById_NotFound_ThrowsPaymentException() {
		assertThrows(PaymentException.class, () -> paymentService.getPaymentById(999999));
	}

	@Test
	@Order(3)
	void testGetPaymentByBooking_Success() {
		int userId = createGuest("pay_guest2@cozy.com");
		int roomId = createHotelAndRoom("pay_owner2@cozy.com", 201, "Pay Hotel 2");

		BookingDTO bookingDTO = new BookingDTO();
		bookingDTO.setUserId(userId);
		bookingDTO.setRoomId(roomId);
		bookingDTO.setCheckIn(LocalDate.now().plusDays(40));
		bookingDTO.setCheckOut(LocalDate.now().plusDays(43));
		bookingDTO.setAdults(1);
		bookingDTO.setChildrens(0);
		BookingResponseDTO booking = bookingService.createBooking(bookingDTO);

		Payment payment = paymentService.getPaymentByBooking(booking.getBookingId());

		assertNotNull(payment);
		assertEquals(PaymentStatus.PENDING, payment.getStatus());
		assertNotNull(payment.getTransactionId());
	}

	@Test
	@Order(4)
	void testGetAllPayment_ReturnsList() {
		List<Payment> payments = paymentService.getAllPayment();
		assertNotNull(payments);
		assertTrue(payments.size() > 0);
	}

	@Test
	@Order(5)
	void testUpdatePaymentStatus_ToSuccess_ConfirmsBooking() {
		int paymentId = createBookingAndGetPaymentId("pay_guest3@cozy.com", "pay_owner3@cozy.com", 301, "Pay Hotel 3");

		UpdatePaymentStatusDTO dto = new UpdatePaymentStatusDTO();
		dto.setPaymentId(paymentId);
		dto.setStatus(PaymentStatus.SUCCESS);

		Payment updated = paymentService.updatePaymentStatus(dto);

		assertEquals(PaymentStatus.SUCCESS, updated.getStatus());
		// Booking should now be CONFIRMED
		BookingResponseDTO booking = bookingService.getBookingById(updated.getBooking().getBookingId());
		assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
	}

	@Test
	@Order(6)
	void testUpdatePaymentStatus_ToFailed_CancelsBooking() {
		int paymentId = createBookingAndGetPaymentId("pay_guest4@cozy.com", "pay_owner4@cozy.com", 401, "Pay Hotel 4");

		UpdatePaymentStatusDTO dto = new UpdatePaymentStatusDTO();
		dto.setPaymentId(paymentId);
		dto.setStatus(PaymentStatus.FAILED);

		Payment updated = paymentService.updatePaymentStatus(dto);

		assertEquals(PaymentStatus.FAILED, updated.getStatus());
		BookingResponseDTO booking = bookingService.getBookingById(updated.getBooking().getBookingId());
		assertEquals(BookingStatus.CANCELLED, booking.getStatus());
	}

	@Test
	@Order(7)
	void testUpdatePaymentStatus_NotFound_ThrowsPaymentException() {
		UpdatePaymentStatusDTO dto = new UpdatePaymentStatusDTO();
		dto.setPaymentId(999999);
		dto.setStatus(PaymentStatus.SUCCESS);

		assertThrows(PaymentException.class, () -> paymentService.updatePaymentStatus(dto));
	}

	@Test
	@Order(8)
	void testDeletePayment_OnlyFailedStatus_Success() {
		int paymentId = createBookingAndGetPaymentId("pay_guest5@cozy.com", "pay_owner5@cozy.com", 501, "Pay Hotel 5");

		// First mark it FAILED
		UpdatePaymentStatusDTO dto = new UpdatePaymentStatusDTO();
		dto.setPaymentId(paymentId);
		dto.setStatus(PaymentStatus.FAILED);
		paymentService.updatePaymentStatus(dto);

		boolean result = paymentService.deletePayment(paymentId);
		assertTrue(result);

		assertThrows(PaymentException.class, () -> paymentService.getPaymentById(paymentId));
	}

	@Test
	@Order(9)
	void testDeletePayment_NonFailedStatus_ThrowsPaymentException() {
		int paymentId = createBookingAndGetPaymentId("pay_guest6@cozy.com", "pay_owner6@cozy.com", 601, "Pay Hotel 6");
		// Still PENDING — deletion must fail
		assertThrows(PaymentException.class, () -> paymentService.deletePayment(paymentId));
	}

}
