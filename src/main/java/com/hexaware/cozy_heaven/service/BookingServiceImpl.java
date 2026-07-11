package com.hexaware.cozy_heaven.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hexaware.cozy_heaven.dto.BookingDTO;
import com.hexaware.cozy_heaven.dto.CancelBookingDTO;
import com.hexaware.cozy_heaven.dto.RefundDTO;
import com.hexaware.cozy_heaven.dto.response.BookingResponseDTO;
import com.hexaware.cozy_heaven.dto.response.GuestResponseDTO;
import com.hexaware.cozy_heaven.entity.Booking;
import com.hexaware.cozy_heaven.entity.Guest;
import com.hexaware.cozy_heaven.entity.Hotel;
import com.hexaware.cozy_heaven.entity.Payment;
import com.hexaware.cozy_heaven.entity.Room;
import com.hexaware.cozy_heaven.entity.User;
import com.hexaware.cozy_heaven.exception.BookingException;
import com.hexaware.cozy_heaven.model.BookingStatus;
import com.hexaware.cozy_heaven.model.PaymentStatus;
import com.hexaware.cozy_heaven.repository.BookingRepository;
import com.hexaware.cozy_heaven.repository.GuestRepository;
import com.hexaware.cozy_heaven.repository.PaymentRepository;
import com.hexaware.cozy_heaven.repository.RefundRepository;
import com.hexaware.cozy_heaven.repository.RoomRepository;
import com.hexaware.cozy_heaven.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService {

	final BookingRepository bookingRepo;

	final UserRepository userRepo;

	final RoomRepository roomRepo;

	final PaymentRepository paymentRepo;

	final GuestRepository guestRepo;

	final RoomService roomService;

	final RefundService refundService;
	
	final RefundRepository refundRepo;
	
	String notFoundMessage = "No Booking found with id:";

	BookingServiceImpl(BookingRepository bookingRepo, UserRepository userRepo, RoomRepository roomRepo, PaymentRepository paymentRepo, GuestRepository guestRepo, RoomService roomService, RefundService refundService, RefundRepository refundRepository) {
		this.bookingRepo = bookingRepo;
		this.userRepo = userRepo;
		this.roomRepo = roomRepo;
		this.paymentRepo = paymentRepo;
		this.guestRepo = guestRepo;
		this.roomService = roomService;
		this.refundService = refundService;
		this.refundRepo = refundRepository;
	}

	@Override
	public BookingResponseDTO createBooking(BookingDTO dto) {


		User user = userRepo.findById(dto.getUserId()).orElseThrow(() -> {
			log.error("User not found with id: {}", dto.getUserId());
			return new BookingException("No User found with id: " + dto.getUserId());
		});


		Room room = roomRepo.findById(dto.getRoomId()).orElseThrow(() -> {
			log.error("Room not found with id: {}", dto.getRoomId());
			return new BookingException("No Room found with id: " + dto.getRoomId());
		});


		if (!dto.getCheckOut().isAfter(dto.getCheckIn())) {
			log.error("Check-out date must be after check-in date");
			throw new BookingException("Check-out date must be after check-in date");
		}


		List<Room> availableRooms = roomRepo.findAvailableRooms(room.getHotel().getHotelId(), dto.getCheckIn(),
				dto.getCheckOut());

		boolean isAvailable = availableRooms.stream().anyMatch(r -> r.getRoomId().equals(room.getRoomId()));

		if (!isAvailable) {
			log.error("Room {} is not available from {} to {}", room.getRoomId(), dto.getCheckIn(), dto.getCheckOut());
			throw new BookingException("Room " + room.getRoomId() + " is not available for the selected dates");
		}


		int totalNights = (int) ChronoUnit.DAYS.between(dto.getCheckIn(), dto.getCheckOut());
		int totalAmount = room.getFare() * totalNights;


		Booking booking = new Booking();
		booking.setUser(user);
		booking.setRoom(room);
		booking.setCheckIn(dto.getCheckIn());
		booking.setCheckOut(dto.getCheckOut());
		booking.setTotalNights(totalNights);
		booking.setAdults(dto.getAdults());
		booking.setChildrens(dto.getChildrens());
		booking.setTotalAmount(totalAmount);
		booking.setStatus(BookingStatus.PENDING); 

		Booking savedBooking = bookingRepo.save(booking);
		log.info("Booking created with id: {}, totalNights: {}, totalAmount: {}", savedBooking.getBookingId(),
				totalNights, totalAmount);


		roomService.blockRoom(room.getRoomId());
		log.info("Room {} blocked for booking {}", room.getRoomId(), savedBooking.getBookingId());


		Payment payment = new Payment();
		payment.setBooking(savedBooking);
		payment.setAmount(totalAmount);
		payment.setStatus(PaymentStatus.PENDING);
		payment.setTransactionId(UUID.randomUUID().toString());

		paymentRepo.save(payment);
		log.info("Payment created with PENDING status for booking id: {}", savedBooking.getBookingId());


		if (dto.getGuestDetails() != null && !dto.getGuestDetails().isEmpty()) {
			for (var guestDTO : dto.getGuestDetails()) {
				Guest guest = new Guest();
				guest.setBooking(savedBooking);
				guest.setName(guestDTO.getName());
				guest.setAge(guestDTO.getAge());
				guest.setGuestType(guestDTO.getGuestType());
				guestRepo.save(guest);
			}
			log.info("{} guests saved for booking id: {}", dto.getGuestDetails().size(), savedBooking.getBookingId());
		}
		
		Booking freshBooking = bookingRepo.findById(savedBooking.getBookingId())
				.orElseThrow(() -> new BookingException("Unexpected error fetching booking"));

		return toResponseDTO(freshBooking);
	}

	@Override
	public BookingResponseDTO cancelBooking(CancelBookingDTO dto) {
		int bookingId = dto.getBookingId();

		Booking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new BookingException(notFoundMessage + bookingId));

		if (booking.getStatus() == BookingStatus.CANCELLED)
			throw new BookingException("Booking " + bookingId + " is already cancelled");
		if (booking.getStatus() == BookingStatus.COMPLETED)
			throw new BookingException("Completed bookings cannot be cancelled");

		booking.setStatus(BookingStatus.CANCELLED);
		booking.setCancellationReason(dto.getReason());
		booking.setCancelledAt(LocalDateTime.now());

		bookingRepo.save(booking);
		roomService.unblockRoom(booking.getRoom().getRoomId());

		long daysToCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), booking.getCheckIn());
		int refundPercentage = daysToCheckIn >= 7 ? 100 : daysToCheckIn >= 3 ? 50 : 0;
		
		log.info("Days to login: {}, Refund Percentage: {}", daysToCheckIn, refundPercentage);

		if (refundPercentage > 0) {
			RefundDTO refundDTO = new RefundDTO();
			refundDTO.setBookingId(bookingId);
			refundDTO.setReason(dto.getReason());
			refundDTO.setRefundPercentage(refundPercentage);
			refundService.createRefund(refundDTO);
			
		}

		return toResponseDTO(booking);
	}

	@Override
	public BookingResponseDTO completeBooking(int bookingId) {
		Booking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new BookingException(notFoundMessage + bookingId));

		if (booking.getStatus() != BookingStatus.CONFIRMED) {
			throw new BookingException("Only CONFIRMED bookings can be marked as completed");
		}

		booking.setStatus(BookingStatus.COMPLETED);
		log.info("Booking {} marked as COMPLETED", bookingId);
		
		roomService.unblockRoom(booking.getRoom().getRoomId());
		
		return toResponseDTO(bookingRepo.save(booking));
	}

	@Override
	public BookingResponseDTO getBookingById(int bookingId) {
		Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> {
			log.error("Booking not found with id: {}", bookingId);
			return new BookingException(notFoundMessage + bookingId);
		});

		return toResponseDTO(booking);
	}
	
	@Override
	public BookingResponseDTO markNoShow(int bookingId) {
	    Booking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new BookingException(notFoundMessage + bookingId));
	    if (booking.getStatus() != BookingStatus.CONFIRMED) {
	        throw new BookingException("Only CONFIRMED bookings can be marked as No Show");
	    }
	    booking.setStatus(BookingStatus.NO_SHOW);
	    log.info("Booking Marked as No Show BookingId: {}", bookingId);
	    roomService.unblockRoom(booking.getRoom().getRoomId());
	    return toResponseDTO(bookingRepo.save(booking));
	}

	@Override
	public List<BookingResponseDTO> getAllBooking() {
		log.info("Fetching all bookings");
		return bookingRepo.findAll().stream().map(this::toResponseDTO).toList();
	}

	@Override
	public List<BookingResponseDTO> getBookingsByUser(int userId) {
		log.info("Fetching bookings for userId: {}", userId);
		return bookingRepo.findByUserUserId(userId).stream().map(this::toResponseDTO).toList();
	}

	@Override
	public List<BookingResponseDTO> getBookingsByHotel(int hotelId) {
		log.info("Fetching bookings for hotelId: {}", hotelId);
		return bookingRepo.findByRoomHotelHotelId(hotelId).stream().map(this::toResponseDTO).toList();
	}

	@Override
	public boolean deleteBooking(int bookingId) {
		Booking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new BookingException(notFoundMessage + bookingId));

		if (booking.getStatus() != BookingStatus.CANCELLED) {
			throw new BookingException("Only CANCELLED bookings can be deleted");
		}

		bookingRepo.delete(booking);
		log.info("Booking {} deleted", bookingId);
		return true;
	}

	private BookingResponseDTO toResponseDTO(Booking booking) {
		BookingResponseDTO dto = new BookingResponseDTO();

		dto.setBookingId(booking.getBookingId());
		dto.setStatus(booking.getStatus());
		dto.setCheckIn(booking.getCheckIn());
		dto.setCheckOut(booking.getCheckOut());
		dto.setTotalNights(booking.getTotalNights());
		dto.setAdults(booking.getAdults());
		dto.setChildrens(booking.getChildrens());
		dto.setTotalAmount(booking.getTotalAmount());
		dto.setCancellationReason(booking.getCancellationReason());
		dto.setCancelledAt(booking.getCancelledAt());
		dto.setBookedAt(booking.getBookedAt());


		dto.setUserId(booking.getUser().getUserId());
		dto.setUserName(booking.getUser().getName());
		dto.setUserEmail(booking.getUser().getEmail());
		dto.setUserContact(booking.getUser().getContactNumber());


		Room room = booking.getRoom();
		dto.setRoomId(room.getRoomId());
		dto.setRoomNumber(room.getRoomNumber());
		dto.setRoomType(room.getRoomType().name());


		Hotel hotel = room.getHotel();
		dto.setHotelId(hotel.getHotelId());
		dto.setHotelName(hotel.getName());
		dto.setHotelLocation(hotel.getLocation());


		if (booking.getPayment() != null) {
			Payment payment = booking.getPayment();
			dto.setPaymentId(payment.getPaymentId());
			dto.setPaymentStatus(payment.getStatus());
			dto.setTransactionId(payment.getTransactionId());
			dto.setPaymentAmount(payment.getAmount());
		}


		if (booking.getGuests() != null) {
			List<GuestResponseDTO> guests = booking.getGuests().stream().map(g -> {
				GuestResponseDTO gDto = new GuestResponseDTO();
				gDto.setGuestId(g.getGuestId());
				gDto.setName(g.getName());
				gDto.setAge(g.getAge());
				gDto.setGuestType(g.getGuestType());
				gDto.setBookingId(booking.getBookingId());
				return gDto;
			}).toList();
			dto.setGuests(guests);
		}

		return dto;
	}
}