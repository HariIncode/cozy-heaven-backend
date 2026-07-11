package com.hexaware.cozy_heaven.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.hexaware.cozy_heaven.dto.ReviewDTO;
import com.hexaware.cozy_heaven.dto.response.ReviewResponseDTO;
import com.hexaware.cozy_heaven.entity.Booking;
import com.hexaware.cozy_heaven.entity.Hotel;
import com.hexaware.cozy_heaven.entity.Review;
import com.hexaware.cozy_heaven.entity.User;
import com.hexaware.cozy_heaven.exception.BookingException;
import com.hexaware.cozy_heaven.exception.ReviewException;
import com.hexaware.cozy_heaven.exception.UserException;
import com.hexaware.cozy_heaven.model.BookingStatus;
import com.hexaware.cozy_heaven.repository.BookingRepository;
import com.hexaware.cozy_heaven.repository.HotelRepository;
import com.hexaware.cozy_heaven.repository.ReviewRepository;
import com.hexaware.cozy_heaven.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ReviewServiceImpl implements ReviewService {

	final HotelRepository hotelRepo;

	final UserRepository userRepo;

	final BookingRepository bookingRepo;

	final ReviewRepository repo;

	final HotelService hotelService;

	ReviewServiceImpl(HotelRepository hotelRepo, UserRepository userRepo, BookingRepository bookingRepo,
			ReviewRepository repo, HotelService hotelService) {
		this.hotelRepo = hotelRepo;
		this.userRepo = userRepo;
		this.bookingRepo = bookingRepo;
		this.repo = repo;
		this.hotelService = hotelService;
	}

	private ReviewResponseDTO toResponse(Review review) {
		ReviewResponseDTO dto = new ReviewResponseDTO();

		dto.setReviewId(review.getReviewId());
		dto.setRating(review.getRating());
		dto.setComments(review.getComments());
		dto.setReviewedAt(review.getReviewedAt());

		dto.setUserId(review.getUser().getUserId());
		dto.setUserName(review.getUser().getName());
		dto.setUserEmail(review.getUser().getEmail());

		dto.setHotelId(review.getHotel().getHotelId());
		dto.setHotelName(review.getHotel().getName());
		dto.setHotelLocation(review.getHotel().getLocation());

		dto.setBookingId(review.getBooking().getBookingId());
		dto.setBookingDate(review.getBooking().getBookedAt());

		return dto;
	}

	@Override
	public ReviewResponseDTO addReview(ReviewDTO reviewDTO) {

		Booking booking = bookingRepo.findById(reviewDTO.getBookingId()).orElseThrow(() -> {
			log.error("Booking not found with id: {}", reviewDTO.getBookingId());
			return new BookingException("No Booking found with id: " + reviewDTO.getBookingId());
		});

		if (booking.getStatus() != BookingStatus.COMPLETED) {
			log.error("Booking {} is not COMPLETED — review not allowed", reviewDTO.getBookingId());
			throw new ReviewException("You can only review a hotel after your stay is completed");
		}

		if (!Objects.equals(booking.getUser().getUserId(), reviewDTO.getUserId())) {
			log.error("Booking {} does not belong to user {}", reviewDTO.getBookingId(), reviewDTO.getUserId());
			throw new ReviewException("This booking does not belong to the given user");
		}

		Hotel hotel = booking.getRoom().getHotel();
		if (!Objects.equals(hotel.getHotelId(), reviewDTO.getHotelId())) {
			log.error("Booking {} is not for hotel {}", reviewDTO.getBookingId(), reviewDTO.getHotelId());
			throw new ReviewException("This booking is not associated with the given hotel");
		}

		if (repo.existsByBookingBookingId(reviewDTO.getBookingId())) {
			log.error("Review already exists for booking id: {}", reviewDTO.getBookingId());
			throw new ReviewException("You have already reviewed this booking");
		}

		User user = userRepo.findById(reviewDTO.getUserId()).orElseThrow(() -> {
			log.error("User not found with id: {}", reviewDTO.getUserId());
			return new UserException("No User found with id: " + reviewDTO.getUserId());
		});

		Review review = new Review();
		review.setUser(user);
		review.setHotel(hotel);
		review.setBooking(booking);
		review.setRating(reviewDTO.getRating());
		review.setComments(reviewDTO.getComments());

		Review savedReview = repo.save(review);
		log.info("Review {} created for hotel {} by user {}", savedReview.getReviewId(), hotel.getHotelId(),
				user.getUserId());

		hotelService.updateAverageRating(hotel.getHotelId());

		return toResponse(savedReview);
	}

	@Override
	public ReviewResponseDTO updateReview(int reviewId, ReviewDTO reviewDTO) {

		Review review = repo.findById(reviewId).orElseThrow(() -> {
			log.error("Review not found with id: {}", reviewId);
			return new ReviewException("No Review found with id: " + reviewId);
		});

		if (!Objects.equals(review.getUser().getUserId(), reviewDTO.getUserId())) {
			log.error("User {} attempted to update review {} owned by user {}", reviewDTO.getUserId(), reviewId,
					review.getUser().getUserId());
			throw new ReviewException("You can only update your own review");
		}

		review.setRating(reviewDTO.getRating());
		review.setComments(reviewDTO.getComments());

		Review updatedReview = repo.save(review);
		log.info("Review {} updated", reviewId);

		hotelService.updateAverageRating(review.getHotel().getHotelId());

		return toResponse(updatedReview);
	}

	@Override
	public ReviewResponseDTO getReviewById(int reviewId) {
		Review review = repo.findById(reviewId).orElseThrow(() -> {
			log.error("Review not found with id: {}", reviewId);
			return new ReviewException("No Review found with id: " + reviewId);
		});

		log.info("Getting Review For ID: {}", reviewId);

		return toResponse(review);
	}

	@Override
	public List<ReviewResponseDTO> getAllReviews() {
		List<ReviewResponseDTO> reviews = repo.findAll().stream().map(this::toResponse).toList();
		log.info("Fetching all reviews");
		return reviews;
	}

	@Override
	public boolean deleteReview(int reviewId) {
		Review review = repo.findById(reviewId).orElseThrow(() -> {
			log.error("Review not found with id: {}", reviewId);
			return new ReviewException("No Review found with id: " + reviewId);
		});
		int hotelId = review.getHotel().getHotelId();

		repo.delete(review);
		log.info("Review {} deleted", reviewId);

		hotelService.updateAverageRating(hotelId);

		return true;
	}

	@Override
	public List<ReviewResponseDTO> getReviewsByHotel(int hotelId) {
		List<ReviewResponseDTO> reviews = repo.findByHotelHotelId(hotelId).stream().map(this::toResponse).toList();
		log.info("Fetching reviews for hotel id: {}", hotelId);
		return reviews;
	}

	@Override
	public List<ReviewResponseDTO> getReviewsByUser(int userId) {
		List<ReviewResponseDTO> reviews = repo.findByUserUserId(userId).stream().map(this::toResponse).toList();
		log.info("Fetching reviews for user id: {}", userId);
		return reviews;
	}
}