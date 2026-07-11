package com.hexaware.cozy_heaven.dto;

import com.hexaware.cozy_heaven.model.Bed;
import com.hexaware.cozy_heaven.model.RoomType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomDTO {

	@NotNull(message = "Hotel Id Required")
	private int hotelId;
	@NotNull(message = "Room Number Required")
	private int roomNumber;
	
	@NotNull(message = "Room Size in SQM Required")
	private float sizeSQM;
	@NotNull(message = "Bed Size Required(SINGLE, DOUBLE, KING)")
	private Bed bedSize;
	@NotNull(message = "Room Capacity Required")
	private int capacity;
	@NotNull(message = "Room Fare Required")
	private int fare;
	@NotNull(message = "Room Type Required")
    private RoomType roomType;

	private boolean ac = false;
	private boolean available = true;

}
