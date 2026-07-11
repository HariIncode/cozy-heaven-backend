package com.hexaware.cozy_heaven.dto.response;

import com.hexaware.cozy_heaven.model.GuestType;

import lombok.Data;

@Data
public class GuestResponseDTO {

    private int guestId;
    private String name;
    private int age;
    private GuestType guestType;

    private int bookingId;

    private String hotelName;
    private Integer roomNumber;

    private Integer userId;
    private String bookedByName;
}