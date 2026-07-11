package com.hexaware.cozy_heaven.service;

import java.util.List;

import com.hexaware.cozy_heaven.dto.ReviewDTO;
import com.hexaware.cozy_heaven.dto.response.ReviewResponseDTO;

public interface ReviewService {

	ReviewResponseDTO addReview(ReviewDTO dto);
	
	ReviewResponseDTO updateReview(int reviewId, ReviewDTO dto);
	
	ReviewResponseDTO getReviewById(int reviewId);
	
	List<ReviewResponseDTO> getAllReviews();
	
	List<ReviewResponseDTO> getReviewsByHotel(int hotelId);
	
	List<ReviewResponseDTO> getReviewsByUser(int userId);
	
	boolean deleteReview(int reviewId);
	
}
