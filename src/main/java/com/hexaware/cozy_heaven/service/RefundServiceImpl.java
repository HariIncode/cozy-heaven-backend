package com.hexaware.cozy_heaven.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hexaware.cozy_heaven.dto.RefundDTO;
import com.hexaware.cozy_heaven.dto.response.RefundResponseDTO;
import com.hexaware.cozy_heaven.entity.Booking;
import com.hexaware.cozy_heaven.entity.Payment;
import com.hexaware.cozy_heaven.entity.Refund;
import com.hexaware.cozy_heaven.entity.User;
import com.hexaware.cozy_heaven.exception.RefundException;
import com.hexaware.cozy_heaven.model.RefundStatus;
import com.hexaware.cozy_heaven.repository.BookingRepository;
import com.hexaware.cozy_heaven.repository.RefundRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class RefundServiceImpl implements RefundService {

	private final RefundRepository refundRepo;

	private final BookingRepository bookingRepo;

	RefundServiceImpl(RefundRepository refundRepo, BookingRepository bookingRepo) {
		this.refundRepo = refundRepo;
		this.bookingRepo = bookingRepo;
	}

	private RefundResponseDTO toResponse(Refund refund) {
		RefundResponseDTO dto = new RefundResponseDTO();

		dto.setRefundId(refund.getRefundId());
		dto.setRefundPercentage(refund.getRefundPercentage());
		dto.setRefundAmount(refund.getRefundAmount());
		dto.setRefundStatus(refund.getRefundStatus());
		dto.setReason(refund.getReason());
		dto.setRefundedAt(refund.getRefundedAt());
		dto.setProcessedAt(refund.getProcessedAt());

		dto.setBookingId(refund.getBooking().getBookingId());
		dto.setCheckIn(refund.getBooking().getCheckIn());
		dto.setCheckOut(refund.getBooking().getCheckOut());
		dto.setOriginalAmount(refund.getBooking().getTotalAmount());

		User user = refund.getBooking().getUser();

		dto.setUserId(user.getUserId());
		dto.setUserName(user.getName());
		dto.setUserEmail(user.getEmail());
		dto.setUserContact(user.getContactNumber());

		Payment payment = refund.getBooking().getPayment();

		dto.setPaymentId(payment.getPaymentId());
		dto.setTransactionId(payment.getTransactionId());

		return dto;
	}

	@Override
	public RefundResponseDTO createRefund(RefundDTO dto) {

		Booking booking = bookingRepo.findById(dto.getBookingId())
				.orElseThrow(() -> new RefundException("Booking not found : " + dto.getBookingId()));

		if (refundRepo.findByBookingBookingId(dto.getBookingId()) != null) {
			throw new RefundException("Refund already exists for this booking.");
		}

		int refundAmount = (booking.getTotalAmount() * dto.getRefundPercentage()) / 100;

		Refund refund = new Refund();

		refund.setBooking(booking);
		refund.setRefundPercentage(dto.getRefundPercentage());
		refund.setRefundAmount(refundAmount);
		refund.setReason(dto.getReason());
		refund.setRefundStatus(RefundStatus.REQUESTED);

		log.info("Refund requested for booking {}", dto.getBookingId());

		return toResponse(refundRepo.save(refund));
	}

	@Override
	public RefundResponseDTO approveRefund(int refundId) {

		Refund refund = refundRepo.findById(refundId)
				.orElseThrow(() -> new RefundException("Refund not found : " + refundId));

		if (refund.getRefundStatus() != RefundStatus.REQUESTED) {
			throw new RefundException("Only REQUESTED refund can be approved.");
		}

		refund.setRefundStatus(RefundStatus.APPROVED);

		log.info("Refund {} approved", refundId);

		return toResponse(refundRepo.save(refund));
	}

	@Override
	public RefundResponseDTO processRefund(int refundId) {

		Refund refund = refundRepo.findById(refundId)
				.orElseThrow(() -> new RefundException("Refund not found : " + refundId));

		if (refund.getRefundStatus() != RefundStatus.APPROVED) {
			throw new RefundException("Refund must be APPROVED before processing.");
		}

		refund.setRefundStatus(RefundStatus.PROCESSED);
		refund.setProcessedAt(LocalDateTime.now());

		log.info("Refund {} processed", refundId);

		return toResponse(refundRepo.save(refund));
	}

	@Override
	public RefundResponseDTO rejectRefund(int refundId, String reason) {

		Refund refund = refundRepo.findById(refundId)
				.orElseThrow(() -> new RefundException("Refund not found : " + refundId));

		if (refund.getRefundStatus() == RefundStatus.PROCESSED) {
			throw new RefundException("Processed refund cannot be rejected.");
		}

		refund.setRefundStatus(RefundStatus.REJECTED);
		refund.setReason(reason);

		log.info("Refund {} rejected", refundId);

		return toResponse(refundRepo.save(refund));
	}

	@Override
	public RefundResponseDTO getRefundById(int refundId) {
		log.info("Getting Refund for Id: {}", refundId);
		return toResponse(refundRepo.findById(refundId).orElseThrow(() -> {
			log.error("Refund not found : {}", refundId);
			return new RefundException("Refund not found : " + refundId);
		}));
	}

	@Override
	public RefundResponseDTO getRefundByBooking(int bookingId) {

		Refund refund = refundRepo.findByBookingBookingId(bookingId);

		if (refund == null) {
			log.error("No refund found for the BookingId: {}", bookingId);
			throw new RefundException("No refund found for booking : " + bookingId);
		}
		
		log.info("Getting Refund For BookingId: {}", bookingId);

		return toResponse(refund);
	}

	@Override
	public List<RefundResponseDTO> getAllRefund() {
		log.info("Getting All Refunds");
		return refundRepo.findAll().stream().map(this::toResponse).toList();
	}

}