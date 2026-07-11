package com.hexaware.cozy_heaven.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class HotelResponseDTO {

    private Integer hotelId;
    private String name;
    private String location;
    private String description;
    private String imageUrl;

    private Integer ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerContact;

    private boolean dining;
    private boolean parking;
    private boolean wifi;
    private boolean roomService;
    private boolean pool;
    private boolean gym;

    private double averageRating;
    private int totalReviews;

    private int totalRooms;
    private int availableRooms;

    private boolean isActive;
    private LocalDateTime createdAt;
}
