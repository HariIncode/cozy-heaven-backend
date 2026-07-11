package com.hexaware.cozy_heaven.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hexaware.cozy_heaven.model.Bed;
import com.hexaware.cozy_heaven.model.RoomType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "rooms", uniqueConstraints = { @UniqueConstraint(columnNames = { "hotel_id", "room_number" }) })
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "room_id")
	private Integer roomId;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hotel_id", nullable = false)
	@JsonBackReference("hotel-rooms")
	private Hotel hotel;

	@Column(name = "room_number", nullable = false)
	private Integer roomNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "room_type", nullable = false)
	private RoomType roomType;

	@Column(name = "size_sqm")
	private Float sizeSQM;

	@Enumerated(EnumType.STRING)
	@Column(name = "bed_size", nullable = false)
	private Bed bedSize;

	@Column(nullable = false)
	private Integer capacity;

	@Column(nullable = false)
	private Integer fare;

	@Column(name = "is_ac")
	private boolean ac;

	@Column(name = "is_available", nullable = false)
	private boolean available = true;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	@OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JsonManagedReference("room-bookings")
	private List<Booking> bookings;
}