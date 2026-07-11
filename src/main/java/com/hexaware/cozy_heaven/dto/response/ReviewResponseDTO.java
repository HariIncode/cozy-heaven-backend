package com.hexaware.cozy_heaven.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewResponseDTO {

    private Long reviewId;
    private float rating;
    private String comments;
    private LocalDateTime reviewedAt;

    private Integer userId;
    private String userName;
    private String userEmail;

    private Integer hotelId;
    private String hotelName;
    private String hotelLocation;

    private int bookingId;
    private LocalDateTime bookingDate;
}
