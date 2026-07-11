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
import com.hexaware.cozy_heaven.dto.ReviewDTO;
import com.hexaware.cozy_heaven.dto.RoomDTO;
import com.hexaware.cozy_heaven.dto.UpdatePaymentStatusDTO;
import com.hexaware.cozy_heaven.dto.UserDTO;
import com.hexaware.cozy_heaven.dto.response.BookingResponseDTO;
import com.hexaware.cozy_heaven.dto.response.HotelResponseDTO;
import com.hexaware.cozy_heaven.dto.response.ReviewResponseDTO;
import com.hexaware.cozy_heaven.entity.Payment;
import com.hexaware.cozy_heaven.exception.ReviewException;
import com.hexaware.cozy_heaven.model.Bed;
import com.hexaware.cozy_heaven.model.Gender;
import com.hexaware.cozy_heaven.model.PaymentStatus;
import com.hexaware.cozy_heaven.model.Role;
import com.hexaware.cozy_heaven.model.RoomType;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReviewServiceImplTest {
	
	@Autowired
    ReviewService reviewService;

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
    }

    private int createGuest(String email) {
        UserDTO dto = new UserDTO();
        dto.setName("Review Guest");
        dto.setEmail(email);
        dto.setPassword("Guest@1234");
        dto.setGender(Gender.MALE);
        dto.setContactNumber("9100000000");
        dto.setAddress("Review Street, Bhopal");
        dto.setRole(Role.GUEST);
        return userService.addUser(dto).getUserId();
    }

    /** Returns [hotelId, roomId] */
    private int[] createHotelAndRoom(String ownerEmail, int roomNumber, String hotelName) {
        UserDTO ownerDTO = new UserDTO();
        ownerDTO.setName("Review Owner");
        ownerDTO.setEmail(ownerEmail);
        ownerDTO.setPassword("Owner@1234");
        ownerDTO.setGender(Gender.FEMALE);
        ownerDTO.setContactNumber("9200000000");
        ownerDTO.setAddress("Owner Address, Surat");
        ownerDTO.setRole(Role.HOTEL_OWNER);
        int ownerId = userService.addUser(ownerDTO).getUserId();

        HotelDTO hotelDTO = new HotelDTO();
        hotelDTO.setOwnerId(ownerId);
        hotelDTO.setName(hotelName);
        hotelDTO.setLocation("Bhopal");
        hotelDTO.setDescription("Review test hotel");
        hotelDTO.setImageUrl("https://example.com/rv.jpg");
        hotelDTO.setWifi(true);
        hotelDTO.setParking(true);
        hotelDTO.setDining(false);
        hotelDTO.setGym(false);
        hotelDTO.setPool(false);
        hotelDTO.setRoomService(false);
        HotelResponseDTO hotel = hotelService.addHotel(hotelDTO);

        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setHotelId(hotel.getHotelId());
        roomDTO.setRoomNumber(roomNumber);
        roomDTO.setSizeSQM(28f);
        roomDTO.setBedSize(Bed.DOUBLE);
        roomDTO.setCapacity(2);
        roomDTO.setFare(3500);
        roomDTO.setRoomType(RoomType.DELUXE);
        roomDTO.setAc(true);
        roomDTO.setAvailable(true);
        int roomId = roomService.addRoom(hotel.getHotelId(), roomDTO).getRoomId();

        return new int[]{ hotel.getHotelId(), roomId };
    }

    /** Book → confirm → complete. Returns bookingId. */
    private int createCompletedBooking(int userId, int roomId, int dayOffset) {
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

        bookingService.completeBooking(booking.getBookingId());
        return booking.getBookingId();
    }

    private ReviewDTO buildReviewDTO(int userId, int hotelId, int bookingId, float rating, String comments) {
        ReviewDTO dto = new ReviewDTO();
        dto.setUserId(userId);
        dto.setHotelId(hotelId);
        dto.setBookingId(bookingId);
        dto.setRating(rating);
        dto.setComments(comments);
        return dto;
    }
    
    @Test
    @Order(1)
    void testAddReview_Success() {
        int userId    = createGuest("rv_guest1@cozy.com");
        int[] ids     = createHotelAndRoom("rv_owner1@cozy.com", 101, "Review Hotel 1");
        int hotelId   = ids[0];
        int roomId    = ids[1];
        int bookingId = createCompletedBooking(userId, roomId, 30);

        ReviewResponseDTO saved = reviewService.addReview(
                buildReviewDTO(userId, hotelId, bookingId, 4.5f, "Great stay, very cozy!"));

        assertNotNull(saved);
        assertNotNull(saved.getReviewId());
        assertEquals(4.5f, saved.getRating());
        assertEquals("Great stay, very cozy!", saved.getComments());
        assertEquals(userId,  saved.getUserId());
        assertEquals(hotelId, saved.getHotelId());
    }

    @Test
    @Order(2)
    void testAddReview_BookingNotCompleted_ThrowsReviewException() {
        int userId  = createGuest("rv_guest2@cozy.com");
        int[] ids   = createHotelAndRoom("rv_owner2@cozy.com", 201, "Review Hotel 2");
        int hotelId = ids[0];
        int roomId  = ids[1];

        // Book and confirm only — status = CONFIRMED, not COMPLETED
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setUserId(userId);
        bookingDTO.setRoomId(roomId);
        bookingDTO.setCheckIn(LocalDate.now().plusDays(40));
        bookingDTO.setCheckOut(LocalDate.now().plusDays(42));
        bookingDTO.setAdults(1);
        bookingDTO.setChildrens(0);
        BookingResponseDTO booking = bookingService.createBooking(bookingDTO);

        Payment payment = paymentService.getPaymentByBooking(booking.getBookingId());
        UpdatePaymentStatusDTO statusDTO = new UpdatePaymentStatusDTO();
        statusDTO.setPaymentId(Math.toIntExact(payment.getPaymentId()));
        statusDTO.setStatus(PaymentStatus.SUCCESS);
        paymentService.updatePaymentStatus(statusDTO);

        ReviewDTO dto = buildReviewDTO(userId, hotelId, booking.getBookingId(), 3.0f, "Not complete yet");
        assertThrows(ReviewException.class, () -> reviewService.addReview(dto));
    }

    @Test
    @Order(3)
    void testAddReview_WrongUser_ThrowsReviewException() {
        int userId      = createGuest("rv_guest3@cozy.com");
        int wrongUserId = createGuest("rv_wrong_user@cozy.com");
        int[] ids       = createHotelAndRoom("rv_owner3@cozy.com", 301, "Review Hotel 3");
        int hotelId     = ids[0];
        int roomId      = ids[1];
        int bookingId   = createCompletedBooking(userId, roomId, 50);

        ReviewDTO dto = buildReviewDTO(wrongUserId, hotelId, bookingId, 2.0f, "Not mine");
        assertThrows(ReviewException.class, () -> reviewService.addReview(dto));
    }

    @Test
    @Order(4)
    void testAddReview_WrongHotel_ThrowsReviewException() {
        int userId    = createGuest("rv_guest4@cozy.com");
        int[] ids     = createHotelAndRoom("rv_owner4@cozy.com", 401, "Review Hotel 4");
        int roomId    = ids[1];
        int bookingId = createCompletedBooking(userId, roomId, 55);

        // Use a completely different hotelId
        ReviewDTO dto = buildReviewDTO(userId, 999999, bookingId, 3.0f, "Wrong hotel test");
        assertThrows(ReviewException.class, () -> reviewService.addReview(dto));
    }

    @Test
    @Order(5)
    void testAddReview_DuplicateReview_ThrowsReviewException() {
        int userId    = createGuest("rv_guest5@cozy.com");
        int[] ids     = createHotelAndRoom("rv_owner5@cozy.com", 501, "Review Hotel 5");
        int hotelId   = ids[0];
        int roomId    = ids[1];
        int bookingId = createCompletedBooking(userId, roomId, 60);

        ReviewDTO dto = buildReviewDTO(userId, hotelId, bookingId, 5.0f, "Excellent stay here!");
        reviewService.addReview(dto);

        // Second review for same booking must throw
        assertThrows(ReviewException.class, () -> reviewService.addReview(dto));
    }
    
    @Test
    @Order(6)
    void testGetReviewById_Success() {
        int userId    = createGuest("rv_guest6@cozy.com");
        int[] ids     = createHotelAndRoom("rv_owner6@cozy.com", 601, "Review Hotel 6");
        int hotelId   = ids[0];
        int roomId    = ids[1];
        int bookingId = createCompletedBooking(userId, roomId, 70);

        ReviewResponseDTO created = reviewService.addReview(
                buildReviewDTO(userId, hotelId, bookingId, 3.5f, "Good experience overall"));
        ReviewResponseDTO fetched = reviewService.getReviewById(Math.toIntExact(created.getReviewId()));

        assertNotNull(fetched);
        assertEquals(created.getReviewId(), fetched.getReviewId());
        assertEquals(hotelId, fetched.getHotelId());
    }

    @Test
    @Order(7)
    void testGetReviewById_NotFound_ThrowsReviewException() {
        assertThrows(ReviewException.class, () -> reviewService.getReviewById(999999));
    }
    
    @Test
    @Order(8)
    void testGetAllReviews_ReturnsList() {
        List<ReviewResponseDTO> reviews = reviewService.getAllReviews();
        assertNotNull(reviews);
        assertTrue(reviews.size() > 0);
    }
    
    @Test
    @Order(9)
    void testGetReviewsByHotel_Success() {
        int userId    = createGuest("rv_guest7@cozy.com");
        int[] ids     = createHotelAndRoom("rv_owner7@cozy.com", 701, "Review Hotel 7");
        int hotelId   = ids[0];
        int roomId    = ids[1];
        int bookingId = createCompletedBooking(userId, roomId, 80);

        reviewService.addReview(buildReviewDTO(userId, hotelId, bookingId, 4.0f, "Lovely place to stay!"));

        List<ReviewResponseDTO> reviews = reviewService.getReviewsByHotel(hotelId);
        assertNotNull(reviews);
        assertTrue(reviews.size() > 0);
        assertEquals(hotelId, reviews.get(0).getHotelId());
    }
    
    @Test
    @Order(10)
    void testGetReviewsByUser_Success() {
        int userId    = createGuest("rv_guest8@cozy.com");
        int[] ids     = createHotelAndRoom("rv_owner8@cozy.com", 801, "Review Hotel 8");
        int hotelId   = ids[0];
        int roomId    = ids[1];
        int bookingId = createCompletedBooking(userId, roomId, 90);

        reviewService.addReview(buildReviewDTO(userId, hotelId, bookingId, 4.5f, "Wonderful service overall."));

        List<ReviewResponseDTO> reviews = reviewService.getReviewsByUser(userId);
        assertNotNull(reviews);
        assertTrue(reviews.size() > 0);
        assertEquals(userId, reviews.get(0).getUserId());
    }

    @Test
    @Order(11)
    void testUpdateReview_Success() {
        int userId    = createGuest("rv_guest9@cozy.com");
        int[] ids     = createHotelAndRoom("rv_owner9@cozy.com", 901, "Review Hotel 9");
        int hotelId   = ids[0];
        int roomId    = ids[1];
        int bookingId = createCompletedBooking(userId, roomId, 100);

        ReviewResponseDTO created = reviewService.addReview(
                buildReviewDTO(userId, hotelId, bookingId, 3.0f, "Average stay overall."));

        ReviewDTO updateDTO = buildReviewDTO(userId, hotelId, bookingId, 5.0f, "Changed my mind - it was perfect!");
        ReviewResponseDTO updated = reviewService.updateReview(Math.toIntExact(created.getReviewId()), updateDTO);

        assertNotNull(updated);
        assertEquals(5.0f, updated.getRating());
        assertEquals("Changed my mind - it was perfect!", updated.getComments());
    }

    @Test
    @Order(12)
    void testUpdateReview_WrongOwner_ThrowsReviewException() {
        int userId       = createGuest("rv_guest10@cozy.com");
        int impersonator = createGuest("rv_impersonator@cozy.com");
        int[] ids        = createHotelAndRoom("rv_owner10@cozy.com", 1001, "Review Hotel 10");
        int hotelId      = ids[0];
        int roomId       = ids[1];
        int bookingId    = createCompletedBooking(userId, roomId, 110);

        ReviewResponseDTO created = reviewService.addReview(
                buildReviewDTO(userId, hotelId, bookingId, 3.0f, "Original review text."));

        ReviewDTO fraudDTO = buildReviewDTO(impersonator, hotelId, bookingId, 1.0f, "I did not write this!");
        assertThrows(ReviewException.class, () -> reviewService.updateReview(Math.toIntExact(created.getReviewId()), fraudDTO));
    }

    @Test
    @Order(13)
    void testUpdateReview_NotFound_ThrowsReviewException() {
        ReviewDTO dto = new ReviewDTO();
        dto.setUserId(1);
        dto.setHotelId(1);
        dto.setBookingId(1);
        dto.setRating(3.0f);
        dto.setComments("Ghost review");
        assertThrows(ReviewException.class, () -> reviewService.updateReview(999999, dto));
    }
    
    @Test
    @Order(14)
    void testDeleteReview_Success() {
        int userId    = createGuest("rv_guest11@cozy.com");
        int[] ids     = createHotelAndRoom("rv_owner11@cozy.com", 1101, "Review Hotel 11");
        int hotelId   = ids[0];
        int roomId    = ids[1];
        int bookingId = createCompletedBooking(userId, roomId, 120);

        ReviewResponseDTO created = reviewService.addReview(
                buildReviewDTO(userId, hotelId, bookingId, 2.5f, "Decent stay, nothing special."));

        boolean result = reviewService.deleteReview(Math.toIntExact(created.getReviewId()));

        assertTrue(result);
        assertThrows(ReviewException.class, () -> reviewService.getReviewById(Math.toIntExact(created.getReviewId())));
    }

    @Test
    @Order(15)
    void testDeleteReview_NotFound_ThrowsReviewException() {
        assertThrows(ReviewException.class, () -> reviewService.deleteReview(999999));
    }
    
    @Test
    @Order(16)
    void testAverageRating_UpdatedAfterReviewAdded() {
        int userId    = createGuest("rv_guest12@cozy.com");
        int[] ids     = createHotelAndRoom("rv_owner12@cozy.com", 1201, "Review Hotel 12");
        int hotelId   = ids[0];
        int roomId    = ids[1];
        int bookingId = createCompletedBooking(userId, roomId, 130);

        reviewService.addReview(buildReviewDTO(userId, hotelId, bookingId, 4.0f, "Great place!"));

        HotelResponseDTO hotel = hotelService.getHotelById(hotelId);
        assertEquals(4.0, hotel.getAverageRating(), 0.01);
        assertEquals(1, hotel.getTotalReviews());
    }
    
}
