package com.hexaware.cozy_heaven.dto.response;

import java.time.LocalDateTime;

import com.hexaware.cozy_heaven.model.Gender;
import com.hexaware.cozy_heaven.model.Role;

import lombok.Data;

@Data
public class UserResponseDTO {

    private Integer userId;
    private String name;
    private String email;

    // Password is NEVER included in any response — not even for admin
    private Gender gender;
    private String contactNumber;
    private String address;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Summary counts — useful for admin dashboard
    private int totalBookings;
    private int totalReviews;
    private int totalHotels; // non-zero only if role = HOTEL_OWNER
}
