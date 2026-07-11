package com.hexaware.cozy_heaven.rest_controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.cozy_heaven.dto.RefundDTO;
import com.hexaware.cozy_heaven.dto.response.RefundResponseDTO;
import com.hexaware.cozy_heaven.service.RefundService;

import jakarta.validation.Valid;

@CrossOrigin(origins = {
	    "http://localhost:5173",
	    "https://d2xp6setbjof39.cloudfront.net",
	    "https://hariincode.github.io/cozy-heaven/"
	})
@RestController
@RequestMapping("/refund")
public class RefundRestController {

	private final RefundService service;

	RefundRestController(RefundService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<RefundResponseDTO> createRefund(@Valid @RequestBody RefundDTO dto) {
		return new ResponseEntity<>(service.createRefund(dto), HttpStatus.CREATED);
	}

	@PutMapping("/approve/{refundId}")
	public ResponseEntity<RefundResponseDTO> approveRefund(@PathVariable int refundId) {
		return ResponseEntity.ok(service.approveRefund(refundId));
	}

	@PutMapping("/process/{refundId}")
	public ResponseEntity<RefundResponseDTO> processRefund(@PathVariable int refundId) {
		return ResponseEntity.ok(service.processRefund(refundId));
	}

	@PutMapping("/reject/{refundId}")
	public ResponseEntity<RefundResponseDTO> rejectRefund(@PathVariable int refundId, @RequestParam String reason) {
		return ResponseEntity.ok(service.rejectRefund(refundId, reason));
	}

	@GetMapping("/{refundId}")
	public ResponseEntity<RefundResponseDTO> getRefundById(@PathVariable int refundId) {
		return ResponseEntity.ok(service.getRefundById(refundId));
	}

	@GetMapping
	public ResponseEntity<List<RefundResponseDTO>> getAllRefund() {
		return ResponseEntity.ok(service.getAllRefund());
	}

	@GetMapping("/booking/{bookingId}")
	public ResponseEntity<RefundResponseDTO> getRefundByBooking(@PathVariable int bookingId) {
		return ResponseEntity.ok(service.getRefundByBooking(bookingId));
	}

}