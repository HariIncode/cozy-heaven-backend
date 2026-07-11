package com.hexaware.cozy_heaven.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HotelDTO {

	private int ownerId;

	@NotBlank(message = "Hotel Name Required")
	@Size(min = 3, max = 30, message = "Name Length Should be Between 3 and 30")
	private String name;

	@NotBlank(message = "Hotel Location Required")
	@Size(min = 3, max = 25, message = "Location Length Should be Between 3 and 25")
	private String location;

	@NotBlank(message = "Hotel Description Required")
	@Size(max = 255, message = "Description must not exceed 255 characters")
	private String description;

	@NotBlank(message = "Hotel Image Url Required")
	private String imageUrl;

	@NotNull
	private boolean dining;
	@NotNull
	private boolean parking;
	@NotNull
	private boolean wifi;
	@NotNull
	private boolean roomService;
	@NotNull
	private boolean pool;
	@NotNull
	private boolean gym;

}
