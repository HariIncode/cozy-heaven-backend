package com.hexaware.cozy_heaven.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hexaware.cozy_heaven.dto.GuestDTO;
import com.hexaware.cozy_heaven.entity.Booking;
import com.hexaware.cozy_heaven.entity.Guest;
import com.hexaware.cozy_heaven.exception.GuestException;
import com.hexaware.cozy_heaven.model.BookingStatus;
import com.hexaware.cozy_heaven.repository.BookingRepository;
import com.hexaware.cozy_heaven.repository.GuestRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class GuestServiceImpl implements GuestService {

	final GuestRepository repo;

	final BookingRepository bookingRepo;

	GuestServiceImpl(BookingRepository bookingRepo, GuestRepository repo) {
		this.bookingRepo = bookingRepo;
		this.repo = repo;
	}

	@Override
	public Guest addGuest(int bookingId, GuestDTO guestDTO) {

		Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> {
			log.error("Booking not found with id: {}", bookingId);
			return new GuestException("No Booking found with id: " + bookingId);
		});

		if (booking.getStatus() != BookingStatus.CONFIRMED) {
			log.error("Cannot add guest — booking {} is not CONFIRMED (current: {})", bookingId, booking.getStatus());
			throw new GuestException("Guests can only be added to a CONFIRMED booking");
		}

		List<Guest> existingGuests = repo.findByBookingBookingId(bookingId);
		int currentCount = existingGuests.size();
		int capacity = booking.getRoom().getCapacity();

		if (currentCount >= capacity) {
			log.error("Room capacity exceeded for booking {}. Capacity: {}, Current: {}", bookingId, capacity,
					currentCount);
			throw new GuestException("Room capacity exceeded. Max allowed: " + capacity);
		}

		Guest guest = new Guest();
		guest.setBooking(booking);
		guest.setName(guestDTO.getName());
		guest.setAge(guestDTO.getAge());
		guest.setGuestType(guestDTO.getGuestType());

		Guest savedGuest = repo.save(guest);
		log.info("Guest {} added to booking {}", savedGuest.getGuestId(), bookingId);
		return savedGuest;
	}

	@Override
	public Guest updateGuest(int guestId, GuestDTO guestDTO) {

		Guest guest = getGuestById(guestId);

		if (guest.getBooking().getStatus() != BookingStatus.CONFIRMED) {
			log.error("Cannot update guest — booking {} is not CONFIRMED", guest.getBooking().getBookingId());
			throw new GuestException("Guests can only be updated while the booking is CONFIRMED");
		}

		guest.setName(guestDTO.getName());
		guest.setAge(guestDTO.getAge());
		guest.setGuestType(guestDTO.getGuestType());

		Guest updatedGuest = repo.save(guest);
		log.info("Guest {} updated", guestId);
		return updatedGuest;
	}

	@Override
	public Guest getGuestById(int guestId) {

		Guest guest = repo.findById(guestId).orElseThrow(() -> {
			log.error("Guest not found with id: {}", guestId);
			return new GuestException("No Guest found with id: " + guestId);
		});

		log.info("Getting Guest for ID: {}", guestId);

		return guest;
	}

	@Override
	public List<Guest> getAllGuest() {
		List<Guest> guests = repo.findAll();
		log.info("Fetching all guests");
		return guests;
	}

	@Override
	public boolean deleteGuest(int guestId) {
		Guest guest = getGuestById(guestId);

		if (guest.getBooking().getStatus() != BookingStatus.CONFIRMED) {
			log.error("Cannot delete guest — booking {} is not CONFIRMED", guest.getBooking().getBookingId());
			throw new GuestException("Guests can only be removed while the booking is CONFIRMED");
		}

		repo.delete(guest);
		log.info("Guest {} deleted", guestId);
		return true;
	}

	@Override
	public List<Guest> getGuestByBooking(int bookingId) {
		log.info("Fetching guests for booking id: {}", bookingId);
		return repo.findByBookingBookingId(bookingId);
	}
}