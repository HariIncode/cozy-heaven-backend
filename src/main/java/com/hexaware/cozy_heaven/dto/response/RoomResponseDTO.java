package com.hexaware.cozy_heaven.dto.response;

import java.time.LocalDateTime;

import com.hexaware.cozy_heaven.model.Bed;
import com.hexaware.cozy_heaven.model.RoomType;

import lombok.Data;

@Data
public class RoomResponseDTO {

    private Integer roomId;
    private Integer roomNumber;
    private RoomType roomType;
    private Float sizeSQM;
    private Bed bedSize;
    private Integer capacity;
    private Integer fare; // per night
    private boolean isAC;
    private boolean isAvailable;

    // Hotel info — denormalized so admin/frontend gets full context
    private Integer hotelId;
    private String hotelName;
    private String hotelLocation;

    // Owner info — needed for hotel-owner dashboards
    private Integer ownerId;
    private String ownerName;

    private LocalDateTime createdAt;
}
