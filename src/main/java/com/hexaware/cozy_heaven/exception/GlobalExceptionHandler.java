package com.hexaware.cozy_heaven.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserException.class)
	public ResponseEntity<String> handleUserException(UserException ex) {
		log.error("UserException occurred: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(HotelException.class)
	public ResponseEntity<String> handleHotelException(HotelException ex) {
		log.error("HotelException occurred: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(RoomException.class)
	public ResponseEntity<String> handleRoomException(RoomException ex) {
		log.error("RoomException occurred: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(BookingException.class)
	public ResponseEntity<String> handleBookingException(BookingException ex) {
		log.error("BookingException occurred: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(GuestException.class)
	public ResponseEntity<String> handleGuestException(GuestException ex) {
		log.error("GuestException occurred: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(PaymentException.class)
	public ResponseEntity<String> handlePaymentException(PaymentException ex) {
		log.error("PaymentException occurred: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(RefundException.class)
	public ResponseEntity<String> handleRefundException(RefundException ex) {
		log.error("RefundException occurred: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(ReviewException.class)
	public ResponseEntity<String> handleReviewException(ReviewException ex) {
		log.error("ReviewException occurred: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
	public ResponseEntity<String> handleBadCredentials(
			org.springframework.security.authentication.BadCredentialsException ex) {

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Email or Password");
	}
}
