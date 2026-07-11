package com.hexaware.cozy_heaven.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewDTO {

    @NotNull(message = "User Id Required")
    private Integer userId;

    @NotNull(message = "Hotel Id Required")
    private Integer hotelId;

    @NotNull(message = "Booking Id Required")
    private Integer bookingId;

    @NotNull(message = "Rating Required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Float rating;

    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    private String comments;
}