package com.hexaware.cozy_heaven.rest_controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.cozy_heaven.dto.UpdatePaymentStatusDTO;
import com.hexaware.cozy_heaven.entity.Payment;
import com.hexaware.cozy_heaven.service.PaymentService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/payment")
public class PaymentRestController {

	final PaymentService service;

	PaymentRestController(PaymentService service) {
		this.service = service;
	}

	@PutMapping
	public ResponseEntity<Payment> updatePaymentStatus(@Valid @RequestBody UpdatePaymentStatusDTO dto) {
		return ResponseEntity.status(HttpStatus.OK).body(service.updatePaymentStatus(dto));
	}

	@GetMapping("/{paymentId}")
	public ResponseEntity<Payment> getPaymentById(@PathVariable int paymentId) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getPaymentById(paymentId));
	}

	@GetMapping
	public ResponseEntity<List<Payment>> getAllPayments() {
		return ResponseEntity.status(HttpStatus.OK).body(service.getAllPayment());
	}

	@GetMapping("/booking/{bookingId}")
	public ResponseEntity<Payment> getPaymentByBookingId(@PathVariable int bookingId) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getPaymentByBooking(bookingId));
	}

	@DeleteMapping("/{paymentId}")
	public ResponseEntity<String> deletePayment(@PathVariable int paymentId) {
		service.deletePayment(paymentId);
		return ResponseEntity.status(HttpStatus.OK).body("Payment Deleted Successfully.");
	}
}
