package com.hexaware.cozy_heaven.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hexaware.cozy_heaven.dto.UpdatePaymentStatusDTO;
import com.hexaware.cozy_heaven.entity.Booking;
import com.hexaware.cozy_heaven.entity.Payment;
import com.hexaware.cozy_heaven.exception.PaymentException;
import com.hexaware.cozy_heaven.model.BookingStatus;
import com.hexaware.cozy_heaven.model.PaymentStatus;
import com.hexaware.cozy_heaven.repository.BookingRepository;
import com.hexaware.cozy_heaven.repository.PaymentRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	final PaymentRepository repo;

	final BookingRepository bookingRepo;

	final RoomService roomService;

	PaymentServiceImpl(PaymentRepository repo, BookingRepository bookingRepo, RoomService roomService) {
		this.repo = repo;
		this.bookingRepo = bookingRepo;
		this.roomService = roomService;
	}

	@Override
	public Payment getPaymentById(int paymentId) {
		Payment payment = repo.findById(paymentId).orElseThrow(() -> {
			log.error("Payment Not found for Given Id: {}", paymentId);
			return new PaymentException("Payment not Found for given Id: " + paymentId);
		});

		log.info("Getting Payment for Id: {}", paymentId);

		return payment;
	}

	@Override
	public List<Payment> getAllPayment() {
		log.info("Getting All Payments");
		List<Payment> payments = repo.findAll();

		if (payments.isEmpty())
			log.error("No Payments");

		return payments;
	}

	@Override
	public boolean deletePayment(int paymentId) {
		Payment payment = getPaymentById(paymentId);

		if (payment.getStatus() != PaymentStatus.FAILED) {
			log.error("Only FAILED payments can be deleted, Payment status is: {}", payment.getStatus());
			throw new PaymentException("Only FAILED payments can be deleted");
		}

		repo.delete(payment);

		log.info("Payment with Payment ID :{} deleted Successfully", paymentId);

		return true;
	}

	@Override
	public Payment getPaymentByBooking(int bookingId) {
		log.info("Getting Payment for Booking ID: {}", bookingId);

		return repo.findByBookingBookingId(bookingId);
	}

	@Override
	public Payment updatePaymentStatus(UpdatePaymentStatusDTO dto) {

		Payment payment = getPaymentById(dto.getPaymentId());

		payment.setStatus(dto.getStatus());

		log.info("Gathering Information to Update Payment for payment ID: {}", dto.getPaymentId());

		Booking booking = payment.getBooking();

		if (dto.getStatus() == PaymentStatus.SUCCESS) {
			booking.setStatus(BookingStatus.CONFIRMED);
			bookingRepo.save(booking);

		} else if (dto.getStatus() == PaymentStatus.FAILED) {
			booking.setStatus(BookingStatus.CANCELLED);
			bookingRepo.save(booking);
			roomService.unblockRoom(booking.getRoom().getRoomId());
		}

		Payment savedPayment = repo.save(payment);

		log.info("Payment Updated Susscessfully.");

		return savedPayment;
	}

}
