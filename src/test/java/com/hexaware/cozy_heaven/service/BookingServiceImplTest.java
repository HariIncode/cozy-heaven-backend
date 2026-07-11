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
import com.hexaware.cozy_heaven.dto.CancelBookingDTO;
import com.hexaware.cozy_heaven.dto.HotelDTO;
import com.hexaware.cozy_heaven.dto.RoomDTO;
import com.hexaware.cozy_heaven.dto.UpdatePaymentStatusDTO;
import com.hexaware.cozy_heaven.dto.UserDTO;
import com.hexaware.cozy_heaven.dto.response.BookingResponseDTO;
import com.hexaware.cozy_heaven.dto.response.HotelResponseDTO;
import com.hexaware.cozy_heaven.entity.Room;
import com.hexaware.cozy_heaven.exception.BookingException;
import com.hexaware.cozy_heaven.model.Bed;
import com.hexaware.cozy_heaven.model.BookingStatus;
import com.hexaware.cozy_heaven.model.Gender;
import com.hexaware.cozy_heaven.model.PaymentStatus;
import com.hexaware.cozy_heaven.model.Role;
import com.hexaware.cozy_heaven.model.RoomType;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingServiceImplTest {
	
	@Autowired
    BookingService bookingService;

    @Autowired
    UserService userService;

    @Autowired
    HotelService hotelService;

    @Autowired
    RoomService roomService;

    @Autowired
    PaymentService paymentService;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
      // TODO document why this method is empty
    }
    
    private int createGuest(String email) {
        UserDTO dto = new UserDTO();
        dto.setName("Booking Guest");
        dto.setEmail(email);
        dto.setPassword("Guest@1234");
        dto.setGender(Gender.MALE);
        dto.setContactNumber("9222222222");
        dto.setAddress("Guest Address, Chennai");
        dto.setRole(Role.GUEST);
        return userService.addUser(dto).getUserId();
    }

    private int createOwner(String email) {
        UserDTO dto = new UserDTO();
        dto.setName("Booking Owner");
        dto.setEmail(email);
        dto.setPassword("Owner@1234");
        dto.setGender(Gender.FEMALE);
        dto.setContactNumber("9333333333");
        dto.setAddress("Owner Address, Mumbai");
        dto.setRole(Role.HOTEL_OWNER);
        return userService.addUser(dto).getUserId();
    }

    private int createHotelAndRoom(int ownerId, int roomNumber, String uniqueHotelName) {
        HotelDTO hotelDTO = new HotelDTO();
        hotelDTO.setOwnerId(ownerId);
        hotelDTO.setName(uniqueHotelName);
        hotelDTO.setLocation("Hyderabad");
        hotelDTO.setDescription("Booking test hotel");
        hotelDTO.setImageUrl("https://example.com/bk.jpg");
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
        roomDTO.setSizeSQM(30f);
        roomDTO.setBedSize(Bed.KING);
        roomDTO.setCapacity(3);
        roomDTO.setFare(3000);
        roomDTO.setRoomType(RoomType.STANDARD);
        roomDTO.setAc(true);
        roomDTO.setAvailable(true);
        Room room = roomService.addRoom(hotel.getHotelId(), roomDTO);
        return room.getRoomId();
    }

    private BookingDTO buildBookingDTO(int userId, int roomId, int daysFromNow, int nights) {
        BookingDTO dto = new BookingDTO();
        dto.setUserId(userId);
        dto.setRoomId(roomId);
        dto.setCheckIn(LocalDate.now().plusDays(daysFromNow));
        dto.setCheckOut(LocalDate.now().plusDays(daysFromNow + nights));
        dto.setAdults(2);
        dto.setChildrens(0);
        return dto;
    }

    /** Confirm a booking by paying (sets booking to CONFIRMED). */
    private void confirmBooking(int bookingId) {
        var payment = paymentService.getPaymentByBooking(bookingId);
        UpdatePaymentStatusDTO statusDTO = new UpdatePaymentStatusDTO();
        statusDTO.setPaymentId(Math.toIntExact(payment.getPaymentId()));
        statusDTO.setStatus(PaymentStatus.SUCCESS);
        paymentService.updatePaymentStatus(statusDTO);
    }
    
    @Test
    @Order(1)
    void testCreateBooking_Success() {
        int userId  = createGuest("booking_guest1@cozy.com");
        int ownerId = createOwner("booking_owner1@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 101, "Booking Hotel 1");

        BookingDTO dto = buildBookingDTO(userId, roomId, 10, 3);
        BookingResponseDTO response = bookingService.createBooking(dto);

        assertNotNull(response);
        assertNotNull(response.getBookingId());
        assertEquals(BookingStatus.PENDING, response.getStatus());
        assertEquals(3, response.getTotalNights());
        assertEquals(9000, response.getTotalAmount()); // 3000 * 3
    }

    @Test
    @Order(2)
    void testCreateBooking_UserNotFound_ThrowsBookingException() {
        int ownerId = createOwner("booking_owner2@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 201, "Booking Hotel 2");

        BookingDTO dto = buildBookingDTO(999999, roomId, 15, 2);
        assertThrows(BookingException.class, () -> bookingService.createBooking(dto));
    }

    @Test
    @Order(3)
    void testCreateBooking_RoomNotFound_ThrowsBookingException() {
        int userId = createGuest("booking_guest2@cozy.com");

        BookingDTO dto = buildBookingDTO(userId, 999999, 15, 2);
        assertThrows(BookingException.class, () -> bookingService.createBooking(dto));
    }

    @Test
    @Order(4)
    void testCreateBooking_InvalidDates_CheckOutBeforeCheckIn_ThrowsBookingException() {
        int userId  = createGuest("booking_guest3@cozy.com");
        int ownerId = createOwner("booking_owner3@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 301, "Booking Hotel 3");

        BookingDTO dto = new BookingDTO();
        dto.setUserId(userId);
        dto.setRoomId(roomId);
        dto.setCheckIn(LocalDate.now().plusDays(10));
        dto.setCheckOut(LocalDate.now().plusDays(5)); // checkout BEFORE checkin
        dto.setAdults(1);
        dto.setChildrens(0);

        assertThrows(BookingException.class, () -> bookingService.createBooking(dto));
    }

    @Test
    @Order(5)
    void testCreateBooking_RoomUnavailable_ThrowsBookingException() {
        int userId  = createGuest("booking_guest4@cozy.com");
        int ownerId = createOwner("booking_owner4@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 401, "Booking Hotel 4");

        // First booking — occupies the room
        bookingService.createBooking(buildBookingDTO(userId, roomId, 20, 5));

        // Second booking — overlapping dates, same room → must fail
        int userId2 = createGuest("booking_guest4b@cozy.com");
        BookingDTO overlapping = buildBookingDTO(userId2, roomId, 22, 2);
        assertThrows(BookingException.class, () -> bookingService.createBooking(overlapping));
    }
    
    @Test
    @Order(6)
    void testGetBookingById_Success() {
        int userId  = createGuest("booking_guest5@cozy.com");
        int ownerId = createOwner("booking_owner5@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 501, "Booking Hotel 5");

        BookingResponseDTO created = bookingService.createBooking(buildBookingDTO(userId, roomId, 30, 2));
        BookingResponseDTO fetched = bookingService.getBookingById(created.getBookingId());

        assertNotNull(fetched);
        assertEquals(created.getBookingId(), fetched.getBookingId());
    }

    @Test
    @Order(7)
    void testGetBookingById_NotFound_ThrowsBookingException() {
        assertThrows(BookingException.class, () -> bookingService.getBookingById(999999));
    }

    @Test
    @Order(8)
    void testGetAllBooking_ReturnsList() {
        List<BookingResponseDTO> bookings = bookingService.getAllBooking();
        assertNotNull(bookings);
        assertTrue(bookings.size() > 0);
    }
    
    @Test
    @Order(9)
    void testGetBookingsByUser_Success() {
        int userId  = createGuest("booking_guest6@cozy.com");
        int ownerId = createOwner("booking_owner6@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 601, "Booking Hotel 6");

        bookingService.createBooking(buildBookingDTO(userId, roomId, 40, 2));

        List<BookingResponseDTO> bookings = bookingService.getBookingsByUser(userId);
        assertNotNull(bookings);
        assertTrue(bookings.size() > 0);
        assertEquals(userId, bookings.get(0).getUserId());
    }
    
    @Test
    @Order(10)
    void testCancelBooking_Success() {
        int userId  = createGuest("booking_guest7@cozy.com");
        int ownerId = createOwner("booking_owner7@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 701, "Booking Hotel 7");

        BookingResponseDTO created = bookingService.createBooking(buildBookingDTO(userId, roomId, 50, 3));

        CancelBookingDTO cancelDTO = new CancelBookingDTO();
        cancelDTO.setBookingId(created.getBookingId());
        cancelDTO.setReason("Change of travel plans");

        BookingResponseDTO cancelled = bookingService.cancelBooking(cancelDTO);

        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
        assertEquals("Change of travel plans", cancelled.getCancellationReason());
    }

    @Test
    @Order(11)
    void testCancelBooking_AlreadyCancelled_ThrowsBookingException() {
        int userId  = createGuest("booking_guest8@cozy.com");
        int ownerId = createOwner("booking_owner8@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 801, "Booking Hotel 8");

        BookingResponseDTO created = bookingService.createBooking(buildBookingDTO(userId, roomId, 60, 2));

        CancelBookingDTO cancelDTO = new CancelBookingDTO();
        cancelDTO.setBookingId(created.getBookingId());
        cancelDTO.setReason("First cancellation");

        bookingService.cancelBooking(cancelDTO); // first cancel

        // Second attempt must fail
        cancelDTO.setReason("Second attempt");
        assertThrows(BookingException.class, () -> bookingService.cancelBooking(cancelDTO));
    }

    @Test
    @Order(12)
    void testCompleteBooking_Success() {
        int userId  = createGuest("booking_guest9@cozy.com");
        int ownerId = createOwner("booking_owner9@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 901, "Booking Hotel 9");

        BookingResponseDTO created = bookingService.createBooking(buildBookingDTO(userId, roomId, 70, 2));
        confirmBooking(created.getBookingId());

        BookingResponseDTO completed = bookingService.completeBooking(created.getBookingId());

        assertEquals(BookingStatus.COMPLETED, completed.getStatus());
    }

    @Test
    @Order(13)
    void testCompleteBooking_NotConfirmed_ThrowsBookingException() {
        int userId  = createGuest("booking_guest10@cozy.com");
        int ownerId = createOwner("booking_owner10@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 1001, "Booking Hotel 10");

        BookingResponseDTO created = bookingService.createBooking(buildBookingDTO(userId, roomId, 80, 2));
        // Status is still PENDING — complete should fail
        assertThrows(BookingException.class, () -> bookingService.completeBooking(created.getBookingId()));
    }
    
    @Test
    @Order(14)
    void testMarkNoShow_Success() {
        int userId  = createGuest("booking_guest11@cozy.com");
        int ownerId = createOwner("booking_owner11@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 1101, "Booking Hotel 11");

        BookingResponseDTO created = bookingService.createBooking(buildBookingDTO(userId, roomId, 90, 2));
        confirmBooking(created.getBookingId());

        BookingResponseDTO noShow = bookingService.markNoShow(created.getBookingId());
        assertEquals(BookingStatus.NO_SHOW, noShow.getStatus());
    }

    @Test
    @Order(15)
    void testMarkNoShow_NotConfirmed_ThrowsBookingException() {
        int userId  = createGuest("booking_guest12@cozy.com");
        int ownerId = createOwner("booking_owner12@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 1201, "Booking Hotel 12");

        BookingResponseDTO created = bookingService.createBooking(buildBookingDTO(userId, roomId, 100, 2));
        // Still PENDING — mark no-show must fail
        assertThrows(BookingException.class, () -> bookingService.markNoShow(created.getBookingId()));
    }
    
    @Test
    @Order(16)
    void testDeleteBooking_Success() {
        int userId  = createGuest("booking_guest13@cozy.com");
        int ownerId = createOwner("booking_owner13@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 1301, "Booking Hotel 13");

        BookingResponseDTO created = bookingService.createBooking(buildBookingDTO(userId, roomId, 110, 2));

        CancelBookingDTO cancelDTO = new CancelBookingDTO();
        cancelDTO.setBookingId(created.getBookingId());
        cancelDTO.setReason("Cancel before delete");
        bookingService.cancelBooking(cancelDTO);

        boolean result = bookingService.deleteBooking(created.getBookingId());

        assertTrue(result);
        assertThrows(BookingException.class, () -> bookingService.getBookingById(created.getBookingId()));
    }

    @Test
    @Order(17)
    void testDeleteBooking_NotCancelled_ThrowsBookingException() {
        int userId  = createGuest("booking_guest14@cozy.com");
        int ownerId = createOwner("booking_owner14@cozy.com");
        int roomId  = createHotelAndRoom(ownerId, 1401, "Booking Hotel 14");

        BookingResponseDTO created = bookingService.createBooking(buildBookingDTO(userId, roomId, 120, 2));
        // Status is PENDING — deletion must fail
        assertThrows(BookingException.class, () -> bookingService.deleteBooking(created.getBookingId()));
    }

}
