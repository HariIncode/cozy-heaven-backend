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
import com.hexaware.cozy_heaven.dto.RefundDTO;
import com.hexaware.cozy_heaven.dto.RoomDTO;
import com.hexaware.cozy_heaven.dto.UpdatePaymentStatusDTO;
import com.hexaware.cozy_heaven.dto.UserDTO;
import com.hexaware.cozy_heaven.dto.response.BookingResponseDTO;
import com.hexaware.cozy_heaven.dto.response.HotelResponseDTO;
import com.hexaware.cozy_heaven.dto.response.RefundResponseDTO;
import com.hexaware.cozy_heaven.entity.Payment;
import com.hexaware.cozy_heaven.exception.RefundException;
import com.hexaware.cozy_heaven.model.Bed;
import com.hexaware.cozy_heaven.model.Gender;
import com.hexaware.cozy_heaven.model.PaymentStatus;
import com.hexaware.cozy_heaven.model.RefundStatus;
import com.hexaware.cozy_heaven.model.Role;
import com.hexaware.cozy_heaven.model.RoomType;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RefundServiceImplTest {

    @Autowired
    RefundService refundService;

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
        dto.setName("Refund Guest");
        dto.setEmail(email);
        dto.setPassword("Guest@1234");
        dto.setGender(Gender.MALE);
        dto.setContactNumber("9666666666");
        dto.setAddress("Refund Street, Kolkata");
        dto.setRole(Role.GUEST);
        return userService.addUser(dto).getUserId();
    }

    private int createHotelAndRoom(String ownerEmail, int roomNumber, String hotelName) {
        UserDTO ownerDTO = new UserDTO();
        ownerDTO.setName("Refund Owner");
        ownerDTO.setEmail(ownerEmail);
        ownerDTO.setPassword("Owner@1234");
        ownerDTO.setGender(Gender.FEMALE);
        ownerDTO.setContactNumber("9777777777");
        ownerDTO.setAddress("Refund Owner Address, Ahmedabad");
        ownerDTO.setRole(Role.HOTEL_OWNER);
        int ownerId = userService.addUser(ownerDTO).getUserId();

        HotelDTO hotelDTO = new HotelDTO();
        hotelDTO.setOwnerId(ownerId);
        hotelDTO.setName(hotelName);
        hotelDTO.setLocation("Kolkata");
        hotelDTO.setDescription("Refund test hotel");
        hotelDTO.setImageUrl("https://example.com/ref.jpg");
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
        roomDTO.setSizeSQM(22f);
        roomDTO.setBedSize(Bed.DOUBLE);
        roomDTO.setCapacity(2);
        roomDTO.setFare(4000);
        roomDTO.setRoomType(RoomType.DELUXE);
        roomDTO.setAc(true);
        roomDTO.setAvailable(true);
        return roomService.addRoom(hotel.getHotelId(), roomDTO).getRoomId();
    }

    private int createConfirmedBooking(int userId, int roomId, int dayOffset) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setUserId(userId);
        bookingDTO.setRoomId(roomId);
        bookingDTO.setCheckIn(LocalDate.now().plusDays(dayOffset));
        bookingDTO.setCheckOut(LocalDate.now().plusDays(dayOffset + 3));
        bookingDTO.setAdults(2);
        bookingDTO.setChildrens(0);
        BookingResponseDTO booking = bookingService.createBooking(bookingDTO);

        Payment payment = paymentService.getPaymentByBooking(booking.getBookingId());
        UpdatePaymentStatusDTO statusDTO = new UpdatePaymentStatusDTO();
        statusDTO.setPaymentId(Math.toIntExact(payment.getPaymentId()));
        statusDTO.setStatus(PaymentStatus.SUCCESS);
        paymentService.updatePaymentStatus(statusDTO);

        return booking.getBookingId();
    }

    @Test
    @Order(1)
    void testCreateRefund_Success() {
        int userId    = createGuest("refund_guest1@cozy.com");
        int roomId    = createHotelAndRoom("refund_owner1@cozy.com", 101, "Refund Hotel 1");
        int bookingId = createConfirmedBooking(userId, roomId, 60);

        CancelBookingDTO cancelDTO = new CancelBookingDTO();
        cancelDTO.setBookingId(bookingId);
        cancelDTO.setReason("Cancelled for refund test");
        bookingService.cancelBooking(cancelDTO);

        RefundResponseDTO refund = refundService.getRefundByBooking(bookingId);

        assertNotNull(refund);
        assertTrue(refund.getRefundAmount() > 0);
        assertEquals(RefundStatus.REQUESTED, refund.getRefundStatus());
    }

    @Test
    @Order(2)
    void testCreateRefund_BookingNotFound_ThrowsRefundException() {
        RefundDTO dto = new RefundDTO();
        dto.setBookingId(999999);
        dto.setRefundPercentage(100);
        dto.setReason("Booking does not exist test");

        assertThrows(RefundException.class, () -> refundService.createRefund(dto));
    }

    @Test
    @Order(3)
    void testCreateRefund_DuplicateRefund_ThrowsRefundException() {
        int userId    = createGuest("refund_guest2@cozy.com");
        int roomId    = createHotelAndRoom("refund_owner2@cozy.com", 201, "Refund Hotel 2");
        int bookingId = createConfirmedBooking(userId, roomId, 70);

        RefundDTO dto = new RefundDTO();
        dto.setBookingId(bookingId);
        dto.setRefundPercentage(100);
        dto.setReason("First refund for booking");
        refundService.createRefund(dto);

        assertThrows(RefundException.class, () -> refundService.createRefund(dto));
    }


    @Test
    @Order(4)
    void testGetRefundById_Success() {
        int userId    = createGuest("refund_guest3@cozy.com");
        int roomId    = createHotelAndRoom("refund_owner3@cozy.com", 301, "Refund Hotel 3");
        int bookingId = createConfirmedBooking(userId, roomId, 80);

        RefundDTO dto = new RefundDTO();
        dto.setBookingId(bookingId);
        dto.setRefundPercentage(50);
        dto.setReason("Partial refund test for booking");
        RefundResponseDTO created = refundService.createRefund(dto);

        RefundResponseDTO fetched = refundService.getRefundById(Math.toIntExact(created.getRefundId()));

        assertNotNull(fetched);
        assertEquals(created.getRefundId(), fetched.getRefundId());
        assertEquals(bookingId, fetched.getBookingId());
    }

    @Test
    @Order(5)
    void testGetRefundById_NotFound_ThrowsRefundException() {
        assertThrows(RefundException.class, () -> refundService.getRefundById(999999));
    }


    @Test
    @Order(6)
    void testGetRefundByBooking_NotFound_ThrowsRefundException() {
        assertThrows(RefundException.class, () -> refundService.getRefundByBooking(888888));
    }


    @Test
    @Order(7)
    void testGetAllRefund_ReturnsList() {
        List<RefundResponseDTO> refunds = refundService.getAllRefund();
        assertNotNull(refunds);
    }


    @Test
    @Order(8)
    void testApproveRefund_Success() {
        int userId    = createGuest("refund_guest4@cozy.com");
        int roomId    = createHotelAndRoom("refund_owner4@cozy.com", 401, "Refund Hotel 4");
        int bookingId = createConfirmedBooking(userId, roomId, 90);

        RefundDTO dto = new RefundDTO();
        dto.setBookingId(bookingId);
        dto.setRefundPercentage(100);
        dto.setReason("Approve refund test for this booking");
        RefundResponseDTO created = refundService.createRefund(dto);

        RefundResponseDTO approved = refundService.approveRefund(Math.toIntExact(created.getRefundId()));

        assertEquals(RefundStatus.APPROVED, approved.getRefundStatus());
    }

    @Test
    @Order(9)
    void testApproveRefund_NotRequested_ThrowsRefundException() {
        int userId    = createGuest("refund_guest5@cozy.com");
        int roomId    = createHotelAndRoom("refund_owner5@cozy.com", 501, "Refund Hotel 5");
        int bookingId = createConfirmedBooking(userId, roomId, 95);

        RefundDTO dto = new RefundDTO();
        dto.setBookingId(bookingId);
        dto.setRefundPercentage(100);
        dto.setReason("Already approved refund test for booking");
        RefundResponseDTO created = refundService.createRefund(dto);

        refundService.approveRefund(Math.toIntExact(created.getRefundId()));

        assertThrows(RefundException.class, () -> refundService.approveRefund(Math.toIntExact(created.getRefundId())));
    }


    @Test
    @Order(10)
    void testProcessRefund_Success() {
        int userId    = createGuest("refund_guest6@cozy.com");
        int roomId    = createHotelAndRoom("refund_owner6@cozy.com", 601, "Refund Hotel 6");
        int bookingId = createConfirmedBooking(userId, roomId, 100);

        RefundDTO dto = new RefundDTO();
        dto.setBookingId(bookingId);
        dto.setRefundPercentage(50);
        dto.setReason("Process refund test for this booking");
        RefundResponseDTO created = refundService.createRefund(dto);
        refundService.approveRefund(Math.toIntExact(created.getRefundId()));

        RefundResponseDTO processed = refundService.processRefund(Math.toIntExact(created.getRefundId()));

        assertEquals(RefundStatus.PROCESSED, processed.getRefundStatus());
        assertNotNull(processed.getProcessedAt());
    }

    @Test
    @Order(11)
    void testProcessRefund_NotApproved_ThrowsRefundException() {
        int userId    = createGuest("refund_guest7@cozy.com");
        int roomId    = createHotelAndRoom("refund_owner7@cozy.com", 701, "Refund Hotel 7");
        int bookingId = createConfirmedBooking(userId, roomId, 110);

        RefundDTO dto = new RefundDTO();
        dto.setBookingId(bookingId);
        dto.setRefundPercentage(50);
        dto.setReason("Process without approve test for this booking");
        RefundResponseDTO created = refundService.createRefund(dto); // REQUESTED

        assertThrows(RefundException.class, () -> refundService.processRefund(Math.toIntExact(created.getRefundId())));
    }


    @Test
    @Order(12)
    void testRejectRefund_Success() {
        int userId    = createGuest("refund_guest8@cozy.com");
        int roomId    = createHotelAndRoom("refund_owner8@cozy.com", 801, "Refund Hotel 8");
        int bookingId = createConfirmedBooking(userId, roomId, 120);

        RefundDTO dto = new RefundDTO();
        dto.setBookingId(bookingId);
        dto.setRefundPercentage(25);
        dto.setReason("Reject refund test for this booking");
        RefundResponseDTO created = refundService.createRefund(dto);

        RefundResponseDTO rejected = refundService.rejectRefund(Math.toIntExact(created.getRefundId()), "Non-eligible cancellation");

        assertEquals(RefundStatus.REJECTED, rejected.getRefundStatus());
        assertEquals("Non-eligible cancellation", rejected.getReason());
    }

    @Test
    @Order(13)
    void testRejectRefund_AlreadyProcessed_ThrowsRefundException() {
        int userId    = createGuest("refund_guest9@cozy.com");
        int roomId    = createHotelAndRoom("refund_owner9@cozy.com", 901, "Refund Hotel 9");
        int bookingId = createConfirmedBooking(userId, roomId, 130);

        RefundDTO dto = new RefundDTO();
        dto.setBookingId(bookingId);
        dto.setRefundPercentage(100);
        dto.setReason("Processed refund reject attempt test for booking");
        RefundResponseDTO created = refundService.createRefund(dto);
        refundService.approveRefund(Math.toIntExact(created.getRefundId()));
        refundService.processRefund(Math.toIntExact(created.getRefundId())); // → PROCESSED

        assertThrows(RefundException.class,
                () -> refundService.rejectRefund(Math.toIntExact(created.getRefundId()), "Too late to reject"));
    }


    @Test
    @Order(14)
    void testCreateRefund_AmountCalculatedCorrectly() {
        int userId    = createGuest("refund_guest10@cozy.com");
        int roomId    = createHotelAndRoom("refund_owner10@cozy.com", 1001, "Refund Hotel 10");
        int bookingId = createConfirmedBooking(userId, roomId, 140);

        RefundDTO dto = new RefundDTO();
        dto.setBookingId(bookingId);
        dto.setRefundPercentage(50);
        dto.setReason("Refund amount calculation test for booking");
        RefundResponseDTO created = refundService.createRefund(dto);

        assertEquals(6000, created.getRefundAmount());
        assertEquals(12000, created.getOriginalAmount());
    }
}

